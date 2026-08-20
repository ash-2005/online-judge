import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { get } from '../api';

export default function Companies() {
  const [companies, setCompanies] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const list = await get('/api/companies');
        if (!cancelled) setCompanies(list || []);
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
        <h1>Companies</h1>
        <p>Problems tagged by company interviews.</p>
      </div>
      {error && <p className="error">{error}</p>}
      {companies.length === 0 ? (
        <p className="empty">No company tags yet.</p>
      ) : (
        <div className="company-grid">
          {companies.map((c) => (
            <Link
              key={c.name}
              to={`/companies/${encodeURIComponent(c.name)}`}
              className="company-card"
            >
              <strong>{c.name}</strong>
              <span className="muted">
                {c.problemCount} problem{c.problemCount === 1 ? '' : 's'}
              </span>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
