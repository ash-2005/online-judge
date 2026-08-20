import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { get } from '../api';

function DifficultyBadge({ difficulty }) {
  const d = (difficulty || '').toLowerCase();
  return <span className={`badge badge-${d}`}>{difficulty}</span>;
}

export default function Home() {
  const [stats, setStats] = useState(null);
  const [recent, setRecent] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [s, r] = await Promise.all([
          get('/api/stats/summary'),
          get('/api/problems/recent'),
        ]);
        if (!cancelled) {
          setStats(s);
          setRecent(r || []);
        }
      } catch (e) {
        if (!cancelled) setError(e.message);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div>
      <div className="page-header">
        <h1>Practice. Submit. Compete.</h1>
        <p>Solve coding problems, track submissions, and join war rooms.</p>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="stats-row">
        <div className="stat">
          <div className="stat-value">{stats?.problemCount ?? '—'}</div>
          <div className="stat-label">Problems</div>
        </div>
        <div className="stat">
          <div className="stat-value">{stats?.submissionCount ?? '—'}</div>
          <div className="stat-label">Submissions</div>
        </div>
        <div className="stat">
          <div className="stat-value">{stats?.activeWarRooms ?? '—'}</div>
          <div className="stat-label">Active war rooms</div>
        </div>
      </div>

      <h2>Recent problems</h2>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Difficulty</th>
              <th>Tags</th>
            </tr>
          </thead>
          <tbody>
            {recent.length === 0 ? (
              <tr>
                <td colSpan={3} className="empty">
                  No problems yet.
                </td>
              </tr>
            ) : (
              recent.map((p) => (
                <tr key={p.id}>
                  <td>
                    <Link to={`/problems/${p.slug || p.id}`}>{p.title}</Link>
                  </td>
                  <td>
                    <DifficultyBadge difficulty={p.difficulty} />
                  </td>
                  <td>
                    <div className="tags">
                      {(p.tags || []).slice(0, 4).map((t) => (
                        <span key={t} className="tag">
                          {t}
                        </span>
                      ))}
                    </div>
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

export { DifficultyBadge };
