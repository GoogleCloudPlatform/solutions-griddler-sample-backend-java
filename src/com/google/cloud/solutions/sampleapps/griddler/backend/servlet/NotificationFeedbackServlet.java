package com.google.cloud.solutions.sampleapps.griddler.backend.servlet;

import com.google.cloud.solutions.sampleapps.griddler.backend.services.IosNotificationService;

import javapns.communication.exceptions.CommunicationException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used to process the notification feedback service. It is intended to be called by a cron
 * job.
 */
@SuppressWarnings("serial")
public class NotificationFeedbackServlet extends HttpServlet {
  private static final Logger logger =
      Logger.getLogger(NotificationFeedbackServlet.class.getSimpleName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    IosNotificationService service = new IosNotificationService();

    try {
      service.processNotificationFeedback();
    } catch (CommunicationException e) {
      logger.log(Level.INFO, "CommunicationException when processing notification feedback.");
    }
  }
}
