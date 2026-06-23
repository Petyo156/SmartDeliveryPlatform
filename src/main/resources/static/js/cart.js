(function () {
    const checkout = document.querySelector("[data-checkout-address]");

    if (!checkout) {
        return;
    }

    const radios = Array.from(checkout.querySelectorAll('input[name="addressMode"]'));
    const panels = Array.from(checkout.querySelectorAll("[data-address-panel]"));

    function getSelectedMode() {
        const selected = radios.find((radio) => radio.checked);

        if (selected) {
            return selected.value;
        }

        return radios[0] ? radios[0].value : "EXISTING";
    }

    function syncAddressMode() {
        const selectedMode = getSelectedMode();

        radios.forEach((radio) => {
            const option = radio.closest(".checkout-address-mode");
            if (option) {
                option.classList.toggle("is-selected", radio.value === selectedMode);
            }
        });

        panels.forEach((panel) => {
            const isActive = panel.dataset.addressPanel === selectedMode;
            panel.classList.toggle("is-active", isActive);

            panel.querySelectorAll("input, select, textarea").forEach((field) => {
                field.disabled = !isActive;
            });
        });
    }

    radios.forEach((radio) => {
        radio.addEventListener("change", syncAddressMode);
    });

    syncAddressMode();
})();
