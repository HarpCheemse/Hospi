window.Hospi = window.Hospi || {};

Hospi.MediaUploader = function (config) {

    const uploadBox = document.querySelector(config.uploadBox);
    const input = document.querySelector(config.input);
    const previewContainer = document.querySelector(config.previewContainer);

    uploadBox.addEventListener("click", () => {
        input.click();
    });

    input.addEventListener("change", (e) => {

        const file = e.target.files[0];

        if (!file) return;

        replaceFile(file);
    });

    uploadBox.addEventListener("dragover", (e) => {
        e.preventDefault();

        uploadBox.classList.add("border-primary");
    });

    uploadBox.addEventListener("dragleave", () => {
        uploadBox.classList.remove("border-primary");
    });

    uploadBox.addEventListener("drop", (e) => {

        e.preventDefault();

        uploadBox.classList.remove("border-primary");

        const file = e.dataTransfer.files[0];

        if (!file) return;

        replaceFile(file);
    });

    document.addEventListener("paste", (e) => {

        const file = e.clipboardData.files[0];

        if (!file) return;

        replaceFile(file);
    });

    function replaceFile(file) {

        if (!file.type.startsWith("image/")) return;

        const dt = new DataTransfer();

        dt.items.add(file);

        input.files = dt.files;

        renderPreview(file);
    }

    function renderPreview(file) {

        previewContainer.innerHTML = "";

        const reader = new FileReader();

        reader.onload = (e) => {

            const img = document.createElement("img");

            img.src = e.target.result;

            img.className =
                "h-32 w-32 rounded-2xl border border-border object-cover";

            previewContainer.appendChild(img);
        };

        reader.readAsDataURL(file);
    }
};