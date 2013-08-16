package com.google.cloud.solutions.sampleapps.griddler.backend.servlet;

import com.google.cloud.solutions.sampleapps.griddler.backend.services.DeviceService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for processing requests to unregister iOS devices. It is intended to be called as a
 * Push Queue handler such that requests that throw exceptions are retried.
 */
@SuppressWarnings("serial")
public class DeviceCleanupServlet extends HttpServlet {
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    DeviceService service = new DeviceService();
    service.unregisterIosDevice(req.getParameter("DeviceId"));
  }
}
