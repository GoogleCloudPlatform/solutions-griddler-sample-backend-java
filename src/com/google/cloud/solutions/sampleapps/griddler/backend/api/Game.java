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
 * Game resource. This class is used in the Griddler Cloud Endpoints API and is projected to the
 * clients in the generated client libraries.
 */
public class Game {
  private long id;
  private Board board;
  private List<GamePlay> gamePlays;

  /**
   * Constructor
   *
   * @param id the id of the game.
   * @param board the board used in this game.
   * @param gamePlays the list of game plays from each player in this game.
   */
  public Game(long id, Board board, List<GamePlay> gamePlays) {
    this.id = id;
    this.board = board;
    this.gamePlays = gamePlays;
  }

  /**
   * Gets the game id.
   *
   */
  public long getId() {
    return id;
  }

  /**
   * Gets the board used for this game.
   *
   */
  public Board getBoard() {
    return board;
  }

  /**
   * Gets the list of game plays from each player in this game.
   *
   */
  public List<GamePlay> getGamePlays() {
    return gamePlays;
  }
}