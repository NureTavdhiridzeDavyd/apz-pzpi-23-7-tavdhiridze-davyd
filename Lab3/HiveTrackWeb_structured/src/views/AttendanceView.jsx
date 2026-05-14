import React, { useState } from 'react';
import { useApiData } from '../hooks.js';
import { Card, DataState, DetailRows, Field, PageHeader, SelectField } from '../components/ui.jsx';
import { ensureArray, formatDate, statusLabel } from '../utils/format.js';

export default function AttendanceView({ api, t, user, locale, setNotice }) {
  const endpoint = user.role === 'Student' ? '/api/student/me/attendance' : '/api/attendance/student/1';
  const { data, loading, error, reload } = useApiData(api, endpoint, (text) => setNotice({ type: 'error', text }));
  const items = ensureArray(data);

  return (
    <section className="stack">
      <PageHeader title={t.attendance} subtitle="Історія відвідуваності" onRefresh={reload} t={t} />
      {(user.role === 'Admin' || user.role === 'Instructor') && (
        <CreateAttendanceForm api={api} t={t} setNotice={setNotice} onDone={reload} />
      )}
      <DataState loading={loading} error={error} empty={!items.length} t={t} />
      <div className="grid cards">
        {items.map((item) => (
          <Card key={item.id} title={item.lessonTopic || `Заняття #${item.lessonId || item.lesson_id}`} badge={statusLabel(item.status)}>
            <DetailRows
              rows={{
                ID: item.id,
                Група: item.groupName,
                Час: formatDate(item.lessonDateTime, locale),
                Статус: statusLabel(item.status),
                Коментар: item.comment
              }}
            />
          </Card>
        ))}
      </div>
    </section>
  );
}

function CreateAttendanceForm({ api, t, setNotice, onDone }) {
  const [form, setForm] = useState({
    lessonId: '1',
    studentId: '1',
    status: 'Present',
    comment: 'Був присутній'
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();

    try {
      await api.post('/api/attendance', {
        ...form,
        lessonId: Number(form.lessonId),
        studentId: Number(form.studentId)
      });
      setNotice({ type: 'ok', text: 'Відвідуваність додано' });
      onDone();
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  return (
    <form className="form-card" onSubmit={submit}>
      <Field label={t.lessonId} value={form.lessonId} onChange={(value) => update('lessonId', value)} />
      <Field label={t.studentId} value={form.studentId} onChange={(value) => update('studentId', value)} />
      <SelectField
        label={t.status}
        value={form.status}
        onChange={(value) => update('status', value)}
        options={[
          { value: 'Present', label: 'Present' },
          { value: 'Absent', label: 'Absent' },
          { value: 'Late', label: 'Late' }
        ]}
      />
      <Field label={t.comment} value={form.comment} onChange={(value) => update('comment', value)} />
      <button className="primary">{t.create}</button>
    </form>
  );
}
