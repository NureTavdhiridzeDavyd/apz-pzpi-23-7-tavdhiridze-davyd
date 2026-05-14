import React from 'react';
import { RefreshCw } from 'lucide-react';

export function Notice({ notice, onClose }) {
  if (!notice) {
    return null;
  }

  return (
    <div className={`notice ${notice.type || 'info'}`} onClick={onClose}>
      {notice.text}
    </div>
  );
}

export function Card({ title, badge, children, className = '' }) {
  return (
    <article className={`card ${className}`}>
      <div className="card-header">
        <h3>{title}</h3>
        {badge && <span className="badge">{badge}</span>}
      </div>
      {children}
    </article>
  );
}

export function StatCard({ icon: Icon, label, value }) {
  return (
    <article className="stat-card">
      {Icon && <Icon size={22} />}
      <div>
        <span>{label}</span>
        <strong>{value ?? '—'}</strong>
      </div>
    </article>
  );
}

export function EmptyState({ title, text }) {
  return (
    <div className="empty-state">
      <strong>{title}</strong>
      <span>{text}</span>
    </div>
  );
}

export function DataState({ loading, error, empty, t }) {
  if (error) {
    return <EmptyState title={t.error} text={error} />;
  }

  // Показуємо блок завантаження тільки тоді, коли даних ще немає.
  // Якщо користувач натискає "Оновити", старі картки залишаються на екрані
  // без миготіння великої плашки "Завантаження".
  if (loading && empty) {
    return <EmptyState title="Завантаження" text="Дані завантажуються з сервера." />;
  }

  if (!loading && empty) {
    return <EmptyState title={t.noData} text={t.emptyHint} />;
  }

  return null;
}

export function PageHeader({ title, subtitle, onRefresh, t }) {
  return (
    <div className="page-header">
      <div>
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {onRefresh && (
        <button className="secondary" onClick={onRefresh}>
          <RefreshCw size={16} />
          {t.refresh}
        </button>
      )}
    </div>
  );
}

function formatDetailValue(value) {
  if (value === undefined || value === null || value === '') {
    return '';
  }

  if (typeof value === 'object') {
    if (Array.isArray(value)) {
      return value.length ? value.map(formatDetailValue).join(', ') : 'Немає даних';
    }

    return Object.entries(value)
      .filter(([, innerValue]) => innerValue !== undefined && innerValue !== null && innerValue !== '')
      .map(([innerKey, innerValue]) => `${innerKey}: ${formatDetailValue(innerValue)}`)
      .join('; ');
  }

  return String(value);
}

export function DetailRows({ rows }) {
  return (
    <dl className="detail-rows">
      {Object.entries(rows)
        .map(([key, value]) => [key, formatDetailValue(value)])
        .filter(([, value]) => value !== '')
        .map(([key, value]) => (
          <React.Fragment key={key}>
            <dt>{key}</dt>
            <dd>{value}</dd>
          </React.Fragment>
        ))}
    </dl>
  );
}

export function Field({ label, value, onChange, type = 'text', placeholder }) {
  return (
    <label className="field">
      <span>{label}</span>
      <input
        type={type}
        value={value}
        placeholder={placeholder}
        onChange={(event) => onChange(event.target.value)}
      />
    </label>
  );
}

export function SelectField({ label, value, onChange, options }) {
  return (
    <label className="field">
      <span>{label}</span>
      <select value={value} onChange={(event) => onChange(event.target.value)}>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
    </label>
  );
}
