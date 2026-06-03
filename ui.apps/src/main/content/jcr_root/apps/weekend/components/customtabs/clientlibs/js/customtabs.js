(function () {
    "use strict";

    function initializeTabs() {
        var tabContainers = document.querySelectorAll(".custom-tabs-container");

        tabContainers.forEach(function (container) {
            // Check if already initialized to prevent duplicate event listeners
            if (container.dataset.tabsInitialized) {
                return;
            }

            var headers = container.querySelectorAll(".custom-tab-header-item");
            var panels = container.querySelectorAll(".custom-tab-panel-item");

            headers.forEach(function (header) {
                header.addEventListener("click", function () {
                    var targetIndex = header.getAttribute("data-tab-index");

                    // 1. Remove active class from all headers in this container
                    headers.forEach(function (h) {
                        h.classList.remove("active");
                    });

                    // 2. Remove active class from all panels in this container
                    panels.forEach(function (p) {
                        p.classList.remove("active");
                    });

                    // 3. Set active class on clicked header
                    header.classList.add("active");

                    // 4. Set active class on matching panel
                    var matchingPanel = container.querySelector(".custom-tab-panel-item[data-tab-index='" + targetIndex + "']");
                    if (matchingPanel) {
                        matchingPanel.classList.add("active");
                    }
                });
            });

            container.dataset.tabsInitialized = "true";
        });
    }

    // Run on DOM load
    document.addEventListener("DOMContentLoaded", function () {
        initializeTabs();
    });

    // Run in AEM Authoring mode when components are edited or re-rendered
    if (window.Granite && window.Granite.author) {
        if (window.jQuery) {
            window.jQuery(document).on("cq-content-loaded", function () {
                initializeTabs();
            });
        }
    }
})();
