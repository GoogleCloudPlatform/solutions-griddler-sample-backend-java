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

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.DeviceEntity;
import com.google.cloud.solutions.sampleapps.griddler.backend.services.DeviceService;
import com.google.cloud.solutions.sampleapps.griddler.backend.services.PlayerService;
import com.google.cloud.solutions.sampleapps.griddler.backend.utilities.Authentication;
import com.google.cloud.solutions.sampleapps.griddler.backend.utilities.Strings;

import java.util.List;

import javax.inject.Named;

/**
 * Endpoint for managing players.
 */
@Api(name = Configuration.NAME, version = Configuration.VERSION,
    description = Configuration.DESCRIPTION, namespace = @ApiNamespace(
        ownerDomain = Configuration.NAMESPACE_OWNER_DOMAIN,
        ownerName = Configuration.NAMESPACE_OWNER_NAME,
        packagePath = Configuration.NAMESPACE_PACKAGE_PATH), clientIds = {
        Configuration.WEB_CLIENT_ID, Configuration.ANDROID_CLIENT_ID, Configuration.IOS_CLIENT_ID},
    audiences = {Configuration.AUDIENCE}, scopes = {Configuration.EMAIL_SCOPE})
public class PlayerEndpoint {
  private PlayerService playerService = new PlayerService();
  private DeviceService deviceService = new DeviceService();

  /**
   * Gets a list of active players. This method is suitable for a small number of players. In
   * production games that may have a large number of players, paging should be implemented in both
   * the server as well as in the client applications.
   *
   * @param player the current player making the request.
   * @throws UnauthorizedException if the player is not authorized.
   */
  @ApiMethod(httpMethod = "GET", path = "players")
  public List<Player> list(User player) throws UnauthorizedException {

    Authentication.validateUser(player);

    return playerService.getPlayers(player);
  }

  /**
   * Gets the player's resource, including game statistics, such as the number of wins.
   *
   * @param player the current player making the request.
   * @throws UnauthorizedException if the player is not authorized.
   * @throws NotFoundException if the player is not registered.
   */
  @ApiMethod(httpMethod = "GET", path = "players/me")
  public Player get(User player) throws UnauthorizedException, NotFoundException {

    Authentication.validateUser(player);

    return playerService.getPlayer(player);
  }

  /**
   * Registers the current user as a player.
   *
   * @param googlePlusId user's Google+ id. Can be empty if user doesn't use a G+ profile.
   * @param user the current user making the request.
   * @return Player object for the registered user.
   * @throws UnauthorizedException if the user is not authorized.
   */
  @ApiMethod(httpMethod = "POST", path = "players")
  public Player insert(@Named("googlePlusId") String googlePlusId, User user)
      throws UnauthorizedException {

    Authentication.validateUser(user);

    if (Strings.isNullOrEmpty(googlePlusId)) {
      return playerService.registerPlayer(user);
    } else {
      return playerService.registerPlayerWithPlusId(user, googlePlusId);
    }
  }

  /**
   * Updates the current player record with Google+ profile Id.
   *
   * @param googlePlusId user's Google+ id. Cannot be empty or else a BadRequestException is thrown.
   * @param player the current player making the request.
   * @throws UnauthorizedException if the player is not authorized.
   * @throws BadRequestException if googlePlusId is empty.
   * @throws NotFoundException if the player is not registered.
   */
  @ApiMethod(httpMethod = "PUT", path = "players/me/{googlePlusId}")
  public void assignPlayerPlusId(@Named("googlePlusId") String googlePlusId, User player)
      throws UnauthorizedException, BadRequestException, NotFoundException {

    Authentication.validateUser(player);

    if (Strings.isNullOrEmpty(googlePlusId)) {
      throw new BadRequestException("googlePlusId cannot be empty");
    }

    playerService.assignPlusId(player, googlePlusId);
  }

  /**
   * Registers a device for the current player. There can be multiple devices registered for a
   * player.
   *
   * @param deviceId the id for a device typically assigned by the push notification system.
   * @param deviceType indicates the type of the device.
   * @param player the current player making the request.
   * @throws UnauthorizedException if the player is not authorized.
   * @throws BadRequestException if deviceId is empty or deviceType is invalid.
   * @throws NotFoundException if the player is not registered.
   */
  @ApiMethod(httpMethod = "PUT", path = "players/me/device")
  public void registerDevice(
      @Named("deviceId") String deviceId, @Named("deviceType") int deviceType, User player)
      throws UnauthorizedException, BadRequestException, NotFoundException {

    Authentication.validateUser(player);

    if (Strings.isNullOrEmpty(deviceId)) {
      throw new BadRequestException("deviceId cannot be empty");
    }

    if (deviceType != DeviceEntity.DEVICE_TYPE_ANDROID
        && deviceType != DeviceEntity.DEVICE_TYPE_IOS) {
      throw new BadRequestException("invalid deviceType");
    }

    deviceService.registerDevice(player, deviceType, deviceId);
  }

  /**
   * Unregisters a device for the current player.
   *
   * @param deviceId the id for a device typically assigned by the push notification system.
   * @param player the current player making the request.
   * @throws UnauthorizedException if the player is not authorized.
   * @throws BadRequestException if deviceId is empty.
   * @throws NotFoundException if the player is not registered.
   */
  @ApiMethod(httpMethod = "DELETE", path = "players/me/device")
  public void unRegisterDevice(@Named("deviceId") String deviceId, User player)
      throws UnauthorizedException, BadRequestException, NotFoundException {

    Authentication.validateUser(player);

    if (Strings.isNullOrEmpty(deviceId)) {
      throw new BadRequestException("deviceId cannot be empty");
    }

    deviceService.unregisterDevice(player, deviceId);
  }
}
