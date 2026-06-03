document.addEventListener("DOMContentLoaded", () => {

    console.log("Character Panel loaded!");

    const panel = document.querySelector(".character-panel");

    if (!panel) {
        return;
    }

    const cards = panel.querySelectorAll(".character-card");

    cards.forEach(card => {

        card.addEventListener("mouseenter", () => {
            card.style.transform = "scale(1.05)";
        });

        card.addEventListener("mouseleave", () => {
            card.style.transform = "scale(1)";
        });

    });

});