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
 * Player resource. This class is used in the Griddler Cloud Endpoints API and is projected to the
 * clients in the generated client libraries.
 */
public class Player {
  private long id;
  private String nickname;
  private PlayerStatistics statistics;

  /**
   * Constructor
   *
   * @param id player's resource id.
   * @param nickname player's nickname.
   */
  public Player(long id, String nickname) {
    this.id = id;
    this.nickname = nickname;
    this.statistics = null;
  }

  /**
   * Constructor
   *
   * @param id player's resource id.
   * @param nickName player's nickname.
   * @param statistics player's game statistics.
   */
  public Player(long id, String nickName, PlayerStatistics statistics) {
    this.id = id;
    this.nickname = nickName;
    this.statistics = statistics;
  }

  /**
   * Gets the player's id.
   *
   */
  public long getId() {
    return id;
  }

  /**
   * Get the player's nickname.
   *
   */
  public String getNickname() {
    return nickname;
  }

  /**
   * Get the player's statistics. Can be null.
   *
   */
  public PlayerStatistics getStatistics() {
    return statistics;
  }
}
