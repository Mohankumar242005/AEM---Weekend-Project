package com.weekend.core.servlets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Servlet acting as a backend retriever for mock API data for the no-js component.
 * Registered by ResourceType for weekend/components/mockapinojs with users selector.
 */
@Component(service = { Servlet.class }, property = {
        "sling.servlet.resourceTypes=weekend/components/mockapinojs",
        "sling.servlet.selectors=users",
        "sling.servlet.extensions=json",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
@ServiceDescription("Mock API Server-Side No-JS Servlet")
public class MockApiNoJsServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(MockApiNoJsServlet.class);
    private static final String DEFAULT_API_URL = "https://jsonplaceholder.typicode.com/users";
    private static final int DEFAULT_LIMIT = 6;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        ValueMap properties = req.getResource().getValueMap();
        String apiEndpoint = properties.get("apiEndpoint", String.class);
        Integer limit = properties.get("limit", Integer.class);

        if (apiEndpoint == null || apiEndpoint.trim().isEmpty()) {
            apiEndpoint = DEFAULT_API_URL;
        }
        if (limit == null || limit <= 0) {
            limit = DEFAULT_LIMIT;
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiEndpoint.trim()))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.body());

                if (rootNode.isArray()) {
                    ArrayNode limitedArray = mapper.createArrayNode();
                    int count = 0;
                    for (JsonNode node : rootNode) {
                        if (count >= limit) {
                            break;
                        }
                        limitedArray.add(node);
                        count++;
                    }
                    resp.getWriter().write(limitedArray.toString());
                } else {
                    resp.getWriter().write(response.body());
                }
            } else {
                log.error("NoJS Servlet Mock API returned non-200: {}", response.statusCode());
                resp.setStatus(502);
                resp.getWriter().write("{\"error\": \"Failed to retrieve data from mock API. Status: " + response.statusCode() + "\"}");
            }
        } catch (IOException | InterruptedException e) {
            log.error("Network error in NoJS Servlet connecting to external mock API", e);
            resp.setStatus(504);
            resp.getWriter().write("{\"error\": \"Network connection error: " + e.getMessage() + "\"}");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            log.error("Unexpected error in NoJS servlet", e);
            resp.setStatus(500);
            resp.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }
}
