# Contrato compartido del catálogo (SQL · web · Android)

Las tres capas aplican las mismas reglas:

| Capa | Archivo |
|---|---|
| Servidor | `supabase/05_catalogo_ventas.sql` (`estado_publico_producto`, RLS, `registrar_venta`) |
| Web | `LA PAgina (segun)/proyectode10/js/catalogo.js` |
| Android | `data/ReglasCatalogo.kt` |

La tabla de verdad única está en `tests/contrato/casos_estado.json`, y la prueban las tres capas: `tests/sql`, `tests/web` y `Entrega5Test.kt`.

## Producto

| Campo (SQL / JSON de guardar) | Significado | Por defecto |
|---|---|---|
| `categoria` | TEXTIL, CUERO, JOYERIA, COSMETICA, GOURMET, DECORACION (los datos antiguos pueden traer otro texto) | TEXTIL |
| `tipo` | id de `tipos_producto` (opcional) | null |
| `atributos` | campos específicos del tipo `{clave: valor}` | `{}` |
| `foto_url`, `fotos` | foto principal y fotos adicionales (solo URLs https de Storage) | null, `[]` |
| `variantes` | opciones `[{id, nombre, stock, precio, foto_url}]`; `precio` null usa el del producto | — |
| `modalidad` | REGULAR · A_PEDIDO · PIEZA_UNICA | REGULAR |
| `maneja_stock` | lleva o no la cuenta de unidades | true |
| `al_agotarse` | MOSTRAR_AGOTADO · OCULTAR · PASAR_A_PEDIDO | MOSTRAR_AGOTADO |
| `publicado` | «Lo ven todos» o «Solo yo lo veo» | false |
| `disponible` | no disponible: sigue en su tienda pero no se muestra al público | true |
| `vendida_en` | fecha de venta de una pieza única (permanente) | null |

`tiendas.valores_producto` guarda los valores iniciales de un producto nuevo. `tiendas.historial_activo` es el interruptor del historial.

## Estado público (en este orden)

1. No publicado, no disponible o con `vendida_en` → **OCULTO**.
2. A_PEDIDO → **A_PEDIDO**.
3. PIEZA_UNICA: con su unidad → DISPONIBLE; sin ella → **OCULTO**.
4. Sin conteo de unidades → **DISPONIBLE**.
5. Con unidades en alguna opción → **DISPONIBLE**.
6. Sin unidades en ninguna opción, según `al_agotarse`: OCULTAR → OCULTO; PASAR_A_PEDIDO → A_PEDIDO; MOSTRAR_AGOTADO → **AGOTADO**.

## Decisiones aplicadas (Entrega 5)

- «Al agotarse» se aplica al producto cuando no quedan unidades vendibles entre sus opciones.
- Una opción agotada no se vende como entrega inmediata. Puede anotarse como **encargo** si el producto es a pedido o pasa a pedido. El encargo se distingue en la venta (`encargo: true`) y no descuenta unidades.
- Una pieza única es una unidad total, aunque tenga opciones. Al venderse, todas sus opciones quedan en 0 y `vendida_en` no se borra nunca.
- Historial desactivado: las ventas futuras guardan solo el registro técnico mínimo (producto, opción, cantidad y encargo; sin precio, total, medio de pago ni lugar). Las ventas anteriores no se borran.
- Los tipos y campos específicos los define la administración en `tipos_producto`. No están en el levantamiento: ver [tipos-producto.md](tipos-producto.md).

## Ventas: por qué una función y no triggers

El diagnóstico proponía descontar con un trigger `AFTER INSERT ON ventas` y recortar a 0 si no alcanzaba. Se descartó por tres razones:
- aceptaba la sobreventa en silencio;
- seguía confiando en el `tienda_id` del cliente;
- no permitía responder qué pasó.

Se eligió `registrar_venta(p_venta jsonb)`, `SECURITY DEFINER`:
- toma la tienda de la sesión y valida producto, opción y cantidad;
- bloquea las filas en orden de id (dos ventas cruzadas no se interbloquean);
- rechaza `STOCK_INSUFICIENTE` sin guardar nada;
- descuenta e inserta en la misma transacción;
- es idempotente por `id` con huella del contenido: mismo contenido → `YA_REGISTRADA`; otro contenido → `ID_CONFLICTO`.

Se retiró la escritura directa en `ventas`, para que ningún camino antiguo la eluda.

El trigger que sí se mantiene es `proteger_producto`. Solo **impide** que una actualización directa cambie cantidades, borre campos de opciones o reviva una pieza vendida. Los cambios legítimos de cantidad pasan por `guardar_producto()` con `stockBase`: el servidor aplica la diferencia sobre lo que queda (reponer 5 mientras se venden 3 deja el valor correcto) y responde `CONFLICTO_STOCK` si quedaría bajo cero.

Todo esto está probado en PostgreSQL 15 y 17 (`npm run test:sql`), con conexiones concurrentes reales.

## Ventas sin conexión (Android)

1. La venta se anota en el celular como **PENDIENTE**: descuenta la cantidad y también la base local, para que una edición posterior no vuelva a descontar.
2. Al subir:
   - si el servidor la acepta, queda **CONFIRMADA** y se usan sus cantidades (sin perder ediciones por subir);
   - si la rechaza por una regla, queda **CONFLICTO**: se conserva, se devuelven las unidades en el celular y la emprendedora puede **Reintentar** o **Anular** desde el historial.
3. Un error de red la deja pendiente y se reintenta sola (al volver la conexión, con espera creciente).

## Cuentas y caché (Android)

Estado local y cola por cuenta (`estado_<uid>`, `supabase_pendientes_<uid>`):
- otra cuenta no ve ni sube lo de la anterior;
- cerrar sesión avisa si hay pendientes y no los descarta;
- un cambio hecho con la sesión vencida queda en la cola de esa cuenta;
- un borrado pendiente no reaparece al descargar.
