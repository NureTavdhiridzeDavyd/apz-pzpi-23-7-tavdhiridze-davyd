export function ensureArray(value) {
  if (Array.isArray(value)) {
    return value;
  }

  if (!value) {
    return [];
  }

  return [value];
}

export function formatDate(value, locale = 'uk-UA') {
  if (!value) {
    return '—';
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return String(value);
  }

  return new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: value.includes?.('T') || value.includes?.(':') ? 'short' : undefined
  }).format(date);
}

export function roleLabel(role, t) {
  if (role === 'Admin') {
    return t.admin;
  }

  if (role === 'Instructor') {
    return t.instructor;
  }

  return t.student;
}

export function statusLabel(status) {
  const labels = {
    Present: 'Присутній',
    Absent: 'Відсутній',
    Late: 'Запізнився',
    Active: 'Активний',
    Finished: 'Завершено'
  };

  return labels[status] || status || '—';
}
