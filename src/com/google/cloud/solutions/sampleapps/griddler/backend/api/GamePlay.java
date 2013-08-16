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
 * Representation of one player's participation in a game. This class is used in the Griddler Cloud
 * Endpoints API and is projected to the clients in the generated client libraries.
 */
public class GamePlay {
  private Player player;
  private List<Integer> correctAnswers;
  private boolean finished;
  private long timeLeft;
  private boolean isWinner;

  /**
   * Constructor
   *
   * @param player the player who played a game.
   * @param correctAnswers the list of player's correct answers.
   * @param finished true if the player finished the game and submitted the answers; false
   *        otherwise.
   * @param timeLeft time in milliseconds still left when the player submitted the answers.
   * @param isWinner true if this player is the winner of the game; false otherwise.
   */
  public GamePlay(Player player, List<Integer> correctAnswers, boolean finished, long timeLeft,
      boolean isWinner) {
    this.player = player;
    this.correctAnswers = correctAnswers;
    this.finished = finished;
    this.timeLeft = timeLeft;
    this.isWinner = isWinner;
  }

  /**
   * Gets the player resource.
   *
   */
  public Player getPlayer() {
    return player;
  }

  /**
   * Gets the list of correct answers.
   *
   */
  public List<Integer> getCorrectAnswers() {
    return correctAnswers;
  }

  /**
   * Gets the flag that indicates whether the player finished the game.
   *
   * @return true if the player finished the game and submitted the answers; false otherwise
   */
  public boolean getFinished() {
    return finished;
  }

  /**
   * Gets the time in milliseconds still left when the player submitted the answers.
   *
   */
  public long getTimeLeft() {
    return timeLeft;
  }

  /**
   * Gets the flag that indicates whether the player won the game.
   *
   * @return true if this player is the winner of the game; false otherwise.
   */
  public boolean getIsWinner() {
    return isWinner;
  }
}
