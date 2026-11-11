// Sustituto de supabase-js para pruebas de recorridos visibles de la web.
// Imita la API usada por la web (from/select/update/delete/rpc/storage/auth)
// con datos en memoria que cada prueba controla mediante window.__FALSO__.
// NO prueba RLS ni PostgreSQL: eso lo cubren las pruebas SQL reales.
(function () {
  "use strict";
  var F = window.__FALSO__ = window.__FALSO__ || {};
  F.tablas = F.tablas || { tiendas: [], productos: [], perfiles: [], tipos_producto: [], ferias: [], ventas: [] };
  F.llamadas = [];
  F.fallar = F.fallar || {};          // { "productos:select": "Failed to fetch" | { message } }
  F.respuestasRpc = F.respuestasRpc || {}; // { registrar_venta: [ {data}|{error} , ... ] } en orden
  var oyentes = [];

  function copia(x) { return JSON.parse(JSON.stringify(x)); }

  function consulta(tabla) {
    var q = { tabla: tabla, op: "select", filtros: [], valores: null };
    var api = {
      select: function () { return api; },
      update: function (v) { q.op = "update"; q.valores = v; return api; },
      delete: function () { q.op = "delete"; return api; },
      insert: function (v) { q.op = "insert"; q.valores = v; return api; },
      eq: function (k, v) { q.filtros.push(function (f) { return f[k] === v; }); return api; },
      gte: function (k, v) { q.filtros.push(function (f) { return !f[k] || f[k] >= v; }); return api; },
      in: function (k, v) { q.filtros.push(function (f) { return v.indexOf(f[k]) >= 0; }); return api; },
      order: function () { return api; },
      maybeSingle: function () { q.unico = true; return api; },
      then: function (ok, ko) { return ejecutar(q).then(ok, ko); }
    };
    return api;
  }

  function ejecutar(q) {
    F.llamadas.push({ tabla: q.tabla, op: q.op, valores: q.valores ? copia(q.valores) : null });
    var clave = q.tabla + ":" + q.op;
    return new Promise(function (resolver, rechazar) {
      setTimeout(function () {
        var falla = F.fallar[clave];
        if (falla) {
          if (typeof falla === "string") return rechazar(new TypeError(falla));
          return resolver({ data: null, error: falla });
        }
        var filas = (F.tablas[q.tabla] || []).filter(function (f) { return q.filtros.every(function (fn) { return fn(f); }); });
        if (q.op === "update") { filas.forEach(function (f) { Object.assign(f, q.valores); }); }
        if (q.op === "delete") { F.tablas[q.tabla] = F.tablas[q.tabla].filter(function (f) { return filas.indexOf(f) < 0; }); }
        var datos = copia(filas);
        resolver({ data: q.unico ? (datos[0] || null) : datos, error: null });
      }, F.demora || 20);
    });
  }

  var cliente = {
    from: consulta,
    rpc: function (nombre, args) {
      F.llamadas.push({ rpc: nombre, args: copia(args) });
      var cola = F.respuestasRpc[nombre] || [];
      var r = cola.length ? cola.shift() : { data: null, error: { message: "Sin respuesta configurada para " + nombre } };
      return new Promise(function (resolver, rechazar) {
        setTimeout(function () {
          if (r.red) return rechazar(new TypeError("Failed to fetch"));
          if (r.data && r.alGuardar) r.alGuardar(args);
          resolver({ data: r.data === undefined ? null : r.data, error: r.error || null });
        }, F.demora || 20);
      });
    },
    storage: {
      from: function (bucket) {
        return {
          upload: function (ruta) { F.llamadas.push({ subida: bucket + "/" + ruta }); return Promise.resolve(F.fallaSubida ? { data: null, error: { message: F.fallaSubida } } : { data: { path: ruta }, error: null }); },
          getPublicUrl: function (ruta) { return { data: { publicUrl: "https://falso.supabase.co/storage/v1/object/public/" + bucket + "/" + ruta } }; },
          remove: function (rutas) { F.llamadas.push({ borrado: rutas }); return Promise.resolve({ data: [], error: null }); }
        };
      }
    },
    auth: {
      getSession: function () { return Promise.resolve({ data: { session: F.sesion || null }, error: null }); },
      onAuthStateChange: function (fn) {
        oyentes.push(fn);
        setTimeout(function () { fn("INITIAL_SESSION", F.sesion || null); }, 0);
        return { data: { subscription: { unsubscribe: function () {} } } };
      },
      signInWithPassword: function (cred) {
        F.llamadas.push({ login: cred.email });
        if (!F.usuarioLogin) return Promise.resolve({ data: {}, error: { message: "Invalid login credentials" } });
        F.sesion = { user: F.usuarioLogin, access_token: "falso" };
        oyentes.forEach(function (fn) { setTimeout(function () { fn("SIGNED_IN", F.sesion); }, 0); });
        return Promise.resolve({ data: { session: F.sesion, user: F.usuarioLogin }, error: null });
      },
      signOut: function () {
        F.sesion = null;
        oyentes.forEach(function (fn) { setTimeout(function () { fn("SIGNED_OUT", null); }, 0); });
        return Promise.resolve({ error: null });
      },
      signInWithOAuth: function (o) { F.llamadas.push({ oauth: o.provider }); return Promise.resolve({ data: {}, error: null }); },
      resetPasswordForEmail: function (email, o) { F.llamadas.push({ recuperar: email, redirectTo: o && o.redirectTo }); return Promise.resolve({ data: {}, error: null }); },
      updateUser: function () { return Promise.resolve({ data: {}, error: F.sesion ? null : { message: "Auth session missing!" } }); },
      signUp: function () { return Promise.resolve({ data: {}, error: { message: "no disponible en pruebas" } }); }
    }
  };
  window.supabase = { createClient: function () { return cliente; } };
})();
