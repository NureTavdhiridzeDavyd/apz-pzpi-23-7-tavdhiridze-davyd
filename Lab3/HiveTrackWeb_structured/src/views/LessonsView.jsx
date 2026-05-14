import React, { useState } from 'react';
import { useApiData } from '../hooks.js';
import { Card, DataState, DetailRows, Field, PageHeader, SelectField } from '../components/ui.jsx';
import { ensureArray, formatDate } from '../utils/format.js';

function lessonsPath(role) {
  if (role === 'Student') {
    return '/api/student/me/attendance';
  }

  if (role === 'Instructor') {
    return '/api/instructor/me/lessons';
  }

  return '/api/lessons';
}

export default function LessonsView({ api, t, user, locale, setNotice }) {
  const { data, loading, error, reload } = useApiData(api, lessonsPath(user.role), (text) => setNotice({ type: 'error', text }));

  const lessons = ensureArray(data).map((item) => {
    if (item.lessonTopic) {
      return {
        id: item.lessonId,
        topic: item.lessonTopic,
        type: item.lessonType,
        lessonDateTime: item.lessonDateTime,
        groupName: item.groupName,
        status: item.status
      };
    }

    return item;
  });

  return (
    <section className="stack">
      <PageHeader title={t.lessons} subtitle="Розклад та навчальні заняття" onRefresh={reload} t={t} />
      {(user.role === 'Admin' || user.role === 'Instructor') && (
        <CreateLessonForm api={api} t={t} setNotice={setNotice} onDone={reload} />
      )}
      <DataState loading={loading} error={error} empty={!lessons.length} t={t} />
      <div className="grid cards">
        {lessons.map((lesson) => (
          <Card key={`${lesson.id}-${lesson.lessonDateTime}`} title={lesson.topic} badge={lesson.type}>
            <DetailRows
              rows={{
                ID: lesson.id,
                Група: lesson.groupName || lesson.groupId,
                Інструктор: lesson.instructorName || lesson.instructorId,
                Час: formatDate(lesson.lessonDateTime || lesson.lesson_datetime, locale),
                Статус: lesson.status
              }}
            />
          </Card>
        ))}
      </div>
    </section>
  );
}

function CreateLessonForm({ api, t, setNotice, onDone }) {
  const [form, setForm] = useState({
    groupId: '1',
    instructorId: '2',
    type: 'Theory',
    topic: 'Дорожні знаки',
    lessonDateTime: '2026-03-10T17:00'
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();

    try {
      await api.post('/api/lessons', {
        ...form,
        groupId: Number(form.groupId),
        instructorId: Number(form.instructorId)
      });
      setNotice({ type: 'ok', text: 'Заняття створено' });
      onDone();
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  return (
    <form className="form-card" onSubmit={submit}>
      <Field label={t.groupId} value={form.groupId} onChange={(value) => update('groupId', value)} />
      <Field label={t.instructorId} value={form.instructorId} onChange={(value) => update('instructorId', value)} />
      <SelectField
        label={t.type}
        value={form.type}
        onChange={(value) => update('type', value)}
        options={[
          { value: 'Theory', label: 'Theory' },
          { value: 'Practice', label: 'Practice' }
        ]}
      />
      <Field label={t.topic} value={form.topic} onChange={(value) => update('topic', value)} />
      <Field label={t.dateTime} value={form.lessonDateTime} onChange={(value) => update('lessonDateTime', value)} type="datetime-local" />
      <button className="primary">{t.create}</button>
    </form>
  );
}
