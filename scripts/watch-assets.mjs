import chokidar from "chokidar";
import { build } from "vite";

const watcher = chokidar.watch(
  [
    "src/main/resources/script",
    "src/main/resources/style",
    "src/main/resources/templates",
  ],
  { usePolling: true, interval: 1000, ignoreInitial: true },
);

let building = false;
let pending = false;

async function rebuild() {
  if (building) {
    pending = true;
    return;
  }

  building = true;
  do {
    pending = false;
    try {
      await build();
    } catch (error) {
      console.error(error);
    }
  } while (pending);
  building = false;
}

watcher.on("all", rebuild);
await rebuild();
