import React, { useState } from 'react';
import { useApiData } from '../hooks.js';
import { Card, DataState, Field, PageHeader } from '../components/ui.jsx';
import { ensureArray } from '../utils/format.js';

export default function NotificationsView({ api, t, user, setNotice }) {
  const { data, loading, error, reload } = useApiData(api, '/api/notifications/my', (text) => setNotice({ type: 'error', text }));
  const notifications = ensureArray(data);

  async function markRead(id) {
    try {
      await api.put(`/api/notifications/${id}/read`, {});
      reload();
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  return (
    <section className="stack">
      <PageHeader title={t.notifications} subtitle="Персональні повідомлення" onRefresh={reload} t={t} />
      {user.role === 'Admin' && <CreateNotificationForm api={api} t={t} setNotice={setNotice} onDone={reload} />}
      <DataState loading={loading} error={error} empty={!notifications.length} t={t} />
      <div className="grid cards">
        {notifications.map((item) => {
          const read = item.isRead || item.is_read;

          return (
            <Card key={item.id} title={item.title} badge={read ? 'Read' : 'New'}>
              <p>{item.message}</p>
              {!read && (
                <button className="secondary" onClick={() => markRead(item.id)}>
                  {t.markRead}
                </button>
              )}
            </Card>
          );
        })}
      </div>
    </section>
  );
}

function CreateNotificationForm({ api, t, setNotice, onDone }) {
  const [form, setForm] = useState({
    userId: '1',
    title: 'Нове повідомлення',
    message: 'Заплановано нове заняття.'
  });

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function submit(event) {
    event.preventDefault();

    try {
      await api.post('/api/admin/notifications', {
        ...form,
        userId: Number(form.userId)
      });
      setNotice({ type: 'ok', text: 'Сповіщення створено' });
      onDone();
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  return (
    <form className="form-card" onSubmit={submit}>
      <Field label={t.userId} value={form.userId} onChange={(value) => update('userId', value)} />
      <Field label={t.title} value={form.title} onChange={(value) => update('title', value)} />
      <Field label={t.message} value={form.message} onChange={(value) => update('message', value)} />
      <button className="primary">{t.send}</button>
    </form>
  );
}
