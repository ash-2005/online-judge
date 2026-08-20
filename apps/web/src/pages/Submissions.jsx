import { useEffect, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { get } from '../api';
import { useAuth } from '../auth';

export default function Submissions() {
  const { isAuthenticated, loading } = useAuth();
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isAuthenticated) return;
    let cancelled = false;
    (async () => {
      try {
        const list = await get('/api/users/me/submissions');
        if (!cancelled) setItems(list || []);
      } catch (e) {
        if (!cancelled) setError(e.message);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [isAuthenticated]);

  if (loading) return <p className="muted">Loading…</p>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return (
    <div>
      <div className="page-header">
        <h1>My submissions</h1>
        <p>Recent attempts across all problems.</p>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Problem</th>
              <th>Language</th>
              <th>Status</th>
              <th>Runtime</th>
              <th>Submitted</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 ? (
              <tr>
                <td colSpan={6} className="empty">
                  No submissions yet.
                </td>
              </tr>
            ) : (
              items.map((s) => (
                <tr key={s.id}>
                  <td className="mono">{s.id}</td>
                  <td>
                    <Link to={`/problems/${s.problemId}`}>{s.problemTitle || s.problemId}</Link>
                  </td>
                  <td className="mono">{s.language}</td>
                  <td>
                    <span className={`badge badge-status ${s.status}`}>{s.status}</span>
                  </td>
                  <td className="mono muted">
                    {s.runtimeMs != null ? `${s.runtimeMs} ms` : '—'}
                  </td>
                  <td className="muted">
                    {s.submittedAt ? new Date(s.submittedAt).toLocaleString() : '—'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
