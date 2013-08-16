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

/**
 * Player statistics. This class is used in the Griddler Cloud Endpoints API and is projected to the
 * clients in the generated client libraries.
 */

public class PlayerStatistics {
  private int numberOfWins = 0;
  private int numberofGames = 0;

  /**
   * Constructor
   *
   * @param numberOfWins number of wins in multiplayer games.
   * @param numberOfGames number of multiplayer games played.
   */
  public PlayerStatistics(int numberOfWins, int numberOfGames) {
    this.numberOfWins = numberOfWins;
    this.numberofGames = numberOfGames;
  }

  /**
   * Gets the number of multiplayer games won.
   *
   */
  public int getNumberOfWins() {
    return numberOfWins;
  }

  /**
   * Gets the number of multiplayer games played.
   *
   */
  public int getNumberOfGames() {
    return numberofGames;
  }
}
