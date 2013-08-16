package com.google.cloud.solutions.sampleapps.griddler.backend.servlet;

import com.google.cloud.solutions.sampleapps.griddler.backend.services.InvitationService;

import javapns.communication.exceptions.CommunicationException;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for sending invitation notifications. It is intended to be called as a
 * Push Queue handler such that requests that throw exceptions are retried.
 */
@SuppressWarnings("serial")
public class InvitationNotificationServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    long invitationId = Long.parseLong(req.getParameter("InvitationId"));
    long gameId = Long.parseLong(req.getParameter("GameId"));
    String messageText = req.getParameter("MessageText");

    InvitationService service = new InvitationService();
    try {
      service.sendInvitationNotification(invitationId, gameId, messageText);
    } catch (CommunicationException e) {
      // Rethrow the exception so the request can be retried by the push queue.
      throw new ServletException(e.getMessage());
    }
  }
}
