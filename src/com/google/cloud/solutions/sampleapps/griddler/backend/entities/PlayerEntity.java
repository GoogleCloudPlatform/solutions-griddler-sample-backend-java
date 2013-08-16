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
import com.google.appengine.api.users.User;

/**
 * Player Datastore entity.
 */
public class PlayerEntity extends DatastoreEntity {
  private static final long serialVersionUID = 1;
  /**
   * Name of Player entity kind in Datastore. Public because it is also used for Datastore queries.
   */
  public static final String KIND = "Player";

  /**
   * Names of properties in Datastore. Public if used in queries. Private otherwise.
   */
  public static final String EMAIL_PROPERTY = "email";
  public static final String ACTIVE_PROPERTY = "active";
  public static final String GOOGLEPLUSID_PROPERTY = "googlePlusId";
  private static final String NUMBER_OF_MULTIPLAYER_GAMES_WON = "multiplayerGamesWon";
  private static final String NUMBER_OF_MULTIPLAYER_GAMES_PLAYED = "multiplayerGamesPlayed";

  /**
   * Constructor that is used when retrieving an existing player entity from Datastore.
   *
   * @param entity {@link Entity}
   */
  public PlayerEntity(Entity entity) {
    super(entity, false);
  }

  /**
   * Constructor used to create a new player entity.
   *
   * @param user the user object for which the player entity is created.
   * @throws IllegalArgumentException if user is null.
   */
  public PlayerEntity(User user) {
    super(new Entity(KIND), true);

    if (user == null) {
      throw new IllegalArgumentException("user cannot be null");
    }

    setProperty(EMAIL_PROPERTY, user.getEmail());
    setProperty(ACTIVE_PROPERTY, false);
    setProperty(GOOGLEPLUSID_PROPERTY, null);
    setProperty(NUMBER_OF_MULTIPLAYER_GAMES_PLAYED, 0);
    setProperty(NUMBER_OF_MULTIPLAYER_GAMES_WON, 0);
  }

  /**
   * Constructor used to create a new player with a Google+ ID.
   *
   * @param user the user object for which player entity is created.
   * @param plusId the user's Google+ ID.
   * @throws IllegalArgumentException if user is null or if plusId is null or empty.
   */
  public PlayerEntity(User user, String plusId) {
    super(new Entity(KIND), true);

    if (user == null) {
      throw new IllegalArgumentException("user cannot be null");
    }

    if (plusId == null || plusId.length() == 0) {
      throw new IllegalArgumentException("plusId cannot be empty");
    }

    setProperty(EMAIL_PROPERTY, user.getEmail());
    setProperty(ACTIVE_PROPERTY, false);
    setProperty(GOOGLEPLUSID_PROPERTY, plusId);
  }

  /**
   * Gets the number of multiplayer games won by this player.
   */
  public int getNumberOfMultiplayerGamesWon() {
    Long gamesWon = getPropertyOfType(NUMBER_OF_MULTIPLAYER_GAMES_WON);

    return gamesWon.intValue();
  }

  /**
   * Gets the number of multiplayer games played.
   */
  public int getNumberOfMultiplayerGamesPlayed() {
    Long gamesPlayed = getPropertyOfType(NUMBER_OF_MULTIPLAYER_GAMES_PLAYED);

    return gamesPlayed.intValue();
  }

  /**
   * Gets the player's email.
   *
   */
  public String getEmail() {
    return getPropertyOfType(EMAIL_PROPERTY);
  }

  /**
   * Gets the player's nickname.
   *
   * @return nickname if available; null otherwise.
   */
  public String getNickname() {
    String email = getEmail();

    if (email != null) {
      int index = email.indexOf("@");
      if (index > 0) {
        return email.substring(0, index);
      }
    }

    return null;
  }

  /**
   * Assigns the user's Google+ ID.
   *
   * @param plusId the users Google+ ID.
   */
  public void assignGooglePlusId(String plusId) {
    setProperty(GOOGLEPLUSID_PROPERTY, plusId);
  }

  /**
   * Deactivates the player.
   */
  public void deactivate() {
    setProperty(ACTIVE_PROPERTY, false);
  }

  /**
   * Activates the player.
   */
  public void activate() {
    setProperty(ACTIVE_PROPERTY, true);
  }

  /**
   * Updates the players multiplayer statistics.
   */
  public void wonGame() {

    playedGame();

    setProperty(NUMBER_OF_MULTIPLAYER_GAMES_WON, getNumberOfMultiplayerGamesWon() + 1);
  }

  /**
   * Updates the players multiplayer statistics.
   */
  public void lostGame() {
    playedGame();
  }

  /**
   * Increases the number of multiplayer games played.
   */
  private void playedGame() {
    setProperty(NUMBER_OF_MULTIPLAYER_GAMES_PLAYED, getNumberOfMultiplayerGamesPlayed() + 1);
  }
}
