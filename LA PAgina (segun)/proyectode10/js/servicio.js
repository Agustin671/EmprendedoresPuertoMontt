/**
 * Acceso a Supabase para la web (equivalente a los repositorios de la app).
 * Cada operación devuelve { ok: true, datos } o { ok: false, error }:
 * un error nunca se confunde con una lista vacía.
 */
(function (raiz, fabrica) {
  "use strict";
  var modulo = fabrica();
  if (typeof module === "object" && module.exports) module.exports = modulo;
  else raiz.AlerceServicio = modulo;
})(typeof self !== "undefined" ? self : this, function () {
  "use strict";

  var COLUMNAS_PRODUCTO = "id,tienda_id,nombre,descripcion,categoria,tipo,atributos,precio,publicado,disponible,destacado," +
    "color_foto,variantes,foto_url,fotos,modalidad,maneja_stock,al_agotarse,vendida_en,actualizado_en";

  function envolver(promesa) {
    return Promise.resolve(promesa).then(function (res) {
      if (!res) return { ok: false, error: { message: "Respuesta vacía del servidor" } };
      if (res.error) return { ok: false, error: res.error };
      return { ok: true, datos: res.data === undefined ? null : res.data };
    }, function (e) {
      return { ok: false, error: e && e.message ? e : { message: String(e) } };
    });
  }

  function crear(cliente) {
    if (!cliente) throw new Error("Falta el cliente de Supabase");
    return {
      /** Tiendas públicas. */
      listarTiendas: function () {
        return envolver(cliente.from("tiendas").select("*").order("nombre", { ascending: true }));
      },
      /** Productos que la RLS deja ver a esta sesión (sin sesión: solo los públicos). */
      listarProductosVisibles: function () {
        return envolver(cliente.from("productos").select(COLUMNAS_PRODUCTO));
      },
      /** Todos los productos de mi tienda (la RLS solo entrega los de la dueña). */
      listarMisProductos: function (tiendaId) {
        return envolver(cliente.from("productos").select(COLUMNAS_PRODUCTO).eq("tienda_id", tiendaId).order("actualizado_en", { ascending: false }));
      },
      listarTipos: function () {
        return envolver(cliente.from("tipos_producto").select("*").eq("activo", true).order("orden", { ascending: true }));
      },
      listarFerias: function () {
        return envolver(cliente.from("ferias").select("id,nombre,ubicacion,fecha_inicio,fecha_fin,estado"));
      },
      leerPerfil: function (uid) {
        return envolver(cliente.from("perfiles").select("nombre,rol,tienda_id,foto_url").eq("id", uid).maybeSingle());
      },
      leerTienda: function (tiendaId) {
        return envolver(cliente.from("tiendas").select("*").eq("id", tiendaId).maybeSingle());
      },
      actualizarTienda: function (tiendaId, cambios) {
        return envolver(cliente.from("tiendas").update(cambios).eq("id", tiendaId).select("*")).then(function (r) {
          if (r.ok && (!r.datos || !r.datos.length)) return { ok: false, error: { message: "permission denied: la tienda no se actualizó" } };
          return r;
        });
      },
      /** Alta o edición segura (la tienda la decide el servidor). */
      guardarProducto: function (cuerpo) {
        return envolver(cliente.rpc("guardar_producto", { p: cuerpo }));
      },
      /** Disponible / no disponible para varios a la vez. */
      cambiarDisponible: function (ids, disponible) {
        return envolver(cliente.from("productos").update({ disponible: disponible }).in("id", ids).select("id")).then(function (r) {
          if (r.ok && (r.datos || []).length !== ids.length) {
            return { ok: false, error: { message: "permission denied: solo se actualizaron " + (r.datos || []).length + " de " + ids.length } };
          }
          return r;
        });
      },
      cambiarPublicado: function (id, publicado) {
        return envolver(cliente.from("productos").update({ publicado: publicado }).eq("id", id).select("id")).then(function (r) {
          if (r.ok && !(r.datos || []).length) return { ok: false, error: { message: "permission denied: el producto no se actualizó" } };
          return r;
        });
      },
      eliminarProducto: function (id) {
        return envolver(cliente.from("productos").delete().eq("id", id).select("id")).then(function (r) {
          if (r.ok && !(r.datos || []).length) return { ok: false, error: { message: "permission denied: el producto no se eliminó" } };
          return r;
        });
      },
      /** Venta y descuento atómicos en el servidor. */
      registrarVenta: function (cuerpo) {
        return envolver(cliente.rpc("registrar_venta", { p_venta: cuerpo }));
      },
      /** Sube una imagen ya comprimida a la carpeta del usuario y devuelve la URL pública. */
      subirImagen: function (bucket, ruta, blob) {
        return envolver(cliente.storage.from(bucket).upload(ruta, blob, { upsert: false, cacheControl: "3600", contentType: "image/jpeg" }))
          .then(function (r) {
            if (!r.ok) return r;
            var pub = cliente.storage.from(bucket).getPublicUrl(ruta);
            var url = pub && pub.data ? pub.data.publicUrl : "";
            return url ? { ok: true, datos: url } : { ok: false, error: { message: "No se obtuvo la URL pública de la imagen" } };
          });
      },
      /** Borra una imagen subida que finalmente no se usó (fallo parcial). */
      borrarImagen: function (bucket, ruta) {
        return envolver(cliente.storage.from(bucket).remove([ruta]));
      }
    };
  }

  return { crear: crear, COLUMNAS_PRODUCTO: COLUMNAS_PRODUCTO };
});
