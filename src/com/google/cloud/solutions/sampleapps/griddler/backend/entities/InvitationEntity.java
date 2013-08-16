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

import java.io.Serializable;

/**
 * Invitation Datastore entity.
 */
public class InvitationEntity extends DatastoreEntity implements Serializable {
  private static final long serialVersionUID = 1;
  /**
   * Name of Invitation entity kind in Datastore. Public because it is also used for Datastore
   * queries.
   */
  public static final String KIND = "Invitation";

  /**
   * Names of properties in Datastore.
   */
  private static final String SENDER_KEY_PROPERTY = "senderKey";
  private static final String INVITEE_KEY_PROPERTY = "inviteeKey";
  private static final String STATUS_PROPERTY = "status";

  /**
   * Invitation Status.
   */
  public enum Status {
    SENT, ACCEPTED, DECLINED, CANCELED;
  }

  /**
   * Constructor that is used when retrieving an existing invitation entity from Datastore.
   *
   * @param entity {@link Entity}.
   */
  public InvitationEntity(Entity entity) {
    super(entity, false);
  }

  /**
   * Constructor used to create a new invitation entity.
   *
   * @param senderKey the key of the sender.
   * @param inviteeKey the key of the invitee.
   * @param gameKey the game key.
   * @throws IllegalArgumentException if senderKey or inviteeKey is null.
   */
  public InvitationEntity(Key senderKey, Key inviteeKey, Key gameKey) {
    super(new Entity(KIND, gameKey), true);

    if (senderKey == null) {
      throw new IllegalArgumentException("senderKey cannot be null");
    }

    if (inviteeKey == null) {
      throw new IllegalArgumentException("inviteeKey cannot be null");
    }

    setProperty(SENDER_KEY_PROPERTY, senderKey);
    setProperty(INVITEE_KEY_PROPERTY, inviteeKey);
    setStatus(Status.SENT);
  }

  /**
   * Accepts the invitation.
   *
   * @return true if the invitation can be accepted; false otherwise.
   */
  public boolean accept() {
    boolean valid = false;

    switch (this.getStatus()) {
      case SENT:
        setStatus(Status.ACCEPTED);
        valid = true;
        break;
      case ACCEPTED:
        valid = true; // Still valid, but not changing state.
        break;
      default:
        break;
    }

    return valid;
  }

  /**
   * Declines the invitation.
   *
   * @return true if the invitation can be declined; false otherwise.
   */
  public boolean decline() {
    boolean valid = false;

    switch (this.getStatus()) {
      case SENT:
        setStatus(Status.DECLINED);
        valid = true;
        break;
      case DECLINED:
      case CANCELED:
        valid = true; // Still valid, but not changing state.
        break;
      default:
        break;
    }

    return valid;
  }

  /**
   * Cancels the invitation.
   *
   * @return true if the invitation can be cancelled; false otherwise.
   */
  public boolean cancel() {
    boolean valid = false;

    switch (this.getStatus()) {
      case SENT:
        setStatus(Status.CANCELED);
        valid = true;
        break;
      case CANCELED:
      case DECLINED:
        valid = true; // Still valid, but not changing state.
        break;
      default:
        break;
    }

    return valid;
  }

  /**
   * Gets the sender entity key.
   */
  public Key getSenderKey() {
    return getPropertyOfType(SENDER_KEY_PROPERTY);
  }

  /**
   * Gets the invitee entity key.
   */
  public Key getInviteeKey() {
    return getPropertyOfType(INVITEE_KEY_PROPERTY);
  }

  /**
   * Gets the status of the invitation.
   */
  public Status getStatus() {
    long status = this.getPropertyOfType(STATUS_PROPERTY);
    return Status.values()[(int) status];
  }

  /**
   * Returns true if the invitation was accepted.
   *
   * @return {@link Boolean}
   */
  public Boolean wasAccepted() {
    return getStatus() == Status.ACCEPTED;
  }

  /**
   * Sets the status of the invitation.
   */
  private void setStatus(Status status) {
    /*
     * Cast the ordinal value to long. Otherwise the STATUS_PROPERTY property could be either of
     * type Long (after / retrieving the entity from Datastore), or of type Integer (after setting
     * it in memory).
     */
    this.setProperty(STATUS_PROPERTY, (long) status.ordinal());
  }
}
