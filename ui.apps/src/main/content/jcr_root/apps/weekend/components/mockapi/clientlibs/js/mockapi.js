(function () {
    "use strict";

    function initializeMockApi() {
        var containers = document.querySelectorAll(".mock-api-container");

        containers.forEach(function (container) {
            // Check if already initialized to prevent duplicate event listeners
            if (container.dataset.mockapiInitialized) {
                return;
            }

            var searchInput = container.querySelector(".mock-api-search-input");
            var cards = container.querySelectorAll(".mock-api-card");
            var noResults = container.querySelector(".mock-api-no-results");

            if (searchInput) {
                searchInput.addEventListener("input", function () {
                    var query = searchInput.value.toLowerCase().trim();
                    var visibleCount = 0;

                    cards.forEach(function (card) {
                        var cardText = card.textContent.toLowerCase();
                        if (cardText.indexOf(query) > -1) {
                            card.style.display = "";
                            visibleCount++;
                        } else {
                            card.style.display = "none";
                        }
                    });

                    if (noResults) {
                        if (visibleCount === 0) {
                            noResults.style.display = "block";
                        } else {
                            noResults.style.display = "none";
                        }
                    }
                });
            }

            container.dataset.mockapiInitialized = "true";
        });
    }

    // Run on DOM load or immediately if DOM is already ready
    if (document.readyState === "complete" || document.readyState === "interactive") {
        initializeMockApi();
    } else {
        document.addEventListener("DOMContentLoaded", initializeMockApi);
    }

    // Run in AEM Authoring mode when components are edited or re-rendered
    if (window.Granite && window.Granite.author) {
        if (window.jQuery) {
            window.jQuery(document).on("cq-content-loaded", function () {
                initializeMockApi();
            });
        }
    }
})();

