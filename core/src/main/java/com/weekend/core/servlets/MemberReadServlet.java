package com.weekend.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * A safe servlet that only handles GET requests.
 * It extends SlingSafeMethodsServlet because GET is read-only and idempotent.
 */
@Component(service = { Servlet.class }, property = {
        "sling.servlet.paths=/bin/members/read",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
@ServiceDescription("Team Member Read GET Servlet")
public class MemberReadServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // Return dummy JSON response representing read operation
        resp.getWriter().write("{\"status\": \"active\", \"service\": \"SlingSafeMethodsServlet GET\", \"message\": \"Team data read successfully.\"}");
    }
}
