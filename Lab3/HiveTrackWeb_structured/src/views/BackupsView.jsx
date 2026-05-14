import React from 'react';
import { Database, Download, Globe2, Upload } from 'lucide-react';
import { Card, DetailRows, PageHeader } from '../components/ui.jsx';

export default function BackupsView({ t, user, lang, locale, setNotice }) {
  function exportSettings() {
    const payload = {
      exportedAt: new Date().toISOString(),
      user,
      settings: {
        language: lang,
        locale
      },
      note: 'HiveTrack Web settings backup'
    };

    const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');

    link.href = url;
    link.download = 'hivetrack-web-backup.json';
    link.click();

    URL.revokeObjectURL(url);
    setNotice({ type: 'ok', text: 'Файл експорту створено' });
  }

  function importSettings(event) {
    const file = event.target.files?.[0];

    if (!file) {
      return;
    }

    setNotice({ type: 'ok', text: 'Файл налаштувань імпортовано для демонстрації' });
  }

  return (
    <section className="stack">
      <PageHeader title={t.backups} subtitle="Резервне копіювання та налаштування" t={t} />
      <div className="grid cards">
        <Card title="Резервна копія" badge="JSON">
          <Database size={28} />
          <p className="muted">
            Розділ демонструє експорт та імпорт даних або налаштувань web-застосунку.
          </p>
          <div className="row">
            <button className="primary" onClick={exportSettings}>
              <Download size={16} />
              {t.exportData}
            </button>
            <label className="secondary file-input">
              <Upload size={16} />
              {t.importData}
              <input type="file" accept="application/json" onChange={importSettings} />
            </label>
          </div>
        </Card>

        <Card title="Локалізація" badge={lang.toUpperCase()}>
          <Globe2 size={28} />
          <DetailRows
            rows={{
              [t.language]: lang === 'uk' ? t.ukrainian : t.english,
              'Формат дати': new Intl.DateTimeFormat(locale, { dateStyle: 'full', timeStyle: 'short' }).format(new Date()),
              'Сортування': 'localeCompare'
            }}
          />
        </Card>
      </div>
    </section>
  );
}
