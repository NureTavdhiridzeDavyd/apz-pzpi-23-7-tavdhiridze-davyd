import React, { useState } from 'react';
import { useApiData } from '../hooks.js';
import { Card, DataState, DetailRows, Field, PageHeader } from '../components/ui.jsx';
import { ensureArray } from '../utils/format.js';

export default function AdminView({ api, t, setNotice }) {
  const { data, loading, error, reload } = useApiData(api, '/api/admin/enrollments', (text) => setNotice({ type: 'error', text }));
  const enrollments = ensureArray(data);

  return (
    <section className="stack">
      <PageHeader title={t.administration} subtitle="Керування зарахуваннями учнів" onRefresh={reload} t={t} />
      <CreateEnrollmentForm api={api} t={t} setNotice={setNotice} onDone={reload} />
      <DataState loading={loading} error={error} empty={!enrollments.length} t={t} />
      <div className="grid cards">
        {enrollments.map((item) => (
          <Card key={item.id} title={item.studentName || `Учень #${item.studentId}`} badge={item.status}>
            <DetailRows rows={{ ID: item.id, Група: item.groupName || item.groupId, Email: item.email }} />
          </Card>
        ))}
      </div>
    </section>
  );
}

function CreateEnrollmentForm({ api, t, setNotice, onDone }) {
  const [form, setForm] = useState({
    userId: '1',
    groupId: '1',
    status: 'Active'
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();

    try {
      await api.post('/api/admin/enrollments', {
        userId: Number(form.userId),
        groupId: Number(form.groupId),
        status: form.status
      });
      setNotice({ type: 'ok', text: 'Учня зараховано до групи' });
      onDone();
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  return (
    <form className="form-card" onSubmit={submit}>
      <Field label={t.userId} value={form.userId} onChange={(value) => update('userId', value)} />
      <Field label={t.groupId} value={form.groupId} onChange={(value) => update('groupId', value)} />
      <Field label={t.status} value={form.status} onChange={(value) => update('status', value)} />
      <button className="primary">{t.enroll}</button>
    </form>
  );
}
