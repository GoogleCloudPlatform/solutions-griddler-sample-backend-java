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

package com.google.cloud.solutions.sampleapps.griddler.backend.api;

import java.util.List;

/**
 * Board definition. This class is used in the Griddler Cloud Endpoints API and is projected to the
 * clients in the generated client libraries.
 */
public class Board {
  private List<String> gridDefinition;
  private List<String> riddles;
  private List<String> answers;
  private long allottedTime;

  /**
   * Constructor
   *
   * @param gridDefinition list of strings representing letters in each of the rows of the grid.
   * @param riddles list of riddles for this board.
   * @param answers list of answers for the riddles.
   * @param allottedTime time in milliseconds allotted to complete a game based on this board.
   */
  public Board(
      List<String> gridDefinition, List<String> riddles, List<String> answers, long allottedTime) {
    this.gridDefinition = gridDefinition;
    this.riddles = riddles;
    this.answers = answers;
    this.allottedTime = allottedTime;
  }

  /**
   * Gets the grid definition.
   *
   * @return list of strings representing letters in each of the rows of the grid.
   */
  public List<String> getGridDefinition() {
    return gridDefinition;
  }

  /**
   * Gets the list of riddles.
   *
   */
  public List<String> getRiddles() {
    return riddles;
  }

  /**
   * Gets the list of answers.
   *
   */
  public List<String> getAnswers() {
    return answers;
  }

  /**
   * Gets the allotted time in milliseconds.
   *
   */
  public long getAllottedTime() {
    return allottedTime;
  }
}
