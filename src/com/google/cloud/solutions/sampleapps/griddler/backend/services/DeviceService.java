package com.google.cloud.solutions.sampleapps.griddler.backend.services;

import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.users.User;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.DeviceEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing device registration and unregistration.
 */
public class DeviceService {
  private DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

  /**
   * Registers a device to a player (idempotent operation).
   *
   * @param player the player using the device.
   * @param deviceType the device type.
   * @param deviceId the id of the device assigned by the platform specific push notification
   *        system.
   * @throws NotFoundException when player is not registered.
   */
  public void registerDevice(User player, int deviceType, String deviceId)
      throws NotFoundException {
    PlayerEntity playerEntity = new PlayerService().getByEmail(player.getEmail());

    if (playerEntity == null) {
      throw new NotFoundException("Player not registered.");
    }

    if (isDeviceRegistered(playerEntity, deviceId, deviceType)) {
      return;
    }

    // If this is an iOS device then unregister it from other players.
    if (deviceType == DeviceEntity.DEVICE_TYPE_IOS) {
      unregisterIosDevice(deviceId);
    }

    Transaction transaction = dataStore.beginTransaction();

    try {
      DeviceEntity newDevice = new DeviceEntity(deviceId, deviceType, playerEntity.getKey());
      dataStore.put(newDevice.getEntity());

      // Mark the player as active since the player has a device registered.
      playerEntity.activate();
      dataStore.put(playerEntity.getEntity());

      transaction.commit();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Unregisters a device for a player.
   *
   * @param player the player that was using the device.
   * @param deviceId the id of the device to be unregistered.
   * @throws NotFoundException when player is not registered.
   */
  public void unregisterDevice(User player, String deviceId) throws NotFoundException {
    PlayerEntity playerEntity = new PlayerService().getByEmail(player.getEmail());
    if (playerEntity == null) {
      throw new NotFoundException("Player not registered.");
    }

    Entity deviceEntity = getDevice(playerEntity.getKey(), deviceId);
    if (deviceEntity != null) {
      this.unregisterDevice(playerEntity.getKey(), deviceEntity.getKey());
    }
  }

  /**
   * Unregisters an iOS device.
   *
   * @param deviceId the id of iOS device to unregister.
   */
  public void unregisterIosDevice(String deviceId) {
    for (DeviceEntity device : getByDeviceId(deviceId)) {
      unregisterDevice(device.getParentKey(), device.getKey());
    }
  }

  /**
   * Unregisters a device for a player. If this was the only device used by the player then the
   * player is marked as inactive.
   *
   * @param playerKey the entity key of the player who is registered with the device.
   * @param deviceKey the entity key of the device to be unregistered.
   */
  protected void unregisterDevice(Key playerKey, Key deviceKey) {
    PlayerEntity player = new PlayerService().get(playerKey);

    List<DeviceEntity> devices = getDevicesForPlayer(playerKey);

    Transaction transaction = dataStore.beginTransaction();

    try {
      for (DeviceEntity device : devices) {
        if (device.getKey().compareTo(deviceKey) == 0) {
          dataStore.delete(device.getKey());
          if (devices.size() == 1) {
            player.deactivate();
            dataStore.put(player.getEntity());
          }
          break;
        }
      }

      transaction.commit();
    } finally {
      if (transaction.isActive()) {
        transaction.rollback();
      }
    }
  }

  /**
   * Updates device's id.
   *
   * @param deviceEntity the device entity to update.
   * @param newDeviceId the new device id.
   */
  protected void updateDeviceRegistration(DeviceEntity deviceEntity, String newDeviceId) {
    deviceEntity.updateDeviceId(newDeviceId);
    dataStore.put(deviceEntity.getEntity());
  }

  /**
   * Gets a not null list of device entities by device id.
   *
   * @param deviceId the id of the device.
   */
  protected List<DeviceEntity> getByDeviceId(String deviceId) {
    List<DeviceEntity> result = new ArrayList<DeviceEntity>();

    Filter deviceIdFilter =
        new FilterPredicate(DeviceEntity.DEVICEID_PROPERTY, FilterOperator.EQUAL, deviceId);

    Query query = new Query(DeviceEntity.KIND).setFilter(deviceIdFilter);
    PreparedQuery preparedQuery = dataStore.prepare(query);

    for (Entity entity : preparedQuery.asIterable()) {
      result.add(new DeviceEntity(entity));
    }

    return result;
  }

  /**
   * Gets a not null list of device entities registered for a player.
   *
   * @param playerKey the entity key of the player
   */
  protected List<DeviceEntity> getDevicesForPlayer(Key playerKey) {

    List<DeviceEntity> results = new ArrayList<DeviceEntity>();

    Query query = new Query(DeviceEntity.KIND, playerKey);
    PreparedQuery preparedQuery = dataStore.prepare(query);

    for (Entity entity : preparedQuery.asIterable()) {
      results.add(new DeviceEntity(entity));
    }

    return results;
  }

  /**
   * Gets device entity for a given player and device id.
   *
   * @param playerKey the entity key of the player.
   * @param deviceId the id of the device to retrieve.
   * @return the retrieved device entity or null if there is no device entity for this playerKey and
   *         deviceId.
   */
  private Entity getDevice(Key playerKey, String deviceId) {
    Filter deviceIdFilter =
        new FilterPredicate(DeviceEntity.DEVICEID_PROPERTY, FilterOperator.EQUAL, deviceId);

    Query query = new Query(DeviceEntity.KIND, playerKey).setFilter(deviceIdFilter).setKeysOnly();

    PreparedQuery preparedQuery = dataStore.prepare(query);

    return preparedQuery.asSingleEntity();
  }

  /**
   * Checks if a device is registered for a given player.
   *
   * @param playerEntity the player entity.
   * @param deviceId the id of the device.
   * @param deviceType the type of the device.
   * @return true if device is registered for this player; false otherwise.
   */
  private boolean isDeviceRegistered(PlayerEntity playerEntity, String deviceId, int deviceType) {
    for (DeviceEntity device : getDevicesForPlayer(playerEntity.getKey())) {
      if (device.getDeviceId().compareToIgnoreCase(deviceId) == 0
          && device.getDeviceType() == deviceType) {
        return true;
      }
    }
    return false;
  }
}
