package com.weekend.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

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
 * Dynamic Sling Model for API-Based Data Rendering.
 * Recursively parses and flattens any JSON array response on load.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class MockApiModel {

    private static final Logger log = LoggerFactory.getLogger(MockApiModel.class);
    private static final String DEFAULT_API_URL = "https://jsonplaceholder.typicode.com/users";
    private static final int DEFAULT_LIMIT = 6;

    @ValueMapValue
    private String apiEndpoint;

    @ValueMapValue
    private Integer limit;

    private List<DynamicCard> cards = new ArrayList<>();
    private String errorMessage;

    @PostConstruct
    protected void init() {
        // Fallbacks if not authored
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
                    int count = 0;
                    for (JsonNode node : rootNode) {
                        if (count >= limit) {
                            break;
                        }
                        
                        // Flatten JSON object keys recursively
                        List<DynamicProperty> properties = new ArrayList<>();
                        flattenNode("", node, properties);

                        // Find best keys for title and subtitle dynamically
                        String title = "";
                        String subtitle = "";

                        for (DynamicProperty prop : properties) {
                            String key = prop.getKey().toLowerCase();
                            if (title.isEmpty() && (key.equals("name") || key.equals("title") || key.endsWith(".name") || key.endsWith(".title"))) {
                                title = prop.getValue();
                            }
                            if (subtitle.isEmpty() && (key.equals("company.name") || key.equals("email") || key.contains("role") || key.contains("company"))) {
                                subtitle = prop.getValue();
                            }
                        }

                        // Fallbacks if no title/subtitle keys match
                        if (title.isEmpty()) {
                            title = "Record #" + (count + 1);
                        }

                        cards.add(new DynamicCard(title, subtitle, properties));
                        count++;
                    }
                } else {
                    errorMessage = "Mock API response is not a valid JSON Array.";
                    log.warn("API response is not an array: {}", response.body());
                }
            } else {
                errorMessage = "API returned HTTP status code: " + response.statusCode();
                log.error("Failed to call mock API. Status code: {}, Response: {}", response.statusCode(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            errorMessage = "Network error connecting to API: " + e.getMessage();
            log.error("Connection exception occurred while calling mock API", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        } catch (Exception e) {
            errorMessage = "Unexpected mapping error: " + e.getMessage();
            log.error("Exception in MockApiModel parsing", e);
        }
    }

    /**
     * Recursively flattens JSON nodes into key-value pairs.
     */
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

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Data Transfer Object representing a dynamic card structure.
     */
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

    /**
     * Key-Value Pair representing flat property records.
     */
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
