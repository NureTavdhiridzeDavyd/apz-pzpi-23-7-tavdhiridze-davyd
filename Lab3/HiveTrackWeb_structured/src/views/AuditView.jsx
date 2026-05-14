import React from 'react';
import { useApiData } from '../hooks.js';
import { Card, DataState, DetailRows, PageHeader } from '../components/ui.jsx';
import { ensureArray, formatDate } from '../utils/format.js';

export default function AuditView({ api, t, locale, setNotice }) {
  const { data, loading, error, reload } = useApiData(api, '/api/admin/audit', (text) => setNotice({ type: 'error', text }));
  const logs = ensureArray(data);

  return (
    <section className="stack">
      <PageHeader title={t.audit} subtitle="Журнал дій адміністратора" onRefresh={reload} t={t} />
      <DataState loading={loading} error={error} empty={!logs.length} t={t} />
      <div className="grid cards">
        {logs.map((item) => (
          <Card key={item.id} title={item.action} badge={`#${item.id}`}>
            <DetailRows
              rows={{
                User: item.user_id || item.userId,
                Деталі: item.details,
                Час: formatDate(item.created_at || item.createdAt, locale)
              }}
            />
          </Card>
        ))}
      </div>
    </section>
  );
}
