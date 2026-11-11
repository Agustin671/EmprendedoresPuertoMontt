select jsonb_build_object(
 'version', current_setting('server_version'),
 'tables', (select jsonb_agg(jsonb_build_object('name',c.relname,'rls',c.relrowsecurity)) from pg_class c join pg_namespace n on n.oid=c.relnamespace where n.nspname='public' and c.relkind='r'),
 'columns', (select jsonb_agg(jsonb_build_object('table',table_name,'column',column_name,'type',data_type)) from information_schema.columns where table_schema='public'),
 'functions', (select jsonb_agg(jsonb_build_object('name',p.proname,'args',pg_get_function_identity_arguments(p.oid))) from pg_proc p join pg_namespace n on n.oid=p.pronamespace where n.nspname='public' and p.prokind='f'),
 'buckets', (select jsonb_agg(jsonb_build_object('id',id,'public',public)) from storage.buckets),
 'counts', jsonb_build_object('users',(select count(*) from auth.users),'profiles',(select count(*) from public.perfiles),'stores',(select count(*) from public.tiendas),'products',(select count(*) from public.productos),'sales',(select count(*) from public.ventas),'fairs',(select count(*) from public.ferias)),
 'storage_files', (select count(*) from storage.objects),
 'policies', (select jsonb_agg(to_jsonb(p)) from pg_policies p where schemaname in ('public','storage')),
 'sales_grants', (select jsonb_agg(to_jsonb(g)) from information_schema.role_table_grants g where table_schema='public' and table_name='ventas' and grantee in ('anon','authenticated'))
) as diagnosis;
