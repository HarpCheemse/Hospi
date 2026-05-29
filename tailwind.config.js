module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/webapp/**/*.jsp",
    "./src/main/webapp/**/*.html",
    "./src/**/*.{js,ts}",
  ],

  theme: {
    extend: {
      colors: {
        primary: "#1A1953",
        "primary-hover": "#4B4038",
        "primary-soft": "#9A8678",

        secondary: "#B2C09B",
        mint: "#AAEBC8",
        accent: "#9FB73C",
        earth: "#9A6B53",

        success: "#9FB73C",
        warning: "#D89A3D",
        danger: "#D9534F",
        info: "#62B6CB",

        background: "#F0F4F2",
        surface: "#FFFFFF",
        "surface-muted": "#EBF0ED",

        text: "#121311",
        muted: "#5B5E58",
        subtle: "#8A9086",
        border: "#DDE5DB",
      },

      borderRadius: {
        lg: "1rem",
        xl: "1.25rem",
        "2xl": "1.5rem",
      },

      spacing: {
        sidebar: "17.5rem",
        card: "1.5rem",
        section: "2rem",
      },

      boxShadow: {
        card: "0 12px 32px rgba(18, 19, 17, 0.06)",
        soft: "0 18px 45px rgba(18, 19, 17, 0.08)",
      },
    },
  },

  plugins: [],
};
