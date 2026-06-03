package com.weekend.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * A Resource-Type based servlet.
 * It binds directly to the teamgallery component resource type.
 * Triggers when GET request ends with selector/extension ".json".
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes = "weekend/components/teamgallery",
        methods = HttpConstants.METHOD_GET,
        extensions = "json"
)
@ServiceDescription("Team Gallery Component JSON Exporter Servlet")
public class TeamGalleryServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    
    private static final Logger log = LoggerFactory.getLogger(TeamGalleryServlet.class);

    @Override
    protected void doGet(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Fetching the current component JCR Resource
        Resource componentResource = req.getResource();
        
        // Read property values from the component node
        String galleryTitle = componentResource.getValueMap().get("galleryTitle", "Default Gallery Title");

        log.info("TeamGalleryServlet GET method triggered. Component Path: {}, Title: {}", 
                componentResource.getPath(), galleryTitle);


        // Return JCR data as JSON response
        resp.getWriter().write(String.format(
                "{\"status\": \"success\", \"type\": \"Resource-Type Servlet\", \"componentPath\": \"%s\", \"galleryTitle\": \"%s\"}",
                componentResource.getPath(), galleryTitle
        ));
    }
}
