package com.google.cloud.solutions.sampleapps.griddler.backend.servlet;

import com.google.api.server.spi.response.NotFoundException;
import com.google.cloud.solutions.sampleapps.griddler.backend.services.GameService;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for updating player statistics. It is intended to be called as a Push Queue handler such
 * that requests that throw exceptions are retried.
 *
 */
@SuppressWarnings("serial")
public class ProcessGameResultsServlet extends HttpServlet {
  private static final Logger logger =
      Logger.getLogger(ProcessGameResultsServlet.class.getSimpleName());
  private GameService service = new GameService();

  /*
   * This method is is intended to by called as a push task handler and it relies on the retry
   * mechanism provided by Task Queues to handle concurrent modification exceptions and other
   * transient issues.
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    long gameId = Long.parseLong(req.getParameter("gameId"));
    long winnerId = Long.parseLong(req.getParameter("winnerId"));

    try {
      service.updatePlayerStatistics(gameId, winnerId);
    } catch (NotFoundException e) {
      logger.log(Level.WARNING, "Game " + gameId + " wasn't found. This may indicate a bug.", e);
    }
  }
}
