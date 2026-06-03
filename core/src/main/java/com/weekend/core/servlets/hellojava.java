package com.weekend.core.servlets;

import com.weekend.core.services.Hellojava;

import java.io.IOException;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/hellojava"
})
public class hellojava extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private Hellojava helloJavaService;

    @Override
    protected void doGet(
            final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/plain");
        resp.getWriter().write(helloJavaService.getMessage());
    }
}
