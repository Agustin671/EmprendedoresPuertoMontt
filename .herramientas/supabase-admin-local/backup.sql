begin transaction isolation level repeatable read read only;
set local search_path=public,pg_catalog;
select jsonb_build_object(
 'captured_at', now(),
 'data', jsonb_build_object(
  'cuentas_autorizadas',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.cuentas_autorizadas t),
  'perfiles',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.perfiles t),
  'tiendas',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.tiendas t),
  'productos',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.productos t),
  'ventas',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.ventas t),
  'asociaciones',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.asociaciones t),
  'ferias',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.ferias t),
  'participaciones',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from public.participaciones t)),
 'auth_users_for_rehearsal',(select jsonb_agg(jsonb_build_object('id',id,'email',email,'raw_user_meta_data',raw_user_meta_data)) from auth.users),
 'buckets',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from storage.buckets t),
 'objects',(select coalesce(jsonb_agg(to_jsonb(t)),'[]') from storage.objects t),
 'columns',(select jsonb_agg(jsonb_build_object('table',c.relname,'name',a.attname,'type',format_type(a.atttypid,a.atttypmod),'notnull',a.attnotnull,'default',pg_get_expr(d.adbin,d.adrelid),'position',a.attnum)) from pg_class c join pg_namespace n on n.oid=c.relnamespace join pg_attribute a on a.attrelid=c.oid and a.attnum>0 and not a.attisdropped left join pg_attrdef d on d.adrelid=c.oid and d.adnum=a.attnum where n.nspname='public' and c.relkind='r'),
 'constraints',(select jsonb_agg(jsonb_build_object('table',c.relname,'name',co.conname,'type',co.contype,'definition',pg_get_constraintdef(co.oid))) from pg_constraint co join pg_class c on c.oid=co.conrelid join pg_namespace n on n.oid=c.relnamespace where n.nspname='public'),
 'indexes',(select jsonb_agg(to_jsonb(i)) from pg_indexes i where schemaname='public'),
 'functions',(select jsonb_agg(jsonb_build_object('name',p.proname,'args',pg_get_function_identity_arguments(p.oid),'definition',pg_get_functiondef(p.oid),'owner',pg_get_userbyid(p.proowner),'acl',p.proacl::text)) from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='public' and p.prokind='f'),
 'triggers',(select jsonb_agg(jsonb_build_object('schema',n.nspname,'table',c.relname,'name',t.tgname,'definition',pg_get_triggerdef(t.oid),'enabled',t.tgenabled)) from pg_trigger t join pg_class c on c.oid=t.tgrelid join pg_namespace n on n.oid=c.relnamespace where not t.tgisinternal and (n.nspname='public' or (n.nspname='auth' and c.relname='users'))),
 'policies',(select jsonb_agg(to_jsonb(p)) from pg_policies p where schemaname in ('public','storage')),
 'grants',(select jsonb_agg(to_jsonb(g)) from information_schema.role_table_grants g where table_schema='public'),
 'tables',(select jsonb_agg(jsonb_build_object('name',c.relname,'rls',c.relrowsecurity,'force_rls',c.relforcerowsecurity,'owner',pg_get_userbyid(c.relowner))) from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relkind='r'),
 'sequences',(select coalesce(jsonb_agg(to_jsonb(s)),'[]') from pg_sequences s where schemaname='public'),
 'repair04_count',(select count(*) from auth.users u join public.perfiles p on p.id=u.id where p.rol='VISITANTE' and upper(coalesce(u.raw_user_meta_data->>'tipo_cuenta',''))='EMPRENDEDORA')
) as snapshot;
commit;
