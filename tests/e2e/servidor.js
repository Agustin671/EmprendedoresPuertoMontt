"use strict";
// Servidor estático mínimo para probar la web localmente (sin dependencias).
// Uso: node tests/e2e/servidor.js [puerto]
const http = require("http");
const fs = require("fs");
const path = require("path");

const RAIZ = path.resolve(__dirname, "..", "..", "LA PAgina (segun)", "proyectode10");
const TIPOS = { ".html": "text/html; charset=utf-8", ".js": "text/javascript; charset=utf-8", ".css": "text/css; charset=utf-8",
  ".png": "image/png", ".jpg": "image/jpeg", ".svg": "image/svg+xml", ".otf": "font/otf", ".apk": "application/vnd.android.package-archive" };

function crearServidor() {
  return http.createServer((req, res) => {
    const ruta = decodeURIComponent(new URL(req.url, "http://x").pathname);
    let archivo = path.normalize(path.join(RAIZ, ruta === "/" ? "index.html" : ruta));
    if (!archivo.startsWith(RAIZ)) { res.writeHead(403); res.end(); return; }
    fs.stat(archivo, (err, st) => {
      if (err || !st.isFile()) { res.writeHead(404, { "Content-Type": "text/plain" }); res.end("No existe"); return; }
      res.writeHead(200, { "Content-Type": TIPOS[path.extname(archivo)] || "application/octet-stream", "Cache-Control": "no-store" });
      fs.createReadStream(archivo).pipe(res);
    });
  });
}

if (require.main === module) {
  const puerto = Number(process.argv[2] || 4173);
  crearServidor().listen(puerto, "127.0.0.1", () => console.log(`Web local en http://localhost:${puerto}/ (modo demostración si no hay js/config.js)`));
}
module.exports = { crearServidor, RAIZ };
