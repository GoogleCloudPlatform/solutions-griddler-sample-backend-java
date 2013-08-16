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
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.users.User;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.Player;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.PlayerStatistics;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for managing players.
 */
public class PlayerService {
  private DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

  /**
   * Gets the player resource associated with a given user.
   *
   * @param user user making the request.
   */
  public Player getPlayer(User user) throws NotFoundException {
    if (user == null) {
      throw new IllegalArgumentException("User cannot be null.");
    }

    PlayerEntity player = getByEmail(user.getEmail());

    if (player == null) {
      throw new NotFoundException("Player record not found.");
    }

    return new Player(player.getId(), player.getNickname(), new PlayerStatistics(
        player.getNumberOfMultiplayerGamesWon(), player.getNumberOfMultiplayerGamesPlayed()));
  }

  /**
   * Gets a list of players that a given user can play with. This implementation simply returns the
   * list of all players other than the current user.
   *
   * @param user user making the request.
   */
  public List<Player> getPlayers(User user) {
    List<Player> results = new ArrayList<Player>();

    Filter activePlayersFilter =
        new FilterPredicate(PlayerEntity.ACTIVE_PROPERTY, FilterOperator.EQUAL, true);
    Query query = new Query(PlayerEntity.KIND).setFilter(activePlayersFilter);
    PreparedQuery preparedQuery = dataStore.prepare(query);

    for (Entity entity : preparedQuery.asIterable()) {
      PlayerEntity playerEntity = new PlayerEntity(entity);
      if (!playerEntity.getEmail().equalsIgnoreCase(user.getEmail())) {
        results.add(new Player(playerEntity.getId(), playerEntity.getNickname()));
      }
    }

    return results;
  }

  /**
   * Registers a player resource for the user (idempotent operation).
   *
   * @param user user making the request.
   * @return a not null Player resource for this user.
   */
  public Player registerPlayer(User user) {
    PlayerEntity player = getByEmail(user.getEmail());

    if (player == null) {
      player = new PlayerEntity(user);
      dataStore.put(player.getEntity());
    }

    return new Player(player.getId(), player.getNickname());
  }

  /**
   * Registers a player resource for the user with Google+ Id (idempotent operation).
   *
   * @param user user making the request.
   * @param plusId user's Google+ Id.
   * @return a not null Player resource for this user.
   */
  public Player registerPlayerWithPlusId(User user, String plusId) {
    PlayerEntity player = getByEmail(user.getEmail());

    if (player == null) {
      player = new PlayerEntity(user, plusId);
    } else {
      player.assignGooglePlusId(plusId);
    }

    dataStore.put(player.getEntity());
    return new Player(player.getId(), player.getNickname());
  }

  /**
   * Assigns a Google+ Id to the player.
   *
   * @param user user making the request.
   * @param plusId User's Google+ Id.
   * @throws NotFoundException when player record not found.
   */
  public void assignPlusId(User user, String plusId) throws NotFoundException {
    PlayerEntity playerEntity = getByEmail(user.getEmail());

    if (playerEntity == null) {
      throw new NotFoundException("Player record not found.");
    }

    playerEntity.assignGooglePlusId(plusId);
    dataStore.put(playerEntity.getEntity());
  }

  /**
   * Gets a player by numeric id.
   *
   * @param id the numeric entity id.
   * @return player entity or null if there is no player entity with this id.
   */
  protected PlayerEntity get(long id) {
    Key key = KeyFactory.createKey(PlayerEntity.KIND, id);
    return get(key);
  }

  /**
   * Gets a player by entity key.
   *
   * @param key the entity key of the player to retrieve.
   * @return player entity or null if there is no player entity with key.
   */
  protected PlayerEntity get(Key key) {
    try {
      return new PlayerEntity(dataStore.get(key));
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  /**
   * Gets a player by email address.
   *
   * @param email the email of the player to retrieve.
   * @return player entity or null if there is no player entity with this email.
   */
  protected PlayerEntity getByEmail(String email) {
    Filter emailFilter =
        new FilterPredicate(PlayerEntity.EMAIL_PROPERTY, FilterOperator.EQUAL, email);

    Query query = new Query(PlayerEntity.KIND).setFilter(emailFilter);
    PreparedQuery preparedQuery = dataStore.prepare(query);

    Entity entity = preparedQuery.asSingleEntity();

    return entity == null ? null : new PlayerEntity(entity);
  }

  /**
   * Gets the player entity by player's Google+ Id.
   *
   * @param plusId User's Google+ Id.
   * @return player entity or null if there is no player entity with this email.
   */
  protected PlayerEntity getByPlusId(String plusId) {
    Filter plusFilter =
        new FilterPredicate(PlayerEntity.GOOGLEPLUSID_PROPERTY, FilterOperator.EQUAL, plusId);
    Filter activeFilter =
        new FilterPredicate(PlayerEntity.ACTIVE_PROPERTY, FilterOperator.EQUAL, true);
    Filter filter = CompositeFilterOperator.and(activeFilter, plusFilter);

    Query query = new Query(PlayerEntity.KIND).setFilter(filter);
    PreparedQuery preparedQuery = dataStore.prepare(query);

    Entity entity = preparedQuery.asSingleEntity();

    return entity == null ? null : new PlayerEntity(entity);
  }

  /**
   * Gets a list of players by keys.
   *
   * @param playerKeys list of player entity keys to retrieve.
   */
  protected List<PlayerEntity> getPlayers(List<Key> playerKeys) {
    List<PlayerEntity> players = new ArrayList<PlayerEntity>();

    Map<Key, Entity> entityMap = dataStore.get(playerKeys);

    for (Entity entity : entityMap.values()) {
      players.add(new PlayerEntity(entity));
    }

    return players;
  }

  /**
   * Updates win/loss statistics for players that competed in a game. Updates are done atomically.
   * The method may throw Datastore and other exceptions, e.g., in case of concurrent entity
   * modifications. The caller is responsible for retrying. This can be done automatically when the
   * method is called from a push task queue handler.
   *
   * @param playerIds Ids of the players who competed in a game.
   * @param winnerId Id of the winner.
   */
  protected void updatePlayerStatistics(List<Long> playerIds, long winnerId) {
    List<Key> playerKeys = new ArrayList<Key>();

    for (Long playerId : playerIds) {
      playerKeys.add(KeyFactory.createKey(PlayerEntity.KIND, playerId));
    }

    List<PlayerEntity> players = getPlayers(playerKeys);

    Transaction transaction = dataStore.beginTransaction(TransactionOptions.Builder.withXG(true));
    try {
      for (PlayerEntity player : players) {
        if (player.getId() == winnerId) {
          player.wonGame();
        } else {
          player.lostGame();
        }

        dataStore.put(player.getEntity());
      }
      transaction.commit();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }
}
