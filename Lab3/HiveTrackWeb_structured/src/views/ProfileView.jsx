import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { Card, DetailRows, PageHeader } from '../components/ui.jsx';
import { roleLabel } from '../utils/format.js';

export default function ProfileView({ t, user, lang, locale }) {
  return (
    <section className="stack">
      <PageHeader title={t.profile} subtitle="Дані поточного користувача" t={t} />
      <div className="grid cards">
        <Card title={user.fullName} badge={roleLabel(user.role, t)}>
          <ShieldCheck size={32} />
          <DetailRows
            rows={{
              ID: user.id,
              Email: user.email,
              [t.role]: roleLabel(user.role, t),
              [t.language]: lang,
              'Поточний час': new Intl.DateTimeFormat(locale, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date())
            }}
          />
        </Card>
      </div>
    </section>
  );
}
