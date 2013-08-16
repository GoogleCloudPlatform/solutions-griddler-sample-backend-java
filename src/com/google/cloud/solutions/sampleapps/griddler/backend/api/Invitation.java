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
 * Invitation resource. This class is used in the Griddler Cloud Endpoints API and is projected to
 * the clients in the generated client libraries.
 */
public class Invitation {
  private long invitationId;
  private long gameId;
  private Status status;
  
  /**
   * Invitation status.
   */
  public enum Status {
    SENT,
    ACCEPTED,
    DECLINED,
    CANCELED;
  }

  /**
   * Constructor
   *
   * @param invitationId the id of the invitation.
   * @param gameId the id of the game.
   */
  public Invitation(long invitationId, long gameId, Status status) {
    this.invitationId = invitationId;
    this.gameId = gameId;
    this.status = status;
  }

  /**
   * Gets the invitation id.
   */
  public long getInvitationId() {
    return invitationId;
  }

  /**
   * Gets the game Id.
   *
   */
  public long getGameId() {
    return gameId;
  }

  /**
   * Gets the invitation status.
   */
  public Status getStatus() {
    return status;
  }
}
