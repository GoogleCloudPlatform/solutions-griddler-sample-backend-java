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

package com.google.cloud.solutions.sampleapps.griddler.backend.services;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.users.User;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.Invitation;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.BoardEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.DeviceEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.GameEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.GamePlayEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.InvitationEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.PlayerEntity;

import javapns.communication.exceptions.CommunicationException;
import javapns.notification.PushNotificationPayload;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing invitations.
 */
public class InvitationService {
  private static final Logger logger = Logger.getLogger(InvitationService.class.getSimpleName());

  DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
  MemcacheService cache = MemcacheServiceFactory.getMemcacheService();
  Queue invitationQueue = QueueFactory.getQueue("invitations");

  /**
   * Number of milliseconds to expire an invitation status. Set to 1 minute.
   */
  private static final int CACHE_EXPIRATION_IN_MILLISECONDS = 1000 * 60;

  public InvitationService() {
    cache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
  }

  /**
   * Gets the invitation status.
   *
   * @param gameId the game id.
   * @param invitationId the invitation Id.
   * @throws NotFoundException if there is no record for this invitationId and gameId.
   */
  public Invitation.Status getInvitationStatus(long gameId, long invitationId)
      throws NotFoundException {
    String cacheKey = getInvitationCacheKey(gameId, invitationId);

    InvitationEntity.Status status = null;
    Integer statusValue = (Integer) cache.get(cacheKey);

    if (statusValue != null && statusValue >= 0
        && statusValue < InvitationEntity.Status.values().length) {
      status = InvitationEntity.Status.values()[statusValue];
    } else {
      InvitationEntity invitation = get(gameId, invitationId);

      if (invitation == null) {
        throw new NotFoundException("Invitation not found.");
      }

      status = invitation.getStatus();

      cacheInvitationStatus(gameId, invitationId, status);
    }

    // Map from enum defined in the entity class to enum defined in the API layer, to avoid
    // "leaking" entity layer data structures to the API layer.
    switch (status) {
      case ACCEPTED:
        return Invitation.Status.ACCEPTED;
      case DECLINED:
        return Invitation.Status.DECLINED;
      case CANCELED:
        return Invitation.Status.CANCELED;
      case SENT:
        return Invitation.Status.SENT;
      default:
        return Invitation.Status.SENT;
    }
  }

  /**
   * Sends an invitation to start a new game.
   *
   * @param sender the player that wants to invite another player.
   * @param inviteeId the id of the player that was invited.
   * @param boardLevel the board level for the new game.
   * @return Invitation resource representing the created invitation.
   * @throws NotFoundException if there is no record for this user, inviteeId or boardLevel.
   */
  public Invitation sendInvitation(User sender, long inviteeId, int boardLevel)
      throws NotFoundException {
    PlayerService playerService = new PlayerService();
    PlayerEntity senderEntity = playerService.getByEmail(sender.getEmail());
    if (senderEntity == null) {
      throw new NotFoundException("Player record not found.");
    }

    PlayerEntity invitee = playerService.get(inviteeId);
    if (invitee == null) {
      throw new NotFoundException("Player record for the inviteee not found.");
    }

    BoardEntity board = new GameService().getRandomBoard(boardLevel);
    if (board == null) {
      throw new NotFoundException("No board record found for this level.");
    }

    return sendInvitation(senderEntity, invitee, board);
  }

  /**
   * Sends an invitation using the invitee's Google+ Id.
   *
   * @param sender the player that wants to invite another player.
   * @param inviteePlusId the Google+ id of the invited player.
   * @param boardLevel the board level for the new game.
   * @return Invitation resource representing the created invitation.
   * @throws NotFoundException if there is no record for this invitee.
   */
  public Invitation sendInvitation(User sender, String inviteePlusId, int boardLevel)
      throws NotFoundException {
    PlayerEntity player = new PlayerService().getByPlusId(inviteePlusId);

    if (player == null) {
      throw new NotFoundException("Player record for the invitee not found.");
    }

    return sendInvitation(sender, player.getId(), boardLevel);
  }

  /**
   * Accepts an invitation.
   *
   * @param gameId the game id this invitation is for.
   * @param invitationId the invitation id.
   * @throws NotFoundException if there is no record for this gameId and invitationId.
   * @throws ConflictException if the invitation state has changed and the invitation cannot be
   *         accepted.
   */
  public void acceptInvitation(long gameId, long invitationId)
      throws NotFoundException, ConflictException {
    InvitationEntity invitation = get(gameId, invitationId);

    if (invitation == null) {
      throw new NotFoundException("Invitation record not found.");
    }

    if (!invitation.accept()) {
      throw new ConflictException(
          "Unable to accept the invitation because it has already been declined "
          + "or is no longer valid.");
    }

    Transaction transaction = dataStore.beginTransaction();

    try {
      GamePlayEntity inviteePlayer =
          new GamePlayEntity(invitation.getInviteeKey(), invitation.getParentKey());
      GamePlayEntity senderPlayer =
          new GamePlayEntity(invitation.getSenderKey(), invitation.getParentKey());

      dataStore.put(transaction, senderPlayer.getEntity());
      dataStore.put(transaction, inviteePlayer.getEntity());
      dataStore.put(transaction, invitation.getEntity());

      transaction.commit();

      cacheInvitationStatus(gameId, invitationId, InvitationEntity.Status.ACCEPTED);
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Declines an invitation.
   *
   * @param gameId the game id this invitation is for.
   * @param invitationId the invitation id.
   * @throws NotFoundException if there is no record for this gameId and invitationId.
   * @throws ConflictException if the invitation state has changed and the invitation cannot be
   *         declined.
   */
  public void declineInvitation(long gameId, long invitationId)
      throws NotFoundException, ConflictException {
    InvitationEntity invitation = get(gameId, invitationId);

    if (invitation == null) {
      throw new NotFoundException("Invitation record not found.");
    }

    if (!invitation.decline()) {
      throw new ConflictException(
          "Unable to decline the invitation because it has already been accepted "
          + "or is no longer valid.");
    }

    dataStore.put(invitation.getEntity());
    cacheInvitationStatus(gameId, invitationId, InvitationEntity.Status.DECLINED);
  }

  /**
   * Cancels an invitation.
   *
   * @param gameId the game id this invitation is for.
   * @param invitationId the invitation id.
   * @throws NotFoundException if there is no record for this gameId and invitationId.
   * @throws ConflictException if the invitation state has changed and the invitation cannot be
   *         cancelled.
   */
  public void cancelInvitation(long gameId, long invitationId)
      throws NotFoundException, ConflictException {
    InvitationEntity invitation = get(gameId, invitationId);

    if (invitation == null) {
      throw new NotFoundException("Invitation record not found.");
    }

    if (!invitation.cancel()) {
      throw new ConflictException(
          "Unable to decline the invitation because it has already been accepted.");
    }

    dataStore.put(invitation.getEntity());
    cacheInvitationStatus(gameId, invitationId, InvitationEntity.Status.CANCELED);
  }

  /**
   * Sends an invitation notification to all devices registered for the invitee.
   *
   * @param invitationId the invitation id.
   * @param gameId the game id.
   * @throws CommunicationException when an error occurred when sending iOS push notifications.
   * @throws IOException when an error occurred when sending Android push notifications.
   */
  public void sendInvitationNotification(long invitationId, long gameId, String messageText)
      throws IOException, CommunicationException {
    InvitationEntity invitation = get(gameId, invitationId);

    if (invitation == null) {
      logger.log(Level.WARNING, "Invitation with invitationId=" + invitationId + " and gameId="
          + gameId + " not found. Ignoring sending notifications for this invitation.");
      return;
    }

    PlayerService playerService = new PlayerService();
    PlayerEntity invitee = playerService.get(invitation.getInviteeKey());

    if (invitee == null) {
      logger.log(Level.WARNING, "Invitee for the invitation with invitationId=" + invitationId
          + " and gameId=" + gameId
          + " not found. Ignoring sending notifications for this invitation.");
      return;
    }

    List<DeviceEntity> devices = new DeviceService().getDevicesForPlayer(invitee.getKey());

    List<DeviceEntity> iOsDevices = new ArrayList<DeviceEntity>();
    List<DeviceEntity> androidDevices = new ArrayList<DeviceEntity>();

    for (DeviceEntity device : devices) {
      switch (device.getDeviceType()) {
        case DeviceEntity.DEVICE_TYPE_ANDROID:
          androidDevices.add(device);
          break;
        case DeviceEntity.DEVICE_TYPE_IOS:
          iOsDevices.add(device);
          break;
      }
    }

    if (androidDevices.size() > 0) {
      sendAndroidInvitationNotification(androidDevices, invitee, invitationId, gameId, messageText);
    }

    if (iOsDevices.size() > 0) {
      sendIosInvitationNotification(iOsDevices, invitee, invitationId, gameId, messageText);
    }
  }

  /**
   * Atomically create game entity, invitation entity and a task to deliver the invitation
   * notification.
   *
   * @param senderEntity the Player entity that wants to invite another player.
   * @param invitee the Player entity of the invited player.
   * @param board the board to use for the new game
   * @return Invitation resource representing the created invitation.
   */
  private Invitation sendInvitation(
      PlayerEntity senderEntity, PlayerEntity invitee, BoardEntity board) {
    Transaction transaction = dataStore.beginTransaction(TransactionOptions.Builder.withXG(true));

    try {
      GameEntity game = new GameEntity(board.getKey());
      dataStore.put(game.getEntity());

      InvitationEntity invitation =
          new InvitationEntity(senderEntity.getKey(), invitee.getKey(), game.getKey());
      dataStore.put(transaction, invitation.getEntity());

      String invitationText = String.format(
          "%s has invited you to play a game of Griddler", senderEntity.getNickname());

      TaskOptions taskOptions = TaskOptions.Builder.withUrl("/admin/push/invitation")
          .param("InvitationId", String.valueOf(invitation.getId()))
          .param("MessageText", invitationText) 
          .param("GameId", String.valueOf(game.getId())).method(TaskOptions.Method.POST);

      invitationQueue.add(taskOptions);

      transaction.commit();

      return new Invitation(
          invitation.getKey().getId(), game.getKey().getId(), Invitation.Status.SENT);
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Caches the invitation status.
   *
   * @param gameId the game id.
   * @param invitationId the invitation id.
   * @param invitationStatus the status of the invitation to be cached.
   */
  private void cacheInvitationStatus(
      long gameId, long invitationId, InvitationEntity.Status invitationStatus) {
    String cacheKey = getInvitationCacheKey(gameId, invitationId);
    
    // Cache just the ordinal value of the status to reduce the used space in memcache.
    cache.put(cacheKey, invitationStatus.ordinal(),
        Expiration.byDeltaMillis(CACHE_EXPIRATION_IN_MILLISECONDS));
  }

  /**
   * Gets Invitation entity by game id and invitation id.
   *
   * @param gameId the game id.
   * @param invitationId the invitation id.
   */
  private InvitationEntity get(long gameId, long invitationId) {
    Key parentKey = KeyFactory.createKey(GameEntity.KIND, gameId);
    Key invitationKey = KeyFactory.createKey(parentKey, InvitationEntity.KIND, invitationId);
    return get(invitationKey);
  }

  /**
   * Gets Invitation entity by entity key.
   *
   * @param invitationKey the invitation entity key to retrieve.
   */
  private InvitationEntity get(Key invitationKey) {
    try {
      return new InvitationEntity(dataStore.get(invitationKey));
    } catch (EntityNotFoundException e) {
      return null;
    }
  }

  private String getInvitationCacheKey(long gameId, long invitationId) {
    return String.format("Invitation_Status_%d_%d", gameId, invitationId);
  }

  /**
   * Sends an invitation notification to Android devices.
   *
   * @param androidDevices the list of Android devices to send the notification to.
   * @param invitee the Player who has been invited.
   * @param invitationId the invitation id.
   * @param gameId the game id.
   * @param messageText invitationText to be sent.
   * @throws IOException if an error occurred while sending Android push notifications.
   */
  private void sendAndroidInvitationNotification(List<DeviceEntity> androidDevices,
      PlayerEntity invitee, long invitationId, long gameId, String messageText) throws IOException {
    Builder builder = new Message.Builder();
    builder.addData("messageText", messageText);
    builder.addData("invitationId", String.valueOf(invitationId));
    builder.addData("gameId", String.valueOf(gameId));
    builder.addData("playerId", String.valueOf(invitee.getId()));
    builder.addData("nickName", invitee.getNickname());
    Message message = builder.build();

    AndroidNotificationService notificationService = new AndroidNotificationService();
    notificationService.sendMessage(androidDevices, message, invitee.getKey());
  }

  /**
   * Sends an invitation notification to iOS devices.
   *
   * @param iOsDevices the list of Android devices to send the notification to.
   * @param invitee the Player who has been invited.
   * @param invitationId the invitation id.
   * @param gameId the game id.
   * @param messageText invitationText to be sent.
   * @throws CommunicationException if an error occurred while sending iOS push notifications.
   */
  private void sendIosInvitationNotification(List<DeviceEntity> iOsDevices, PlayerEntity invitee,
      long invitationId, long gameId, String messageText) throws CommunicationException {
    PushNotificationPayload payload = PushNotificationPayload.complex();

    try {
      payload.addAlert(messageText);
      payload.addCustomDictionary("invitationId", String.valueOf(invitationId));
      payload.addCustomDictionary("gameId", String.valueOf(gameId));
      payload.addCustomDictionary("playerId", String.valueOf(invitee.getId()));
      payload.addCustomDictionary("nickName", invitee.getNickname());

      IosNotificationService notificationService = new IosNotificationService();
      notificationService.sendPushNotification(iOsDevices, payload, invitee.getKey());
    } catch (JSONException e) {
      logger.log(Level.WARNING, "Invalid format of a push notification payload", e);
    }
  }
}
