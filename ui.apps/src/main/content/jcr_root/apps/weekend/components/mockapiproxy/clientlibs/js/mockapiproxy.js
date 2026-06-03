(function () {
    "use strict";

    function initializeMockApiProxy() {
        var containers = document.querySelectorAll(".mock-api-proxy-container");

        containers.forEach(function (container) {
            // Prevent duplicate initialization
            if (container.dataset.proxyInitialized) {
                return;
            }

            var grid = container.querySelector(".mock-api-proxy-grid");
            var searchInput = container.querySelector(".mock-api-proxy-search-input");
            var noResults = container.querySelector(".mock-api-proxy-no-results");
            var errorAlert = container.querySelector(".mock-api-proxy-error-alert");
            var errorMessageSpan = container.querySelector(".error-message-text");

            // 1. Fetch data from resourceType-based Sling Servlet Proxy
            var resourcePath = container.dataset.resourcePath;
            var fetchUrl = resourcePath ? (resourcePath + ".users.json") : "/bin/weekend/proxy/users";

            fetch(fetchUrl)
                .then(function (response) {
                    if (!response.ok) {
                        throw new Error("HTTP error! status: " + response.status);
                    }
                    return response.json();
                })
                .then(function (data) {
                    // Check if response is an error JSON
                    if (data.error) {
                        throw new Error(data.error);
                    }

                    // Clear the grid (removes loader)
                    grid.innerHTML = "";

                    // Render cards dynamically
                    if (Array.isArray(data)) {
                        data.forEach(function (item, index) {
                            var properties = [];

                            // Recursive flattener in JS
                            function flatten(prefix, val) {
                                if (typeof val === 'object' && val !== null && !Array.isArray(val)) {
                                    Object.keys(val).forEach(function (k) {
                                        flatten(prefix ? (prefix + '.' + k) : k, val[k]);
                                    });
                                } else if (Array.isArray(val)) {
                                    var simpleArray = val.filter(function (el) {
                                        return typeof el !== 'object';
                                    });
                                    if (simpleArray.length > 0) {
                                        properties.push({ key: prefix, value: simpleArray.join(', ') });
                                    }
                                } else if (val !== null && val !== undefined) {
                                    properties.push({ key: prefix, value: String(val) });
                                }
                            }

                            flatten("", item);

                            // Find best keys for title and subtitle dynamically
                            var title = "";
                            var subtitle = "";

                            properties.forEach(function (prop) {
                                var k = prop.key.toLowerCase();
                                if (!title && (k === "name" || k === "title" || k.endsWith(".name") || k.endsWith(".title"))) {
                                    title = prop.value;
                                }
                                if (!subtitle && (k === "company.name" || k === "email" || k.indexOf("role") > -1 || k.indexOf("company") > -1)) {
                                    subtitle = prop.value;
                                }
                            });

                            if (!title) {
                                title = "Record #" + (index + 1);
                            }
                            var initials = title.trim().substring(0, 1).toUpperCase();

                            // Construct card HTML dynamically
                            var cardHtml = 
                                '<div class="mock-api-proxy-card">' +
                                '    <div class="mock-api-proxy-card-header">' +
                                '        <div class="mock-api-proxy-avatar">' + initials + '</div>' +
                                '        <div class="mock-api-proxy-card-title-group">' +
                                '            <h3>' + title + '</h3>';

                            if (subtitle) {
                                cardHtml += '            <p class="mock-api-proxy-company">' + subtitle + '</p>';
                            }

                            cardHtml +=
                                '        </div>' +
                                '    </div>' +
                                '    <div class="mock-api-proxy-card-body">';

                            properties.forEach(function (prop) {
                                cardHtml +=
                                '        <div class="mock-api-proxy-detail-item">' +
                                '            <span class="mock-api-proxy-label">' + prop.key + ':</span>' +
                                '            <span class="mock-api-proxy-val">' + prop.value + '</span>' +
                                '        </div>';
                            });

                            cardHtml +=
                                '    </div>' +
                                '</div>';

                            grid.insertAdjacentHTML("beforeend", cardHtml);
                        });
                    }

                    // 2. Wire up client-side search filtering
                    if (searchInput) {
                        searchInput.addEventListener("input", function () {
                            var query = searchInput.value.toLowerCase().trim();
                            var cards = container.querySelectorAll(".mock-api-proxy-card");
                            var visibleCount = 0;

                            cards.forEach(function (card) {
                                var text = card.textContent.toLowerCase();
                                if (text.indexOf(query) > -1) {
                                    card.style.display = "";
                                    visibleCount++;
                                } else {
                                    card.style.display = "none";
                                }
                            });

                            if (noResults) {
                                noResults.style.display = (visibleCount === 0) ? "block" : "none";
                            }
                        });
                    }
                })
                .catch(function (error) {
                    console.error("[MockApiProxy] Error loading user profiles:", error);
                    // Hide loader
                    grid.innerHTML = "";
                    
                    // Show error alert
                    if (errorAlert && errorMessageSpan) {
                        errorMessageSpan.textContent = error.message;
                        errorAlert.style.display = "block";
                    }
                });

            container.dataset.proxyInitialized = "true";
        });
    }

    // Run on DOM load
    document.addEventListener("DOMContentLoaded", function () {
        initializeMockApiProxy();
    });

    // Run in AEM Authoring mode when components are edited or re-rendered
    if (window.Granite && window.Granite.author) {
        if (window.jQuery) {
            window.jQuery(document).on("cq-content-loaded", function () {
                initializeMockApiProxy();
            });
        }
    }
})();
