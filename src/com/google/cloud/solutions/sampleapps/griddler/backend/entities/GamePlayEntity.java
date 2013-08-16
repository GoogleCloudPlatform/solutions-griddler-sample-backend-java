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

import java.util.ArrayList;
import java.util.List;

/**
 * GamePlay Datastore entity. It represents one player's play as part of a game. A multiplayer game
 * has multiple GamePlay entities. All GamePlay entities for a given game belong to the entity group
 * rooted at the game entity.
 */
public class GamePlayEntity extends DatastoreEntity {
  private static final long serialVersionUID = 1;
  /**
   * Name of GamePlay entity kind in Datastore. Public because it is also used for Datastore
   * queries.
   */
  public static final String KIND = "GamePlay";

  /**
   * Names of properties in Datastore.
   */
  private static final String PLAYERKEY_PROPERTY = "playerKey";
  private static final String ANSWERS_PROPERTY = "answers";
  private static final String FINISHED_PROPERTY = "finished";
  private static final String TIMELEFT_PROPERTY = "timeLeft";

  /**
   * Constructor that is used when retrieving an existing game player entity from Datastore.
   *
   */
  public GamePlayEntity(Entity entity) {
    super(entity, false);
  }

  /**
   * Constructor used to create a new game player entity.
   *
   * @param playerKey the key of the player.
   * @param gameKey the key of the game.
   * @throws IllegalArgumentException if playerKey is null.
   */
  public GamePlayEntity(Key playerKey, Key gameKey) {
    super(new Entity(KIND, gameKey), true);

    if (playerKey == null) {
      throw new IllegalArgumentException("playerKey cannot be null");
    }

    setProperty(PLAYERKEY_PROPERTY, playerKey);
    setProperty(ANSWERS_PROPERTY, null);
    setProperty(FINISHED_PROPERTY, false);
    setProperty(TIMELEFT_PROPERTY, 0L);
  }

  /**
   * Submits answers and record how much time was left on the clock when the player finished the
   * game.
   *
   * @param answers the list of answers.
   * @param timeLeft time left in milliseconds.
   */
  public void submitAnswers(List<Integer> answers, long timeLeft) {
    setProperty(FINISHED_PROPERTY, true);
    setProperty(TIMELEFT_PROPERTY, timeLeft);
    
    // Explicitly convert answers to a list of Longs to guarantee that this property always holds
    // the list of Longs, whether the property was set in memory or retrieved from Datastore. 
    List<Long> answersProperty = new ArrayList<Long>();

    for (Integer answer : answers) {
      answersProperty.add(answer.longValue());
    }

    setProperty(ANSWERS_PROPERTY, answersProperty);
  }

  /**
   * Gets the player's answers.
   *
   * @return {@link List}&lt;{@link Integer}&gt; indices of correct answers.
   */
  public List<Integer> getAnswers() {
    List<Long> answers = getPropertyOfType(ANSWERS_PROPERTY);

    List<Integer> result = new ArrayList<Integer>();

    if (answers == null) {
      return result;
    }

    for (Long answer : answers) {
      result.add(answer.intValue());
    }

    return result;
  }

  /**
   * Checks if the player finished the game.
   *
   * @return true if the player finished the game; false otherwise.
   */
  public boolean getFinished() {
    return getPropertyOfType(FINISHED_PROPERTY);
  }

  /**
   * Returns how much time was left in milliseconds when the player finished.
   */
  public long getTimeLeft() {
    return getPropertyOfType(TIMELEFT_PROPERTY);
  }

  /**
   * Gets the player's entity key.
   *
   * @return {@link Key}.
   */
  public Key getPlayerKey() {
    return getPropertyOfType(PLAYERKEY_PROPERTY);
  }
}
