import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import { resolve } from "path";

export default defineConfig({
  plugins: [tailwindcss()],
  build: {
    outDir: resolve(__dirname, "src/main/resources/static"),
    emptyOutDir: true,
    rollupOptions: {
      input: resolve(__dirname, "src/main/resources/script/app.js"),
      output: {
        entryFileNames: "assets/app.js",
        assetFileNames: "assets/[name].[ext]",
      },
    },
  },
});
