import React, { useState } from 'react';
import { Globe2 } from 'lucide-react';

export default function LoginView({ t, lang, setLang, onLogin, notice, setNotice }) {
  const [email, setEmail] = useState('newadmin@test.com');
  const [password, setPassword] = useState('123456');
  const [loading, setLoading] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setLoading(true);
    setNotice(null);

    try {
      await onLogin(email, password);
    } catch (error) {
      setNotice({ type: 'error', text: error.message });
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <section className="login-art">
        <div className="hexagon">HT</div>
        <h1>HiveTrack</h1>
        <p>{t.appSubtitle}</p>
      </section>

      <form className="login-card" onSubmit={submit}>
        <button
          type="button"
          className="pill lang"
          onClick={() => setLang(lang === 'uk' ? 'en' : 'uk')}
        >
          <Globe2 size={16} />
          {lang === 'uk' ? 'EN' : 'UA'}
        </button>

        <h2>{t.loginTitle}</h2>
        <p>{t.demoHint}</p>

        {notice && <div className={`notice ${notice.type}`}>{notice.text}</div>}

        <label className="field">
          <span>{t.email}</span>
          <input value={email} onChange={(event) => setEmail(event.target.value)} />
        </label>

        <label className="field">
          <span>{t.password}</span>
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>

        <button className="primary" disabled={loading}>
          {loading ? '...' : t.signIn}
        </button>
      </form>
    </div>
  );
}
