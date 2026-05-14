import React, { useMemo, useState } from 'react';
import { useApiData } from '../hooks.js';
import { Card, DataState, DetailRows, Field, PageHeader } from '../components/ui.jsx';
import { ensureArray, formatDate } from '../utils/format.js';

export default function GroupsView({ api, t, user, locale, setNotice }) {
  const endpoint = user.role === 'Student' ? '/api/student/me/group' : '/api/groups';
  const { data, loading, error, reload } = useApiData(api, endpoint, (text) => setNotice({ type: 'error', text }));
  const groups = user.role === 'Student' ? (data?.group ? [data.group] : []) : ensureArray(data);

  const sortedGroups = useMemo(() => {
    return [...groups].sort((left, right) => String(left.name || '').localeCompare(String(right.name || ''), locale));
  }, [groups, locale]);

  return (
    <section className="stack">
      <PageHeader title={t.groups} subtitle="Список навчальних груп" onRefresh={reload} t={t} />
      {user.role === 'Admin' && <CreateGroupForm api={api} t={t} setNotice={setNotice} onDone={reload} />}
      <DataState loading={loading} error={error} empty={!sortedGroups.length} t={t} />
      <div className="grid cards">
        {sortedGroups.map((group) => (
          <Card key={group.id} title={group.name} badge={group.category}>
            <DetailRows
              rows={{
                ID: group.id,
                [t.startDate]: formatDate(group.startDate || group.start_date, locale),
                [t.endDate]: formatDate(group.endDate || group.end_date, locale),
                Статус: group.status
              }}
            />
          </Card>
        ))}
      </div>
    </section>
  );
}

function CreateGroupForm({ api, t, setNotice, onDone }) {
  const [form, setForm] = useState({
    name: 'Група B-3',
    category: 'B',
    startDate: '2026-03-01',
    endDate: '2026-06-01'
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();

    try {
      await api.post('/api/groups', form);
      setNotice({ type: 'ok', text: 'Групу створено' });
      onDone();
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  return (
    <form className="form-card" onSubmit={submit}>
      <Field label={t.groupName} value={form.name} onChange={(value) => update('name', value)} />
      <Field label={t.category} value={form.category} onChange={(value) => update('category', value)} />
      <Field label={t.startDate} value={form.startDate} onChange={(value) => update('startDate', value)} type="date" />
      <Field label={t.endDate} value={form.endDate} onChange={(value) => update('endDate', value)} type="date" />
      <button className="primary">{t.create}</button>
    </form>
  );
}
