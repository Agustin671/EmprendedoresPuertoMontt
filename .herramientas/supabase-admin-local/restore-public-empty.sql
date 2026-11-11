-- Respaldo lógico de public. Restaurar solo en un esquema vacío y revisado.
-- Requiere auth.users y storage existentes; NO recrea Auth ni sustituye un respaldo del host.
begin;
set local search_path=public,pg_catalog;
set local standard_conforming_strings=on;
create table public."cuentas_autorizadas" ("email" text not null,
"rol" text not null,
"nombre" text,
"tienda_id" text,
"asociacion_id" text,
"creado_en" timestamp with time zone default now() not null);
create table public."perfiles" ("id" uuid not null,
"email" text,
"nombre" text,
"rol" text default 'VISITANTE'::text not null,
"tienda_id" text,
"creado_en" timestamp with time zone default now() not null);
create table public."ventas" ("id" text not null,
"tienda_id" text not null,
"fecha" timestamp with time zone default now() not null,
"fecha_local" text,
"items" jsonb default '[]'::jsonb not null,
"medio_pago" text,
"feria_id" text,
"total" numeric default 0 not null);
create table public."asociaciones" ("id" text not null,
"nombre" text not null,
"localidad" text,
"color" bigint);
create table public."tiendas" ("id" text not null,
"owner_id" uuid,
"nombre" text not null,
"rubro" text default 'Comercio'::text,
"sector" text default 'Alerce'::text,
"bio" text default ''::text,
"whatsapp" text default ''::text,
"instagram" text default ''::text,
"color_portada" bigint,
"color_logo" bigint,
"necesidades" jsonb default '{}'::jsonb not null,
"mostrar_precios" boolean default true not null,
"actualizado_en" timestamp with time zone default now() not null);
create table public."productos" ("id" text not null,
"tienda_id" text not null,
"nombre" text not null,
"descripcion" text default ''::text not null,
"categoria" text default 'TEXTIL'::text not null,
"precio" numeric default 0 not null,
"publicado" boolean default false not null,
"destacado" boolean default false not null,
"color_foto" bigint,
"variantes" jsonb default '[]'::jsonb not null,
"foto_url" text,
"actualizado_en" timestamp with time zone default now() not null);
create table public."ferias" ("id" text not null,
"nombre" text not null,
"ubicacion" text,
"fecha_inicio" date,
"fecha_fin" date,
"horario" text,
"cupos" integer default 20,
"precio_entrada" numeric default 0,
"estado" text default 'BORRADOR'::text,
"latitud" double precision,
"longitud" double precision);
create table public."participaciones" ("feria_id" text not null,
"tienda_id" text not null,
"invitacion" text default 'PENDIENTE'::text not null,
"asistencia" text default 'PENDIENTE'::text not null);
alter table public."cuentas_autorizadas" add constraint "cuentas_autorizadas_rol_check" CHECK ((rol = ANY (ARRAY['ADMIN'::text, 'EMPRENDEDORA'::text])));
alter table public."cuentas_autorizadas" add constraint "cuentas_autorizadas_pkey" PRIMARY KEY (email);
alter table public."perfiles" add constraint "perfiles_rol_check" CHECK ((rol = ANY (ARRAY['ADMIN'::text, 'EMPRENDEDORA'::text, 'VISITANTE'::text])));
alter table public."perfiles" add constraint "perfiles_pkey" PRIMARY KEY (id);
alter table public."asociaciones" add constraint "asociaciones_pkey" PRIMARY KEY (id);
alter table public."tiendas" add constraint "tiendas_pkey" PRIMARY KEY (id);
alter table public."productos" add constraint "productos_pkey" PRIMARY KEY (id);
alter table public."ventas" add constraint "ventas_pkey" PRIMARY KEY (id);
alter table public."ferias" add constraint "ferias_pkey" PRIMARY KEY (id);
alter table public."participaciones" add constraint "participaciones_pkey" PRIMARY KEY (feria_id, tienda_id);
insert into public."ferias" select * from jsonb_populate_recordset(null::public."ferias",'[]'::jsonb);
insert into public."ventas" select * from jsonb_populate_recordset(null::public."ventas",'[{"id":"v_test_1793751871719","fecha":"2026-11-04T00:24:31.797241+00:00","items":[{"id":"p_mini_1","nombre":"Pan Amasado Caliente","precio":2500,"cantidad":2}],"total":5000,"feria_id":null,"tienda_id":"t_minimarket_rosa","medio_pago":"Efectivo","fecha_local":"2026-11-04T00:24:31.719Z"}]'::jsonb);
insert into public."tiendas" select * from jsonb_populate_recordset(null::public."tiendas",'[{"id":"t_minimarket_rosa","bio":"Frutas y verduras frescas, pan amasado caliente todos los días a las 17:00 hrs, abarrotes, cecinas y bebidas.","rubro":"Abarrotes y Alimentos","nombre":"Minimarket Doña Marta","sector":"Alerce Histórico","owner_id":"b9ceb90c-4afb-4230-b09f-71d49c9ad97c","whatsapp":"+56991234567","instagram":"@minimarket.donamarta","color_logo":4292700960,"necesidades":{"tieneStock":true,"participaEnFerias":false,"tieneAgendamiento":false,"tieneForoComunidad":true,"tieneProductosAPedido":false,"tieneVentaPorWhatsApp":true},"color_portada":4286601941,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true},{"id":"t_barberia_alerce","bio":"Cortes de cabello clásicos y modernos, perfilado de barba y tratamientos capilares. Atención personalizada en local y a domicilio.","rubro":"Servicios y Peluquería","nombre":"Barbería & Estilo Alerce","sector":"Alerce Norte","owner_id":"8fe7410f-3d87-4b27-80f3-9f299067619b","whatsapp":"+56987654321","instagram":"@estilo_alerce","color_logo":4281440149,"necesidades":{"tieneStock":false,"participaEnFerias":false,"tieneAgendamiento":true,"tieneForoComunidad":true,"tieneProductosAPedido":false,"tieneVentaPorWhatsApp":true},"color_portada":4281153352,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true},{"id":"t_tejidos_austral","bio":"Piezas únicas tejidas a palillo y telar con lana 100% natural de oveja chilota, teñida con tintes de cortezas nativas.","rubro":"Artesanía y Textil","nombre":"Tejidos & Lanares Alerce Austral","sector":"Alerce Sur","owner_id":"23a6384f-f36e-47df-8f38-fad1f24627fd","whatsapp":"+56976543210","instagram":"@alerceaustral.tejidos","color_logo":4281236827,"necesidades":{"tieneStock":true,"participaEnFerias":true,"tieneAgendamiento":false,"tieneForoComunidad":true,"tieneProductosAPedido":true,"tieneVentaPorWhatsApp":true},"color_portada":4284232027,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true},{"id":"t_reposteria_surena","bio":"Kuchenes tradicionales de murta y frambuesa, tortas de hojarasca a pedido, alfajores caseros y galletas horneadas con amor.","rubro":"Pastelería y Gastronomía","nombre":"Repostería Casera Tradición Sureña","sector":"Senda Central","owner_id":"35aa6cc6-64bc-4ad8-be57-611201ea1adb","whatsapp":"+56965432109","instagram":"@tradicion_surena","color_logo":4291113008,"necesidades":{"tieneStock":true,"participaEnFerias":true,"tieneAgendamiento":false,"tieneForoComunidad":true,"tieneProductosAPedido":true,"tieneVentaPorWhatsApp":true},"color_portada":4292255278,"actualizado_en":"2026-11-02T13:38:53.543493+00:00","mostrar_precios":true}]'::jsonb);
insert into public."perfiles" select * from jsonb_populate_recordset(null::public."perfiles",'[{"id":"30bc9911-a2d7-4115-a87b-8141621b1a79","rol":"VISITANTE","email":"donarosa@alerce.app","nombre":"Minimarket Doña Rosa","creado_en":"2026-11-04T00:10:02.098034+00:00","tienda_id":null},{"id":"b9ceb90c-4afb-4230-b09f-71d49c9ad97c","rol":"EMPRENDEDORA","email":"donamarta@alerce.app","nombre":"Doña Marta","creado_en":"2026-11-04T00:24:10.354535+00:00","tienda_id":"t_minimarket_rosa"},{"id":"8fe7410f-3d87-4b27-80f3-9f299067619b","rol":"EMPRENDEDORA","email":"barberia@alerce.app","nombre":"Barbería Alerce","creado_en":"2026-11-04T00:24:40.98455+00:00","tienda_id":"t_barberia_alerce"},{"id":"23a6384f-f36e-47df-8f38-fad1f24627fd","rol":"EMPRENDEDORA","email":"tejidos@alerce.app","nombre":"Tejidos Austral","creado_en":"2026-11-04T00:24:40.997334+00:00","tienda_id":"t_tejidos_austral"},{"id":"35aa6cc6-64bc-4ad8-be57-611201ea1adb","rol":"EMPRENDEDORA","email":"reposteria@alerce.app","nombre":"Repostería Sureña","creado_en":"2026-11-04T00:24:41.009203+00:00","tienda_id":"t_reposteria_surena"},{"id":"86272071-f5f7-46be-a75e-49d4b3a64c80","rol":"ADMIN","email":"admin@alerce.app","nombre":"Administración Alerce","creado_en":"2026-11-04T00:24:41.011892+00:00","tienda_id":null},{"id":"5f0aeb32-6c95-4ba9-b8ec-149074cac33b","rol":"VISITANTE","email":"agustin@gmail.com","nombre":"Punto 3D","creado_en":"2026-11-04T01:33:37.310711+00:00","tienda_id":null},{"id":"30deddb9-5ed7-4e46-9fd1-fba0afc41f80","rol":"VISITANTE","email":"botanica123@gmail.com","nombre":"Botanica","creado_en":"2026-11-04T01:50:23.775336+00:00","tienda_id":null},{"id":"80b94be6-e404-4e86-8fd2-3cfc41d25406","rol":"VISITANTE","email":"67@gmail.com","nombre":"Seis Ciete","creado_en":"2026-11-04T13:43:43.712012+00:00","tienda_id":null},{"id":"e98a6bcf-2f2c-44e1-ad2b-c820e784c5f0","rol":"VISITANTE","email":"labarcafelipe21@gmail.com","nombre":"Felipe labarca","creado_en":"2026-11-04T14:32:15.748175+00:00","tienda_id":null},{"id":"13118cef-4d60-43d7-b950-6a2f0cd6d867","rol":"VISITANTE","email":"tonito@gmail.com","nombre":"tonito","creado_en":"2026-11-04T14:46:43.529429+00:00","tienda_id":null},{"id":"ee277e89-29ea-4cec-8a57-1e115228a073","rol":"VISITANTE","email":"paulferrada578@gmail.com","nombre":"FerradaTienda","creado_en":"2026-11-04T14:52:12.146046+00:00","tienda_id":null},{"id":"4c085ede-fc86-4576-9a9e-cf526974afb8","rol":"VISITANTE","email":"creepercool578@gmail.com","nombre":"maxito perez","creado_en":"2026-11-04T14:55:46.996264+00:00","tienda_id":null},{"id":"f7953754-6853-45e0-ad7c-656cf9c684f3","rol":"VISITANTE","email":"hugoamedinao27@gmail.com","nombre":"muro d3 berlin","creado_en":"2026-11-09T03:17:36.612606+00:00","tienda_id":null},{"id":"013b2809-3573-4f8c-8f51-9aa1193ed0fc","rol":"VISITANTE","email":"g@gmail.com","nombre":"si","creado_en":"2026-11-09T13:50:45.947871+00:00","tienda_id":null}]'::jsonb);
insert into public."productos" select * from jsonb_populate_recordset(null::public."productos",'[{"id":"p_rep_1","nombre":"Kuchen Tradicional de Murta y Frambuesa","precio":12000,"foto_url":null,"categoria":"GOURMET","destacado":true,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep1","stock":4,"nombre":"Molde Familiar (8 porciones)"}],"color_foto":4292255278,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_rep_2","nombre":"Torta de Hojarasca con Manjar Casero","precio":18500,"foto_url":null,"categoria":"GOURMET","destacado":true,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep2","stock":2,"nombre":"15 Personas"}],"color_foto":4291113008,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_rep_3","nombre":"Pack 6 Alfajores Artesanales con Nuez","precio":5500,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep3","stock":8,"nombre":"Caja 6 un."}],"color_foto":4287257147,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_rep_4","nombre":"Galletas Horneadas de Avena y Miel de Ulmo","precio":3800,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_reposteria_surena","variantes":[{"id":"v_rep4","stock":12,"nombre":"Bolsa 250g"}],"color_foto":4290804286,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_1","nombre":"Pan Amasado Caliente (Salida 17:00 hrs)","precio":2500,"foto_url":null,"categoria":"GOURMET","destacado":true,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min1","stock":20,"nombre":"Bolsa 5 unidades"}],"color_foto":4292700960,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_2","nombre":"Canasta de Verduras Frescas de Alerce","precio":7000,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min2","stock":5,"nombre":"Surtido Semanal"}],"color_foto":4281236827,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_3","nombre":"Huevos de Campo Libres (Docena)","precio":4200,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min3","stock":15,"nombre":"Docena"}],"color_foto":4292255278,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_mini_4","nombre":"Miel de Ulmo Pura de Bosque Nativo 1kg","precio":6500,"foto_url":null,"categoria":"GOURMET","destacado":false,"publicado":true,"tienda_id":"t_minimarket_rosa","variantes":[{"id":"v_min4","stock":6,"nombre":"Frasco 1kg"}],"color_foto":4290804286,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_tej_1","nombre":"Chaleco de Lana Chilota a Palillo","precio":38000,"foto_url":null,"categoria":"TEXTIL","destacado":true,"publicado":true,"tienda_id":"t_tejidos_austral","variantes":[{"id":"v_tej1","stock":2,"nombre":"Gris Natural · Talla M"},{"id":"v_tej2","stock":1,"nombre":"Blanco Crudo · Talla L"}],"color_foto":4284232027,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_tej_2","nombre":"Gorro y Cuello de Lana Pura de Oveja","precio":14000,"foto_url":null,"categoria":"TEXTIL","destacado":false,"publicado":true,"tienda_id":"t_tejidos_austral","variantes":[{"id":"v_tej3","stock":6,"nombre":"Talla Única"}],"color_foto":4281236827,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_tej_3","nombre":"Piecera Rústica en Telar Tradicional","precio":45000,"foto_url":null,"categoria":"DECORACION","destacado":true,"publicado":true,"tienda_id":"t_tejidos_austral","variantes":[{"id":"v_tej4","stock":2,"nombre":"2 Plazas"}],"color_foto":4290804286,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_bar_1","nombre":"Corte de Cabello Clásico / Degrade Moderno","precio":8000,"foto_url":null,"categoria":"COSMETICA","destacado":true,"publicado":true,"tienda_id":"t_barberia_alerce","variantes":[{"id":"v_bar1","stock":10,"nombre":"Sesión 40 min"}],"color_foto":4281153352,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_bar_2","nombre":"Perfilado y Ritual de Barba con Toalla Caliente","precio":6500,"foto_url":null,"categoria":"COSMETICA","destacado":false,"publicado":true,"tienda_id":"t_barberia_alerce","variantes":[{"id":"v_bar2","stock":8,"nombre":"Sesión 30 min"}],"color_foto":4281440149,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"},{"id":"p_bar_3","nombre":"Cera y Pomada Modeladora Capilar","precio":7500,"foto_url":null,"categoria":"COSMETICA","destacado":false,"publicado":true,"tienda_id":"t_barberia_alerce","variantes":[{"id":"v_bar3","stock":12,"nombre":"Pote 100g"}],"color_foto":4283061608,"descripcion":"","actualizado_en":"2026-11-02T13:38:53.543493+00:00"}]'::jsonb);
insert into public."asociaciones" select * from jsonb_populate_recordset(null::public."asociaciones",'[{"id":"as1","color":4287523732,"nombre":"Manos del Sur","localidad":"Puerto Montt"},{"id":"as2","color":4282285422,"nombre":"Red del Lago","localidad":"Puerto Montt"},{"id":"as3","color":4290804286,"nombre":"Taller Costero","localidad":"Puerto Montt"}]'::jsonb);
insert into public."participaciones" select * from jsonb_populate_recordset(null::public."participaciones",'[]'::jsonb);
insert into public."cuentas_autorizadas" select * from jsonb_populate_recordset(null::public."cuentas_autorizadas",'[{"rol":"ADMIN","email":"admin@alerce.app","nombre":"Administración Alerce","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":null,"asociacion_id":null},{"rol":"EMPRENDEDORA","email":"donamarta@alerce.app","nombre":"Doña Marta","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_minimarket_rosa","asociacion_id":null},{"rol":"EMPRENDEDORA","email":"barberia@alerce.app","nombre":"Barbería Alerce","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_barberia_alerce","asociacion_id":null},{"rol":"EMPRENDEDORA","email":"tejidos@alerce.app","nombre":"Tejidos Austral","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_tejidos_austral","asociacion_id":null},{"rol":"EMPRENDEDORA","email":"reposteria@alerce.app","nombre":"Repostería Sureña","creado_en":"2026-11-02T13:38:53.543493+00:00","tienda_id":"t_reposteria_surena","asociacion_id":null}]'::jsonb);
alter table public."perfiles" add constraint "perfiles_id_fkey" FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE;
alter table public."tiendas" add constraint "tiendas_owner_id_fkey" FOREIGN KEY (owner_id) REFERENCES auth.users(id) ON DELETE SET NULL;
alter table public."productos" add constraint "productos_tienda_id_fkey" FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE;
alter table public."ventas" add constraint "ventas_tienda_id_fkey" FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE;
alter table public."participaciones" add constraint "participaciones_feria_id_fkey" FOREIGN KEY (feria_id) REFERENCES ferias(id) ON DELETE CASCADE;
alter table public."participaciones" add constraint "participaciones_tienda_id_fkey" FOREIGN KEY (tienda_id) REFERENCES tiendas(id) ON DELETE CASCADE;
CREATE OR REPLACE FUNCTION public.al_crear_usuario()
 RETURNS trigger
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
declare
  v_rol    text;
  v_nombre text;
  v_tienda text;
begin
  select rol, nombre, tienda_id into v_rol, v_nombre, v_tienda
  from public.cuentas_autorizadas where lower(email) = lower(new.email);

  v_rol := coalesce(v_rol, 'VISITANTE');
  v_nombre := coalesce(nullif(new.raw_user_meta_data ->> 'nombre', ''), v_nombre, split_part(new.email, '@', 1));

  if v_rol = 'EMPRENDEDORA' then
    if v_tienda is null then
      v_tienda := 'tienda_' || replace(left(new.id::text, 13), '-', '');
      insert into public.tiendas (id, owner_id, nombre, rubro, sector, bio)
      values (v_tienda, new.id, v_nombre, 'General y Artesanías', 'Alerce',
              'Emprendimiento local de Alerce. Consulta por WhatsApp.')
      on conflict (id) do nothing;
    else
      update public.tiendas set owner_id = new.id where id = v_tienda and owner_id is null;
    end if;
  else
    v_tienda := null;
  end if;

  insert into public.perfiles (id, email, nombre, rol, tienda_id)
  values (new.id, new.email, v_nombre, v_rol, v_tienda)
  on conflict (id) do nothing;
  return new;
end;
$function$
;
CREATE OR REPLACE FUNCTION public.es_admin()
 RETURNS boolean
 LANGUAGE sql
 STABLE SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
  select exists (select 1 from public.perfiles where id = auth.uid() and rol = 'ADMIN');
$function$
;
CREATE OR REPLACE FUNCTION public.es_duena_de(p_tienda text)
 RETURNS boolean
 LANGUAGE sql
 STABLE SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
  select exists (select 1 from public.tiendas where id = p_tienda and owner_id = auth.uid());
$function$
;
CREATE INDEX ventas_tienda_fecha_idx ON public.ventas USING btree (tienda_id, fecha DESC);
CREATE INDEX productos_tienda_idx ON public.productos USING btree (tienda_id);
drop trigger if exists "al_crear_usuario" on "auth"."users";
CREATE TRIGGER al_crear_usuario AFTER INSERT ON auth.users FOR EACH ROW EXECUTE FUNCTION al_crear_usuario();
alter table public."cuentas_autorizadas" enable row level security;
alter table public."perfiles" enable row level security;
alter table public."ventas" enable row level security;
alter table public."asociaciones" enable row level security;
alter table public."tiendas" enable row level security;
alter table public."productos" enable row level security;
alter table public."ferias" enable row level security;
alter table public."participaciones" enable row level security;
drop policy if exists "cuentas_admin" on "public"."cuentas_autorizadas";
create policy "cuentas_admin" on "public"."cuentas_autorizadas" as PERMISSIVE for ALL to "authenticated" using (es_admin()) with check (es_admin());
drop policy if exists "perfiles_admin" on "public"."perfiles";
create policy "perfiles_admin" on "public"."perfiles" as PERMISSIVE for UPDATE to "authenticated" using (es_admin()) with check (es_admin());
drop policy if exists "perfiles_ver" on "public"."perfiles";
create policy "perfiles_ver" on "public"."perfiles" as PERMISSIVE for SELECT to "authenticated" using (((id = auth.uid()) OR es_admin()));
drop policy if exists "ventas_duena" on "public"."ventas";
create policy "ventas_duena" on "public"."ventas" as PERMISSIVE for ALL to "authenticated" using (es_duena_de(tienda_id)) with check (es_duena_de(tienda_id));
drop policy if exists "asociaciones_admin" on "public"."asociaciones";
create policy "asociaciones_admin" on "public"."asociaciones" as PERMISSIVE for ALL to "authenticated" using (es_admin()) with check (es_admin());
drop policy if exists "asociaciones_ver" on "public"."asociaciones";
create policy "asociaciones_ver" on "public"."asociaciones" as PERMISSIVE for SELECT to public using (true);
drop policy if exists "tiendas_admin_borrar" on "public"."tiendas";
create policy "tiendas_admin_borrar" on "public"."tiendas" as PERMISSIVE for DELETE to "authenticated" using (es_admin());
drop policy if exists "tiendas_admin_crear" on "public"."tiendas";
create policy "tiendas_admin_crear" on "public"."tiendas" as PERMISSIVE for INSERT to "authenticated" with check (es_admin());
drop policy if exists "tiendas_editar" on "public"."tiendas";
create policy "tiendas_editar" on "public"."tiendas" as PERMISSIVE for UPDATE to "authenticated" using (((owner_id = auth.uid()) OR es_admin())) with check (((owner_id = auth.uid()) OR es_admin()));
drop policy if exists "tiendas_ver" on "public"."tiendas";
create policy "tiendas_ver" on "public"."tiendas" as PERMISSIVE for SELECT to public using (true);
drop policy if exists "productos_borrar" on "public"."productos";
create policy "productos_borrar" on "public"."productos" as PERMISSIVE for DELETE to "authenticated" using (es_duena_de(tienda_id));
drop policy if exists "productos_editar" on "public"."productos";
create policy "productos_editar" on "public"."productos" as PERMISSIVE for UPDATE to "authenticated" using (es_duena_de(tienda_id)) with check (es_duena_de(tienda_id));
drop policy if exists "productos_crear" on "public"."productos";
create policy "productos_crear" on "public"."productos" as PERMISSIVE for INSERT to "authenticated" with check (es_duena_de(tienda_id));
drop policy if exists "productos_ver" on "public"."productos";
create policy "productos_ver" on "public"."productos" as PERMISSIVE for SELECT to public using ((publicado OR es_duena_de(tienda_id)));
drop policy if exists "ferias_admin" on "public"."ferias";
create policy "ferias_admin" on "public"."ferias" as PERMISSIVE for ALL to "authenticated" using (es_admin()) with check (es_admin());
drop policy if exists "ferias_ver" on "public"."ferias";
create policy "ferias_ver" on "public"."ferias" as PERMISSIVE for SELECT to public using (true);
drop policy if exists "participaciones_responder" on "public"."participaciones";
create policy "participaciones_responder" on "public"."participaciones" as PERMISSIVE for UPDATE to "authenticated" using (es_duena_de(tienda_id)) with check (es_duena_de(tienda_id));
drop policy if exists "participaciones_admin" on "public"."participaciones";
create policy "participaciones_admin" on "public"."participaciones" as PERMISSIVE for ALL to "authenticated" using (es_admin()) with check (es_admin());
drop policy if exists "participaciones_ver" on "public"."participaciones";
create policy "participaciones_ver" on "public"."participaciones" as PERMISSIVE for SELECT to public using (true);
drop policy if exists "fotos_borrar" on "storage"."objects";
create policy "fotos_borrar" on "storage"."objects" as PERMISSIVE for DELETE to "authenticated" using (((bucket_id = 'productos'::text) AND ((storage.foldername(name))[1] = (auth.uid())::text)));
drop policy if exists "fotos_reemplazar" on "storage"."objects";
create policy "fotos_reemplazar" on "storage"."objects" as PERMISSIVE for UPDATE to "authenticated" using (((bucket_id = 'productos'::text) AND ((storage.foldername(name))[1] = (auth.uid())::text)));
drop policy if exists "fotos_subir" on "storage"."objects";
create policy "fotos_subir" on "storage"."objects" as PERMISSIVE for INSERT to "authenticated" with check (((bucket_id = 'productos'::text) AND ((storage.foldername(name))[1] = (auth.uid())::text)));
drop policy if exists "fotos_ver" on "storage"."objects";
create policy "fotos_ver" on "storage"."objects" as PERMISSIVE for SELECT to public using ((bucket_id = 'productos'::text));
grant INSERT on public."cuentas_autorizadas" to "anon";
grant SELECT on public."cuentas_autorizadas" to "anon";
grant UPDATE on public."cuentas_autorizadas" to "anon";
grant DELETE on public."cuentas_autorizadas" to "anon";
grant TRUNCATE on public."cuentas_autorizadas" to "anon";
grant REFERENCES on public."cuentas_autorizadas" to "anon";
grant TRIGGER on public."cuentas_autorizadas" to "anon";
grant INSERT on public."cuentas_autorizadas" to "authenticated";
grant SELECT on public."cuentas_autorizadas" to "authenticated";
grant UPDATE on public."cuentas_autorizadas" to "authenticated";
grant DELETE on public."cuentas_autorizadas" to "authenticated";
grant TRUNCATE on public."cuentas_autorizadas" to "authenticated";
grant REFERENCES on public."cuentas_autorizadas" to "authenticated";
grant TRIGGER on public."cuentas_autorizadas" to "authenticated";
grant INSERT on public."cuentas_autorizadas" to "service_role";
grant SELECT on public."cuentas_autorizadas" to "service_role";
grant UPDATE on public."cuentas_autorizadas" to "service_role";
grant DELETE on public."cuentas_autorizadas" to "service_role";
grant TRUNCATE on public."cuentas_autorizadas" to "service_role";
grant REFERENCES on public."cuentas_autorizadas" to "service_role";
grant TRIGGER on public."cuentas_autorizadas" to "service_role";
grant INSERT on public."perfiles" to "anon";
grant SELECT on public."perfiles" to "anon";
grant UPDATE on public."perfiles" to "anon";
grant DELETE on public."perfiles" to "anon";
grant TRUNCATE on public."perfiles" to "anon";
grant REFERENCES on public."perfiles" to "anon";
grant TRIGGER on public."perfiles" to "anon";
grant INSERT on public."perfiles" to "authenticated";
grant SELECT on public."perfiles" to "authenticated";
grant UPDATE on public."perfiles" to "authenticated";
grant DELETE on public."perfiles" to "authenticated";
grant TRUNCATE on public."perfiles" to "authenticated";
grant REFERENCES on public."perfiles" to "authenticated";
grant TRIGGER on public."perfiles" to "authenticated";
grant INSERT on public."perfiles" to "service_role";
grant SELECT on public."perfiles" to "service_role";
grant UPDATE on public."perfiles" to "service_role";
grant DELETE on public."perfiles" to "service_role";
grant TRUNCATE on public."perfiles" to "service_role";
grant REFERENCES on public."perfiles" to "service_role";
grant TRIGGER on public."perfiles" to "service_role";
grant INSERT on public."ventas" to "anon";
grant SELECT on public."ventas" to "anon";
grant UPDATE on public."ventas" to "anon";
grant DELETE on public."ventas" to "anon";
grant TRUNCATE on public."ventas" to "anon";
grant REFERENCES on public."ventas" to "anon";
grant TRIGGER on public."ventas" to "anon";
grant INSERT on public."ventas" to "authenticated";
grant SELECT on public."ventas" to "authenticated";
grant UPDATE on public."ventas" to "authenticated";
grant DELETE on public."ventas" to "authenticated";
grant TRUNCATE on public."ventas" to "authenticated";
grant REFERENCES on public."ventas" to "authenticated";
grant TRIGGER on public."ventas" to "authenticated";
grant INSERT on public."ventas" to "service_role";
grant SELECT on public."ventas" to "service_role";
grant UPDATE on public."ventas" to "service_role";
grant DELETE on public."ventas" to "service_role";
grant TRUNCATE on public."ventas" to "service_role";
grant REFERENCES on public."ventas" to "service_role";
grant TRIGGER on public."ventas" to "service_role";
grant INSERT on public."asociaciones" to "anon";
grant SELECT on public."asociaciones" to "anon";
grant UPDATE on public."asociaciones" to "anon";
grant DELETE on public."asociaciones" to "anon";
grant TRUNCATE on public."asociaciones" to "anon";
grant REFERENCES on public."asociaciones" to "anon";
grant TRIGGER on public."asociaciones" to "anon";
grant INSERT on public."asociaciones" to "authenticated";
grant SELECT on public."asociaciones" to "authenticated";
grant UPDATE on public."asociaciones" to "authenticated";
grant DELETE on public."asociaciones" to "authenticated";
grant TRUNCATE on public."asociaciones" to "authenticated";
grant REFERENCES on public."asociaciones" to "authenticated";
grant TRIGGER on public."asociaciones" to "authenticated";
grant INSERT on public."asociaciones" to "service_role";
grant SELECT on public."asociaciones" to "service_role";
grant UPDATE on public."asociaciones" to "service_role";
grant DELETE on public."asociaciones" to "service_role";
grant TRUNCATE on public."asociaciones" to "service_role";
grant REFERENCES on public."asociaciones" to "service_role";
grant TRIGGER on public."asociaciones" to "service_role";
grant INSERT on public."tiendas" to "anon";
grant SELECT on public."tiendas" to "anon";
grant UPDATE on public."tiendas" to "anon";
grant DELETE on public."tiendas" to "anon";
grant TRUNCATE on public."tiendas" to "anon";
grant REFERENCES on public."tiendas" to "anon";
grant TRIGGER on public."tiendas" to "anon";
grant INSERT on public."tiendas" to "authenticated";
grant SELECT on public."tiendas" to "authenticated";
grant UPDATE on public."tiendas" to "authenticated";
grant DELETE on public."tiendas" to "authenticated";
grant TRUNCATE on public."tiendas" to "authenticated";
grant REFERENCES on public."tiendas" to "authenticated";
grant TRIGGER on public."tiendas" to "authenticated";
grant INSERT on public."tiendas" to "service_role";
grant SELECT on public."tiendas" to "service_role";
grant UPDATE on public."tiendas" to "service_role";
grant DELETE on public."tiendas" to "service_role";
grant TRUNCATE on public."tiendas" to "service_role";
grant REFERENCES on public."tiendas" to "service_role";
grant TRIGGER on public."tiendas" to "service_role";
grant INSERT on public."productos" to "anon";
grant SELECT on public."productos" to "anon";
grant UPDATE on public."productos" to "anon";
grant DELETE on public."productos" to "anon";
grant TRUNCATE on public."productos" to "anon";
grant REFERENCES on public."productos" to "anon";
grant TRIGGER on public."productos" to "anon";
grant INSERT on public."productos" to "authenticated";
grant SELECT on public."productos" to "authenticated";
grant UPDATE on public."productos" to "authenticated";
grant DELETE on public."productos" to "authenticated";
grant TRUNCATE on public."productos" to "authenticated";
grant REFERENCES on public."productos" to "authenticated";
grant TRIGGER on public."productos" to "authenticated";
grant INSERT on public."productos" to "service_role";
grant SELECT on public."productos" to "service_role";
grant UPDATE on public."productos" to "service_role";
grant DELETE on public."productos" to "service_role";
grant TRUNCATE on public."productos" to "service_role";
grant REFERENCES on public."productos" to "service_role";
grant TRIGGER on public."productos" to "service_role";
grant INSERT on public."ferias" to "anon";
grant SELECT on public."ferias" to "anon";
grant UPDATE on public."ferias" to "anon";
grant DELETE on public."ferias" to "anon";
grant TRUNCATE on public."ferias" to "anon";
grant REFERENCES on public."ferias" to "anon";
grant TRIGGER on public."ferias" to "anon";
grant INSERT on public."ferias" to "authenticated";
grant SELECT on public."ferias" to "authenticated";
grant UPDATE on public."ferias" to "authenticated";
grant DELETE on public."ferias" to "authenticated";
grant TRUNCATE on public."ferias" to "authenticated";
grant REFERENCES on public."ferias" to "authenticated";
grant TRIGGER on public."ferias" to "authenticated";
grant INSERT on public."ferias" to "service_role";
grant SELECT on public."ferias" to "service_role";
grant UPDATE on public."ferias" to "service_role";
grant DELETE on public."ferias" to "service_role";
grant TRUNCATE on public."ferias" to "service_role";
grant REFERENCES on public."ferias" to "service_role";
grant TRIGGER on public."ferias" to "service_role";
grant INSERT on public."participaciones" to "anon";
grant SELECT on public."participaciones" to "anon";
grant UPDATE on public."participaciones" to "anon";
grant DELETE on public."participaciones" to "anon";
grant TRUNCATE on public."participaciones" to "anon";
grant REFERENCES on public."participaciones" to "anon";
grant TRIGGER on public."participaciones" to "anon";
grant INSERT on public."participaciones" to "authenticated";
grant SELECT on public."participaciones" to "authenticated";
grant UPDATE on public."participaciones" to "authenticated";
grant DELETE on public."participaciones" to "authenticated";
grant TRUNCATE on public."participaciones" to "authenticated";
grant REFERENCES on public."participaciones" to "authenticated";
grant TRIGGER on public."participaciones" to "authenticated";
grant INSERT on public."participaciones" to "service_role";
grant SELECT on public."participaciones" to "service_role";
grant UPDATE on public."participaciones" to "service_role";
grant DELETE on public."participaciones" to "service_role";
grant TRUNCATE on public."participaciones" to "service_role";
grant REFERENCES on public."participaciones" to "service_role";
grant TRIGGER on public."participaciones" to "service_role";
commit;