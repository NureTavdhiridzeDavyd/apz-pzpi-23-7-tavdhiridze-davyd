import React from 'react';
import { useApiData } from '../hooks.js';
import { Card, DataState, DetailRows, PageHeader } from '../components/ui.jsx';
import { ensureArray, formatDate } from '../utils/format.js';

export default function TestsView({ api, t, user, locale, setNotice }) {
  const endpoint = user.role === 'Student' ? '/api/student/me/tests' : '/api/tests';
  const { data, loading, error, reload } = useApiData(api, endpoint, (text) => setNotice({ type: 'error', text }));
  const tests = ensureArray(data);

  async function createAttempt(testId) {
    try {
      await api.post(`/api/student/me/tests/${testId}/attempt`, {});
      setNotice({ type: 'ok', text: 'Демонстраційний тест пройдено' });
      reload();
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    }
  }

  return (
    <section className="stack">
      <PageHeader title={t.tests} subtitle="Тести та результати учня" onRefresh={reload} t={t} />
      <DataState loading={loading} error={error} empty={!tests.length} t={t} />
      <div className="grid cards">
        {tests.map((test) => (
          <Card key={test.id} title={test.name || test.title} badge={test.mode || test.category}>
            <DetailRows
              rows={{
                Тема: test.topic,
                Бал: test.score ?? '—',
                Максимум: test.maxScore || test.max_score,
                Статус: test.status,
                Дата: formatDate(test.completedAt || test.completed_at, locale)
              }}
            />
            {user.role === 'Student' && (
              <button className="secondary" onClick={() => createAttempt(test.id)}>
                Пройти демо-тест
              </button>
            )}
          </Card>
        ))}
      </div>
    </section>
  );
}
