package com.weekend.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Sling Model for the No-JS component.
 * Performs a loopback HTTP call to its corresponding Sling Servlet,
 * then parses and recursively flattens the returned JSON array.
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class MockApiNoJsModel {

    private static final Logger log = LoggerFactory.getLogger(MockApiNoJsModel.class);

    @Self
    private SlingHttpServletRequest request;

    private List<DynamicCard> cards = new ArrayList<>();
    private String errorMessage;
    private String servletUrl;
    private String title;

    @PostConstruct
    protected void init() {
        // Resolve title property from the JCR resource
        Resource resource = request.getResource();
        if (resource != null) {
            ValueMap properties = resource.getValueMap();
            title = properties.get("title", String.class);
        }
        if (title == null || title.trim().isEmpty()) {
            title = "Server-Side Directory (No JS)";
        }

        // Construct loopback servlet URL
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String resourcePath = request.getResource().getPath();
        servletUrl = scheme + "://" + serverName + ":" + serverPort + contextPath + resourcePath + ".users.json";

        log.info("MockApiNoJsModel calling loopback servlet URL: {}", servletUrl);

        try {
            // Copy credentials/cookies to pass along AEM session
            String authHeader = request.getHeader("Authorization");
            String cookieHeader = request.getHeader("Cookie");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(servletUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET();

            if (authHeader != null) {
                reqBuilder.header("Authorization", authHeader);
            }
            if (cookieHeader != null) {
                reqBuilder.header("Cookie", cookieHeader);
            }

            HttpResponse<String> response = client.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.body());

                // Check for proxy servlet error payload
                if (rootNode.isObject() && rootNode.has("error")) {
                    errorMessage = rootNode.get("error").asText();
                } else if (rootNode.isArray()) {
                    int count = 0;
                    for (JsonNode node : rootNode) {
                        List<DynamicProperty> properties = new ArrayList<>();
                        flattenNode("", node, properties);

                        // Extract title and subtitle heuristically
                        String cardTitle = "";
                        String cardSubtitle = "";

                        for (DynamicProperty prop : properties) {
                            String key = prop.getKey().toLowerCase();
                            if (cardTitle.isEmpty() && (key.equals("name") || key.equals("title") || key.endsWith(".name") || key.endsWith(".title"))) {
                                cardTitle = prop.getValue();
                            }
                            if (cardSubtitle.isEmpty() && (key.equals("company.name") || key.equals("email") || key.contains("role") || key.contains("company"))) {
                                cardSubtitle = prop.getValue();
                            }
                        }

                        if (cardTitle.isEmpty()) {
                            cardTitle = "Record #" + (count + 1);
                        }

                        cards.add(new DynamicCard(cardTitle, cardSubtitle, properties));
                        count++;
                    }
                } else {
                    errorMessage = "Servlet returned non-array payload.";
                    log.warn("Payload is not an array: {}", response.body());
                }
            } else {
                errorMessage = "Servlet returned HTTP status: " + response.statusCode();
                log.error("Failed to query loopback servlet. Status: {}, Response: {}", response.statusCode(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            errorMessage = "Loopback connection error: " + e.getMessage();
            log.error("Connection exception occurred on loopback call", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            errorMessage = "Model processing error: " + e.getMessage();
            log.error("Unexpected error in MockApiNoJsModel", e);
        }
    }

    private void flattenNode(String prefix, JsonNode node, List<DynamicProperty> properties) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String keyName = prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey();
                flattenNode(keyName, field.getValue(), properties);
            }
        } else if (node.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode element : node) {
                if (element.isValueNode()) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(element.asText());
                }
            }
            if (sb.length() > 0) {
                properties.add(new DynamicProperty(prefix, sb.toString()));
            }
        } else if (node.isValueNode() && !node.isNull()) {
            properties.add(new DynamicProperty(prefix, node.asText()));
        }
    }

    public List<DynamicCard> getCards() {
        return cards;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getServletUrl() {
        return servletUrl;
    }

    public String getTitle() {
        return title;
    }

    public static class DynamicCard {
        private final String title;
        private final String subtitle;
        private final List<DynamicProperty> properties;

        public DynamicCard(String title, String subtitle, List<DynamicProperty> properties) {
            this.title = title;
            this.subtitle = subtitle;
            this.properties = properties;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getInitials() {
            if (title != null && !title.trim().isEmpty()) {
                return title.trim().substring(0, 1).toUpperCase();
            }
            return "?";
        }

        public List<DynamicProperty> getProperties() {
            return properties;
        }
    }

    public static class DynamicProperty {
        private final String key;
        private final String value;

        public DynamicProperty(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
