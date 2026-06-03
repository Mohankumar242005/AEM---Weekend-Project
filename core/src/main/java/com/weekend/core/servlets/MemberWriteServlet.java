package com.weekend.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * An all-methods servlet that handles POST requests.
 * It extends SlingAllMethodsServlet to allow unsafe/write operations (POST).
 */
@Component(service = { Servlet.class }, property = {
        "sling.servlet.paths=/bin/members/write",
        "sling.servlet.methods=" + HttpConstants.METHOD_POST
})
@ServiceDescription("Team Member Write POST Servlet")
public class MemberWriteServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Retrieve raw post parameters
        String fullName = req.getParameter("fullName");
        String role = req.getParameter("role");

        // Basic parameter validation
        if (fullName == null || fullName.trim().isEmpty() || role == null || role.trim().isEmpty()) {
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"fullName and role are required parameters.\"}");
            return;
        }

        // Return JSON success response representing write operation
        resp.getWriter().write(String.format(
                "{\"status\": \"success\", \"service\": \"SlingAllMethodsServlet POST\", \"received\": {\"fullName\": \"%s\", \"role\": \"%s\"}}",
                fullName, role
        ));
    }
}
