import React from 'react';
import {
  BarChart3,
  Bell,
  CalendarDays,
  ClipboardList,
  Database,
  FileClock,
  Globe2,
  GraduationCap,
  LogOut,
  ShieldCheck,
  Users
} from 'lucide-react';
import { roleLabel } from '../utils/format.js';

const menuByRole = {
  Student: [
    ['dashboard', 'dashboard', BarChart3],
    ['groups', 'groups', GraduationCap],
    ['attendance', 'attendance', ClipboardList],
    ['tests', 'tests', ShieldCheck],
    ['notifications', 'notifications', Bell],
    ['profile', 'profile', Users]
  ],
  Instructor: [
    ['dashboard', 'dashboard', BarChart3],
    ['lessons', 'lessons', CalendarDays],
    ['attendance', 'attendance', ClipboardList],
    ['notifications', 'notifications', Bell],
    ['profile', 'profile', Users]
  ],
  Admin: [
    ['dashboard', 'dashboard', BarChart3],
    ['groups', 'groups', GraduationCap],
    ['lessons', 'lessons', CalendarDays],
    ['attendance', 'attendance', ClipboardList],
    ['admin', 'administration', ShieldCheck],
    ['notifications', 'notifications', Bell],
    ['audit', 'audit', FileClock],
    ['backups', 'backups', Database],
    ['profile', 'profile', Users]
  ]
};

export function Layout({ children, user, t, lang, setLang, screen, setScreen, onLogout, locale }) {
  const menu = menuByRole[user.role] || menuByRole.Student;

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">HT</div>
          <div>
            <strong>{t.appName}</strong>
            <span>{t.appSubtitle}</span>
          </div>
        </div>

        <nav className="menu">
          {menu.map(([id, labelKey, Icon]) => (
            <button
              key={id}
              className={screen === id ? 'active' : ''}
              onClick={() => setScreen(id)}
            >
              <Icon size={18} />
              {t[labelKey]}
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="mini-profile">
            <strong>{user.fullName}</strong>
            <span>{roleLabel(user.role, t)} · ID {user.id}</span>
          </div>
          <button className="ghost full" onClick={onLogout}>
            <LogOut size={17} />
            {t.signOut}
          </button>
        </div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div>
            <h1>{t[screen] || t.dashboard}</h1>
            <p>{new Intl.DateTimeFormat(locale, { dateStyle: 'full', timeStyle: 'short' }).format(new Date())}</p>
          </div>

          <div className="top-actions">
            <button className="pill" onClick={() => setLang(lang === 'uk' ? 'en' : 'uk')}>
              <Globe2 size={16} />
              {lang === 'uk' ? 'EN' : 'UA'}
            </button>
            <button className="pill" onClick={() => setScreen('profile')}>
              {roleLabel(user.role, t)}
            </button>
          </div>
        </header>

        {children}
      </main>
    </div>
  );
}
