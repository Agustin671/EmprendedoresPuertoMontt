const fs = require('fs');
const path = require('path');
const root = __dirname;
const credentials = JSON.parse(fs.readFileSync(path.join(root, 'access.local.json'), 'utf8'));
async function query(sql) {
  const response = await fetch(credentials.url + '/api/platform/pg-meta/default/query', {
    method: 'POST',
    headers: { Authorization: 'Basic ' + Buffer.from(credentials.studioUser + ':' + credentials.studioPassword).toString('base64'), 'Content-Type': 'application/json' },
    body: JSON.stringify({ query: sql }), signal: AbortSignal.timeout(60000)
  });
  const body = await response.json();
  if (!response.ok || !Array.isArray(body)) {
    fs.writeFileSync(path.join(root, 'last-error.local.json'), JSON.stringify(body));
    throw new Error('SQL API status ' + response.status + '; detalle guardado localmente');
  }
  return body;
}
module.exports = { query, credentials, root };
if (require.main === module) {
  const file = process.argv[2];
  query(fs.readFileSync(file, 'utf8')).then(rows => {
    const output = process.argv[3];
    if (output) { fs.writeFileSync(output, JSON.stringify(rows, null, 2)); console.log(JSON.stringify({ ok: true, rows: rows.length, saved: output })); }
    else console.log(JSON.stringify(rows));
  }).catch(e => { console.error(e.message); process.exitCode = 1; });
}
