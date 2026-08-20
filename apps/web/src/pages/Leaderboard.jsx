import { useEffect, useState } from 'react';
import { get } from '../api';

export default function Leaderboard() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const data = await get('/api/leaderboard');
        if (!cancelled) setRows(Array.isArray(data) ? data : data?.entries || []);
      } catch (e) {
        if (!cancelled) setError(e.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div>
      <div className="page-header">
        <h1>Leaderboard</h1>
        <p className="muted">Accepted submission counts by user.</p>
      </div>

      {error && <p className="error">{error}</p>}
      {loading && <p className="muted">Loading…</p>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Username</th>
              <th>Accepted</th>
            </tr>
          </thead>
          <tbody>
            {!loading && rows.length === 0 ? (
              <tr>
                <td colSpan={3} className="empty">
                  No entries yet.
                </td>
              </tr>
            ) : (
              rows.map((row, i) => (
                <tr key={row.userId ?? row.username ?? i}>
                  <td className="mono muted">{i + 1}</td>
                  <td>{row.username}</td>
                  <td className="mono">
                    {row.acceptedCount ?? row.accepted ?? row.solved ?? 0}
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
