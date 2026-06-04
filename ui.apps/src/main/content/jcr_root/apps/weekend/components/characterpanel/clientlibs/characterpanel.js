document.addEventListener("DOMContentLoaded", () => {
    const wrapper = document.querySelector(".character-panel-wrapper");
    if (!wrapper) {
        return;
    }

    const searchInput = wrapper.querySelector(".character-search-input");
    const cards = wrapper.querySelectorAll(".character-card");
    const noResults = wrapper.querySelector(".character-no-results");

    // Modal elements
    const modal = wrapper.querySelector("#characterModal");
    const modalClose = wrapper.querySelector("#characterModalClose");
    const modalImg = wrapper.querySelector("#modalImage");
    const modalName = wrapper.querySelector("#modalName");
    const modalActor = wrapper.querySelector("#modalActor");

    // 1. Search filter functionality
    if (searchInput) {
        searchInput.addEventListener("input", () => {
            const query = searchInput.value.toLowerCase().trim();
            let visibleCount = 0;

            cards.forEach(card => {
                const name = (card.getAttribute("data-name") || "").toLowerCase();
                const actor = (card.getAttribute("data-actor") || "").toLowerCase();

                if (name.includes(query) || actor.includes(query)) {
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

    // 2. Modal Details Popup functionality
    cards.forEach(card => {
        card.addEventListener("click", () => {
            const image = card.getAttribute("data-image");
            const name = card.getAttribute("data-name");
            const actor = card.getAttribute("data-actor");

            if (modalImg && modalName && modalActor) {
                modalImg.src = image || "";
                modalImg.alt = name || "";
                modalName.textContent = name || "";
                modalActor.textContent = actor || "";
                
                // Show modal with animation class
                modal.classList.add("show");
            }
        });
    });

    // Close Modal via 'X' button
    if (modalClose) {
        modalClose.addEventListener("click", () => {
            modal.classList.remove("show");
        });
    }

    // Close Modal when clicking outside the content panel
    window.addEventListener("click", (event) => {
        if (event.target === modal) {
            modal.classList.remove("show");
        }
    });
});