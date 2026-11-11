// Recorridos visibles de la web en un navegador real (Microsoft Edge instalado
// en el equipo; no se descarga otro navegador). Uso: npm run test:e2e
const { defineConfig } = require("@playwright/test");

module.exports = defineConfig({
  testDir: __dirname,
  testMatch: /.*\.spec\.js$/,
  timeout: 30000,
  fullyParallel: false,
  workers: 1,
  reporter: [["list"]],
  outputDir: "../../test-results",
  use: {
    channel: process.env.PW_CANAL || "msedge",
    headless: true,
    locale: "es-CL",
    screenshot: "only-on-failure"
  }
});
