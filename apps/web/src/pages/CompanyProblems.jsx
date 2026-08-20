import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { get } from '../api';
import { DifficultyBadge } from './Home';

export default function CompanyProblems() {
  const { name } = useParams();
  const decoded = decodeURIComponent(name);
  const [problems, setProblems] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const list = await get(`/api/companies/${encodeURIComponent(decoded)}/problems`);
        if (!cancelled) setProblems(list || []);
      } catch (e) {
        if (!cancelled) setError(e.message);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [decoded]);

  return (
    <div>
      <div className="page-header">
        <h1>{decoded}</h1>
        <p>
          <Link to="/companies">All companies</Link>
        </p>
      </div>
      {error && <p className="error">{error}</p>}
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
            {problems.length === 0 ? (
              <tr>
                <td colSpan={3} className="empty">
                  No problems for this company.
                </td>
              </tr>
            ) : (
              problems.map((p) => (
                <tr key={p.id}>
                  <td>
                    <Link to={`/problems/${p.slug || p.id}`}>{p.title}</Link>
                  </td>
                  <td>
                    <DifficultyBadge difficulty={p.difficulty} />
                  </td>
                  <td>
                    <div className="tags">
                      {(p.tags || []).map((t) => (
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
