import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { get } from '../api';
import { DifficultyBadge } from './Home';

export default function Problems() {
  const [difficulty, setDifficulty] = useState('');
  const [tag, setTag] = useState('');
  const [company, setCompany] = useState('');
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const params = new URLSearchParams();
        if (difficulty) params.set('difficulty', difficulty);
        if (tag.trim()) params.set('tag', tag.trim());
        if (company.trim()) params.set('company', company.trim());
        if (q.trim()) params.set('q', q.trim());
        params.set('page', String(page));
        const res = await get(`/api/problems?${params}`);
        if (!cancelled) setData(res);
      } catch (e) {
        if (!cancelled) setError(e.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [difficulty, tag, company, q, page]);

  const content = data?.content || [];
  const totalPages = data?.totalPages ?? 0;

  return (
    <div>
      <div className="page-header">
        <h1>Problems</h1>
        <p>Filter by difficulty, tag, company, or search.</p>
      </div>

      <div className="filters">
        <div className="field">
          <label>Search</label>
          <input
            value={q}
            onChange={(e) => {
              setPage(0);
              setQ(e.target.value);
            }}
            placeholder="Title or keyword"
          />
        </div>
        <div className="field">
          <label>Difficulty</label>
          <select
            value={difficulty}
            onChange={(e) => {
              setPage(0);
              setDifficulty(e.target.value);
            }}
          >
            <option value="">All</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
        </div>
        <div className="field">
          <label>Tag</label>
          <input
            value={tag}
            onChange={(e) => {
              setPage(0);
              setTag(e.target.value);
            }}
            placeholder="e.g. dp"
          />
        </div>
        <div className="field">
          <label>Company</label>
          <input
            value={company}
            onChange={(e) => {
              setPage(0);
              setCompany(e.target.value);
            }}
            placeholder="e.g. Google"
          />
        </div>
      </div>

      {error && <p className="error">{error}</p>}
      {loading && <p className="muted">Loading…</p>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Title</th>
              <th>Difficulty</th>
              <th>Tags</th>
              <th>Limits</th>
            </tr>
          </thead>
          <tbody>
            {!loading && content.length === 0 ? (
              <tr>
                <td colSpan={4} className="empty">
                  No problems found.
                </td>
              </tr>
            ) : (
              content.map((p) => (
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
                  <td className="mono muted">
                    {p.timeLimitMs}ms / {p.memoryLimitMb}MB
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="pager">
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          disabled={page <= 0}
          onClick={() => setPage((p) => p - 1)}
        >
          Previous
        </button>
        <span className="muted">
          Page {page + 1}
          {totalPages ? ` / ${totalPages}` : ''}
        </span>
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          disabled={totalPages ? page + 1 >= totalPages : content.length === 0}
          onClick={() => setPage((p) => p + 1)}
        >
          Next
        </button>
      </div>
    </div>
  );
}
