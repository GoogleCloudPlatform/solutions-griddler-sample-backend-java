/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.solutions.sampleapps.griddler.backend.services;

import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.Board;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.Game;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.GamePlay;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.GamePlayStatus;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.Player;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.BoardEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.GameEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.GamePlayEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

/**
 * Service for managing games.
 */
public class GameService {
  /**
   * Memcache key name for game boards.
   */
  private static final String ALL_BOARDS_CACHE_KEY = "AllBoards";

  private DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

  /**
   * Gets the game resource by its id.
   *
   * @param gameId the id of the game resource.
   * @throws NotFoundException when game with gameId is not found.
   */
  public Game getGame(long gameId) throws NotFoundException {
    GameEntity game = get(gameId);
    if (game == null) {
      throw new NotFoundException("Game " + gameId + " not found.");
    }

    return new Game(game.getId(), getBoard(game.getBoardKey()), getGamePlays(game));
  }

  /**
   * Submits the player's answers for a game.
   *
   * @param gameId the Id of the game.
   * @param player the current player.
   * @param answers the player's answers in this game.
   * @throws NotFoundException when game with gameId is not found or if the player is not registered
   *         or did not play that game.
   */
  public void submitAnswers(long gameId, User player, GamePlayStatus answers)
      throws NotFoundException {
    GameEntity game = get(gameId);
    if (game == null) {
      throw new NotFoundException("Game " + gameId + " not found.");
    }

    PlayerEntity playerEntity = new PlayerService().getByEmail(player.getEmail());
    if (playerEntity == null) {
      throw new NotFoundException("Player not found.");
    }

    // Find the GamePlay entity for this player.
    GamePlayEntity gamePlayForThisPlayer = null;

    List<GamePlayEntity> gamePlays = getGamePlayEntities(gameId);
    for (GamePlayEntity gamePlay : gamePlays) {
      if (gamePlay.getPlayerKey().compareTo(playerEntity.getKey()) == 0) {
        gamePlayForThisPlayer = gamePlay;
        break;
      }
    }

    if (gamePlayForThisPlayer == null) {
      throw new NotFoundException("Player did not play game " + gameId + ".");
    }

    // Update the GamePlay entity for this player and if this was a multiplayer game
    // and the game is complete, update player statistics for every player that participated
    // in this game.

    Transaction transaction = dataStore.beginTransaction();

    try {
      gamePlayForThisPlayer.submitAnswers(answers.getCorrectAnswers(), answers.getTimeLeft());
      dataStore.put(gamePlayForThisPlayer.getEntity());

      if (gamePlays.size() > 1 && game.determineWinner(gamePlays)) {
        dataStore.put(game.getEntity());
        updateMultiplayerStatistics(gameId, game.getWinnerKey().getId());
      }

      transaction.commit();
    } finally {
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Starts a new single player game.
   *
   * @param player the current player
   * @param boardLevel the board level to use.
   * @return {@link Game}
   * @throws NotFoundException if there is no board level specified by boardLevel argument or the
   *         player is not registered.
   */
  public Game startNewSinglePlayerGame(User player, int boardLevel) throws NotFoundException {
    PlayerEntity playerEntity = new PlayerService().getByEmail(player.getEmail());

    if (playerEntity == null) {
      throw new NotFoundException("Player record not found.");
    }

    BoardEntity board = getRandomBoard(boardLevel);
    if (board == null) {
      throw new NotFoundException("No board found for level " + boardLevel);
    }

    // Create a Game entity and GamePlay entity for this game as an atomic operation.
    Transaction transaction = dataStore.beginTransaction();

    try {
      GameEntity game = new GameEntity(board.getKey());
      dataStore.put(game.getEntity());

      GamePlayEntity playerGame = new GamePlayEntity(playerEntity.getKey(), game.getKey());
      dataStore.put(playerGame.getEntity());

      transaction.commit();

      return getGame(game.getId());
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Updates - as an atomic operation - the win/loss statistics for all players involved in the
   * game. The method may throw Datastore and other exceptions, e.g., in case of concurrent entity
   * modifications. The caller is responsible for retrying the request for such exceptions. This can
   * be done automatically when the method is called from a push task queue handler.
   *
   * @param gameId the game id.
   * @param winnerId the id of the player who won this game.
   * @throws NotFoundException if there is no game with gameId. This exception should NOT be
   *         retried.
   */
  public void updatePlayerStatistics(long gameId, long winnerId) throws NotFoundException {
    List<Long> playerIds = new ArrayList<Long>();
    Game game = getGame(gameId);
    for (GamePlay gamePlay : game.getGamePlays()) {
      playerIds.add(gamePlay.getPlayer().getId());
    }

    new PlayerService().updatePlayerStatistics(playerIds, winnerId);
  }

  /**
   * Gets a game by numeric id.
   *
   * @param id the numeric entity id.
   * @return game entity or null if there is no game entity with this id.
   */
  private GameEntity get(long id) {
    Key key = KeyFactory.createKey(GameEntity.KIND, id);
    return get(key);
  }

  /**
   * Gets a game by entity key.
   *
   * @param key the entity key of the game to retrieve.
   * @return game entity or null if there is no game entity with this key.
   */
  private GameEntity get(Key key) {
    try {
      return new GameEntity(dataStore.get(key));
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  /**
   * Gets a game board by entity key.
   *
   * @param key the entity key of the board to retrieve.
   * @return board entity or null if there is no game entity with this key.
   */
  private Board getBoard(Key key) {
    Map<Key, BoardEntity> boards = getBoards();

    BoardEntity boardEntity = boards.get(key);
    if (boardEntity == null) {
      return null;
    }

    return new Board(boardEntity.getBoardDefinition(), boardEntity.getClues(),
        boardEntity.getAnswers(), boardEntity.getAllottedTime());
  }

  /**
   * Gets a list of all game board entities.
   *
   * @return non null map of board entities indexed by entity keys.
   */
  private Map<Key, BoardEntity> getBoards() {
    // Gets a list of all game boards from memcache.
    MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
    cache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

    @SuppressWarnings("unchecked")
    Map<Key, BoardEntity> boards = (Map<Key, BoardEntity>) cache.get(ALL_BOARDS_CACHE_KEY);

    // If the list wasn't in the cache then retrieve it from Datastore
    // and cache it (unless the list is empty).
    if (boards == null) {
      boards = new HashMap<Key, BoardEntity>();

      Query query = new Query(BoardEntity.KIND);
      PreparedQuery preparedQuery = dataStore.prepare(query);

      for (Entity board : preparedQuery.asIterable()) {
        boards.put(board.getKey(), new BoardEntity(board));
      }

      if (boards.size() > 0) {
        cache.put(ALL_BOARDS_CACHE_KEY, boards);
      }
    }

    return boards;
  }

  /**
   * Gets a not null list of game boards for a given level.
   *
   * @param level the level of the board.
   */
  private List<BoardEntity> getBoardsForLevel(int level) {
    List<BoardEntity> result = new ArrayList<BoardEntity>();

    for (BoardEntity board : getBoards().values()) {
      if (board.getLevel() == level) {
        result.add(board);
      }
    }

    return result;
  }

  /**
   * Gets random board of a given level or null if there is no board for this level.
   *
   * @param level the level of the board.
   */
  protected BoardEntity getRandomBoard(int level) {
    List<BoardEntity> boards = getBoardsForLevel(level);

    if (boards.size() == 0) {
      return null;
    }

    return boards.get(new Random().nextInt(boards.size()));
  }

  /**
   * Gets all GamePlay entities for a given game.
   *
   */
  private List<GamePlayEntity> getGamePlayEntities(long gameId) {
    List<GamePlayEntity> gamePlays = new ArrayList<GamePlayEntity>();

    // All GamePlay entities for a given game belong to the same entity group rooted at the game
    // entity.
    Key ancestorKey = KeyFactory.createKey(GameEntity.KIND, gameId);
    Query query = new Query(GamePlayEntity.KIND).setAncestor(ancestorKey);
    PreparedQuery preparedQuery = dataStore.prepare(query);

    for (Entity gamePlayEntity : preparedQuery.asIterable()) {
      gamePlays.add(new GamePlayEntity(gamePlayEntity));
    }

    return gamePlays;
  }

  /**
   * Gets all GamePlay resources for a given game.
   *
   */
  private List<GamePlay> getGamePlays(GameEntity game) {
    List<GamePlay> gamePlays = new ArrayList<GamePlay>();

    Key winnerKey = game.getWinnerKey();

    PlayerService playerService = new PlayerService();

    // Get the list of all GamePlay entities for this game and transform it into a list of GamePlay
    // resources.
    for (GamePlayEntity gamePlay : getGamePlayEntities(game.getId())) {
      PlayerEntity playerEntity = playerService.get(gamePlay.getPlayerKey());
      Player player = new Player(playerEntity.getId(), playerEntity.getNickname());

      boolean isWinner =
          (winnerKey == null) ? false : playerEntity.getKey().compareTo(winnerKey) == 0;

      gamePlays.add(new GamePlay(
          player, gamePlay.getAnswers(), gamePlay.getFinished(), gamePlay.getTimeLeft(),
          isWinner));
    }

    return gamePlays;
  }

  /**
   * Initiates updating player statistics asynchronously for all players that played a given game.
   *
   */
  private void updateMultiplayerStatistics(long gameId, long winnerId) {
    Queue queue = QueueFactory.getQueue("player-statistics");

    TaskOptions taskOptions = TaskOptions.Builder.withUrl("/admin/statistics/processgameresults")
        .param("gameId", String.valueOf(gameId)).param("winnerId", String.valueOf(winnerId))
        .method(TaskOptions.Method.POST);

    queue.add(taskOptions);
  }
}
