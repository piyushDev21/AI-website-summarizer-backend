import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Builds the side panel page; manifest.json, background.js and icons are copied from public/.
export default defineConfig({
  plugins: [react()],
  base: "./",
  build: {
    outDir: "dist",
    emptyOutDir: true,
    rollupOptions: {
      input: "sidepanel.html",
    },
  },
});
