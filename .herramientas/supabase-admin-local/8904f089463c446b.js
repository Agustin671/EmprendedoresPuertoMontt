(globalThis.TURBOPACK||(globalThis.TURBOPACK=[])).push(["object"==typeof document?document.currentScript:void 0,479084,e=>{"use strict";let t=new Set(["AES128","AES256","ALL","ALLOWOVERWRITE","ANALYSE","ANALYZE","AND","ANY","ARRAY","AS","ASC","ASYMMETRIC","AUTHORIZATION","BACKUP","BETWEEN","BIGINT","BINARY","BIT","BLANKSASNULL","BOOLEAN","BOTH","BYTEDICT","CASE","CAST","CHAR","CHARACTER","CHECK","COALESCE","COLLATE","COLLATION","COLUMN","CONCURRENTLY","CONSTRAINT","CREATE","CREDENTIALS","CROSS","CURRENT_CATALOG","CURRENT_DATE","CURRENT_ROLE","CURRENT_SCHEMA","CURRENT_TIME","CURRENT_TIMESTAMP","CURRENT_USER_ID","CURRENT_USER","DEC","DECIMAL","DEFAULT","DEFERRABLE","DEFLATE","DEFRAG","DELETE","DELTA","DELTA32K","DESC","DISABLE","DISTINCT","DO","ELSE","EMPTYASNULL","ENABLE","ENCODE","ENCRYPT","ENCRYPTION","END","EXCEPT","EXISTS","EXPLICIT","EXTRACT","FALSE","FETCH","FLOAT","FOR","FOREIGN","FREEZE","FROM","FULL","GLOBALDICT256","GLOBALDICT64K","GRANT","GREATEST","GROUP","GROUPING","GZIP","HAVING","IDENTITY","IGNORE","ILIKE","IN","INITIALLY","INNER","INOUT","INSERT","INT","INTEGER","INTERSECT","INTERVAL","INTO","IS","ISNULL","JOIN","JSON_ARRAY","JSON_ARRAYAGG","JSON_EXISTS","JSON_OBJECT","JSON_OBJECTAGG","JSON_QUERY","JSON_SCALAR","JSON_SERIALIZE","JSON_TABLE","JSON_VALUE","JSON","LATERAL","LEADING","LEAST","LEFT","LIKE","LIMIT","LOCALTIME","LOCALTIMESTAMP","LUN","LUNS","LZO","LZOP","MERGE_ACTION","MINUS","MOSTLY13","MOSTLY32","MOSTLY8","NATIONAL","NATURAL","NCHAR","NEW","NONE","NORMALIZE","NOT","NOTNULL","NULL","NULLIF","NULLS","NUMERIC","OFF","OFFLINE","OFFSET","OLD","ON","ONLY","OPEN","OR","ORDER","OUT","OUTER","OVERLAPS","OVERLAY","PARALLEL","PARTITION","PERCENT","PLACING","POSITION","PRECISION","PRIMARY","RAW","READRATIO","REAL","RECOVER","REFERENCES","REJECTLOG","RESORT","RESTORE","RETURNING","RIGHT","ROW","SELECT","SESSION_USER","SETOF","SIMILAR","SMALLINT","SOME","SUBSTRING","SYMMETRIC","SYSDATE","SYSTEM_USER","SYSTEM","TABLE","TABLESAMPLE","TAG","TDES","TEXT255","TEXT32K","THEN","TIME","TIMESTAMP","TO","TOP","TRAILING","TREAT","TRIM","TRUE","TRUNCATECOLUMNS","UNION","UNIQUE","UPDATE","USER","USING","VALUES","VARCHAR","VARIADIC","VERBOSE","WALLET","WHEN","WHERE","WINDOW","WITH","WITHOUT","XMLATTRIBUTES","XMLCONCAT","XMLELEMENT","XMLEXISTS","XMLFOREST","XMLNAMESPACES","XMLPARSE","XMLPI","XMLROOT","XMLSERIALIZE","XMLTABLE"]);function n(e){return e.replace("T"," ").replace("Z","+00")}function a(e,t,n){let a="";for(let[i,r]of(a+=e?" (":"(",t.entries()))a+=(0===i?"":", ")+n(r);return a+")"}function i(e){if(null==e)throw Error("SQL identifier cannot be null or undefined");if(!1===e)return'"f"';if(!0===e)return'"t"';if(e instanceof Date)return`"${n(e.toISOString())}"`;if(Array.isArray(e)){let t=[];for(let n of e)if(!0===Array.isArray(n))throw TypeError("Nested array to grouped list conversion is not supported for SQL identifier");else t.push(i(n));return t.toString()}else if(e===Object(e))throw Error("SQL identifier cannot be an object");let a=String(e).slice(0);if(!0===/^[_a-z][\d$_a-z]*$/.test(a)&&!1==!!t.has(a.toUpperCase()))return a;let r='"';for(let e of a)r+='"'===e?e+e:e;return r+'"'}function r(e){let t,i="";if(null==e)return"NULL";if("bigint"==typeof e)return BigInt(e).toString();if(e===1/0)return"'Infinity'";if(e===-1/0)return"'-Infinity'";if(Number.isNaN(e))return"'NaN'";if("number"==typeof e)return Number(e).toString();if(!1===e)return"'f'";if(!0===e)return"'t'";if(e instanceof Date)return`'${n(e.toISOString())}'`;if(Array.isArray(e)){let t=[];for(let[n,i]of e.entries())!0===Array.isArray(i)?t.push(a(0!==n,i,r)):t.push(r(i));return t.toString()}e===Object(e)?(t="jsonb",i=JSON.stringify(e)):i=String(e).slice(0);let o=!1,l="'";for(let e of i)"'"===e?l+=e+e:"\\"===e?(l+=e+e,o=!0):l+=e;return l+="'",!0===o&&(l=`E${l}`),t&&(l+=`::${t}`),l}function o(e,...t){let l,s;return l=0,s=RegExp("%(%|(\\d+\\$)?[ILs])","g"),e.replace(s,(e,o)=>{if("%"===o)return"%";let s=l,c=o.split("$");if(c.length>1&&(s=Number.parseInt(c[0],10)-1,o=c[1]),s<0)throw Error("specified argument 0 but arguments start at 1");if(s>t.length-1)throw Error("too few arguments");return(l=s+1,"I"===o)?i(t[s]):"L"===o?r(t[s]):"s"===o?function e(t){if(null==t)return"";if(!1===t)return"f";if(!0===t)return"t";if(t instanceof Date)return n(t.toISOString());if(Array.isArray(t)){let n=[];for(let[i,r]of t.entries())null!=r&&(!0===Array.isArray(r)?n.push(a(0!==i,r,e)):n.push(e(r)));return n.toString()}return t===Object(t)?JSON.stringify(t):String(t).toString().slice(0)}(t[s]):void 0})}e.s(["format",()=>o,"ident",()=>i,"literal",()=>r],479084)},908937,e=>{"use strict";var t=e.i(479084),n=e.i(48189);function a(){return Math.floor((Date.now()+36e5)/1e3)}function i(e,t){let i=a(),r=Math.floor(Date.now()/1e3);if("authenticated"===t.role){if("native"===t.userType&&t.user){let a=t.user;return{aal:t.aal??"aal1",amr:[{method:"password",timestamp:r}],app_metadata:a.raw_app_meta_data,aud:"authenticated",email:a.email,exp:i,iat:r,iss:`https://${e}.supabase.co/auth/v1`,phone:a.phone,role:a.role??t.role,session_id:(0,n.uuidv4)(),sub:a.id,user_metadata:a.raw_user_meta_data,is_anonymous:a.is_anonymous}}if("external"===t.userType&&t.externalAuth)return{aal:t.aal??"aal1",aud:"authenticated",exp:i,iat:r,role:"authenticated",session_id:(0,n.uuidv4)(),sub:t.externalAuth.sub,...t.externalAuth.additionalClaims}}return{iss:"supabase",ref:e,role:t.role,iat:r,exp:i}}let r="ROLE_IMPERSONATION_NO_RESULTS";function o(e,n){var i;let o,{role:l,claims:s}=n??{role:void 0,claims:void 0};if(void 0===l)return e;let c="postgrest"===l.type?void 0!==s?(o={...s,exp:a()},`
select set_config('role', ${(0,t.literal)(l.role)}, true),
set_config('request.jwt.claims', ${(0,t.literal)(JSON.stringify(o))}, true),
set_config('request.method', 'POST', true),
set_config('request.path', '/impersonation-example-request-path', true),
set_config('request.headers', '{"accept": "*/*"}', true);
  `.trim()):"":(i=l.role,`
    set local role ${(0,t.literal)(i)};
  `.trim());return`
    ${c}

    -- If the users sql returns no rows, pg-meta will
    -- fallback to returning the result of the impersonation sql.
    select 1 as "${r}";

    ${e}
  `.trim()}function l(e){return new TextEncoder().encode(e)}function s(e){return btoa(String.fromCharCode(...new Uint8Array("string"==typeof e?l(e):e))).replace(/\+/g,"-").replace(/\//g,"_").replace(/=+$/,"")}async function c(e,t){let n=s(l(JSON.stringify({alg:"HS256",typ:"JWT"})))+"."+s(l(JSON.stringify(e))),a=s(new Uint8Array(await window.crypto.subtle.sign({name:"HMAC"},await window.crypto.subtle.importKey("raw",l(t),{name:"HMAC",hash:"SHA-256"},!1,["sign","verify"]),l(n))));return`${n}.${a}`}function d(e,t,n){return c({...i(e,n),exp:a()},t)}e.s(["ROLE_IMPERSONATION_NO_RESULTS",0,r,"ROLE_IMPERSONATION_SQL_LINE_COUNT",0,11,"getPostgrestClaims",()=>i,"getRoleImpersonationJWT",()=>d,"wrapWithRoleImpersonation",()=>o])},21150,e=>{"use strict";e.s(["sqlKeys",0,{query:(e,t)=>["projects",e,"query",...t],ongoingQueries:e=>["projects",e,"ongoing-queries"]}])},714403,591052,e=>{"use strict";var t=e.i(248593);e.i(242882);var n=e.i(234745);e.i(635494);var a=e.i(10429);e.i(837508);var i=e.i(908937);function r(e){let t=parseFloat(e);return Number.isFinite(t)?t:void 0}function o(e){let t=parseInt(e,10);return Number.isNaN(t)?void 0:t}function l(e){if(e.details){let t=e.details.match(/Rows Removed by Filter:\s*(\d+)/);t&&(e.rowsRemovedByFilter=o(t[1]))}e.children.forEach(l)}function s(e){return e.reduce((e,t)=>Math.max(e,function e(t){return Math.max(t.actualTime?t.actualTime.end-t.actualTime.start:0,t.children.reduce((t,n)=>Math.max(t,e(n)),0))}(t)),0)}function c(e){let t={totalTime:0,totalCost:0,maxCost:0,hasSeqScan:!1,seqScanTables:[],hasIndexScan:!1},n=e=>{e.actualTime&&(t.totalTime=Math.max(t.totalTime,e.actualTime.end)),e.cost&&(t.maxCost=Math.max(t.maxCost,e.cost.end));let a=e.operation.toLowerCase();if(a.includes("seq scan")){t.hasSeqScan=!0;let n=e.details.match(/on\s+((?:"[^"]+"|[\w]+)(?:\.(?:"[^"]+"|[\w]+))*)/);n&&t.seqScanTables.push(n[1])}a.includes("index")&&(t.hasIndexScan=!0),e.children.forEach(n)};return e.forEach(n),t.totalCost=e[0]?.cost?.end??0,t}function d(e){let t=function(e){let t=e.map(e=>e["QUERY PLAN"]||"").filter(Boolean),n=[],a=[],i=/^(Filter|Sort Key|Group Key|Hash Cond|Join Filter|Index Cond|Recheck Cond|Rows Removed by Filter|Rows Removed by Index Recheck|Output|Merge Cond|Sort Method|Worker \d+|Buffers|Planning Time|Execution Time|One-Time Filter|InitPlan|SubPlan):/;for(let e=0;e<t.length;e++){let l=t[e];if(!l.trim())continue;let s=l.match(/^(\s*)/),c=s?s[1].length:0,d=l.includes("->"),u=l,m=c;if(d){let e=l.indexOf("->");m=e,u=l.substring(e+2).trim()}else u=l.trim();if(u.startsWith("Planning Time:")||u.startsWith("Execution Time:")||u.startsWith("Planning:")||u.startsWith("Execution:"))continue;if(i.test(u)&&a.length>0){let e=a[a.length-1].node;e.details+=(e.details?"\n":"")+u;continue}if(!d&&a.length>0&&c>0){let e=a[a.length-1];if(c>e.indent&&!u.match(/^\w+.*\(cost=/)){e.node.details+=(e.node.details?"\n":"")+u;continue}}let p=u.match(/^(.+?)\s*(\([^)]*cost=[^)]+\)(?:\s*\([^)]+\))*)?\s*$/);if(!p)continue;let[,_,g]=p,E=g?g.replace(/^\(|\)$/g,"").replace(/\)\s*\(/g," "):void 0,h=_.trim(),f="",$=_.match(/^(.+?)\s+on\s+(.+)$/i),b=_.match(/^(.+?)\s+using\s+(.+)$/i);$?(h=$[1].trim(),f="on "+$[2].trim()):b&&(h=b[1].trim(),f="using "+b[2].trim()),function(e,t,n,a){for(;a.length>0&&a[a.length-1].indent>=t;)a.pop();0===a.length?n.push(e):a[a.length-1].node.children.push(e),a.push({node:e,indent:t})}(function(e,t,n,a,i){let l={operation:e.trim(),details:t?.trim()||"",level:a,children:[],raw:i};if(n){let e=n.match(/cost=([\d.]+)\.\.([\d.]+)/);if(e){let t=r(e[1]),n=r(e[2]);void 0!==t&&void 0!==n&&(l.cost={start:t,end:n})}let t=n.match(/rows=(\d+)/);t&&(l.rows=o(t[1]));let a=n.match(/width=(\d+)/);a&&(l.width=o(a[1]));let i=n.match(/actual time=([\d.]+)\.\.([\d.]+)/);if(i){let e=r(i[1]),t=r(i[2]);void 0!==e&&void 0!==t&&(l.actualTime={start:e,end:t});let a=n.substring(n.indexOf("actual time=")).match(/rows=(\d+)/);a&&(l.actualRows=o(a[1]))}}return l}(h,f,E,d?Math.floor(m/6)+1:0,l),m,n,a)}return n}(e);return t.forEach(l),t}function u(e){if(!e)return[];let t=e.split("\n").filter(Boolean),n=[];for(let e of t){let t=e.indexOf(":");t>0?n.push({label:e.substring(0,t+1),value:e.substring(t+1).trim()}):e.trim()&&n.push({label:"",value:e.trim()})}return n}e.i(21150),e.s(["calculateMaxDuration",()=>s,"calculateSummary",()=>c,"createNodeTree",()=>d,"parseDetailLines",()=>u],591052);let m="Query cost exceeds threshold";async function p({projectRef:e,connectionString:r,sql:o,queryKey:l,handleError:s,isRoleImpersonationEnabled:u=!1,isStatementTimeoutDisabled:p=!1,preflightCheck:_=!1},g,E,h){let f,$;if(!e)throw Error("projectRef is required");if(new Blob([o]).size>.98*a.MB)throw Error("Query is too large to be run via the SQL Editor");let b=new Headers(E);if(r&&b.set("x-connection-encrypted",r),h){let e=await h({query:o,headers:b});"data"in e?f=e.data:$=e.error}else{let a={signal:g,headers:b,params:{path:{ref:e},header:{"x-connection-encrypted":r??"","x-pg-application-name":p?"supabase/dashboard-query-editor":t.DEFAULT_PLATFORM_APPLICATION_NAME}}};if(_){let{data:e}=await (0,n.post)("/platform/pg-meta/{ref}/query",{...a,body:{query:`explain ${o}`,disable_statement_timeout:p},params:{...a.params,query:{key:"preflight-check"}}}),t=e?d(e):void 0,i=t?c(t):void 0,r=i?.totalCost??0;if(r>=2e5)return(0,n.handleError)({message:m,code:r,metadata:{cost:r,sql:o}})}let i=l?.filter(e=>"string"==typeof e||"number"==typeof e).join("-")??"",s=await (0,n.post)("/platform/pg-meta/{ref}/query",{...a,body:{query:o,disable_statement_timeout:p},params:{...a.params,query:{key:i}}});f=s.data,$=s.error}if($){if(u&&"object"==typeof $&&null!==$&&"error"in $&&"formattedError"in $){let e=$,t=/LINE (\d+):/im,[,n]=t.exec(e.error)??[],a=Number(n);isNaN(a)||(e={...e,error:e.error.replace(t,`LINE ${a-i.ROLE_IMPERSONATION_SQL_LINE_COUNT}:`),formattedError:e.formattedError.replace(t,`LINE ${a-i.ROLE_IMPERSONATION_SQL_LINE_COUNT}:`)}),$=e}if(void 0!==s)return s($);(0,n.handleError)($)}return u&&Array.isArray(f)&&f?.[0]?.[i.ROLE_IMPERSONATION_NO_RESULTS]===1?{result:[]}:{result:f}}e.s(["COST_THRESHOLD_ERROR",0,m,"executeSql",()=>p],714403)},967533,332357,29659,193767,212695,721490,e=>{"use strict";var t=e.i(479084);function n(e,t){let n=`select count(*) from ${d(e)}`,{filters:a}=t??{};return a&&(n=s(n,a)),n+";"}function a(e,t){let n=`truncate ${d(e)}`,{cascade:a}=t??{};return a&&(n+=" cascade"),n+";"}function i(e,n,a){if(!n||0===n.length)throw Error("no filters for this delete query");let i=`delete from ${d(e)}`,{returning:r,enumArrayColumns:o}=a??{};return n&&(i=s(i,n)),r&&(i+=void 0===o||0===o.length?" returning *":` returning *, ${o.map(e=>`${(0,t.ident)(e)}::text[]`).join(",")}`),i+";"}function r(e,n,a){if(!n||0===n.length)throw Error("no value to insert");let{returning:i,enumArrayColumns:r}=a??{},o=Object.keys(n[0]).map(e=>(0,t.ident)(e)).join(","),l="";return l=0==o.length?(0,t.format)("insert into %1$s select from jsonb_populate_recordset(null::%1$s, %2$s)",d(e),(0,t.literal)(JSON.stringify(n))):(0,t.format)("insert into %1$s (%2$s) select %2$s from jsonb_populate_recordset(null::%1$s, %3$s)",d(e),o,(0,t.literal)(JSON.stringify(n))),i&&(l+=void 0===r||0===r.length?" returning *":` returning *, ${r.map(e=>`${(0,t.ident)(e)}::text[]`).join(",")}`),l+";"}function o(e,n,a,i=!0,r=!1){var l,c;let u,m="";m+=`select ${n??"*"} from ${r?(l=e,`${(0,t.ident)(l.name)}`):d(e)}`;let{filters:p,pagination:_,sorts:g}=a??{};if(p&&(m=s(m,p)),g&&(c=m,m=0===(u=g.filter(e=>e.column)).length?c:c+=` order by ${u.map(e=>{let n=e.ascending?"asc":"desc",a=e.nullsFirst?"nulls first":"nulls last";return`${(0,t.ident)(e.table)}.${(0,t.ident)(e.column)} ${n} ${a}`}).join(", ")}`),_){let{limit:e,offset:n}=_??{};m+=` limit ${(0,t.literal)(e)} offset ${(0,t.literal)(n)}`}return`${m}${i?";":""}`}function l(e,n,a){let{filters:i,returning:r,enumArrayColumns:o}=a??{};if(!i||0===i.length)throw Error("no filters for this update query");let l=Object.keys(n).map(e=>(0,t.ident)(e)).join(","),c=(0,t.format)("update %1$s set (%2$s) = (select %2$s from json_populate_record(null::%1$s, %3$s))",d(e),l,(0,t.literal)(JSON.stringify(n)));return i&&(c=s(c,i)),r&&(c+=void 0===o||0===o.length?" returning *":` returning *, ${o.map(e=>`${(0,t.ident)(e)}::text[]`).join(",")}`),c+";"}function s(e,n){return 0===n.length?e:e+=` where ${n.map(e=>{if(Array.isArray(e.column))switch(e.operator){case"in":var n,a,i=e;if(!Array.isArray(i.column))throw Error("Use inFilterSql for single columns");if(!Array.isArray(i.value))throw Error("Values for a tuple 'in' filter must be an array");let r=`(${i.column.map(e=>(0,t.ident)(e)).join(", ")})`,o=i.value.map(e=>{if(Array.isArray(e)){if(e.length!==i.column.length)throw Error("Tuple value length must match column length");return`(${e.map(e=>c(e)).join(", ")})`}{let t=String(e).split(",");if(t.length!==i.column.length)throw Error("Tuple value length must match column length");return`(${t.map(e=>c(e)).join(", ")})`}});return`${r} ${i.operator} (${o.join(", ")})`;case"=":case"<>":case">":case"<":case">=":case"<=":var l=e;if(!Array.isArray(l.column))throw Error("Use standard applyFilters for single column");if(!Array.isArray(l.value))throw Error("Tuple filter value must be an array");if(l.value.length!==l.column.length)throw Error("Tuple filter value must have the same length as the column array");let s=`(${l.column.map(e=>(0,t.ident)(e)).join(", ")})`,d=`(${l.value.map(e=>c(e)).join(", ")})`;return`${s} ${l.operator} ${d}`;default:throw Error(`Cannot use ${e.operator} operator in a tuple filter`)}switch(e.operator){case"in":let u;return u=Array.isArray((n=e).value)?n.value.map(e=>c(e)):String(n.value).split(",").map(e=>c(e)),`${(0,t.ident)(n.column)} ${n.operator} (${u.join(",")})`;case"is":var m=e;let p=String(m.value);switch(p){case"null":case"false":case"true":case"not null":return`${(0,t.ident)(m.column)} ${m.operator} ${p}`;default:return`${(0,t.ident)(m.column)} ${m.operator} ${c(m.value)}`}case"~~":case"~~*":case"!~~":case"!~~*":return a=e,`${(0,t.ident)(a.column)}::text ${a.operator} ${c(a.value)}`;default:return`${(0,t.ident)(e.column)} ${e.operator} ${c(e.value)}`}}).join(" and ")}`}function c(e){if("string"==typeof e&&!(e?.startsWith("ARRAY[")&&e?.endsWith("]")))return(0,t.literal)(e);return e}function d(e){return`${(0,t.ident)(e.schema)}.${(0,t.ident)(e.name)}`}e.s(["countQuery",()=>n,"deleteQuery",()=>i,"insertQuery",()=>r,"selectQuery",()=>o,"truncateQuery",()=>a,"updateQuery",()=>l],332357);class u{table;action;options;pagination;constructor(e,t,n){this.table=e,this.action=t,this.options=n}range(e,t){return this.pagination={offset:e,limit:t-e+1},this}toSql(e={isCTE:!1,isFinal:!0}){try{let{actionValue:t,actionOptions:s,filters:c,sorts:d}=this.options??{};switch(this.action){case"count":return n(this.table,{filters:c});case"delete":return i(this.table,c,{returning:s?.returning,enumArrayColumns:s?.enumArrayColumns});case"insert":return r(this.table,t,{returning:s?.returning,enumArrayColumns:s?.enumArrayColumns});case"select":return o(this.table,t,{filters:c,pagination:this.pagination,sorts:d},e.isFinal,e.isCTE);case"update":return l(this.table,t,{filters:c,returning:s?.returning,enumArrayColumns:s?.enumArrayColumns});case"truncate":return a(this.table,{cascade:s?.cascade});default:return""}}catch(e){throw e}}}e.s(["QueryModifier",()=>u],29659);class m{table;action;actionValue;actionOptions;filters;sorts;constructor(e,t,n,a){this.table=e,this.action=t,this.actionValue=n,this.actionOptions=a,this.filters=[],this.sorts=[]}filter(e,t,n){return this.filters.push({column:e,operator:t,value:n}),this}match(e){return Object.entries(e).map(([e,t])=>{this.filters.push({column:e,operator:"=",value:t})}),this}order(e,t,n=!0,a=!1){return this.sorts.push({table:e,column:t,ascending:n,nullsFirst:a}),this}range(e,t){return this._getQueryModifier().range(e,t)}clone(){let e=structuredClone({table:this.table,action:this.action,actionValue:this.actionValue,actionOptions:this.actionOptions,filters:this.filters,sorts:this.sorts}),t=new m(e.table,e.action,e.actionValue,e.actionOptions);return t.filters=e.filters,t.sorts=e.sorts,t}toSql(e){return this._getQueryModifier().toSql(e)}_getQueryModifier(){return new u(this.table,this.action,{actionValue:this.actionValue,actionOptions:this.actionOptions,filters:this.filters,sorts:this.sorts})}}e.s(["QueryFilter",()=>m],193767);class p{table;constructor(e){this.table=e}count(){return new m(this.table,"count")}delete(e){return new m(this.table,"delete",void 0,e)}insert(e,t){return new m(this.table,"insert",e,t)}select(e){return new m(this.table,"select",e)}update(e,t){return new m(this.table,"update",e,t)}truncate(e){return new m(this.table,"truncate",void 0,e)}}e.s(["QueryAction",()=>p],212695);class _{from(e,t){return new p({name:e,schema:t??"public"})}}e.s(["Query",()=>_],721490),e.s([],967533)},850036,100121,822797,306255,556955,899850,640696,517638,532605,152770,53336,467394,779262,421530,e=>{"use strict";var t=e.i(97429),n=e.i(248593),a=e.i(479084);let i=(e,t)=>`
COALESCE(
  (
    SELECT
      array_agg(row_to_json(${e})) FILTER (WHERE ${t})
    FROM
      ${e}
  ),
  '{}'
) AS ${e}`;function r(e,t,n){return(n&&(t=n.concat(t??[])),e?.length)?`IN (${e.map(a.literal).join(",")})`:t?.length?`NOT IN (${t.map(a.literal).join(",")})`:""}let o=`
-- Lists each column's privileges in the form of:
--
-- [
--   {
--     "column_id": "12345.1",
--     "relation_schema": "public",
--     "relation_name": "mytable",
--     "column_name": "mycolumn",
--     "privileges": [
--       {
--         "grantor": "postgres",
--         "grantee": "myrole",
--         "privilege_type": "SELECT",
--         "is_grantable": false
--       },
--       ...
--     ]
--   },
--   ...
-- ]
--
-- Modified from information_schema.column_privileges. We try to be as close as
-- possible to the view definition, obtained from:
--
-- select pg_get_viewdef('information_schema.column_privileges');
--
-- The main differences are:
-- - we include column privileges for materialized views
--   (reason for exclusion in information_schema.column_privileges:
--    https://www.postgresql.org/message-id/9136.1502740844%40sss.pgh.pa.us)
-- - we query a.attrelid and a.attnum to generate column_id
-- - table_catalog is omitted
-- - table_schema -> relation_schema, table_name -> relation_name
--
-- Column privileges are intertwined with table privileges in that table
-- privileges override column privileges. E.g. if we do:
--
-- grant all on mytable to myrole;
--
-- Then myrole is granted privileges for ALL columns. Likewise, if we do:
--
-- grant all (id) on mytable to myrole;
-- revoke all on mytable from myrole;
--
-- Then the grant on the id column is revoked.
--
-- This is unlike how grants for schemas and tables interact, where you need
-- privileges for BOTH the schema the table is in AND the table itself in order
-- to access the table.

select (x.attrelid || '.' || x.attnum) as column_id,
       nc.nspname as relation_schema,
       x.relname as relation_name,
       x.attname as column_name,
       coalesce(
         jsonb_agg(
           jsonb_build_object(
             'grantor', u_grantor.rolname,
             'grantee', grantee.rolname,
             'privilege_type', x.prtype,
             'is_grantable', x.grantable
           )
         ),
         '[]'
       ) as privileges
from
  (select pr_c.grantor,
          pr_c.grantee,
          a.attrelid,
          a.attnum,
          a.attname,
          pr_c.relname,
          pr_c.relnamespace,
          pr_c.prtype,
          pr_c.grantable,
          pr_c.relowner
   from
     (select pg_class.oid,
             pg_class.relname,
             pg_class.relnamespace,
             pg_class.relowner,
             (aclexplode(coalesce(pg_class.relacl, acldefault('r', pg_class.relowner)))).grantor as grantor,
             (aclexplode(coalesce(pg_class.relacl, acldefault('r', pg_class.relowner)))).grantee as grantee,
             (aclexplode(coalesce(pg_class.relacl, acldefault('r', pg_class.relowner)))).privilege_type as privilege_type,
             (aclexplode(coalesce(pg_class.relacl, acldefault('r', pg_class.relowner)))).is_grantable as is_grantable
      from pg_class
      where (pg_class.relkind = any (array['r',
                                           'v',
                                           'm',
                                           'f',
                                           'p'])) ) pr_c(oid, relname, relnamespace, relowner, grantor, grantee, prtype, grantable),
                                                    pg_attribute a
   where ((a.attrelid = pr_c.oid)
          and (a.attnum > 0)
          and (not a.attisdropped))
   union select pr_a.grantor,
                pr_a.grantee,
                pr_a.attrelid,
                pr_a.attnum,
                pr_a.attname,
                c.relname,
                c.relnamespace,
                pr_a.prtype,
                pr_a.grantable,
                c.relowner
   from
     (select a.attrelid,
             a.attnum,
             a.attname,
             (aclexplode(coalesce(a.attacl, acldefault('c', cc.relowner)))).grantor as grantor,
             (aclexplode(coalesce(a.attacl, acldefault('c', cc.relowner)))).grantee as grantee,
             (aclexplode(coalesce(a.attacl, acldefault('c', cc.relowner)))).privilege_type as privilege_type,
             (aclexplode(coalesce(a.attacl, acldefault('c', cc.relowner)))).is_grantable as is_grantable
      from (pg_attribute a
            join pg_class cc on ((a.attrelid = cc.oid)))
      where ((a.attnum > 0)
             and (not a.attisdropped))) pr_a(attrelid, attnum, attname, grantor, grantee, prtype, grantable),
                                        pg_class c
   where ((pr_a.attrelid = c.oid)
          and (c.relkind = any (ARRAY['r',
                                      'v',
                                      'm',
                                      'f',
                                      'p'])))) x,
     pg_namespace nc,
     pg_authid u_grantor,
  (select pg_authid.oid,
          pg_authid.rolname
   from pg_authid
   union all select (0)::oid as oid,
                    'PUBLIC') grantee(oid, rolname)
where ((x.relnamespace = nc.oid)
       and (x.grantee = grantee.oid)
       and (x.grantor = u_grantor.oid)
       and (x.prtype = any (ARRAY['INSERT',
                                  'SELECT',
                                  'UPDATE',
                                  'REFERENCES']))
       and (pg_has_role(u_grantor.oid, 'USAGE')
            or pg_has_role(grantee.oid, 'USAGE')
            or (grantee.rolname = 'PUBLIC')))
group by column_id,
         nc.nspname,
         x.relname,
         x.attname
`,l=t.z.object({grantor:t.z.string(),grantee:t.z.string(),privilege_type:t.z.union([t.z.literal("SELECT"),t.z.literal("INSERT"),t.z.literal("UPDATE"),t.z.literal("REFERENCES")]),is_grantable:t.z.boolean()}),s=t.z.object({column_id:t.z.string(),relation_schema:t.z.string(),relation_name:t.z.string(),column_name:t.z.string(),privileges:t.z.array(l)}),c=t.z.array(s);t.z.object({columnId:t.z.string(),grantee:t.z.string(),privilegeType:t.z.union([t.z.literal("ALL"),t.z.literal("SELECT"),t.z.literal("INSERT"),t.z.literal("UPDATE"),t.z.literal("REFERENCES")]),isGrantable:t.z.boolean().optional()});let d=`
-- Adapted from information_schema.columns

SELECT
  c.oid :: int8 AS table_id,
  nc.nspname AS schema,
  c.relname AS table,
  (c.oid || '.' || a.attnum) AS id,
  a.attnum AS ordinal_position,
  a.attname AS name,
  CASE
    WHEN a.atthasdef THEN pg_get_expr(ad.adbin, ad.adrelid)
    ELSE NULL
  END AS default_value,
  CASE
    WHEN t.typtype = 'd' THEN CASE
      WHEN bt.typelem <> 0 :: oid
      AND bt.typlen = -1 THEN 'ARRAY'
      WHEN nbt.nspname = 'pg_catalog' THEN format_type(t.typbasetype, NULL)
      ELSE 'USER-DEFINED'
    END
    ELSE CASE
      WHEN t.typelem <> 0 :: oid
      AND t.typlen = -1 THEN 'ARRAY'
      WHEN nt.nspname = 'pg_catalog' THEN format_type(a.atttypid, NULL)
      ELSE 'USER-DEFINED'
    END
  END AS data_type,
  COALESCE(bt.typname, t.typname) AS format,
  a.attidentity IN ('a', 'd') AS is_identity,
  CASE
    a.attidentity
    WHEN 'a' THEN 'ALWAYS'
    WHEN 'd' THEN 'BY DEFAULT'
    ELSE NULL
  END AS identity_generation,
  a.attgenerated IN ('s') AS is_generated,
  NOT (
    a.attnotnull
    OR t.typtype = 'd' AND t.typnotnull
  ) AS is_nullable,
  (
    c.relkind IN ('r', 'p')
    OR c.relkind IN ('v', 'f') AND pg_column_is_updatable(c.oid, a.attnum, FALSE)
  ) AS is_updatable,
  uniques.table_id IS NOT NULL AS is_unique,
  check_constraints.definition AS "check",
  array_to_json(
    array(
      SELECT
        enumlabel
      FROM
        pg_catalog.pg_enum enums
      WHERE
        enums.enumtypid = coalesce(bt.oid, t.oid)
        OR enums.enumtypid = coalesce(bt.typelem, t.typelem)
      ORDER BY
        enums.enumsortorder
    )
  ) AS enums,
  col_description(c.oid, a.attnum) AS comment
FROM
  pg_attribute a
  LEFT JOIN pg_attrdef ad ON a.attrelid = ad.adrelid
  AND a.attnum = ad.adnum
  JOIN (
    pg_class c
    JOIN pg_namespace nc ON c.relnamespace = nc.oid
  ) ON a.attrelid = c.oid
  JOIN (
    pg_type t
    JOIN pg_namespace nt ON t.typnamespace = nt.oid
  ) ON a.atttypid = t.oid
  LEFT JOIN (
    pg_type bt
    JOIN pg_namespace nbt ON bt.typnamespace = nbt.oid
  ) ON t.typtype = 'd'
  AND t.typbasetype = bt.oid
  LEFT JOIN (
    SELECT DISTINCT ON (table_id, ordinal_position)
      conrelid AS table_id,
      conkey[1] AS ordinal_position
    FROM pg_catalog.pg_constraint
    WHERE contype = 'u' AND cardinality(conkey) = 1
  ) AS uniques ON uniques.table_id = c.oid AND uniques.ordinal_position = a.attnum
  LEFT JOIN (
    -- We only select the first column check
    SELECT DISTINCT ON (table_id, ordinal_position)
      conrelid AS table_id,
      conkey[1] AS ordinal_position,
      substring(
        pg_get_constraintdef(pg_constraint.oid, true),
        8,
        length(pg_get_constraintdef(pg_constraint.oid, true)) - 8
      ) AS "definition"
    FROM pg_constraint
    WHERE contype = 'c' AND cardinality(conkey) = 1
    ORDER BY table_id, ordinal_position, oid asc
  ) AS check_constraints ON check_constraints.table_id = c.oid AND check_constraints.ordinal_position = a.attnum
WHERE
  NOT pg_is_other_temp_schema(nc.oid)
  AND a.attnum > 0
  AND NOT a.attisdropped
  AND (c.relkind IN ('r', 'v', 'm', 'f', 'p'))
  AND (
    pg_has_role(c.relowner, 'USAGE')
    OR has_column_privilege(
      c.oid,
      a.attnum,
      'SELECT, INSERT, UPDATE, REFERENCES'
    )
  )
`,u=t.z.object({id:t.z.string(),table_id:t.z.number(),schema:t.z.string(),table:t.z.string(),name:t.z.string(),ordinal_position:t.z.number(),data_type:t.z.string(),format:t.z.string(),is_identity:t.z.boolean(),identity_generation:t.z.string().nullable(),is_generated:t.z.boolean(),is_nullable:t.z.boolean(),is_updatable:t.z.boolean(),is_unique:t.z.boolean(),check:t.z.string().nullable(),default_value:t.z.any().nullable(),enums:t.z.array(t.z.string()),comment:t.z.string().nullable()}),m=t.z.array(u),p=t.z.optional(u),_=e=>e.endsWith("[]")?`${(0,a.ident)(e.slice(0,-2))}[]`:e.includes(".")?e:(0,a.ident)(e),g=`
SELECT
  name,
  setting,
  category,
  TRIM(split_part(category, '/', 1)) AS group,
  TRIM(split_part(category, '/', 2)) AS subgroup,
  unit,
  short_desc,
  extra_desc,
  context,
  vartype,
  source,
  min_val,
  max_val,
  enumvals,
  boot_val,
  reset_val,
  sourcefile,
  sourceline,
  pending_restart
FROM
  pg_settings
ORDER BY
  category,
  name
`,E=t.z.object({name:t.z.string(),setting:t.z.string(),category:t.z.string(),group:t.z.string(),subgroup:t.z.string(),unit:t.z.string().nullable(),short_desc:t.z.string(),extra_desc:t.z.string().nullable(),context:t.z.string(),vartype:t.z.string(),source:t.z.string(),min_val:t.z.string().nullable(),max_val:t.z.string().nullable(),enumvals:t.z.array(t.z.string()).nullable(),boot_val:t.z.string().nullable(),reset_val:t.z.string().nullable(),sourcefile:t.z.string().nullable(),sourceline:t.z.number().nullable(),pending_restart:t.z.boolean()}),h=t.z.array(E),f=`
SELECT
  e.name,
  n.nspname AS schema,
  e.default_version,
  x.extversion AS installed_version,
  e.comment
FROM
  pg_available_extensions() e(name, default_version, comment)
  LEFT JOIN pg_extension x ON e.name = x.extname
  LEFT JOIN pg_namespace n ON x.extnamespace = n.oid
`,$=t.z.object({name:t.z.string(),schema:t.z.string().nullable(),default_version:t.z.string(),installed_version:t.z.string().nullable(),comment:t.z.string()}),b=t.z.array($),A=t.z.optional($),T=`
select
  c.oid::int8 as id,
  n.nspname as schema,
  c.relname as name,
  obj_description(c.oid) as comment,
  fs.srvname as foreign_server_name,
  fdw.fdwname as foreign_data_wrapper_name,
  handler.proname as foreign_data_wrapper_handler
from
  pg_class c
  join pg_namespace n on n.oid = c.relnamespace
  inner join pg_foreign_table ft on ft.ftrelid = c.oid
  inner join pg_foreign_server fs on fs.oid = ft.ftserver
  inner join pg_foreign_data_wrapper fdw on fdw.oid = fs.srvfdw
  inner join pg_proc handler on handler.oid = fdw.fdwhandler
where
  c.relkind = 'f'
`,S=t.z.object({id:t.z.number(),schema:t.z.string(),name:t.z.string(),comment:t.z.string().nullable(),foreign_server_name:t.z.string(),foreign_data_wrapper_name:t.z.string(),foreign_data_wrapper_handler:t.z.string(),columns:m.optional()}),N=t.z.array(S),y=t.z.optional(S),v=({includeColumns:e})=>`
with foreign_tables as (${T})
  ${e?`, columns as (${d})`:""}
select
  *
  ${e?`, ${i("columns","columns.table_id = foreign_tables.id")}`:""}
from foreign_tables`,L=`
-- CTE with sane arg_modes, arg_names, and arg_types.
-- All three are always of the same length.
-- All three include all args, including OUT and TABLE args.
with functions as (
  select
    *,
    -- proargmodes is null when all arg modes are IN
    coalesce(
      p.proargmodes,
      array_fill('i'::text, array[cardinality(coalesce(p.proallargtypes, p.proargtypes))])
    ) as arg_modes,
    -- proargnames is null when all args are unnamed
    coalesce(
      p.proargnames,
      array_fill(''::text, array[cardinality(coalesce(p.proallargtypes, p.proargtypes))])
    ) as arg_names,
    -- proallargtypes is null when all arg modes are IN
    coalesce(p.proallargtypes, p.proargtypes) as arg_types,
    array_cat(
      array_fill(false, array[pronargs - pronargdefaults]),
      array_fill(true, array[pronargdefaults])) as arg_has_defaults
  from
    pg_proc as p
  where
    p.prokind = 'f'
)
select
  f.oid as id,
  n.nspname as schema,
  f.proname as name,
  l.lanname as language,
  case
    when l.lanname = 'internal' then ''
    else f.prosrc
  end as definition,
  case
    when l.lanname = 'internal' then f.prosrc
    else pg_get_functiondef(f.oid)
  end as complete_statement,
  coalesce(f_args.args, '[]') as args,
  pg_get_function_arguments(f.oid) as argument_types,
  pg_get_function_identity_arguments(f.oid) as identity_argument_types,
  f.prorettype as return_type_id,
  pg_get_function_result(f.oid) as return_type,
  nullif(rt.typrelid, 0) as return_type_relation_id,
  f.proretset as is_set_returning_function,
  case
    when f.provolatile = 'i' then 'IMMUTABLE'
    when f.provolatile = 's' then 'STABLE'
    when f.provolatile = 'v' then 'VOLATILE'
  end as behavior,
  f.prosecdef as security_definer,
  f_config.config_params as config_params
from
  functions f
  left join pg_namespace n on f.pronamespace = n.oid
  left join pg_language l on f.prolang = l.oid
  left join pg_type rt on rt.oid = f.prorettype
  left join (
    select
      oid,
      jsonb_object_agg(param, value) filter (where param is not null) as config_params
    from
      (
        select
          oid,
          (string_to_array(unnest(proconfig), '='))[1] as param,
          (string_to_array(unnest(proconfig), '='))[2] as value
        from
          functions
      ) as t
    group by
      oid
  ) f_config on f_config.oid = f.oid
  left join (
    select
      oid,
      jsonb_agg(jsonb_build_object(
        'mode', t2.mode,
        'name', name,
        'type_id', type_id,
        -- Cast null into false boolean
        'has_default', COALESCE(has_default, false)
      )) as args
    from
      (
        select
          oid,
          unnest(arg_modes) as mode,
          unnest(arg_names) as name,
          -- Coming from: coalesce(p.proallargtypes, p.proargtypes) postgres won't automatically assume
          -- integer, we need to cast it to be properly parsed
          unnest(arg_types)::int8 as type_id,
          unnest(arg_has_defaults) as has_default
        from
          functions
      ) as t1,
      lateral (
        select
          case
            when t1.mode = 'i' then 'in'
            when t1.mode = 'o' then 'out'
            when t1.mode = 'b' then 'inout'
            when t1.mode = 'v' then 'variadic'
            else 'table'
          end as mode
      ) as t2
    group by
      t1.oid
  ) f_args on f_args.oid = f.oid
`,R=t.z.object({id:t.z.number(),schema:t.z.string(),name:t.z.string(),language:t.z.string(),definition:t.z.string(),complete_statement:t.z.string(),args:t.z.array(t.z.object({mode:t.z.union([t.z.literal("in"),t.z.literal("out"),t.z.literal("inout"),t.z.literal("variadic"),t.z.literal("table")]),name:t.z.string(),type_id:t.z.number(),has_default:t.z.boolean()})),argument_types:t.z.string(),identity_argument_types:t.z.string(),return_type_id:t.z.number(),return_type:t.z.string(),return_type_relation_id:t.z.union([t.z.number(),t.z.null()]),is_set_returning_function:t.z.boolean(),behavior:t.z.union([t.z.literal("IMMUTABLE"),t.z.literal("STABLE"),t.z.literal("VOLATILE")]),security_definer:t.z.boolean(),config_params:t.z.union([t.z.record(t.z.string(),t.z.string()),t.z.null()])}),z=t.z.array(R),I=t.z.optional(R);function O({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o}={}){let l=`
    with f as (
      ${L}
    )
    select
      f.*
    from f
  `,s=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return s&&(l+=` where schema ${s}`),i&&(l=`${l} limit ${i}`),o&&(l=`${l} offset ${o}`),{sql:l,zod:z}}function w({id:e,name:t,schema:n="public",args:i=[]}){if(e)return{sql:`
      with f as (
        ${L}
      )
      select
        f.*
      from f where id = ${(0,a.literal)(e)};`,zod:I};if(t&&n&&i)return{sql:`with f as (
      ${L}
    )
    select
      f.*
    from f join pg_proc as p on id = p.oid where schema = ${(0,a.literal)(n)} and name = ${(0,a.literal)(t)} and p.proargtypes::text = ${i.length?`(
          select string_agg(type_oid::text, ' ') from (
            select (
              split_args.arr[
                array_length(
                  split_args.arr,
                  1
                )
              ]::regtype::oid
            ) as type_oid from (
              select string_to_array(
                unnest(
                  array[${i.map(a.literal)}]
                ),
                ' '
              ) as arr
            ) as split_args
          ) args
        )`:(0,a.literal)("")}`,zod:I};throw Error("Must provide either id or name and schema")}let C=t.z.object({name:t.z.string(),definition:t.z.string(),args:t.z.array(t.z.string()).optional(),behavior:t.z.enum(["IMMUTABLE","STABLE","VOLATILE"]).optional(),config_params:t.z.record(t.z.string(),t.z.string()).optional(),schema:t.z.string().optional(),language:t.z.string().optional(),return_type:t.z.string().optional(),security_definer:t.z.boolean().optional()});function x({name:e,schema:t,args:n,definition:i,return_type:r,language:o,behavior:l,security_definer:s,config_params:c},{replace:d=!1}={}){return`
    CREATE ${d?"OR REPLACE":""} FUNCTION ${(0,a.ident)(t)}.${(0,a.ident)(e)}(${n?.join(", ")||""})
    RETURNS ${r}
    AS ${(0,a.literal)(i)}
    LANGUAGE ${o}
    ${l}
    CALLED ON NULL INPUT
    ${s?"SECURITY DEFINER":"SECURITY INVOKER"}
    ${c?Object.entries(c).map(([e,t])=>`SET ${e} ${"FROM CURRENT"===t?"FROM CURRENT":"TO "+('""'===t?"''":t)}`).join("\n"):""};
  `}function j({name:e,schema:n="public",args:a=[],definition:i,return_type:r="void",language:o="sql",behavior:l="VOLATILE",security_definer:s=!1,config_params:c={}}){return{sql:x({name:e,schema:n,args:a,definition:i,return_type:r,language:o,behavior:l,security_definer:s,config_params:c}),zod:t.z.void()}}let D=t.z.object({name:t.z.string().optional(),schema:t.z.string().optional(),definition:t.z.string().optional()});function U(e,{name:n,schema:i,definition:r}){let o=e.argument_types.split(", "),l=e.identity_argument_types,s="string"==typeof r?x({...e,definition:r,args:o,config_params:e.config_params??{}},{replace:!0}):"",c=n&&n!==e.name?`ALTER FUNCTION ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.name)}(${l}) RENAME TO ${(0,a.ident)(n)};`:"",d=i&&i!==e.schema?`ALTER FUNCTION ${(0,a.ident)(e.schema)}.${(0,a.ident)(n||e.name)}(${l})  SET SCHEMA ${(0,a.ident)(i)};`:"";return{sql:`
    DO LANGUAGE plpgsql $$
    BEGIN
      IF ${"string"==typeof r?"TRUE":"FALSE"} THEN
        ${s}

        IF (
          SELECT id
          FROM (${L}) AS f
          WHERE f.schema = ${(0,a.literal)(e.schema)}
          AND f.name = ${(0,a.literal)(e.name)}
          AND f.identity_argument_types = ${(0,a.literal)(l)}
        ) != ${e.id} THEN
          RAISE EXCEPTION 'Cannot find function "${e.schema}"."${e.name}"(${l})';
        END IF;
      END IF;

      ${c}

      ${d}
    END;
    $$;
  `,zod:t.z.void()}}let M=t.z.object({cascade:t.z.boolean().default(!1).optional()});function F(e,{cascade:n=!1}={}){return{sql:`DROP FUNCTION ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.name)}
  (${e.identity_argument_types})
  ${n?"CASCADE":"RESTRICT"};`,zod:t.z.void()}}e.s(["create",()=>j,"list",()=>O,"pgFunctionArrayZod",0,z,"pgFunctionCreateZod",0,C,"pgFunctionDeleteZod",0,M,"pgFunctionOptionalZod",0,I,"pgFunctionUpdateZod",0,D,"pgFunctionZod",0,R,"remove",()=>F,"retrieve",()=>w,"update",()=>U],198687);var k=e.i(198687);let q=`
  SELECT
    idx.indexrelid::int8 AS id,
    idx.indrelid::int8 AS table_id,
    n.nspname AS schema,
    idx.indnatts AS number_of_attributes,
    idx.indnkeyatts AS number_of_key_attributes,
    idx.indisunique AS is_unique,
    idx.indisprimary AS is_primary,
    idx.indisexclusion AS is_exclusion,
    idx.indimmediate AS is_immediate,
    idx.indisclustered AS is_clustered,
    idx.indisvalid AS is_valid,
    idx.indcheckxmin AS check_xmin,
    idx.indisready AS is_ready,
    idx.indislive AS is_live,
    idx.indisreplident AS is_replica_identity,
    idx.indkey::smallint[] AS key_attributes,
    idx.indcollation::integer[] AS collation,
    idx.indclass::integer[] AS class,
    idx.indoption::smallint[] AS options,
    idx.indpred AS index_predicate,
    obj_description(idx.indexrelid, 'pg_class') AS comment,
    ix.indexdef as index_definition,
    am.amname AS access_method,
    jsonb_agg(
      jsonb_build_object(
        'attribute_number', a.attnum,
        'attribute_name', a.attname,
        'data_type', format_type(a.atttypid, a.atttypmod)
      )
      ORDER BY a.attnum
    ) AS index_attributes
  FROM
    pg_index idx
    JOIN pg_class c ON c.oid = idx.indexrelid
    JOIN pg_namespace n ON c.relnamespace = n.oid
    JOIN pg_am am ON c.relam = am.oid
    JOIN pg_attribute a ON a.attrelid = c.oid AND a.attnum = ANY(idx.indkey)
    JOIN pg_indexes ix ON c.relname = ix.indexname
  GROUP BY
    idx.indexrelid, idx.indrelid, n.nspname, idx.indnatts, idx.indnkeyatts, idx.indisunique, 
    idx.indisprimary, idx.indisexclusion, idx.indimmediate, idx.indisclustered, idx.indisvalid, 
    idx.indcheckxmin, idx.indisready, idx.indislive, idx.indisreplident, idx.indkey, 
    idx.indcollation, idx.indclass, idx.indoption, idx.indexprs, idx.indpred, ix.indexdef, am.amname
`,H=t.z.object({id:t.z.number(),table_id:t.z.number(),schema:t.z.string(),number_of_attributes:t.z.number(),number_of_key_attributes:t.z.number(),is_unique:t.z.boolean(),is_primary:t.z.boolean(),is_exclusion:t.z.boolean(),is_immediate:t.z.boolean(),is_clustered:t.z.boolean(),is_valid:t.z.boolean(),check_xmin:t.z.boolean(),is_ready:t.z.boolean(),is_live:t.z.boolean(),is_replica_identity:t.z.boolean(),key_attributes:t.z.array(t.z.number()),collation:t.z.array(t.z.number()),class:t.z.array(t.z.number()),options:t.z.array(t.z.number()),index_predicate:t.z.string().nullable(),comment:t.z.string().nullable(),index_definition:t.z.string(),access_method:t.z.string(),index_attributes:t.z.array(t.z.object({attribute_number:t.z.number(),attribute_name:t.z.string(),data_type:t.z.string()}))}),P=t.z.array(H),B=t.z.optional(H),W=`
select
  c.oid::int8 as id,
  n.nspname as schema,
  c.relname as name,
  c.relispopulated as is_populated,
  obj_description(c.oid) as comment
from
  pg_class c
  join pg_namespace n on n.oid = c.relnamespace
where
  c.relkind = 'm'
`,Y=t.z.object({id:t.z.number(),schema:t.z.string(),name:t.z.string(),is_populated:t.z.boolean(),comment:t.z.string().nullable(),columns:m.optional()}),G=t.z.array(Y),J=t.z.optional(Y),Q=({includeColumns:e})=>`
with materialized_views as (${W})
  ${e?`, columns as (${d})`:""}
select
  *
  ${e?`, ${i("columns","columns.table_id = materialized_views.id")}`:""}
from materialized_views`,X=`
select
  pol.oid :: int8 as id,
  n.nspname as schema,
  c.relname as table,
  c.oid :: int8 as table_id,
  pol.polname as name,
  case
    when pol.polpermissive then 'PERMISSIVE'::text
    else 'RESTRICTIVE'::text
  end as action,
  case
    when pol.polroles = '{0}'::oid[] then array_to_json(string_to_array('public'::text, ''::text)::name[])
    else array_to_json(array(
      select pg_roles.rolname
      from pg_roles
      where pg_roles.oid = any(pol.polroles)
      order by pg_roles.rolname
    ))
  end as roles,
  case pol.polcmd
    when 'r'::"char" then 'SELECT'::text
    when 'a'::"char" then 'INSERT'::text
    when 'w'::"char" then 'UPDATE'::text
    when 'd'::"char" then 'DELETE'::text
    when '*'::"char" then 'ALL'::text
    else null::text
  end as command,
  pg_get_expr(pol.polqual, pol.polrelid) as definition,
  pg_get_expr(pol.polwithcheck, pol.polrelid) as check
from
  pg_policy pol
  join pg_class c on c.oid = pol.polrelid
  left join pg_namespace n on n.oid = c.relnamespace
`,V=t.z.object({id:t.z.number(),schema:t.z.string(),table:t.z.string(),table_id:t.z.number(),name:t.z.string(),action:t.z.union([t.z.literal("PERMISSIVE"),t.z.literal("RESTRICTIVE")]),roles:t.z.array(t.z.string()),command:t.z.union([t.z.literal("SELECT"),t.z.literal("INSERT"),t.z.literal("UPDATE"),t.z.literal("DELETE"),t.z.literal("ALL")]),definition:t.z.union([t.z.string(),t.z.null()]),check:t.z.union([t.z.string(),t.z.null()])}),K=t.z.array(V),Z=t.z.optional(V),ee=`
SELECT
  p.oid :: int8 AS id,
  p.pubname AS name,
  p.pubowner::regrole::text AS owner,
  p.pubinsert AS publish_insert,
  p.pubupdate AS publish_update,
  p.pubdelete AS publish_delete,
  p.pubtruncate AS publish_truncate,
  CASE
    WHEN p.puballtables THEN NULL
    ELSE pr.tables
  END AS tables
FROM
  pg_catalog.pg_publication AS p
  LEFT JOIN LATERAL (
    SELECT
      COALESCE(
        array_agg(
          json_build_object(
            'id',
            c.oid :: int8,
            'name',
            c.relname,
            'schema',
            nc.nspname
          )
        ),
        '{}'
      ) AS tables
    FROM
      pg_catalog.pg_publication_rel AS pr
      JOIN pg_class AS c ON pr.prrelid = c.oid
      join pg_namespace as nc on c.relnamespace = nc.oid
    WHERE
      pr.prpubid = p.oid
  ) AS pr ON 1 = 1
`,et=t.z.object({id:t.z.number().optional(),name:t.z.string(),schema:t.z.string()}),en=t.z.object({id:t.z.number(),name:t.z.string(),owner:t.z.string(),publish_insert:t.z.boolean(),publish_update:t.z.boolean(),publish_delete:t.z.boolean(),publish_truncate:t.z.boolean(),tables:t.z.array(et).nullable()}),ea=t.z.array(en),ei=t.z.optional(en),er=`
-- Can't use pg_authid here since some managed Postgres providers don't expose it
-- https://github.com/supabase/postgres-meta/issues/212

select
  r.oid as id,
  rolname as name,
  rolsuper as "isSuperuser",
  rolcreatedb as "canCreateDb",
  rolcreaterole as "canCreateRole",
  rolinherit as "inheritRole",
  rolcanlogin as "canLogin",
  rolreplication as "isReplicationRole",
  rolbypassrls as "canBypassRls",
  (
    select
      count(*)
    from
      pg_stat_activity
    where
      r.rolname = pg_stat_activity.usename
  ) as "activeConnections",
  case when rolconnlimit = -1 then current_setting('max_connections') :: int8
       else rolconnlimit
  end as "connectionLimit",
  rolvaliduntil as "validUntil",
  coalesce(r_config.role_configs, '{}') as config
from
  pg_roles r
  left join (
    select
      oid,
      jsonb_object_agg(param, value) filter (where param is not null) as role_configs
    from
      (
        select
          oid,
          (string_to_array(unnest(rolconfig), '='))[1] as param,
          (string_to_array(unnest(rolconfig), '='))[2] as value
        from
          pg_roles
      ) as _
    group by
      oid
  ) r_config on r_config.oid = r.oid
`,eo=t.z.object({id:t.z.number(),name:t.z.string(),isSuperuser:t.z.boolean(),canCreateDb:t.z.boolean(),canCreateRole:t.z.boolean(),inheritRole:t.z.boolean(),canLogin:t.z.boolean(),isReplicationRole:t.z.boolean(),canBypassRls:t.z.boolean(),activeConnections:t.z.number(),connectionLimit:t.z.number(),validUntil:t.z.union([t.z.string(),t.z.null()]),config:t.z.record(t.z.string(),t.z.string())}),el=t.z.array(eo),es=t.z.optional(eo);function ec(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name)return`${(0,a.ident)("name")} = ${(0,a.literal)(e.name)}`;throw Error("Must provide either id or name")}let ed=`
-- Adapted from information_schema.schemata

select
  n.oid as id,
  n.nspname as name,
  u.rolname as owner,
   obj_description(n.oid, 'pg_namespace') AS comment
from
  pg_namespace n,
  pg_roles u
where
  n.nspowner = u.oid
  and (
    pg_has_role(n.nspowner, 'USAGE')
    or has_schema_privilege(n.oid, 'CREATE, USAGE')
  )
  and not pg_catalog.starts_with(n.nspname, 'pg_temp_')
  and not pg_catalog.starts_with(n.nspname, 'pg_toast_temp_')
`,eu=t.z.object({id:t.z.number(),name:t.z.string(),owner:t.z.string(),comment:t.z.string().nullable()}),em=t.z.array(eu),ep=t.z.optional(eu),e_=`
-- Despite the name \`table_privileges\`, this includes other kinds of relations:
-- views, matviews, etc. "Relation privileges" just doesn't roll off the tongue.
--
-- For each relation, get its relacl in a jsonb format,
-- e.g.
--
-- '{postgres=arwdDxt/postgres}'
--
-- becomes
--
-- [
--   {
--     "grantee": "postgres",
--     "grantor": "postgres",
--     "is_grantable": false,
--     "privilege_type": "INSERT"
--   },
--   ...
-- ]
select
  c.oid as relation_id,
  nc.nspname as schema,
  c.relname as name,
  case
    when c.relkind = 'r' then 'table'
    when c.relkind = 'v' then 'view'
    when c.relkind = 'm' then 'materialized_view'
    when c.relkind = 'f' then 'foreign_table'
    when c.relkind = 'p' then 'partitioned_table'
  end as kind,
  coalesce(
    jsonb_agg(
      jsonb_build_object(
        'grantor', grantor.rolname,
        'grantee', grantee.rolname,
        'privilege_type', _priv.privilege_type,
        'is_grantable', _priv.is_grantable
      )
    ) filter (where _priv is not null),
    '[]'
  ) as privileges
from pg_class c
join pg_namespace as nc
  on nc.oid = c.relnamespace
left join lateral (
  select grantor, grantee, privilege_type, is_grantable
  from aclexplode(coalesce(c.relacl, acldefault('r', c.relowner)))
) as _priv on true
left join pg_roles as grantor
  on grantor.oid = _priv.grantor
left join (
  select
    pg_roles.oid,
    pg_roles.rolname
  from pg_roles
  union all
  select
    (0)::oid as oid, 'PUBLIC'
) as grantee (oid, rolname)
  on grantee.oid = _priv.grantee
where c.relkind in ('r', 'v', 'm', 'f', 'p')
  and not pg_is_other_temp_schema(c.relnamespace)
  and (
    pg_has_role(c.relowner, 'USAGE')
    or has_table_privilege(
      c.oid,
      'SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER'
      || case when current_setting('server_version_num')::int4 >= 170000 then ', MAINTAIN' else '' end
    )
    or has_any_column_privilege(c.oid, 'SELECT, INSERT, UPDATE, REFERENCES')
  )
group by
  c.oid,
  nc.nspname,
  c.relname,
  c.relkind
`,eg=t.z.object({relation_id:t.z.number(),schema:t.z.string(),name:t.z.string(),kind:t.z.union([t.z.literal("table"),t.z.literal("view"),t.z.literal("materialized_view"),t.z.literal("foreign_table"),t.z.literal("partitioned_table")]),privileges:t.z.array(t.z.object({grantor:t.z.string(),grantee:t.z.string(),privilege_type:t.z.union([t.z.literal("SELECT"),t.z.literal("INSERT"),t.z.literal("UPDATE"),t.z.literal("DELETE"),t.z.literal("TRUNCATE"),t.z.literal("REFERENCES"),t.z.literal("TRIGGER"),t.z.literal("MAINTAIN")]),is_grantable:t.z.boolean()}))}),eE=t.z.array(eg),eh=t.z.optional(eg),ef=`
SELECT
  c.oid :: int8 AS id,
  nc.nspname AS schema,
  c.relname AS name,
  c.relrowsecurity AS rls_enabled,
  c.relforcerowsecurity AS rls_forced,
  CASE
    WHEN c.relreplident = 'd' THEN 'DEFAULT'
    WHEN c.relreplident = 'i' THEN 'INDEX'
    WHEN c.relreplident = 'f' THEN 'FULL'
    ELSE 'NOTHING'
  END AS replica_identity,
  pg_total_relation_size(format('%I.%I', nc.nspname, c.relname)) :: int8 AS bytes,
  pg_size_pretty(
    pg_total_relation_size(format('%I.%I', nc.nspname, c.relname))
  ) AS size,
  pg_stat_get_live_tuples(c.oid) AS live_rows_estimate,
  pg_stat_get_dead_tuples(c.oid) AS dead_rows_estimate,
  obj_description(c.oid) AS comment,
  coalesce(pk.primary_keys, '[]') as primary_keys,
  coalesce(
    jsonb_agg(relationships) filter (where relationships is not null),
    '[]'
  ) as relationships
FROM
  pg_namespace nc
  JOIN pg_class c ON nc.oid = c.relnamespace
  left join (
    select
      c.oid::int8 as table_id,
      jsonb_agg(
        jsonb_build_object(
          'table_id', c.oid::int8,
          'schema', n.nspname,
          'table_name', c.relname,
          'name', a.attname
        )
        order by array_position(i.indkey, a.attnum)
      ) as primary_keys
    from
      pg_index i
      join pg_class c on i.indrelid = c.oid
      join pg_namespace n on c.relnamespace = n.oid
      join pg_attribute a on a.attrelid = c.oid and a.attnum = any(i.indkey)
    where
      i.indisprimary
    group by c.oid
  ) as pk
  on pk.table_id = c.oid
  left join (
    select
      c.oid :: int8 as id,
      c.conname as constraint_name,
      nsa.nspname as source_schema,
      csa.relname as source_table_name,
      sa.attname as source_column_name,
      nta.nspname as target_table_schema,
      cta.relname as target_table_name,
      ta.attname as target_column_name
    from
      pg_constraint c
    join (
      pg_attribute sa
      join pg_class csa on sa.attrelid = csa.oid
      join pg_namespace nsa on csa.relnamespace = nsa.oid
    ) on sa.attrelid = c.conrelid and sa.attnum = any (c.conkey)
    join (
      pg_attribute ta
      join pg_class cta on ta.attrelid = cta.oid
      join pg_namespace nta on cta.relnamespace = nta.oid
    ) on ta.attrelid = c.confrelid and ta.attnum = any (c.confkey)
    where
      c.contype = 'f'
  ) as relationships
  on (relationships.source_schema = nc.nspname and relationships.source_table_name = c.relname)
  or (relationships.target_table_schema = nc.nspname and relationships.target_table_name = c.relname)
WHERE
  c.relkind IN ('r', 'p')
  AND NOT pg_is_other_temp_schema(nc.oid)
  AND (
    pg_has_role(c.relowner, 'USAGE')
    OR has_table_privilege(
      c.oid,
      'SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER'
    )
    OR has_any_column_privilege(c.oid, 'SELECT, INSERT, UPDATE, REFERENCES')
  )
group by
  c.oid,
  c.relname,
  c.relrowsecurity,
  c.relforcerowsecurity,
  c.relreplident,
  nc.nspname,
  pk.primary_keys
`,e$=t.z.object({table_id:t.z.number(),name:t.z.string(),schema:t.z.string(),table_name:t.z.string()}),eb=t.z.object({id:t.z.number(),constraint_name:t.z.string(),source_schema:t.z.string(),source_table_name:t.z.string(),source_column_name:t.z.string(),target_table_schema:t.z.string(),target_table_name:t.z.string(),target_column_name:t.z.string()}),eA=t.z.object({id:t.z.number(),schema:t.z.string(),name:t.z.string(),rls_enabled:t.z.boolean(),rls_forced:t.z.boolean(),replica_identity:t.z.enum(["DEFAULT","INDEX","FULL","NOTHING"]),bytes:t.z.number(),size:t.z.string(),live_rows_estimate:t.z.number(),dead_rows_estimate:t.z.number(),comment:t.z.string().nullable(),primary_keys:t.z.array(e$),relationships:t.z.array(eb),columns:m.optional()}),eT=t.z.array(eA);function eS({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o,includeColumns:l=!0}={}){let s=ev({includeColumns:l}),c=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return c&&(s+=` where schema ${c}`),i&&(s+=` limit ${i}`),o&&(s+=` offset ${o}`),{sql:s,zod:eT}}function eN(e){let t=function(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name&&e.schema)return`${(0,a.ident)("name")} = ${(0,a.literal)(e.name)} and ${(0,a.ident)("schema")} = ${(0,a.literal)(e.schema)}`;throw Error("Must provide either id or name and schema")}(e);return{sql:`${ev({includeColumns:!0})} where ${t};`,zod:eA}}function ey(e,{cascade:t=!1}={}){return{sql:`DROP TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.name)} ${t?"CASCADE":"RESTRICT"};`}}let ev=({includeColumns:e})=>`
  with tables as (${ef})
  ${e?`, columns as (${d})`:""}
  select
    *
    ${e?`, ${i("columns","columns.table_id = tables.id")}`:""}
  from tables`;function eL({name:e,schema:t="public",comment:n}){let i=`CREATE TABLE ${(0,a.ident)(t)}.${(0,a.ident)(e)} ();`,r=void 0!=n?`COMMENT ON TABLE ${(0,a.ident)(t)}.${(0,a.ident)(e)} IS ${(0,a.literal)(n)};`:"";return{sql:`BEGIN; ${i} ${r} COMMIT;`}}function eR(e,{name:t,schema:n,rls_enabled:i,rls_forced:r,replica_identity:o,replica_identity_index:l,primary_keys:s,comment:c}){let d=`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.name)}`,u=void 0===n?"":`${d} SET SCHEMA ${(0,a.ident)(n)};`,m="";if(void 0!==t&&t!==e.name){let i=void 0===n?e.schema:n;m=`ALTER TABLE ${(0,a.ident)(i)}.${(0,a.ident)(e.name)} RENAME TO ${(0,a.ident)(t)};`}let p="";if(void 0!==i){let e=`${d} ENABLE ROW LEVEL SECURITY;`,t=`${d} DISABLE ROW LEVEL SECURITY;`;p=i?e:t}let _="";if(void 0!==r){let e=`${d} FORCE ROW LEVEL SECURITY;`,t=`${d} NO FORCE ROW LEVEL SECURITY;`;_=r?e:t}let g="";void 0===o||(g="INDEX"===o?`${d} REPLICA IDENTITY USING INDEX ${l};`:`${d} REPLICA IDENTITY ${o};`);let E="";void 0===s||(E+=`
DO $$
DECLARE
  r record;
BEGIN
  SELECT conname
    INTO r
    FROM pg_constraint
    WHERE contype = 'p' AND conrelid = ${(0,a.literal)(e.id)};
  IF r IS NOT NULL THEN
    EXECUTE ${(0,a.literal)(`${d} DROP CONSTRAINT `)} || quote_ident(r.conname);
  END IF;
END
$$;
`,0===s.length||(E+=`${d} ADD PRIMARY KEY (${s.map(e=>(0,a.ident)(e.name)).join(",")});`));let h=void 0==c?"":`COMMENT ON TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.name)} IS ${(0,a.literal)(c)};`;return{sql:`
BEGIN;
  ${p}
  ${_}
  ${g}
  ${E}
  ${h}
  ${u}
  ${m}
COMMIT;`}}e.s(["create",()=>eL,"list",()=>eS,"remove",()=>ey,"retrieve",()=>eN,"update",()=>eR],330006);var ez=e.i(330006);let eI=`
SELECT
  pg_t.oid AS id,
  pg_t.tgrelid AS table_id,
  CASE
    WHEN pg_t.tgenabled = 'D' THEN 'DISABLED'
    WHEN pg_t.tgenabled = 'O' THEN 'ORIGIN'
    WHEN pg_t.tgenabled = 'R' THEN 'REPLICA'
    WHEN pg_t.tgenabled = 'A' THEN 'ALWAYS'
  END AS enabled_mode,
  (
    STRING_TO_ARRAY(
      ENCODE(pg_t.tgargs, 'escape'), '\\000'
    )
  )[:pg_t.tgnargs] AS function_args,
  is_t.trigger_name AS name,
  is_t.event_object_table AS table,
  is_t.event_object_schema AS schema,
  is_t.action_condition AS condition,
  is_t.action_orientation AS orientation,
  is_t.action_timing AS activation,
  ARRAY_AGG(is_t.event_manipulation)::text[] AS events,
  pg_p.proname AS function_name,
  pg_n.nspname AS function_schema
FROM
  pg_trigger AS pg_t
JOIN
  pg_class AS pg_c
ON pg_t.tgrelid = pg_c.oid
JOIN information_schema.triggers AS is_t
ON is_t.trigger_name = pg_t.tgname
AND pg_c.relname = is_t.event_object_table
AND pg_c.relnamespace = (quote_ident(is_t.event_object_schema))::regnamespace
JOIN pg_proc AS pg_p
ON pg_t.tgfoid = pg_p.oid
JOIN pg_namespace AS pg_n
ON pg_p.pronamespace = pg_n.oid
GROUP BY
  pg_t.oid,
  pg_t.tgrelid,
  pg_t.tgenabled,
  pg_t.tgargs,
  pg_t.tgnargs,
  is_t.trigger_name,
  is_t.event_object_table,
  is_t.event_object_schema,
  is_t.action_condition,
  is_t.action_orientation,
  is_t.action_timing,
  pg_p.proname,
  pg_n.nspname
`,eO=t.z.object({id:t.z.number(),table_id:t.z.number(),enabled_mode:t.z.enum(["DISABLED","ORIGIN","REPLICA","ALWAYS"]),function_args:t.z.array(t.z.string()),name:t.z.string(),table:t.z.string(),schema:t.z.string(),condition:t.z.string().nullable(),orientation:t.z.string(),activation:t.z.string(),events:t.z.array(t.z.string()),function_name:t.z.string(),function_schema:t.z.string()}),ew=t.z.array(eO),eC=t.z.optional(eO);t.z.object({name:t.z.string(),schema:t.z.string().optional().default("public"),table:t.z.string(),function_schema:t.z.string().optional().default("public"),function_name:t.z.string(),function_args:t.z.array(t.z.string()).optional(),activation:t.z.enum(["BEFORE","AFTER","INSTEAD OF"]),events:t.z.array(t.z.string()),orientation:t.z.enum(["ROW","STATEMENT"]).optional(),condition:t.z.string().optional()}),t.z.object({name:t.z.string().optional(),enabled_mode:t.z.enum(["ORIGIN","REPLICA","ALWAYS","DISABLED"]).optional()});let ex=`
select
  t.oid::int8 as id,
  t.typname as name,
  n.nspname as schema,
  format_type (t.oid, null) as format,
  coalesce(t_enums.enums, '[]') as enums,
  coalesce(t_attributes.attributes, '[]') as attributes,
  obj_description (t.oid, 'pg_type') as comment
from
  pg_type t
  left join pg_namespace n on n.oid = t.typnamespace
  left join (
    select
      enumtypid,
      jsonb_agg(enumlabel order by enumsortorder) as enums
    from
      pg_enum
    group by
      enumtypid
  ) as t_enums on t_enums.enumtypid = t.oid
  left join (
    select
      oid,
      jsonb_agg(
        jsonb_build_object('name', a.attname, 'type_id', a.atttypid::int8)
        order by a.attnum asc
      ) as attributes
    from
      pg_class c
      join pg_attribute a on a.attrelid = c.oid
    where
      c.relkind = 'c' and not a.attisdropped
    group by
      c.oid
  ) as t_attributes on t_attributes.oid = t.typrelid
where
  (
    t.typrelid = 0
    or (
      select
        c.relkind = 'c'
      from
        pg_class c
      where
        c.oid = t.typrelid
    )
  )
`,ej=t.z.object({id:t.z.number(),name:t.z.string(),schema:t.z.string(),format:t.z.string(),enums:t.z.array(t.z.string()),attributes:t.z.array(t.z.object({name:t.z.string(),type_id:t.z.number()})),comment:t.z.string().nullable()}),eD=t.z.array(ej),eU=`
select
  version(),
  current_setting('server_version_num')::int8 as version_number,
  (
    select
      count(*) as active_connections
    from
      pg_stat_activity
  ) as active_connections,
  current_setting('max_connections')::int8 as max_connections
`,eM=t.z.object({version:t.z.string(),version_number:t.z.number(),active_connections:t.z.number(),max_connections:t.z.number()}),eF=`
SELECT
  c.oid :: int8 AS id,
  n.nspname AS schema,
  c.relname AS name,
  (pg_relation_is_updatable(c.oid, false) & 20) = 20 AS is_updatable,
  obj_description(c.oid) AS comment
FROM
  pg_class c
  JOIN pg_namespace n ON n.oid = c.relnamespace
WHERE
  c.relkind = 'v'
`,ek=t.z.object({id:t.z.number(),schema:t.z.string(),name:t.z.string(),is_updatable:t.z.boolean(),comment:t.z.string().nullable(),columns:m.optional()}),eq=t.z.array(ek),eH=t.z.optional(ek),eP=({includeColumns:e})=>`
with views as (${eF})
  ${e?`, columns as (${d})`:""}
select
  *
  ${e?`, ${i("columns","columns.table_id = views.id")}`:""}
from views`;e.i(967533);var eB=e.i(721490),eW=e.i(332357),eY=e.i(193767),eG=e.i(212695),eJ=e.i(29659);e.s(["Query",()=>eB.Query,"QueryAction",()=>eG.QueryAction,"QueryFilter",()=>eY.QueryFilter,"QueryModifier",()=>eJ.QueryModifier,"countQuery",()=>eW.countQuery,"deleteQuery",()=>eW.deleteQuery,"insertQuery",()=>eW.insertQuery,"selectQuery",()=>eW.selectQuery,"truncateQuery",()=>eW.truncateQuery,"updateQuery",()=>eW.updateQuery],377171);var eQ=e.i(377171);e.s(["getUserSQL",0,e=>`
select
  auth.users.id,
  auth.users.email,
  auth.users.banned_until,
  auth.users.created_at,
  auth.users.confirmed_at,
  auth.users.confirmation_sent_at,
  auth.users.is_anonymous,
  auth.users.is_sso_user,
  auth.users.invited_at,
  auth.users.last_sign_in_at,
  auth.users.phone,
  auth.users.raw_app_meta_data,
  auth.users.raw_user_meta_data,
  auth.users.updated_at,
  coalesce(
    (
      select
        array_agg(distinct i.provider)
      from
        auth.identities i
      where
        i.user_id = users.id
    ),
    '{}'::text[]
  ) as providers
from
  auth.users
where id = '${e}';
`.trim()],100121);let eX=["idx_users_email","idx_users_created_at_desc","idx_users_last_sign_in_at_desc","idx_users_name","users_phone_key"];function eV(e,t){let n="00000000-0000-0000-0000-000000000000".split("").map((t,n)=>"-"===t?t:e[n]??t);if(e.length>=n.length)return n.join("");if(e.length&&e.length<15&&(n[14]="4"),e.length&&e.length<20&&(n[19]=t?"b":"8"),t)for(let t=e.length;t<n.length;t+=1)"0"===n[t]&&(n[t]="f");return n.join("")}function eK(e){if(!e)return[e,void 0];let t=e.charCodeAt(e.length-1);if(122===t)return[e,e+"~"];if(t>=126)return[e,e+" "];let n=e.substring(0,e.length-1)+String.fromCharCode(t+1);return[e,n]}e.s(["USER_SEARCH_INDEXES",0,eX,"getIndexStatusesSQL",0,()=>`SELECT c.relname as index_name, i.indisvalid as is_valid, i.indisready as is_ready
    FROM pg_index i
    JOIN pg_class c ON c.oid = i.indexrelid
    JOIN pg_namespace n ON n.oid = c.relnamespace
    WHERE n.nspname = 'auth'
    AND c.relname IN (${eX.map(a.literal).join(", ")});`],822797),e.s(["getIndexWorkerStatusSQL",0,()=>`SELECT EXISTS (
    SELECT 1 FROM pg_locks
    WHERE locktype = 'advisory'
    AND (classid::bigint << 32 | objid::bigint) = hashtext('auth_index_worker')::bigint
  ) as is_in_progress;`],306255);let eZ=50;e.s(["getPaginatedUsersSQL",0,({page:e=0,verified:t,keywords:n,providers:i,sort:r,order:o,limit:l=eZ,column:s,startAt:c,cursor:d,improvedSearchEnabled:u=!1})=>{if(u)return(({column:e,keywords:t,verified:n,providers:i,sort:r,order:o,cursor:l,limit:s=eZ})=>{let c=[];if(t&&""!==t){if("email"===e){let e=eK(t);e[1]?c.push(`email >= ${(0,a.literal)(e[0])} AND email < ${(0,a.literal)(e[1])}`):c.push(`email >= ${(0,a.literal)(e[0])}`)}else if("phone"===e){let e=eK(t);e[1]?c.push(`phone >= ${(0,a.literal)(e[0])} AND phone < ${(0,a.literal)(e[1])}`):c.push(`phone >= ${(0,a.literal)(e[0])}`)}else if("id"===e)c.push(`id = ${(0,a.literal)(t)}`);else if("name"===e){let e=eK(t);e[1]?c.push(`raw_user_meta_data->>'name' >= ${(0,a.literal)(e[0])} AND raw_user_meta_data->>'name' < ${(0,a.literal)(e[1])}`):c.push(`raw_user_meta_data->>'name' >= ${(0,a.literal)(e[0])}`)}}"verified"===n?c.push("(email_confirmed_at IS NOT NULL OR phone_confirmed_at IS NOT NULL)"):"anonymous"===n?c.push("is_anonymous IS TRUE"):"unverified"===n&&c.push("(email_confirmed_at IS NULL AND phone_confirmed_at IS NULL)"),i&&i.length>0&&(i.includes("saml 2.0")?c.push(`(SELECT jsonb_agg(CASE WHEN value ~ '^sso' THEN 'sso' ELSE value END) FROM jsonb_array_elements_text((raw_app_meta_data ->> 'providers')::jsonb)) ?| array[${i.map(e=>(0,a.literal)("saml 2.0"===e?"sso":e)).join(", ")}]`):c.push(`(raw_app_meta_data->>'providers')::jsonb ?| array[${i.map(e=>(0,a.literal)(e)).join(", ")}]`));let d=r??"created_at",u=o??"desc";if(l){let e="desc"===u?"<":">";"id"===d?c.push(`id ${e} ${(0,a.literal)(l.id)}::uuid`):c.push(`(${(0,a.ident)(d)}, id) ${e} (${(0,a.literal)(l.sort)}, ${(0,a.literal)(l.id)}::uuid)`)}let m=c.map(e=>`(${e})`).join(" AND "),p=c.length>0?`WHERE ${m}`:"",_="id"===d?`${(0,a.ident)(d)} ${u}`:`${(0,a.ident)(d)} ${u}, id ${u}`,g=`
    SELECT
      auth.users.id,
      auth.users.email,
      auth.users.banned_until,
      auth.users.created_at,
      auth.users.confirmed_at,
      auth.users.confirmation_sent_at,
      auth.users.is_anonymous,
      auth.users.is_sso_user,
      auth.users.invited_at,
      auth.users.last_sign_in_at,
      auth.users.phone,
      auth.users.raw_app_meta_data,
      auth.users.raw_user_meta_data
    FROM
      auth.users
    ${p}
    ORDER BY
      ${_}
    LIMIT
      ${s}`;return`
WITH
  users_data AS (${g})
SELECT
  *,
  COALESCE(
    (
      SELECT
        array_agg(DISTINCT i.provider)
      FROM
        auth.identities i
      WHERE
        i.user_id = users_data.id
    ),
    '{}'::text[]
  ) AS providers
FROM
  users_data;`.trim()})({column:s??"email",keywords:n,verified:t,providers:i,sort:r,order:o,limit:l,cursor:d});let m=e*l,p=[];if(n&&""!==n){let e=`%${n}%`;p.push(`id::text like ${(0,a.literal)(e)} or email like ${(0,a.literal)(e)} or phone like ${(0,a.literal)(e)} or raw_user_meta_data->>'full_name' ilike ${(0,a.literal)(e)} or raw_user_meta_data->>'first_name' ilike ${(0,a.literal)(e)} or raw_user_meta_data->>'last_name' ilike ${(0,a.literal)(e)} or raw_user_meta_data->>'display_name' ilike ${(0,a.literal)(e)}`)}"verified"===t?p.push("email_confirmed_at IS NOT NULL or phone_confirmed_at IS NOT NULL"):"anonymous"===t?p.push("is_anonymous is true"):"unverified"===t&&p.push("email_confirmed_at IS NULL AND phone_confirmed_at IS NULL"),i&&i.length>0&&(i.includes("saml 2.0")?p.push(`(select jsonb_agg(case when value ~ '^sso' then 'sso' else value end) from jsonb_array_elements_text((raw_app_meta_data ->> 'providers')::jsonb)) ?| array[${i.map(e=>(0,a.literal)("saml 2.0"===e?"sso":e)).join(", ")}]`.trim()):p.push(`(raw_app_meta_data->>'providers')::jsonb ?| array[${i.map(e=>(0,a.literal)(e)).join(", ")}]`));let _=p.map(e=>`(${e})`).join(" and "),g=o??"desc",E=`${p.length>0?` where ${_}`:""}
    order by
      ${(0,a.ident)(r??"created_at")} ${g} nulls last
    limit
      ${l}
    offset
      ${m}
  `,h=c?">":">=";if("email"===s){let e=eK(n??"");E=`where lower(email) ${h} ${(0,a.literal)(c||e[0])} ${e[1]?`and lower(email) < ${(0,a.literal)(e[1])}`:""} and instance_id = '00000000-0000-0000-0000-000000000000'::uuid order by instance_id, lower(email) asc limit ${l}`}else if("phone"===s){let e=eK(n??"");E=`where phone ${h} ${(0,a.literal)(c||e[0])} ${e[1]?`and phone < ${(0,a.literal)(e[1])}`:""} order by phone asc limit ${l}`}else"id"===s&&(E=eV(n??"",!1)===n?`where id = ${(0,a.literal)(n)} order by id asc limit ${l}`:`where id ${h} ${(0,a.literal)(c||eV(n??"",!1))} and id < ${(0,a.literal)(eV(n??"",!0))} order by id asc limit ${l}`);let f=`
    select
      auth.users.id,
      auth.users.email,
      auth.users.banned_until,
      auth.users.created_at,
      auth.users.confirmed_at,
      auth.users.confirmation_sent_at,
      auth.users.is_anonymous,
      auth.users.is_sso_user,
      auth.users.invited_at,
      auth.users.last_sign_in_at,
      auth.users.phone,
      auth.users.raw_app_meta_data,
      auth.users.raw_user_meta_data,
      auth.users.updated_at
    from
      auth.users
    ${E}`;return`
with
  users_data as (${f})
select
  *,
  coalesce(
    (
      select
        array_agg(distinct i.provider)
      from
        auth.identities i
      where
        i.user_id = users_data.id
    ),
    '{}'::text[]
  ) as providers
from
  users_data;
  `.trim()}],556955);let e0=`
CREATE OR REPLACE FUNCTION pg_temp.count_estimate(
    query text
) RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
    plan jsonb;
BEGIN
    EXECUTE 'EXPLAIN (FORMAT JSON)' || query INTO plan;
    RETURN plan->0->'Plan'->'Plan Rows';
END;
$$;
`.trim();e.s(["COUNT_ESTIMATE_SQL",0,e0,"THRESHOLD_COUNT",0,5e4],899850),e.s(["getUsersCountSQL",0,({filter:e,keywords:t,providers:n,forceExactCount:i=!1,column:r})=>{let o=t&&""!==t,l=[],s="select count(*) from auth.users";if(r&&o){if("email"===r){let e=eK(t),n=(0,a.literal)(e[0]),i=e[1]?(0,a.literal)(e[1]):null;l.push(`lower(email) >= ${n}${i?` and lower(email) < ${i}`:""} and instance_id = '00000000-0000-0000-0000-000000000000'::uuid`)}else if("phone"===r){let e=eK(t),n=(0,a.literal)(e[0]),i=e[1]?(0,a.literal)(e[1]):null;l.push(`phone >= ${n}${i?` and phone < ${i}`:""}`)}else if("id"===r){let e=eV(t,!1);if(e===t)l.push(`id = ${(0,a.literal)(t)}`);else{let n=eV(t,!0);l.push(`id >= ${(0,a.literal)(e)} and id < ${(0,a.literal)(n)}`)}}}else{if(o){let e=(0,a.literal)(`%${t}%`);l.push(`id::text ilike ${e} or email ilike ${e} or phone ilike ${e}`)}if("verified"===e?l.push("email_confirmed_at IS NOT NULL or phone_confirmed_at IS NOT NULL"):"anonymous"===e?l.push("is_anonymous is true"):"unverified"===e&&l.push("email_confirmed_at IS NULL AND phone_confirmed_at IS NULL"),n&&n.length>0)if(n.includes("saml 2.0")){let e=n.map(e=>"saml 2.0"===e?"sso":e);l.push(`(select jsonb_agg(case when value ~ '^sso' then 'sso' else value end) from jsonb_array_elements_text((raw_app_meta_data ->> 'providers')::jsonb)) ?| array[${(0,a.literal)(e)}]`.trim())}else l.push(`(raw_app_meta_data->>'providers')::jsonb ?| array[${(0,a.literal)(n)}]`)}let c=l.map(e=>`(${e})`).join(" and "),d=l.length>0?` where ${c}`:"";if(i)return`select (${s}${d}), false as is_estimate;`;{let e=`select * from auth.users${d}`,t=`${s}${d}`,n=(0,a.literal)(e);return`
${e0}

with approximation as (select reltuples as estimate from pg_class where oid = 'auth.users'::regclass)
select 
  case 
    when estimate = -1 then (select pg_temp.count_estimate(${n}))::int
    when estimate > 50000 then ${l.length>0?`(select pg_temp.count_estimate(${n}))::int`:"estimate::int"}
    else (${t})
  end as count,
  estimate = -1 or estimate > 50000 as is_estimate
from approximation;
`.trim()}}],640696);let e1=`
SELECT id, name, file_size_limit
FROM storage.buckets
WHERE file_size_limit IS NOT NULL
ORDER BY file_size_limit DESC
LIMIT 51;
`.trim();function e2({search:e,ignoredSchemas:t=[]}={}){let n=t.map(e=>`'${e}'`).join(", ");return`
    table_privileges as (
      select
        c.oid::int as id,
        n.nspname as schema_name,
        c.relname as name,
        c.relkind as kind,

        -- Anon Privileges
        bool_or(pr.rolname = 'anon' and acl.privilege_type = 'SELECT') as anon_select,
        bool_or(pr.rolname = 'anon' and acl.privilege_type = 'INSERT') as anon_insert,
        bool_or(pr.rolname = 'anon' and acl.privilege_type = 'UPDATE') as anon_update,
        bool_or(pr.rolname = 'anon' and acl.privilege_type = 'DELETE') as anon_delete,

        -- Authenticated Privileges
        bool_or(pr.rolname = 'authenticated' and acl.privilege_type = 'SELECT') as auth_select,
        bool_or(pr.rolname = 'authenticated' and acl.privilege_type = 'INSERT') as auth_insert,
        bool_or(pr.rolname = 'authenticated' and acl.privilege_type = 'UPDATE') as auth_update,
        bool_or(pr.rolname = 'authenticated' and acl.privilege_type = 'DELETE') as auth_delete,

        -- Service Role Privileges
        bool_or(pr.rolname = 'service_role' and acl.privilege_type = 'SELECT') as srv_select,
        bool_or(pr.rolname = 'service_role' and acl.privilege_type = 'INSERT') as srv_insert,
        bool_or(pr.rolname = 'service_role' and acl.privilege_type = 'UPDATE') as srv_update,
        bool_or(pr.rolname = 'service_role' and acl.privilege_type = 'DELETE') as srv_delete

      from pg_class c
      join pg_namespace n
        on n.oid = c.relnamespace
      left join lateral aclexplode(coalesce(c.relacl, acldefault('r', c.relowner))) as acl
        on true
      left join pg_roles pr
        on pr.oid = acl.grantee
      where c.relkind in ('r', 'p', 'v', 'm', 'f')
        and n.nspname not in (${n})
        ${e?`and (n.nspname || '.' || c.relname) ilike '%${e}%'`:""}
      group by c.oid, n.nspname, c.relname, c.relkind
    ),
    table_grants as (
      select
        id,
        schema_name,
        name,
        kind,
        case
          -- 1. Strict Granted: All 3 roles possess ALL 4 privileges
          when (
            anon_select and anon_insert and anon_update and anon_delete and
            auth_select and auth_insert and auth_update and auth_delete and
            srv_select and srv_insert and srv_update and srv_delete
          ) then 'granted'

          -- 2. Strict Revoked: NO role possesses ANY privilege
          when not (
            anon_select or anon_insert or anon_update or anon_delete or
            auth_select or auth_insert or auth_update or auth_delete or
            srv_select or srv_insert or srv_update or srv_delete
          ) then 'revoked'

          -- 3. Custom: Anything in between
          else 'custom'
        end as status
      from table_privileges
    )
  `}function e5({search:e,offset:t,limit:n,ignoredSchemas:a=[]}){return`
    with ${e2({search:e,ignoredSchemas:a})}
    select
      (select count(*)::int from table_grants) as total_count,
      coalesce(
        (
          select jsonb_agg(
            jsonb_build_object(
              'id', tg.id,
              'schema', tg.schema_name,
              'name', tg.name,
              'status', tg.status
            )
          )
          from (
            select *
            from table_grants
            order by schema_name, name
            offset ${t}
            limit ${n}
          ) tg
        ),
        '[]'::jsonb
      ) as tables;
  `}function e3({selectedSchemas:e}){let t=e.length>0?e.map(e=>`'${e}'`).join(", "):"''";return`
    with ${e2()}
    select
      count(*)::int as total_count,
      (count(*) filter (where status = 'granted' and schema_name in (${t})))::int as grants_count
    from table_grants
  `}function e8({search:e,ignoredSchemas:t=[]}={}){let n=t.map(e=>`'${e}'`).join(", ");return`
    function_privileges as (
      select
        n.nspname as schema_name,
        p.proname as name,

        -- Aggregate EXECUTE across all overloads + all 3 roles
        bool_or(pr.rolname = 'anon' and acl.privilege_type = 'EXECUTE') as anon_execute,
        bool_or(pr.rolname = 'authenticated' and acl.privilege_type = 'EXECUTE') as auth_execute,
        bool_or(pr.rolname = 'service_role' and acl.privilege_type = 'EXECUTE') as srv_execute

      from pg_proc p
      join pg_namespace n
        on n.oid = p.pronamespace
      left join lateral aclexplode(coalesce(p.proacl, acldefault('f', p.proowner))) as acl
        on true
      left join pg_roles pr
        on pr.oid = acl.grantee
      where p.prokind in ('f', 'w')
        and n.nspname not in (${n})
        ${e?`and (n.nspname || '.' || p.proname) ilike '%${e}%'`:""}
      group by n.nspname, p.proname
    ),
    function_grants as (
      select
        schema_name,
        name,
        case
          when anon_execute and auth_execute and srv_execute then 'granted'
          when not (anon_execute or auth_execute or srv_execute) then 'revoked'
          else 'custom'
        end as status
      from function_privileges
    )
  `}function e9({search:e,offset:t,limit:n,ignoredSchemas:a=[]}){return`
    with ${e8({search:e,ignoredSchemas:a})}
    select
      (select count(*)::int from function_grants) as total_count,
      coalesce(
        (
          select jsonb_agg(
            jsonb_build_object(
              'schema', fg.schema_name,
              'name', fg.name,
              'status', fg.status
            )
          )
          from (
            select *
            from function_grants
            order by schema_name, name
            offset ${t}
            limit ${n}
          ) fg
        ),
        '[]'::jsonb
      ) as functions;
  `}function e7({selectedSchemas:e}){let t=e.length>0?e.map(e=>`'${e}'`).join(", "):"''";return`
    with ${e8()}
    select
      count(*)::int as total_count,
      (count(*) filter (where status = 'granted' and schema_name in (${t})))::int as grants_count
    from function_grants
  `}function e4({schema:e="public"}={}){return`
    select
      count(*)::int as grant_count
    from pg_default_acl d
    join pg_namespace n on n.oid = d.defaclnamespace
    join pg_roles r on r.oid = d.defaclrole
    where n.nspname = '${e}'
      and r.rolname = 'postgres'
      and d.defaclobjtype in ('r', 'f', 'S')
      and exists (
        select 1
        from aclexplode(d.defaclacl) acl
        join pg_roles gr on gr.oid = acl.grantee
        where gr.rolname in ('anon', 'authenticated', 'service_role')
      )
  `}function e6(e){let t=[];for(let n of["anon","authenticated","service_role"])"grant"===e?t.push(`alter default privileges for role postgres in schema public grant select, insert, update, delete on tables to ${n}`,`alter default privileges for role postgres in schema public grant execute on functions to ${n}`,`alter default privileges for role postgres in schema public grant usage, select on sequences to ${n}`):t.push(`alter default privileges for role postgres in schema public revoke select, insert, update, delete on tables from ${n}`,`alter default privileges for role postgres in schema public revoke execute on functions from ${n}`,`alter default privileges for role postgres in schema public revoke usage, select on sequences from ${n}`);return"revoke"===e?t.push("alter default privileges for role postgres in schema public revoke execute on functions from public"):t.push("alter default privileges for role postgres in schema public grant execute on functions to public"),t.join(";\n")+";"}e.s(["LARGEST_SIZE_LIMIT_BUCKETS_COUNT",0,50,"getLargestSizeLimitBucketsSqlUnoptimized",0,e1],517638),e.s(["buildDefaultPrivilegesSql",()=>e6,"buildFunctionPrivilegesSql",0,(e,t)=>{if(0===e.length)return"";let n=e.map(e=>{let t=e.indexOf("."),n=e.slice(0,t),a=e.slice(t+1);return`('${n}','${a}')`}).join(", ");return`
    do $$
    declare
      nspname name;
      proname name;
      arg_types text;
    begin
      for nspname, proname, arg_types in
        select n.nspname, p.proname, pg_get_function_identity_arguments(p.oid)
        from pg_proc p
        join pg_namespace n on n.oid = p.pronamespace
        where (n.nspname, p.proname) in (${n})
      loop
        execute format('${"grant"===t?"grant execute on function %I.%I(%s) to anon, authenticated, service_role":"revoke all on function %I.%I(%s) from anon, authenticated, service_role"}', nspname, proname, arg_types);
      end loop;
    end $$;
  `},"buildTablePrivilegesSql",0,(e,t)=>0===e.length?"":`
    do $$
    declare
      nspname name;
      relname name;
    begin
      for nspname, relname in
        select n.nspname, c.relname
        from pg_class c
        join pg_namespace n on n.oid = c.relnamespace
        where c.oid in (${e.join(", ")})
      loop
        execute format('${"grant"===t?"grant select, insert, update, delete on table %I.%I to anon, authenticated, service_role":"revoke all on table %I.%I from anon, authenticated, service_role"}', nspname, relname);
      end loop;
    end $$;
  `,"getDefaultPrivilegesStateSql",()=>e4,"getExposedFunctionCountsSql",()=>e7,"getExposedFunctionsSql",()=>e9,"getExposedTableCountsSql",()=>e3,"getExposedTablesSql",()=>e5],532605),e.s(["getIndexesSQL",0,({schema:e})=>`
SELECT
  n.nspname        AS schema,
  t.relname        AS "table",
  i.relname        AS name,
  pg_get_indexdef(idx.indexrelid) AS definition,
  STRING_AGG(
    COALESCE(a.attname, '(expression)'),
    ', ' ORDER BY k.ord
  ) AS columns
FROM pg_index idx
JOIN pg_class      t ON t.oid = idx.indrelid
JOIN pg_class      i ON i.oid = idx.indexrelid
JOIN pg_namespace  n ON n.oid = t.relnamespace
JOIN LATERAL unnest(idx.indkey) WITH ORDINALITY AS k(attnum, ord) ON TRUE
LEFT JOIN pg_attribute a
  ON a.attrelid = t.oid
 AND a.attnum   = k.attnum
WHERE n.nspname = '${e}'
GROUP BY
  n.nspname,
  t.relname,
  i.relname,
  idx.indexrelid
ORDER BY
  schema, "table", name;
`.trim()],152770),e.s(["getDatabaseExtensionDefaultSchemaSQL",0,({extension:e})=>`
select name, version, schema from pg_available_extension_versions where name = ${(0,a.literal)(e)} limit 1;
`.trim()],53336),e.s(["getCronJobsMinimalSql",0,({searchTerm:e,page:t,limit:n})=>`
SELECT 
  job.jobid,
  job.jobname,
  job.schedule,
  job.command,
  job.active
FROM 
  cron.job job
${e?`WHERE job.jobname ILIKE ${(0,a.literal)(`%${e}%`)}`:""}
ORDER BY job.jobid
LIMIT ${n}
OFFSET ${t*n};
`.trim(),"getCronJobsSql",0,({searchTerm:e,page:t,limit:n})=>`
WITH latest_runs AS (
  SELECT 
    jobid,
    status,
    MAX(start_time) AS latest_run
  FROM cron.job_run_details
  GROUP BY jobid, status
), most_recent_runs AS (
  SELECT 
    jobid, 
    status, 
    latest_run
  FROM latest_runs lr1
  WHERE latest_run = (
    SELECT MAX(latest_run) 
    FROM latest_runs lr2 
    WHERE lr2.jobid = lr1.jobid
  )
)
SELECT 
  job.jobid,
  job.jobname,
  job.schedule,
  job.command,
  job.active,
  mr.latest_run,
  mr.status
FROM 
  cron.job job
LEFT JOIN most_recent_runs mr ON job.jobid = mr.jobid
${e?`WHERE job.jobname ILIKE ${(0,a.literal)(`%${e}%`)}`:""}
ORDER BY job.jobid
LIMIT ${n}
OFFSET ${t*n};
`.trim(),"getDeleteOldCronJobRunDetailsByCtidSql",0,(e,t,n)=>{let i=(0,a.literal)(`(${t},0)`),r=(0,a.literal)(`(${n},0)`);return`
WITH deleted AS (
  DELETE FROM cron.job_run_details
  WHERE ctid >= ${i}::tid
    AND ctid < ${r}::tid
    AND end_time < now() - interval ${(0,a.literal)(e)}
  RETURNING 1
)
SELECT count(*) as deleted_count FROM deleted;
`.trim()},"getJobRunDetailsPageCountSql",0,()=>`
SELECT pg_relation_size(oid) / current_setting('block_size')::int8 AS num_pages
FROM pg_class
WHERE relname = 'job_run_details'
  AND relnamespace = 'cron'::regnamespace;
`.trim(),"getScheduleDeleteCronJobRunDetailsSql",0,e=>`
SELECT cron.schedule(
  ${(0,a.literal)("delete-job-run-details")},
  ${(0,a.literal)("0 12 * * *")},
  $$DELETE FROM cron.job_run_details WHERE end_time < now() - interval ${(0,a.literal)(e)}$$
);
`.trim()],467394),e.s(["getTableRowsCountSql",0,({table:e,filters:t=[],enforceExactCount:n=!1,isUsingReadReplica:a=!1})=>{if(!e)return"";if(n){let n=new eB.Query().from(e.name,e.schema??void 0).count();return t.filter(e=>e.value&&""!==e.value).forEach(e=>{n=n.filter(e.column,e.operator,e.value)}),`select (${n.toSql().slice(0,-1)}), false as is_estimate;`}{let n=new eB.Query().from(e.name,e.schema??void 0).select("*");t.filter(e=>e.value&&""!=e.value).forEach(e=>{n=n.filter(e.column,e.operator,e.value)});let i=n.toSql(),r=new eB.Query().from(e.name,e.schema??void 0).count();t.filter(e=>e.value&&""!=e.value).forEach(e=>{r=r.filter(e.column,e.operator,e.value)});let o=r.toSql().slice(0,-1);return a?`
with approximation as (
    select reltuples as estimate
    from pg_class
    where oid = ${e.id}
)
select 
  case 
    when estimate > 50000 then (select -1)
    else (${o})
  end as count,
  estimate > 50000 as is_estimate
from approximation;
`.trim():`
${e0}

with approximation as (
    select reltuples as estimate
    from pg_class
    where oid = ${e.id}
)
select 
  case 
    when estimate > 50000 then ${t.length>0?`pg_temp.count_estimate('${i.replaceAll("'","''")}')`:"estimate"}
    else (${o})
  end as count,
  estimate > 50000 as is_estimate
from approximation;
`.trim()}}],779262),e.s(["getLiveTupleEstimate",0,(e,t="public")=>`
SELECT n_live_tup AS live_tuple_estimate
FROM pg_stat_user_tables
WHERE schemaname = ${(0,a.literal)(t)}
AND relname = ${(0,a.literal)(e)};
`.trim()],421530),e.s(["default",0,{roles:{list:function({includeDefaultRoles:e=!1,limit:t,offset:n}={}){let a=`
with
  roles as (${er})
select
  *
from
  roles
where
  true
`;return e||(a+=" and not pg_catalog.starts_with(name, 'pg_')"),t&&(a+=` limit ${t}`),n&&(a+=` offset ${n}`),{sql:a,zod:el}},retrieve:function(e){return{sql:`with roles as (${er}) select * from roles where ${ec(e)};`,zod:es}},create:function({name:e,isSuperuser:t=!1,canCreateDb:n=!1,canCreateRole:i=!1,inheritRole:r=!0,canLogin:o=!1,isReplicationRole:l=!1,canBypassRls:s=!1,connectionLimit:c=-1,password:d,validUntil:u,memberOf:m=[],members:p=[],admins:_=[],config:g={}}){return{sql:`
create role ${(0,a.ident)(e)}
  ${t?"superuser":""}
  ${n?"createdb":""}
  ${i?"createrole":""}
  ${r?"":"noinherit"}
  ${o?"login":""}
  ${l?"replication":""}
  ${s?"bypassrls":""}
  connection limit ${c}
  ${void 0===d?"":`password ${(0,a.literal)(d)}`}
  ${void 0===u?"":`valid until ${(0,a.literal)(u)}`}
  ${0===m.length?"":`in role ${m.map(a.ident).join(",")}`}
  ${0===p.length?"":`role ${p.map(a.ident).join(",")}`}
  ${0===_.length?"":`admin ${_.map(a.ident).join(",")}`}
  ;
${Object.entries(g).map(([t,n])=>`alter role ${(0,a.ident)(e)} set ${(0,a.ident)(t)} = ${(0,a.literal)(n)};`).join("\n")}
`}},update:function(e,t){let{name:n,isSuperuser:i,canCreateDb:r,canCreateRole:o,inheritRole:l,canLogin:s,isReplicationRole:c,canBypassRls:d,connectionLimit:u,password:m,validUntil:p}=t;return{sql:`
do $$
declare
  old record;
begin
  with roles as (${er})
  select * into old from roles where ${ec(e)};
  if old is null then
    raise exception 'Cannot find role with id %', id;
  end if;

  execute(format('alter role %I
    ${void 0===i?"":i?"superuser":"nosuperuser"}
    ${void 0===r?"":r?"createdb":"nocreatedb"}
    ${void 0===o?"":o?"createrole":"nocreaterole"}
    ${void 0===l?"":l?"inherit":"noinherit"}
    ${void 0===s?"":s?"login":"nologin"}
    ${void 0===c?"":c?"replication":"noreplication"}
    ${void 0===d?"":d?"bypassrls":"nobypassrls"}
    ${void 0===u?"":`connection limit ${u}`}
    ${void 0===m?"":`password ${(0,a.literal)(m)}`}
    ${void 0===p?"":"valid until %L"}
  ', old.name${void 0===p?"":`, ${(0,a.literal)(p)}`}));

  ${void 0===n?"":`
  -- Using the same name in the rename clause gives an error, so only do it if the new name is different.
  if ${(0,a.literal)(n)} != old.name then
    execute(format('alter role %I rename to %I;', old.name, ${(0,a.literal)(n)}));
  end if;
  `}
end
$$;
`}},remove:function(e,{ifExists:t=!1}={}){return{sql:`
do $$
declare
  old record;
begin
  with roles as (${er})
  select * into old from roles where ${ec(e)};
  if old is null then
    raise exception 'Cannot find role with id %', id;
  end if;

  execute(format('drop role ${t?"if exists":""} %I;', old.name));
end
$$;
`}},zod:eo},columns:{list:function({tableId:e,includeSystemSchemas:t=!1,includedSchemas:i,excludedSchemas:o,limit:l,offset:s}={}){let c=`
with
  columns as (${d})
select
  *
from
  columns
where
 true
`,u=r(i,o,t?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return u&&(c+=` and schema ${u}`),void 0!==e&&(c+=` and table_id = ${(0,a.literal)(e)} `),l&&(c=`${c} limit ${l}`),s&&(c=`${c} offset ${s}`),{sql:c,zod:m}},retrieve:function(e){return{sql:`WITH columns AS (${d}) SELECT * FROM columns WHERE ${function(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name&&e.schema&&e.table)return`schema = ${(0,a.literal)(e.schema)} AND ${(0,a.ident)("table")} = ${(0,a.literal)(e.table)} AND name = ${(0,a.literal)(e.name)}`;throw Error("Must provide either id or schema, name and table")}(e)};`,zod:p}},create:function({schema:e,table:t,name:n,type:i,default_value:r,default_value_format:o="literal",is_identity:l=!1,identity_generation:s="BY DEFAULT",is_nullable:c,is_primary_key:d=!1,is_unique:u=!1,comment:m,check:p}){let g="";if(l){if(void 0!==r)throw Error("Columns cannot both be identity and have a default value");g=`GENERATED ${s} AS IDENTITY`}else void 0===r||(g="expression"===o?`DEFAULT ${r}`:`DEFAULT ${(0,a.literal)(r)}`);let E="";void 0!==c&&(E=c?"NULL":"NOT NULL");let h=void 0===p?"":`CHECK (${p})`,f=void 0===m?"":`COMMENT ON COLUMN ${(0,a.ident)(e)}.${(0,a.ident)(t)}.${(0,a.ident)(n)} IS ${(0,a.literal)(m)}`;return{sql:`
BEGIN;
  ALTER TABLE ${(0,a.ident)(e)}.${(0,a.ident)(t)} ADD COLUMN ${(0,a.ident)(n)} ${_(i)}
    ${g}
    ${E}
    ${d?"PRIMARY KEY":""}
    ${u?"UNIQUE":""}
    ${h};
  ${f};
COMMIT;`}},update:function(e,{name:t,type:n,drop_default:i=!1,default_value:r,default_value_format:o="literal",is_identity:l,identity_generation:s="BY DEFAULT",is_nullable:c,is_unique:d,comment:u,check:m}){let p,g,E=void 0===t||t===e.name?"":`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} RENAME COLUMN ${(0,a.ident)(e.name)} TO ${(0,a.ident)(t)};`,h=void 0===n?"":`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ALTER COLUMN ${(0,a.ident)(e.name)} SET DATA TYPE ${_(n)} USING ${(0,a.ident)(e.name)}::${_(n)};`;if(i)p=`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ALTER COLUMN ${(0,a.ident)(e.name)} DROP DEFAULT;`;else if(void 0===r)p="";else{let t="expression"===o?r:(0,a.literal)(r);p=`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ALTER COLUMN ${(0,a.ident)(e.name)} SET DEFAULT ${t};`}let f=`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ALTER COLUMN ${(0,a.ident)(e.name)}`;!1===l?f+=" DROP IDENTITY IF EXISTS;":!0===e.is_identity?void 0===s?f="":f+=` SET GENERATED ${s};`:void 0===l?f="":f+=` ADD GENERATED ${s} AS IDENTITY;`,g=void 0===c?"":c?`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ALTER COLUMN ${(0,a.ident)(e.name)} DROP NOT NULL;`:`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ALTER COLUMN ${(0,a.ident)(e.name)} SET NOT NULL;`;let $="";!0===e.is_unique&&!1===d?$=`
DO $$
DECLARE
  r record;
BEGIN
  FOR r IN
    SELECT conname FROM pg_constraint WHERE
      contype = 'u'
      AND cardinality(conkey) = 1
      AND conrelid = ${(0,a.literal)(e.table_id)}
      AND conkey[1] = ${(0,a.literal)(e.ordinal_position)}
  LOOP
    EXECUTE ${(0,a.literal)(`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} DROP CONSTRAINT `)} || quote_ident(r.conname);
  END LOOP;
END
$$;
`:!1===e.is_unique&&!0===d&&($=`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ADD UNIQUE (${(0,a.ident)(e.name)});`);let b=void 0===u?"":`COMMENT ON COLUMN ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)}.${(0,a.ident)(e.name)} IS ${(0,a.literal)(u)};`,A=void 0===m?"":`
DO $$
DECLARE
  v_conname name;
  v_conkey int2[];
BEGIN
  SELECT conname into v_conname FROM pg_constraint WHERE
    contype = 'c'
    AND cardinality(conkey) = 1
    AND conrelid = ${(0,a.literal)(e.table_id)}
    AND conkey[1] = ${(0,a.literal)(e.ordinal_position)}
    ORDER BY oid asc
    LIMIT 1;

  IF v_conname IS NOT NULL THEN
    EXECUTE format('ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} DROP CONSTRAINT %I', v_conname);
  END IF;

  ${null!==m?`
  ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} ADD CONSTRAINT ${(0,a.ident)(`${e.table}_${e.name}_check`)} CHECK (${m});

  SELECT conkey into v_conkey FROM pg_constraint WHERE conname = ${(0,a.literal)(`${e.table}_${e.name}_check`)};

  ASSERT v_conkey IS NOT NULL, 'error creating column constraint: check condition must refer to this column';
  ASSERT cardinality(v_conkey) = 1, 'error creating column constraint: check condition cannot refer to multiple columns';
  ASSERT v_conkey[1] = ${(0,a.literal)(e.ordinal_position)}, 'error creating column constraint: check condition cannot refer to other columns';
`:""}
END
$$;
`;return{sql:`
BEGIN;
  ${g}
  ${h}
  ${p}
  ${f}
  ${$}
  ${b}
  ${A}
  ${E}
COMMIT;`}},remove:function(e,{cascade:t=!1}={}){return{sql:`ALTER TABLE ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)} DROP COLUMN ${(0,a.ident)(e.name)} ${t?"CASCADE":"RESTRICT"};`}},zod:u},schemas:{list:function({includeSystemSchemas:e=!1,limit:t,offset:i}={}){let r=ed;return e||(r=`${r} and not (n.nspname in (${n.DEFAULT_SYSTEM_SCHEMAS.map(a.literal).join(",")}))`),t&&(r=`${r} limit ${t}`),i&&(r=`${r} offset ${i}`),{sql:r,zod:em}},retrieve:function({id:e,name:t}){return e?{sql:`${ed} and n.oid = ${(0,a.literal)(e)};`,zod:ep}:{sql:`${ed} and n.nspname = ${(0,a.literal)(t)};`,zod:ep}},create:function({name:e,owner:t}){return{sql:`create schema ${(0,a.ident)(e)}
  ${void 0===t?"":`authorization ${(0,a.ident)(t)}`};
`}},update:function({id:e,name:t},{name:n,owner:i}){return{sql:`
do $$
declare
  id oid := ${void 0===e?`${(0,a.literal)(t)}::regnamespace`:(0,a.literal)(e)};
  old record;
  new_name text := ${void 0===n?null:(0,a.literal)(n)};
  new_owner text := ${void 0===i?null:(0,a.literal)(i)};
begin
  select * into old from pg_namespace where oid = id;
  if old is null then
    raise exception 'Cannot find schema with id %', id;
  end if;

  if new_owner is not null then
    execute(format('alter schema %I owner to %I;', old.nspname, new_owner));
  end if;

  -- Using the same name in the rename clause gives an error, so only do it if the new name is different.
  if new_name is not null and new_name != old.nspname then
    execute(format('alter schema %I rename to %I;', old.nspname, new_name));
  end if;
end
$$;
`}},remove:function({id:e,name:t},{cascade:n=!1}={}){return{sql:`
do $$
declare
  id oid := ${void 0===e?`${(0,a.literal)(t)}::regnamespace`:(0,a.literal)(e)};
  old record;
  cascade bool := ${(0,a.literal)(n)};
begin
  select * into old from pg_namespace where oid = id;
  if old is null then
    raise exception 'Cannot find schema with id %', id;
  end if;

  execute(format('drop schema %I %s;', old.nspname, case when cascade then 'cascade' else 'restrict' end));
end
$$;
`}},zod:eu},tables:ez,functions:k,tablePrivileges:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o}={}){let l=`
with table_privileges as (${e_})
select *
from table_privileges
`,s=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return s&&(l+=` where schema ${s}`),i&&(l+=` limit ${i}`),o&&(l+=` offset ${o}`),{sql:l,zod:eE}},retrieve:function({id:e,name:t,schema:n="public"}){return e?{sql:`
with table_privileges as (${e_})
select *
from table_privileges
where table_privileges.relation_id = ${(0,a.literal)(e)};`,zod:eh}:{sql:`
with table_privileges as (${e_})
select *
from table_privileges
where table_privileges.schema = ${(0,a.literal)(n)}
  and table_privileges.name = ${(0,a.literal)(t)}
`,zod:eh}},grant:function(e){return{sql:`
do $$
begin
${e.map(({privilegeType:e,relationId:t,grantee:n,isGrantable:i})=>`execute format('grant ${e} on table %s to ${"public"===n.toLowerCase()?"public":(0,a.ident)(n)} ${i?"with grant option":""}', ${t}::regclass);`).join("\n")}
end $$;
`}},revoke:function(e){return{sql:`
do $$
begin
${e.map(({privilegeType:e,relationId:t,grantee:n})=>`execute format('revoke ${e} on table %s from ${"public"===n.toLowerCase()?"public":(0,a.ident)(n)}', ${t}::regclass);`).join("\n")}
end $$;
`}},zod:eg},publications:{list:function({limit:e,offset:t}={}){let n=`with publications as (${ee}) select * from publications`;return e&&(n+=` limit ${e}`),t&&(n+=` offset ${t}`),{sql:n,zod:ea}},retrieve:function(e){return{sql:`with publications as (${ee}) select * from publications where ${function(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name)return`${(0,a.ident)("name")} = ${(0,a.literal)(e.name)}`;throw Error("Must provide either id or name")}(e)};`,zod:ei}},create:function({name:e,publish_insert:t=!1,publish_update:n=!1,publish_delete:i=!1,publish_truncate:r=!1,tables:o=null}){let l;l=null==o?"FOR ALL TABLES":0===o.length?"":`FOR TABLE ${o.map(e=>{if(!e.includes("."))return(0,a.ident)(e);let[t,...n]=e.split("."),i=n.join(".");return`${(0,a.ident)(t)}.${(0,a.ident)(i)}`}).join(",")}`;let s=[];return t&&s.push("insert"),n&&s.push("update"),i&&s.push("delete"),r&&s.push("truncate"),{sql:`
CREATE PUBLICATION ${(0,a.ident)(e)} ${l}
  WITH (publish = '${s.join(",")}');`}},update:function(e,{name:t,owner:n,publish_insert:i,publish_update:r,publish_delete:o,publish_truncate:l,tables:s}){return{sql:`
do $$
declare
  id oid := ${(0,a.literal)(e)};
  old record;
  new_name text := ${void 0===t?null:(0,a.literal)(t)};
  new_owner text := ${void 0===n?null:(0,a.literal)(n)};
  new_publish_insert bool := ${i??null};
  new_publish_update bool := ${r??null};
  new_publish_delete bool := ${o??null};
  new_publish_truncate bool := ${l??null};
  new_tables text := ${void 0===s?null:(0,a.literal)(null===s?"all tables":s.map(e=>{if(!e.includes("."))return(0,a.ident)(e);let[t,...n]=e.split("."),i=n.join(".");return`${(0,a.ident)(t)}.${(0,a.ident)(i)}`}).join(","))};
begin
  select * into old from pg_publication where oid = id;
  if old is null then
    raise exception 'Cannot find publication with id %', id;
  end if;

  if new_tables is null then
    null;
  elsif new_tables = 'all tables' then
    if old.puballtables then
      null;
    else
      -- Need to recreate because going from list of tables <-> all tables with alter is not possible.
      execute(format('drop publication %1$I; create publication %1$I for all tables;', old.pubname));
    end if;
  else
    if old.puballtables then
      -- Need to recreate because going from list of tables <-> all tables with alter is not possible.
      execute(format('drop publication %1$I; create publication %1$I;', old.pubname));
    elsif exists(select from pg_publication_rel where prpubid = id) then
      execute(
        format(
          'alter publication %I drop table %s',
          old.pubname,
          (select string_agg(prrelid::regclass::text, ', ') from pg_publication_rel where prpubid = id)
        )
      );
    end if;

    -- At this point the publication must have no tables.

    if new_tables != '' then
      execute(format('alter publication %I add table %s', old.pubname, new_tables));
    end if;
  end if;

  execute(
    format(
      'alter publication %I set (publish = %L);',
      old.pubname,
      concat_ws(
        ', ',
        case when coalesce(new_publish_insert, old.pubinsert) then 'insert' end,
        case when coalesce(new_publish_update, old.pubupdate) then 'update' end,
        case when coalesce(new_publish_delete, old.pubdelete) then 'delete' end,
        case when coalesce(new_publish_truncate, old.pubtruncate) then 'truncate' end
      )
    )
  );

  execute(format('alter publication %I owner to %I;', old.pubname, coalesce(new_owner, old.pubowner::regrole::name)));

  -- Using the same name in the rename clause gives an error, so only do it if the new name is different.
  if new_name is not null and new_name != old.pubname then
    execute(format('alter publication %I rename to %I;', old.pubname, coalesce(new_name, old.pubname)));
  end if;

  -- We need to retrieve the publication later, so we need a way to uniquely identify which publication this is.
  -- We can't rely on id because it gets changed if it got recreated.
  -- We use a temp table to store the unique name - DO blocks can't return a value.
  create temp table pg_meta_publication_tmp (name) on commit drop as values (coalesce(new_name, old.pubname));
end $$;
`}},remove:function(e){return{sql:`DROP PUBLICATION IF EXISTS ${(0,a.ident)(e.name)};`}},zod:en},extensions:{list:function({limit:e,offset:t}={}){let n=f;return e&&(n=`${n} LIMIT ${e}`),t&&(n=`${n} OFFSET ${t}`),{sql:n,zod:b}},retrieve:function({name:e}){return{sql:`${f} WHERE name = ${(0,a.literal)(e)};`,zod:A}},create:function({name:e,schema:t,version:n,cascade:i=!1}){return{sql:`
CREATE EXTENSION ${(0,a.ident)(e)}
  ${void 0===t?"":`SCHEMA ${(0,a.ident)(t)}`}
  ${void 0===n?"":`VERSION ${(0,a.literal)(n)}`}
  ${i?"CASCADE":""};`}},update:function(e,{update:t=!1,version:n,schema:i}){let r="";t&&(r=`ALTER EXTENSION ${(0,a.ident)(e)} UPDATE ${void 0===n?"":`TO ${(0,a.literal)(n)}`};`);let o=void 0===i?"":`ALTER EXTENSION ${(0,a.ident)(e)} SET SCHEMA ${(0,a.ident)(i)};`;return{sql:`BEGIN; ${r} ${o} COMMIT;`}},remove:function(e,{cascade:t=!1}={}){return{sql:`DROP EXTENSION ${(0,a.ident)(e)} ${t?"CASCADE":"RESTRICT"};`}},zod:$},config:{list:function({limit:e,offset:t}={}){let n=g;return e&&(n+=` LIMIT ${e}`),t&&(n+=` OFFSET ${t}`),{sql:n,zod:h}},zod:E},materializedViews:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o,includeColumns:l=!0}={}){let s=Q({includeColumns:l}),c=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return c&&(s+=` where schema ${c}`),i&&(s+=` limit ${i}`),o&&(s+=` offset ${o}`),{sql:s,zod:G}},retrieve:function(e){let t=function(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name&&e.schema)return`${(0,a.ident)("name")} = ${(0,a.literal)(e.name)} and ${(0,a.ident)("schema")} = ${(0,a.literal)(e.schema)}`;throw Error("Must provide either id or name and schema")}(e);return{sql:`${Q({includeColumns:!0})} where ${t};`,zod:J}},zod:Y},foreignTables:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o,includeColumns:l=!0}={}){let s=v({includeColumns:l}),c=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return c&&(s+=` where schema ${c}`),i&&(s+=` limit ${i}`),o&&(s+=` offset ${o}`),{sql:s,zod:N}},retrieve:function(e){return{sql:`${v({includeColumns:!0})} where ${function(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name&&e.schema)return`${(0,a.ident)("name")} = ${(0,a.literal)(e.name)} and ${(0,a.ident)("schema")} = ${(0,a.literal)(e.schema)}`;throw Error("Must provide either id or name and schema")}(e)};`,zod:y}},zod:S},views:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o,includeColumns:l=!0}={}){let s=eP({includeColumns:l}),c=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return c&&(s+=` where schema ${c}`),i&&(s+=` limit ${i}`),o&&(s+=` offset ${o}`),{sql:s,zod:eq}},retrieve:function(e){let t=function(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name&&e.schema)return`${(0,a.ident)("name")} = ${(0,a.literal)(e.name)} and ${(0,a.ident)("schema")} = ${(0,a.literal)(e.schema)}`;throw Error("Must provide either id or name and schema")}(e);return{sql:`${eP({includeColumns:!0})} where ${t};`,zod:eH}},zod:ek},policies:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o}={}){let l=`
    with policies as (${X})
    select *
    from policies
    `,s=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return s&&(l+=`where schema ${s}`),i&&(l+=` limit ${i}`),o&&(l+=` offset ${o}`),{sql:l,zod:K}},retrieve:function(e){return{sql:`with policies as (${X}) select * from policies where ${function(e){if("id"in e&&e.id)return`id = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name&&e.schema&&e.table)return`name = ${(0,a.literal)(e.name)} AND schema = ${(0,a.literal)(e.schema)} AND table = ${(0,a.literal)(e.table)}`;throw Error("Must provide either id or name, schema and table")}(e)};`,zod:Z}},create:function({name:e,schema:t="public",table:n,definition:i,check:r,action:o="PERMISSIVE",command:l="ALL",roles:s=["public"]}){return{sql:`
create policy ${(0,a.ident)(e)} on ${(0,a.ident)(t)}.${(0,a.ident)(n)}
  as ${o}
  for ${l}
  to ${s.map(a.ident).join(",")}
  ${i?`using (${i})`:""}
  ${r?`with check (${r})`:""};`}},update:function(e,t){let{name:n,definition:i,check:r,roles:o}=t,l=`ALTER POLICY ${(0,a.ident)(e.name)} ON ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)}`,s=void 0===n?"":`${l} RENAME TO ${(0,a.ident)(n)};`,c=void 0===i?"":`${l} USING (${i});`,d=void 0===r?"":`${l} WITH CHECK (${r});`,u=void 0===o?"":`${l} TO ${o.map(a.ident).join(",")};`;return{sql:`BEGIN; ${c} ${d} ${u} ${s} COMMIT;`}},remove:function(e){return{sql:`DROP POLICY ${(0,a.ident)(e.name)} ON ${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)};`}},zod:V},triggers:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o}={}){let l=`with triggers as (${eI}) select * from triggers`,s=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return s&&(l+=` where schema ${s}`),i&&(l+=` limit ${i}`),o&&(l+=` offset ${o}`),{sql:l,zod:ew}},retrieve:function(e){let t=function(e){if("id"in e&&e.id)return`${(0,a.ident)("id")} = ${(0,a.literal)(e.id)}`;if("name"in e&&e.name&&e.table&&e.schema)return`${(0,a.ident)("name")} = ${(0,a.literal)(e.name)} and ${(0,a.ident)("schema")} = ${(0,a.literal)(e.schema)} and ${(0,a.ident)("table")} = ${(0,a.literal)(e.table)}`;throw Error("Must provide either id or name, schema and table")}(e);return{sql:`with triggers as (${eI}) select * from triggers where ${t};`,zod:eC}},create:function({name:e,schema:n="public",table:i,function_schema:r="public",function_name:o,function_args:l=[],activation:s,events:c,orientation:d,condition:u}){let m=`${(0,a.ident)(n)}.${(0,a.ident)(i)}`,p=`${(0,a.ident)(r)}.${(0,a.ident)(o)}`,_=c.join(" or "),g=d?`for each ${d}`:"",E=u?`when (${u})`:"",h=l.map(a.literal).join(",");return{sql:`create trigger ${(0,a.ident)(e)} ${s} ${_} on ${m} ${g} ${E} execute function ${p}(${h});`,zod:t.z.void()}},update:function(e,n){let i=`${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)}`,r="";switch(n.enabled_mode){case"ORIGIN":r=`alter table ${i} enable trigger ${(0,a.ident)(e.name)};`;break;case"DISABLED":r=`alter table ${i} disable trigger ${(0,a.ident)(e.name)};`;break;case"REPLICA":case"ALWAYS":r=`alter table ${i} enable ${n.enabled_mode} trigger ${(0,a.ident)(e.name)};`}let o=n.name&&n.name!==e.name?`alter trigger ${(0,a.ident)(e.name)} on ${i} rename to ${(0,a.ident)(n.name)};`:"";return{sql:`begin; ${r}; ${o}; commit;`,zod:t.z.void()}},remove:function(e,{cascade:n=!1}={}){let i=`${(0,a.ident)(e.schema)}.${(0,a.ident)(e.table)}`;return{sql:`drop trigger ${(0,a.ident)(e.name)} on ${i} ${n?"cascade":""};`,zod:t.z.void()}},zod:eO},types:{list:function({includeArrayTypes:e=!1,includeSystemSchemas:t=!1,includedSchemas:a,excludedSchemas:i,limit:o,offset:l}={}){let s=ex;e||(s+=` and not exists (
      select from pg_type el
      where el.oid = t.typelem
        and el.typarray = t.oid
    )`);let c=r(a,i,t?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return c&&(s+=` and n.nspname ${c}`),o&&(s+=` limit ${o}`),l&&(s+=` offset ${l}`),{sql:s,zod:eD}},zod:ej},version:{retrieve:function(){return{sql:eU,zod:eM}},zod:eM},indexes:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:a,limit:i,offset:o}={}){let l=`
    with indexes as (${q})
    select *
    from indexes
  `,s=r(t,a,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return s&&(l+=` where schema ${s}`),i&&(l+=` limit ${i}`),o&&(l+=` offset ${o}`),{sql:l,zod:P}},retrieve:function({id:e}){return{sql:`
    with indexes as (${q})
    select *
    from indexes
    where id = ${(0,a.literal)(e)};
  `,zod:B}},zod:H},columnPrivileges:{list:function({includeSystemSchemas:e=!1,includedSchemas:t,excludedSchemas:i,columnIds:l,limit:s,offset:d}={}){let u=`
  with column_privileges as (${o})
  select *
  from column_privileges
  `,m=[],p=r(t,i,e?void 0:n.DEFAULT_SYSTEM_SCHEMAS);return p&&m.push(`relation_schema ${p}`),l?.length&&m.push(`column_id in (${l.map(a.literal).join(",")})`),m.length>0&&(u+=` where ${m.join(" and ")}`),s&&(u+=` limit ${s}`),d&&(u+=` offset ${d}`),{sql:u,zod:c}},grant:function(e){return{sql:`
do $$
declare
  col record;
begin
${e.map(({privilegeType:e,columnId:t,grantee:n,isGrantable:i})=>{let[r,o]=t.split(".");return`
select *
from pg_attribute a
where a.attrelid = ${(0,a.literal)(r)}
  and a.attnum = ${(0,a.literal)(o)}
into col;
execute format(
  'grant ${e} (%I) on %s to ${"public"===n.toLowerCase()?"public":(0,a.ident)(n)} ${i?"with grant option":""}',
  col.attname,
  col.attrelid::regclass
);`}).join("\n")}
end $$;
`}},revoke:function(e){return{sql:`
do $$
declare
  col record;
begin
${e.map(({privilegeType:e,columnId:t,grantee:n})=>{let[i,r]=t.split(".");return`
select *
from pg_attribute a
where a.attrelid = ${(0,a.literal)(i)}
  and a.attnum = ${(0,a.literal)(r)}
into col;
execute format(
  'revoke ${e} (%I) on %s from ${"public"===n.toLowerCase()?"public":(0,a.ident)(n)}',
  col.attname,
  col.attrelid::regclass
);`}).join("\n")}
end $$;
`}},zod:s},query:eQ}],850036)}]);