import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const projectRoot = dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [tailwindcss()],
  build: {
    outDir: resolve(projectRoot, "src/main/resources/static"),
    emptyOutDir: true,
    rollupOptions: {
      input: resolve(projectRoot, "src/main/resources/script/app.js"),
      output: {
        entryFileNames: "assets/app.js",
        assetFileNames: "assets/[name].[ext]",
      },
    },
  },
});
