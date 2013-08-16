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
 * Class representing the status of a game play as reported by the client. This class is used in the
 * Griddler Cloud Endpoints API and is projected to the clients in the generated client libraries.
 */
public class GamePlayStatus {
  private List<Integer> correctAnswers;
  private long timeLeft;

  /**
   * Default constructor needed by Google Cloud Endpoints to instantiate an object from Json passed
   * by the client.
   */
  public GamePlayStatus() {}

  /**
   * Constructor.
   *
   * @param correctAnswers the list of player's correct answers.
   * @param timeLeft time in milliseconds still left when the player submitted the answers.
   */
  public GamePlayStatus(List<Integer> correctAnswers, long timeLeft) {
    this.correctAnswers = correctAnswers;
    this.timeLeft = timeLeft;
  }

  /**
   * Gets the list of correct answers.
   *
   */
  public List<Integer> getCorrectAnswers() {
    return correctAnswers;
  }

  /**
   * Gets the time in milliseconds still left when the player submitted the answers.
   *
   */
  public long getTimeLeft() {
    return timeLeft;
  }
}
