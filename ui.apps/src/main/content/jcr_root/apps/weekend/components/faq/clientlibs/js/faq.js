(function () {
    "use strict";

    function initializeFaq() {
        var faqContainers = document.querySelectorAll(".faq-container");

        faqContainers.forEach(function (container) {
            // Check if already initialized to prevent duplicate event listeners
            if (container.dataset.faqInitialized) {
                return;
            }

            var headers = container.querySelectorAll(".faq-header");
            var items = container.querySelectorAll(".faq-item");

            headers.forEach(function (header) {
                // Direct click listener on each header
                header.addEventListener("click", function (e) {
                    e.preventDefault();
                    e.stopPropagation(); // Stop event bubbling

                    var parentItem = header.closest(".faq-item");
                    if (parentItem) {
                        var isAlreadyActive = parentItem.classList.contains("active");

                        // 1. Close all FAQ items inside this container
                        items.forEach(function (item) {
                            item.classList.remove("active");
                        });

                        // 2. Open only the clicked item if it wasn't already active
                        if (!isAlreadyActive) {
                            parentItem.classList.add("active");
                            console.log("[FAQ] Expanded clicked item:", parentItem);
                        } else {
                            console.log("[FAQ] Collapsed clicked item:", parentItem);
                        }
                    }
                });

                // Direct keydown listener for accessibility (Enter or Spacebar)
                header.addEventListener("keydown", function (e) {
                    if (e.key === "Enter" || e.key === " ") {
                        e.preventDefault();
                        e.stopPropagation();

                        var parentItem = header.closest(".faq-item");
                        if (parentItem) {
                            var isAlreadyActive = parentItem.classList.contains("active");

                            // 1. Close all items
                            items.forEach(function (item) {
                                item.classList.remove("active");
                            });

                            // 2. Open clicked item if not already active
                            if (!isAlreadyActive) {
                                parentItem.classList.add("active");
                            }
                        }
                    }
                });
            });

            container.dataset.faqInitialized = "true";
            console.log("[FAQ] Component initialized inside container:", container);
        });
    }

    // Run on DOM load or immediately if DOM is already ready
    if (document.readyState === "complete" || document.readyState === "interactive") {
        initializeFaq();
    } else {
        document.addEventListener("DOMContentLoaded", initializeFaq);
    }

    // Run in AEM Authoring mode when components are edited or re-rendered
    if (window.Granite && window.Granite.author) {
        if (window.jQuery) {
            window.jQuery(document).on("cq-content-loaded", function () {
                initializeFaq();
            });
        }
    }
})();

