document.addEventListener("alpine:init", () => {

    Alpine.data("imageUploader", () => ({

        dragging: false,

        preview: null,

        init() {

            window.addEventListener("paste", (event) => {

                const imageItem = [...event.clipboardData.items]
                    .find(item => item.type.startsWith("image/"));

                if (!imageItem) {
                    return;
                }

                const file = imageItem.getAsFile();

                if (!file) {
                    return;
                }

                const dataTransfer = new DataTransfer();

                dataTransfer.items.add(file);

                this.$refs.input.files = dataTransfer.files;

                this.handleFile(file);
            });

        },

        handleFile(file) {

            if (!file) {
                return;
            }

            if (!file.type.startsWith("image/")) {
                return;
            }

            if (this.preview) {
                URL.revokeObjectURL(this.preview);
            }

            this.preview = URL.createObjectURL(file);
        },

        handleDrop(event) {

            this.dragging = false;

            const file = event.dataTransfer.files[0];

            if (!file) {
                return;
            }

            this.$refs.input.files = event.dataTransfer.files;

            this.handleFile(file);
        },

        remove() {

            if (this.preview) {
                URL.revokeObjectURL(this.preview);
            }

            this.preview = null;

            this.$refs.input.value = "";
        }

    }));

});