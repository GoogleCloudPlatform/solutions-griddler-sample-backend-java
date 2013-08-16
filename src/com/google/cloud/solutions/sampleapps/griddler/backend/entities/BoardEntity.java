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

import java.io.Serializable;
import java.util.List;

/**
 * Game board Datastore entity.
 */
public class BoardEntity extends DatastoreEntity implements Serializable{
  private static final long serialVersionUID = 1;
  /**
   * Name of the Board entity kind in Datastore. Public because it is also used for Datastore
   * queries.
   */
  public static final String KIND = "Board";

  /**
   * Names of properties in Datastore.
   */
  private static final String GRID_DEFINITION_PROPERTY = "gridDefinition";
  private static final String RIDDLES_PROPERTY = "riddles";
  private static final String ANSWERS_PROPERTY = "answers";
  private static final String LEVEL_PROPERTY = "level";
  private static final String ALLOTTEDTIME_PROPERTY = "allottedTime";

  /**
   * Constructor that is used when retrieving an existing Board entity from Datastore.
   *
   * @param entity {@link Entity} to populate the model with.
   */
  public BoardEntity(Entity entity) {
    super(entity, false);
  }

  /**
   * Gets the board level.
   */
  public int getLevel() {
    Long level = getPropertyOfType(LEVEL_PROPERTY);
    return level.intValue();
  }

  /**
   * Gets the board definition.
   */
  public List<String> getBoardDefinition() {
    return getPropertyOfType(GRID_DEFINITION_PROPERTY);
  }

  /**
   * Gets the list of riddles.
   */
  public List<String> getClues() {
    return getPropertyOfType(RIDDLES_PROPERTY);
  }

  /**
   * Gets the list of answers.
   */
  public List<String> getAnswers() {
    return getPropertyOfType(ANSWERS_PROPERTY);
  }

  /**
   * Gets the time allotted to complete the game in milliseconds.
   */
  public long getAllottedTime() {
    return getPropertyOfType(ALLOTTEDTIME_PROPERTY);
  }
}
