import React from 'react';
import { Bell, BookOpen, CalendarDays, ClipboardList, FileClock, GraduationCap, Users, Wifi } from 'lucide-react';
import { useApiData } from '../hooks.js';
import { DataState, DetailRows, StatCard, Card } from '../components/ui.jsx';
import { formatDate } from '../utils/format.js';

function dashboardPath(role) {
  if (role === 'Admin') {
    return '/api/admin/dashboard';
  }

  if (role === 'Instructor') {
    return '/api/instructor/me/dashboard';
  }

  return '/api/student/me/dashboard';
}

export default function DashboardView({ api, t, user, locale, setNotice }) {
  const { data, loading, error, reload } = useApiData(api, dashboardPath(user.role), (text) => {
    setNotice({ type: 'error', text });
  });

  if (loading || error || !data) {
    return <DataState loading={loading} error={error} empty={!data} t={t} />;
  }

  if (user.role === 'Admin') {
    return (
      <section className="grid stats">
        <StatCard icon={Users} label="Користувачі" value={data.users} />
        <StatCard icon={GraduationCap} label={t.groups} value={data.groups} />
        <StatCard icon={CalendarDays} label={t.lessons} value={data.lessons} />
        <StatCard icon={ClipboardList} label={t.attendance} value={data.attendanceRecords} />
        <StatCard icon={Wifi} label="IoT" value={data.iotSessions} />
        <StatCard icon={FileClock} label={t.audit} value={data.auditEvents} />
      </section>
    );
  }

  if (user.role === 'Instructor') {
    return (
      <section className="grid stats">
        <StatCard icon={CalendarDays} label="Занять сьогодні" value={data.lessonsToday} />
        <StatCard icon={Users} label="Мої учні" value={data.studentsCount} />
        <StatCard icon={ClipboardList} label="Відміток сьогодні" value={data.attendanceToday} />
        <StatCard icon={Bell} label="Непрочитані" value={data.unreadNotifications} />
      </section>
    );
  }

  return (
    <section className="grid stats">
      <StatCard icon={GraduationCap} label={t.groups} value={data.group?.name || '—'} />
      <StatCard icon={ClipboardList} label={t.attendance} value={`${data.attendance?.rate || 0}%`} />
      <StatCard icon={BookOpen} label="Пройдено тестів" value={data.tests?.completed || 0} />
      <StatCard icon={Bell} label="Непрочитані" value={data.unreadNotifications} />
      <Card title="Найближче заняття">
        {data.nextLesson ? (
          <DetailRows
            rows={{
              Тема: data.nextLesson.topic,
              Група: data.nextLesson.groupName,
              Час: formatDate(data.nextLesson.lessonDateTime, locale)
            }}
          />
        ) : (
          <p className="muted">Найближче заняття ще не заплановане.</p>
        )}
      </Card>
    </section>
  );
}
