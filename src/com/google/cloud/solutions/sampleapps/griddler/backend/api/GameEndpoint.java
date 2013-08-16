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
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.cloud.solutions.sampleapps.griddler.backend.services.GameService;
import com.google.cloud.solutions.sampleapps.griddler.backend.services.InvitationService;
import com.google.cloud.solutions.sampleapps.griddler.backend.utilities.Authentication;

import javax.inject.Named;

/**
 * Endpoint for starting new games, submitting answers etc.
 */
@Api(name = Configuration.NAME, version = Configuration.VERSION,
    description = Configuration.DESCRIPTION, namespace = @ApiNamespace(
        ownerDomain = Configuration.NAMESPACE_OWNER_DOMAIN,
        ownerName = Configuration.NAMESPACE_OWNER_NAME,
        packagePath = Configuration.NAMESPACE_PACKAGE_PATH), clientIds = {
        Configuration.WEB_CLIENT_ID, Configuration.ANDROID_CLIENT_ID, Configuration.IOS_CLIENT_ID},
    audiences = {Configuration.AUDIENCE}, scopes = {Configuration.EMAIL_SCOPE})
public class GameEndpoint {
  private GameService gameService = new GameService();
  private InvitationService invitationService = new InvitationService();

  /**
   * Starts a new single player game.
   *
   * @param boardLevel the desired board level.
   * @param player the current player making the request.
   * @return the newly created single player game of type {@link Game}.
   * @throws UnauthorizedException if the player is not authorized.
   * @throws NotFoundException if a board for the specified level is not found.
   */
  @ApiMethod(httpMethod = "POST", path = "games/startSinglePlayer")
  public Game startSinglePlayerGame(@Named("boardLevel") int boardLevel, User player)
      throws UnauthorizedException, NotFoundException {

    Authentication.validateUser(player);

    return gameService.startNewSinglePlayerGame(player, boardLevel);
  }

  /**
   * Retrieves an existing game.
   *
   * @param gameId the game Id.
   * @param player the current player making requests.
   * @throws UnauthorizedException if the player is not authorized.
   * @throws NotFoundException if a game with gameId is not found.
   */
  @ApiMethod(httpMethod = "GET", path = "games/{id}")
  public Game getGame(@Named("id") long gameId, User player)
      throws UnauthorizedException, NotFoundException {

    Authentication.validateUser(player);

    return gameService.getGame(gameId);
  }

  /**
   * Submits player's answers for a game.
   *
   * @param gameId the id of the game to submit the answers for.
   * @param answers a container representing the list of indexes of the correct answers.
   * @param player the current player making the request.
   * @throws UnauthorizedException if the player is not authorized.
   * @throws NotFoundException if a game with gameId is not found.
   */
  @ApiMethod(httpMethod = "PUT", path = "games/{id}/answers")
  public void submitAnswers(@Named("id") long gameId, GamePlayStatus answers, User player)
      throws UnauthorizedException, NotFoundException {

    Authentication.validateUser(player);

    gameService.submitAnswers(gameId, player, answers);
  }

  /**
   * Starts a new multi-player game and sends an invitation to another player.
   *
   * @param inviteeId the ID of the player to invite.
   * @param boardLevel the level of the board.
   * @param player the current player making the request.
   * @return invitation the invitation object created for this request {@link Invitation}
   * @throws UnauthorizedException if the user is not authorized.
   * @throws NotFoundException if a game with gameId is not found.
   */
  @ApiMethod(httpMethod = "POST", path = "games/startMultiPlayer")
  public Invitation startMultiplayerGame(
      @Named("inviteeId") long inviteeId, @Named("boardLevel") int boardLevel, User player)
      throws UnauthorizedException, NotFoundException {

    Authentication.validateUser(player);

    return invitationService.sendInvitation(player, inviteeId, boardLevel);
  }

  /**
   * Starts a new multi-player game and sends an invitation to another player identified by Google+
   * Id.
   *
   * @param inviteeGooglePlusId the Google Plus Id of the invitee.
   * @param boardLevel the level of the board.
   * @param player the current player making the request.
   * @return invitation the invitation object created for this request {@link Invitation}
   * @throws UnauthorizedException if the user is not authorized.
   * @throws NotFoundException if a game with gameId is not found.
   */
  @ApiMethod(httpMethod = "POST", path = "games/startMultiPlayerWithGooglePlus")
  public Invitation startMultiplayerGameByGooglePlusId(
      @Named("inviteeGooglePlusId") String inviteeGooglePlusId, @Named("boardLevel") int boardLevel,
      User player) throws UnauthorizedException, NotFoundException {

    Authentication.validateUser(player);

    return invitationService.sendInvitation(player, inviteeGooglePlusId, boardLevel);
  }

  /**
   * Accepts an invitation.
   *
   * @param gameId the ID of the game.
   * @param invitationId the ID of the invitation.
   * @param player the current player making the request. The player needs to be the invitee for
   *        this invitation.
   * @throws UnauthorizedException if the user is not authorized.
   * @throws NotFoundException if a game with gameId is not found.
   * @throws ConflictException if invitation status has changed and it cannot be accepted.
   */
  @ApiMethod(httpMethod = "PUT", path = "games/{gameId}/invitations/{invitationId}/accept")
  public void acceptInvitation(
      @Named("gameId") long gameId, @Named("invitationId") long invitationId, User player)
      throws UnauthorizedException, NotFoundException, ConflictException {

    Authentication.validateUser(player);

    invitationService.acceptInvitation(gameId, invitationId);
  }

  /**
   * Declines an invitation.
   *
   * @param gameId the ID of the game.
   * @param invitationId the ID of the invitation.
   * @param player the current player making the request. The player needs to be the invitee for
   *        this invitation.
   * @throws UnauthorizedException if the user is not authorized.
   * @throws NotFoundException if a game with gameId is not found.
   * @throws ConflictException if invitation status has changed and it cannot be declined.
   */
  @ApiMethod(httpMethod = "PUT", path = "games/{gameId}/invitations/{invitationId}/decline")
  public void declineInvitation(
      @Named("gameId") long gameId, @Named("invitationId") long invitationId, User player)
      throws UnauthorizedException, NotFoundException, ConflictException {

    Authentication.validateUser(player);

    invitationService.declineInvitation(gameId, invitationId);
  }

  /**
   * Cancels an invitation.
   *
   * @param gameId the ID of the game.
   * @param invitationId the ID of the invitation.
   * @param player the current player making the request. The player needs to be the invitee for
   *        this invitation.
   * @throws UnauthorizedException if the user is not authorized.
   * @throws NotFoundException if a game with gameId is not found.
   * @throws ConflictException if invitation status has changed and it cannot be cancelled.
   */
  @ApiMethod(httpMethod = "PUT", path = "games/{gameId}/invitations/{invitationId}/cancel")
  public void cancelInvitation(
      @Named("gameId") long gameId, @Named("invitationId") long invitationId, User player)
      throws UnauthorizedException, NotFoundException, ConflictException {

    Authentication.validateUser(player);

    invitationService.cancelInvitation(gameId, invitationId);
  }

  /**
   * Gets the status of an invitation.
   *
   * @param gameId the ID of the game.
   * @param invitationId the ID of the invitation.
   * @param player the current user making the request.
   * @throws UnauthorizedException if the user is not authorized.
   * @throws NotFoundException if an invitation for gameId and invitationId is not found.
   */
  @ApiMethod(httpMethod = "GET", path = "games/{gameId}/invitations/{invitationId}")
  public Invitation getInvitationStatus(
      @Named("gameId") long gameId, @Named("invitationId") long invitationId, User player)
      throws UnauthorizedException, NotFoundException {

    Authentication.validateUser(player);

    Invitation.Status status = invitationService.getInvitationStatus(gameId, invitationId);
    return new Invitation(invitationId, gameId, status);
  }
}
