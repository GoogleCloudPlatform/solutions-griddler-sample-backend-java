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

package com.google.cloud.solutions.sampleapps.griddler.backend.entities;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

import java.util.List;

/**
 * Game Datastore entity.
 */
public class GameEntity extends DatastoreEntity {
  private static final long serialVersionUID = 1;
  /**
   * Name of Game entity kind in Datastore. Public because it is also used for Datastore queries.
   */
  public static final String KIND = "Game";

  /**
   * Names of properties in Datastore.
   */
  private static final String BOARDKEY_PROPERTY = "boardKey";
  private static final String PLAYERKEYWON_PROPERTY = "playerKeyWon";

  /**
   * Constructor that is used when retrieving an existing game entity from Datastore.
   *
   * @param entity {@link Entity}.
   */
  public GameEntity(Entity entity) {
    super(entity, false);
  }

  /**
   * Constructor used to create a new game entity.
   *
   * @param boardKey {@link Key}
   * @throws IllegalArgumentException if entity is null.
   */
  public GameEntity(Key boardKey) {
    super(new Entity(KIND), true);

    if (boardKey == null) {
      throw new IllegalArgumentException("boardKey cannot be null");
    }

    setProperty(BOARDKEY_PROPERTY, boardKey);
    setProperty(PLAYERKEYWON_PROPERTY, null);
  }

  /**
   * Determines which player has won the game and update the game entity with that winner player's
   * key.
   *
   * @param players list of players from this game.
   * @return true if the winner was determined; false if the game is not finished yet.
   * @throws IllegalArgumentException if player list is null or empty.
   */
  public boolean determineWinner(List<GamePlayEntity> players) {
    if (players == null || players.size() <= 1) {
      throw new IllegalArgumentException("players list must contain at least two players");
    }

    if (!isGameFinished(players)) {
      return false;
    }

    GamePlayEntity currentWinner = players.get(0);
    int currentWinnerAnswerCount = currentWinner.getAnswers().size();

    for (int index = 1; index < players.size(); index++) {

      GamePlayEntity player = players.get(index);

      int playerAnswerCount = player.getAnswers().size();

      if (playerAnswerCount > currentWinnerAnswerCount) {
        currentWinner = player;
        currentWinnerAnswerCount = playerAnswerCount;
      } else if (playerAnswerCount == currentWinnerAnswerCount) {
        currentWinner = whoWinsTie(currentWinner, player);
      }
    }

    setProperty(PLAYERKEYWON_PROPERTY, currentWinner == null ? null : currentWinner.getPlayerKey());

    return true;
  }

  /**
   * Gets the player key who won the game.
   */
  public Key getWinnerKey() {
    return getPropertyOfType(PLAYERKEYWON_PROPERTY);
  }

  /**
   * Checks if the player is the winner of this game.
   *
   * @param playerKey the key of the player.
   * @return true if the player is the winner; false otherwise.
   */
  public boolean isWinner(Key playerKey) {
    Key key = getWinnerKey();

    return (key != null && key.compareTo(playerKey) == 0);
  }

  /**
   * Gets the board entity key.
   *
   * @return {@link Key}.
   */
  public Key getBoardKey() {
    return getPropertyOfType(BOARDKEY_PROPERTY);
  }

  private GamePlayEntity whoWinsTie(GamePlayEntity player1, GamePlayEntity player2) {
    long player1TimeLeft = player1.getTimeLeft();
    long player2TimeLeft = player2.getTimeLeft();

    if (player1TimeLeft > player2TimeLeft) {
      return player1;
    } else if (player2TimeLeft > player1TimeLeft) {
      return player2;
    } else {
      return null;
    }
  }

  private boolean isGameFinished(List<GamePlayEntity> players) {
    for (GamePlayEntity player : players) {
      if (!player.getFinished() || player.getParentKey().compareTo(this.getKey()) != 0) {
        return false;
      }
    }

    return true;
  }
}
