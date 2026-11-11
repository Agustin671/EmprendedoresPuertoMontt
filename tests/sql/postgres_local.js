// Levanta un PostgreSQL local y desechable con los binarios oficiales empaquetados
// en npm (@embedded-postgres/windows-x64). No instala servicios ni toca la base remota.
"use strict";
const { execFileSync, spawn } = require("child_process");
const fs = require("fs");
const path = require("path");
const { Client } = require("pg");

const RAIZ = path.resolve(__dirname, "..", "..");

/** Carpetas bin disponibles: PG17 (devDependency) y PG15 (opcional, .herramientas/pg15). */
function versionesDisponibles() {
  const plataforma = process.platform === "win32" ? "windows-x64" : `${process.platform}-${process.arch}`;
  const candidatos = [
    { version: 17, bin: path.join(RAIZ, "node_modules", "@embedded-postgres", plataforma, "native", "bin") },
    { version: 15, bin: path.join(RAIZ, ".herramientas", "pg15", "node_modules", "@embedded-postgres", plataforma, "native", "bin") }
  ];
  return candidatos.filter(c => fs.existsSync(path.join(c.bin, process.platform === "win32" ? "postgres.exe" : "postgres")));
}

class PostgresLocal {
  constructor({ version, bin, puerto }) {
    this.version = version;
    this.bin = bin;
    this.puerto = puerto;
    this.datos = path.join(RAIZ, "tests", `.pgdata-${version}`);
  }

  exe(nombre) { return path.join(this.bin, process.platform === "win32" ? nombre + ".exe" : nombre); }

  async iniciar() {
    fs.rmSync(this.datos, { recursive: true, force: true });
    execFileSync(this.exe("initdb"), ["-D", this.datos, "-U", "postgres", "--auth=trust", "-E", "UTF8", "--locale=C"], { stdio: "ignore" });
    execFileSync(this.exe("pg_ctl"), ["-D", this.datos, "-o", `-p ${this.puerto} -c max_connections=40`, "-w", "-l", path.join(this.datos, "registro.log"), "start"], { stdio: "ignore" });
  }

  async detener() {
    try { execFileSync(this.exe("pg_ctl"), ["-D", this.datos, "-m", "fast", "-w", "stop"], { stdio: "ignore" }); } catch (e) { /* ya detenido */ }
    fs.rmSync(this.datos, { recursive: true, force: true });
  }

  cliente(base = "postgres") {
    return new Client({ host: "127.0.0.1", port: this.puerto, user: "postgres", database: base });
  }

  async crearBase(nombre) {
    const c = this.cliente();
    await c.connect();
    await c.query(`drop database if exists ${nombre}`);
    await c.query(`create database ${nombre}`);
    await c.end();
  }
}

/** Ejecuta un archivo .sql completo (sin comandos de psql). */
async function ejecutarArchivo(cliente, archivo) {
  const sql = fs.readFileSync(path.join(RAIZ, archivo), "utf8");
  return cliente.query(sql);
}

module.exports = { PostgresLocal, versionesDisponibles, ejecutarArchivo, RAIZ, spawn };
