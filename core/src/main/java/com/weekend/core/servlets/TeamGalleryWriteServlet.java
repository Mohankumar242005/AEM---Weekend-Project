package com.weekend.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes = "weekend/components/teamgallery",
        methods = HttpConstants.METHOD_POST,
        selectors = "update",
        extensions = "json"
)
@ServiceDescription("Team Gallery Component POST Updater Servlet")
public class TeamGalleryWriteServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;
    
    private static final Logger log = LoggerFactory.getLogger(TeamGalleryWriteServlet.class);

    @Override
    protected void doPost(final SlingHttpServletRequest req,
            final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 1. Get the current component node resource from the request
        Resource componentResource = req.getResource();
        log.info("TeamGalleryWriteServlet POST triggered for resource: {}", componentResource.getPath());

        ResourceResolver resolver = componentResource.getResourceResolver();

        // 2. Retrieve the new gallery title from POST parameters
        String newTitle = req.getParameter("galleryTitle");

        if (newTitle == null || newTitle.trim().isEmpty()) {
            resp.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"Missing parameter: galleryTitle\"}");
            return;
        }

        try {
            // 3. Adapt resource to ModifiableValueMap to perform writes
            ModifiableValueMap properties = componentResource.adaptTo(ModifiableValueMap.class);
            if (properties != null) {
                properties.put("galleryTitle", newTitle.trim());
                
                // 4. Commit changes to JCR Database
                resolver.commit();
                log.info("JCR Node at {} updated successfully with new title: {}", componentResource.getPath(), newTitle.trim());

                resp.getWriter().write(String.format(
                        "{\"status\": \"success\", \"message\": \"JCR node updated successfully!\", \"newTitle\": \"%s\"}",
                        newTitle.trim()
                ));
            } else {
                resp.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"status\": \"error\", \"message\": \"Resource is not modifiable.\"}");
            }
        } catch (Exception e) {
            resp.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
