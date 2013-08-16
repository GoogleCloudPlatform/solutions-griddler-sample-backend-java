package com.google.cloud.solutions.sampleapps.griddler.backend.utilities;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;

/**
 * Utility class used to authenticate and authorize users.
 */
public class Authentication {

  /**
   * Checks if user is authorized.
   *
   * @param user the user to be validated.
   * @throws UnauthorizedException if user is not authorized.
   */
  public static void validateUser(User user) throws UnauthorizedException {
    // Google Cloud Endpoints sets user to a non-null object if the request came from an approved
    // client and the request was correctly authenticated. However, if the request was not correctly
    // authenticated, the user object will be null.
    // Griddler requires that all requests are authenticated and UnauthorizedException is thrown for
    // requests that are not correctly authenticated.
    if (user == null) {
      throw new UnauthorizedException("The user is not authorized.");
    }

    // Griddler doesn't use any other authorization mechanism. However, if for example the game
    // required a paid subscription or had other authorization requirements they could be enforced
    // in this centralized method.
  }
}
