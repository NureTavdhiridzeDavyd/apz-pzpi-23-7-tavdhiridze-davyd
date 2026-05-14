import React, { useMemo, useState } from 'react';
import { createApiClient } from './api/client.js';
import { STORAGE_KEYS } from './config.js';
import { translations, getLocale } from './i18n/translations.js';
import { Layout } from './components/Layout.jsx';
import { Notice } from './components/ui.jsx';
import LoginView from './views/LoginView.jsx';
import DashboardView from './views/DashboardView.jsx';
import GroupsView from './views/GroupsView.jsx';
import LessonsView from './views/LessonsView.jsx';
import AttendanceView from './views/AttendanceView.jsx';
import TestsView from './views/TestsView.jsx';
import NotificationsView from './views/NotificationsView.jsx';
import AdminView from './views/AdminView.jsx';
import AuditView from './views/AuditView.jsx';
import BackupsView from './views/BackupsView.jsx';
import ProfileView from './views/ProfileView.jsx';

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEYS.user) || 'null');
  } catch {
    return null;
  }
}

export default function App() {
  const [lang, setLang] = useState(localStorage.getItem(STORAGE_KEYS.lang) || 'uk');
  const [token, setToken] = useState(localStorage.getItem(STORAGE_KEYS.token) || '');
  const [user, setUser] = useState(readStoredUser());
  const [screen, setScreen] = useState('dashboard');
  const [notice, setNotice] = useState(null);

  const t = translations[lang];
  const locale = getLocale(lang);
  const api = useMemo(() => createApiClient(token), [token]);

  function changeLang(value) {
    localStorage.setItem(STORAGE_KEYS.lang, value);
    setLang(value);
  }

  async function login(email, password) {
    const result = await api.post('/api/auth/login', { email, password }, false);

    localStorage.setItem(STORAGE_KEYS.token, result.token);
    localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(result.user));

    setToken(result.token);
    setUser(result.user);
    setScreen('dashboard');
  }

  function logout() {
    localStorage.removeItem(STORAGE_KEYS.token);
    localStorage.removeItem(STORAGE_KEYS.user);

    setToken('');
    setUser(null);
    setScreen('dashboard');
  }

  if (!token || !user) {
    return (
      <LoginView
        t={t}
        lang={lang}
        setLang={changeLang}
        onLogin={login}
        notice={notice}
        setNotice={setNotice}
      />
    );
  }

  const commonProps = {
    api,
    t,
    user,
    locale,
    lang,
    setNotice
  };

  return (
    <Layout
      user={user}
      t={t}
      lang={lang}
      setLang={changeLang}
      screen={screen}
      setScreen={setScreen}
      onLogout={logout}
      locale={locale}
    >
      <Notice notice={notice} onClose={() => setNotice(null)} />

      {screen === 'dashboard' && <DashboardView {...commonProps} />}
      {screen === 'groups' && <GroupsView {...commonProps} />}
      {screen === 'lessons' && <LessonsView {...commonProps} />}
      {screen === 'attendance' && <AttendanceView {...commonProps} />}
      {screen === 'tests' && <TestsView {...commonProps} />}
      {screen === 'notifications' && <NotificationsView {...commonProps} />}
      {screen === 'admin' && <AdminView {...commonProps} />}
      {screen === 'audit' && <AuditView {...commonProps} />}
      {screen === 'backups' && <BackupsView {...commonProps} />}
      {screen === 'profile' && <ProfileView {...commonProps} />}
    </Layout>
  );
}
