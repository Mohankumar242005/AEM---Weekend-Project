document.addEventListener("DOMContentLoaded", () => {
    // Select all Character Panel instances to support multiple components on the same page
    const wrappers = document.querySelectorAll(".character-panel-wrapper");
    
    wrappers.forEach(wrapper => {
        // Prevent duplicate initialization
        if (wrapper.dataset.characterpanelInitialized) {
            return;
        }

        const cards = wrapper.querySelectorAll(".character-card");
        const modal = wrapper.querySelector(".character-modal");
        const modalClose = wrapper.querySelector(".character-modal-close");
        const modalImg = wrapper.querySelector("#modalImage");
        const modalName = wrapper.querySelector("#modalName");
        const modalActor = wrapper.querySelector("#modalActor");

        // Modal Details Popup functionality
        cards.forEach(card => {
            card.addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation();
                
                const image = card.getAttribute("data-image");
                const name = card.getAttribute("data-name");
                const actor = card.getAttribute("data-actor");

                if (modal && modalImg && modalName && modalActor) {
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
            modalClose.addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation();
                modal.classList.remove("show");
            });
        }

        // Close Modal when clicking outside the content panel
        window.addEventListener("click", (event) => {
            if (event.target === modal) {
                modal.classList.remove("show");
            }
        });

        wrapper.dataset.characterpanelInitialized = "true";
    });
});
