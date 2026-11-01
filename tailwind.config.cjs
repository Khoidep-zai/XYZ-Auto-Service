module.exports = {
  content: ['./src/main/resources/static/**/*.{html,js}'],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'sans-serif'],
        display: ['Space Grotesk', 'Plus Jakarta Sans', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace']
      },
      colors: {asphalt: '#0B0D10', signal: {DEFAULT: '#FF5722', hover: '#F4511E'}}
    }
  }
};
