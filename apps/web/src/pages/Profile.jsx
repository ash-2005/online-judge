import { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { get, patch } from '../api';
import { useAuth } from '../auth';

export default function Profile() {
  const { isAuthenticated, loading, updateUser } = useAuth();
  const [form, setForm] = useState({ fullName: '', email: '', dob: '' });
  const [username, setUsername] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [stats, setStats] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) return;
    let cancelled = false;
    (async () => {
      try {
        const me = await get('/api/users/me');
        if (cancelled) return;
        setUsername(me.username);
        setForm({
          fullName: me.fullName || '',
          email: me.email || '',
          dob: me.dob || '',
        });
        updateUser(me);
      } catch (e) {
        if (!cancelled) setError(e.message);
      }

      try {
        const s = await get('/api/users/me/stats');
        if (!cancelled) setStats(s);
      } catch (e) {
        if (!cancelled && e.status !== 404) {
          setStats(null);
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [isAuthenticated]);

  if (loading) return <p className="muted">Loading…</p>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  async function onSubmit(e) {
    e.preventDefault();
    setBusy(true);
    setMessage('');
    setError('');
    try {
      const body = {
        fullName: form.fullName.trim() || null,
        email: form.email.trim() || null,
        dob: form.dob || null,
      };
      const updated = await patch('/api/users/me', body);
      updateUser(updated);
      setMessage('Profile updated.');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  const easy = stats?.easy ?? stats?.easySolved ?? 0;
  const medium = stats?.medium ?? stats?.mediumSolved ?? 0;
  const hard = stats?.hard ?? stats?.hardSolved ?? 0;
  const total = stats?.total ?? stats?.totalSolved ?? easy + medium + hard;

  return (
    <div>
      <div className="page-header">
        <h1>Profile</h1>
        <p className="muted">Signed in as {username}</p>
      </div>

      {stats && (
        <div className="stats-row">
          <div className="stat">
            <div className="stat-value">{easy}</div>
            <div className="stat-label">Easy</div>
          </div>
          <div className="stat">
            <div className="stat-value">{medium}</div>
            <div className="stat-label">Medium</div>
          </div>
          <div className="stat">
            <div className="stat-value">{hard}</div>
            <div className="stat-label">Hard</div>
          </div>
          <div className="stat">
            <div className="stat-value">{total}</div>
            <div className="stat-label">Total solved</div>
          </div>
        </div>
      )}

      <div className="auth-box" style={{ margin: 0 }}>
        <form onSubmit={onSubmit}>
          <div className="field">
            <label>Full name</label>
            <input
              value={form.fullName}
              onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
            />
          </div>
          <div className="field">
            <label>Email</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            />
          </div>
          <div className="field">
            <label>Date of birth</label>
            <input
              type="date"
              value={form.dob || ''}
              onChange={(e) => setForm((f) => ({ ...f, dob: e.target.value }))}
            />
          </div>
          {message && <p className="success">{message}</p>}
          {error && <p className="error">{error}</p>}
          <button className="btn" type="submit" disabled={busy}>
            {busy ? 'Saving…' : 'Save changes'}
          </button>
        </form>
      </div>
    </div>
  );
}
