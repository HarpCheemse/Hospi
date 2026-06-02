const hotelPalette = {
  ink: "#17211F",
  forest: "#123C35",
  emerald: "#1F6F5B",
  mint: "#DDEDE7",
  gold: "#C7A45D",
  champagne: "#F3E5C6",
  ivory: "#FBF8F1",
  porcelain: "#FFFFFF",
  stone: "#6B6258",
  linen: "#EEE7DA",
  clay: "#A14F35",
  sky: "#2B6F8F",
};

module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/webapp/**/*.jsp",
    "./src/main/webapp/**/*.html",
    "./src/main/**/*.{js,ts}",
  ],

  theme: {
    extend: {
      colors: {
        primary: hotelPalette.emerald,
        "primary-dark": hotelPalette.forest,
        "primary-soft": hotelPalette.mint,
        accent: hotelPalette.gold,
        "accent-soft": hotelPalette.champagne,

        background: hotelPalette.ivory,
        surface: hotelPalette.porcelain,
        "surface-muted": "#F4EFE6",
        "surface-warm": hotelPalette.linen,
        "surface-dark": hotelPalette.ink,

        text: hotelPalette.ink,
        muted: hotelPalette.stone,
        subtle: "#91887D",
        border: "#DED5C7",

        success: "#2F7D4F",
        warning: "#B7791F",
        danger: "#B84A3A",
        info: hotelPalette.sky,
        
        ink: hotelPalette.ink,
        forest: hotelPalette.forest,
        emerald: hotelPalette.emerald,
        mint: hotelPalette.mint,
        gold: hotelPalette.gold,
        champagne: hotelPalette.champagne,
        ivory: hotelPalette.ivory,
        porcelain: hotelPalette.porcelain,
        stone: hotelPalette.stone,
        linen: hotelPalette.linen,
        clay: hotelPalette.clay,
      },

      fontFamily: {
        display: ['"Cormorant Garamond"', "Georgia", "serif"],
        sans: ['"DM Sans"', "ui-sans-serif", "system-ui", "sans-serif"],
        mono: ['"JetBrains Mono"', "ui-monospace", "SFMono-Regular", "monospace"],
      },

      fontSize: {
        "2xs": ["0.6875rem", { lineHeight: "1rem" }],
        xs: ["0.75rem", { lineHeight: "1.125rem" }],
        sm: ["0.875rem", { lineHeight: "1.375rem" }],
        base: ["1rem", { lineHeight: "1.625rem" }],
        lg: ["1.125rem", { lineHeight: "1.75rem" }],
        xl: ["1.25rem", { lineHeight: "1.875rem" }],
        "2xl": ["1.5rem", { lineHeight: "2rem" }],
        "3xl": ["1.875rem", { lineHeight: "2.25rem" }],
        "4xl": ["2.5rem", { lineHeight: "2.75rem" }],
        "5xl": ["3.25rem", { lineHeight: "1.15" }],
        "6xl": ["4rem", { lineHeight: "1.1" }],
      },

      borderRadius: {
        sm: "0.375rem",
        DEFAULT: "0.5rem",
        md: "0.625rem",
        lg: "0.75rem",
        xl: "1rem",
        "2xl": "1.25rem",
      },

      spacing: {
        sidebar: "16rem",
        card: "1.5rem",
        section: "3rem",
        prose: "42rem",
      },

      boxShadow: {
        card: "0 12px 32px rgba(23, 33, 31, 0.08)",
        soft: "0 18px 48px rgba(23, 33, 31, 0.10)",
        float: "0 24px 64px rgba(23, 33, 31, 0.14)",
        glow: "0 0 0 3px rgba(199, 164, 93, 0.34)",
      },

      backgroundImage: {
        "hotel-dark":
          "linear-gradient(135deg, #17211F 0%, #123C35 52%, #1F6F5B 100%)",
        "hotel-gold": "linear-gradient(135deg, #C7A45D 0%, #F3E5C6 100%)",
        "hotel-surface": "linear-gradient(180deg, #FFFFFF 0%, #FBF8F1 100%)",
      },

      letterSpacing: {
        wide: "0.08em",
        widest: "0.16em",
      },

      transitionTimingFunction: {
        hotel: "cubic-bezier(0.22, 1, 0.36, 1)",
      },

      transitionDuration: {
        DEFAULT: "180ms",
        slow: "360ms",
      },
    },
  },

  plugins: [
    function ({ addBase }) {
      addBase({
        html: { "scrollbar-gutter": "stable" },
        body: {
          backgroundColor: hotelPalette.ivory,
          color: hotelPalette.ink,
        },
        "::selection": {
          backgroundColor: hotelPalette.champagne,
          color: hotelPalette.ink,
        },
      });
    },

    function ({ addComponents, theme }) {
      addComponents({
        ".card-dark": {
          background: theme("backgroundImage.hotel-dark"),
          color: theme("colors.ivory"),
          borderRadius: theme("borderRadius.2xl"),
          boxShadow: theme("boxShadow.float"),
        },
        ".label-gold": {
          color: theme("colors.accent"),
          fontFamily: theme("fontFamily.display").join(", "),
          letterSpacing: theme("letterSpacing.wide"),
          fontWeight: "600",
        },
        ".badge-suite": {
          backgroundColor: theme("colors.accent-soft"),
          color: theme("colors.primary-dark"),
          fontSize: theme("fontSize.2xs")[0],
          fontWeight: "700",
          letterSpacing: theme("letterSpacing.widest"),
          textTransform: "uppercase",
          padding: "0.1875rem 0.625rem",
          borderRadius: "9999px",
        },
      });
    },
  ],
};
