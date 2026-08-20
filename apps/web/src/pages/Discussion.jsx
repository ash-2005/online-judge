import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { get, post } from '../api';
import { useAuth } from '../auth';

export default function Discussion() {
  const { id } = useParams();
  const { isAuthenticated } = useAuth();
  const [problem, setProblem] = useState(null);
  const [posts, setPosts] = useState([]);
  const [content, setContent] = useState('');
  const [parentId, setParentId] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function load() {
    const [p, d] = await Promise.all([
      get(`/api/problems/${id}`),
      get(`/api/problems/${id}/discussions`),
    ]);
    setProblem(p);
    setPosts(d || []);
  }

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        await load();
      } catch (e) {
        if (!cancelled) setError(e.message);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [id]);

  async function onPost(e) {
    e.preventDefault();
    if (!isAuthenticated) {
      setError('Log in to post.');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const body = { content: content.trim() };
      if (parentId != null) body.parentId = parentId;
      await post(`/api/problems/${id}/discussions`, body);
      setContent('');
      setParentId(null);
      await load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function onUpvote(discussionId) {
    if (!isAuthenticated) {
      setError('Log in to upvote.');
      return;
    }
    try {
      const updated = await post(`/api/discussions/${discussionId}/upvote`, {});
      setPosts((list) => list.map((d) => (d.id === updated.id ? updated : d)));
    } catch (err) {
      setError(err.message);
    }
  }

  function onReply(d) {
    setParentId(d.id);
    setError('');
  }

  return (
    <div>
      <div className="page-header">
        <h1>Discussion</h1>
        {problem && (
          <p>
            <Link to={`/problems/${problem.slug || problem.id}`}>{problem.title}</Link>
          </p>
        )}
      </div>

      {error && <p className="error">{error}</p>}

      <div className="panel" style={{ marginBottom: '1rem' }}>
        <form className="stack" onSubmit={onPost}>
          <div className="field">
            <label>{parentId != null ? `Reply to #${parentId}` : 'New post'}</label>
            <textarea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder={
                parentId != null
                  ? 'Write your reply…'
                  : 'Share an approach or ask a question'
              }
              required
              style={{ fontFamily: 'inherit' }}
            />
          </div>
          <div className="row">
            <button className="btn" type="submit" disabled={busy || !content.trim()}>
              {busy ? 'Posting…' : parentId != null ? 'Post reply' : 'Post'}
            </button>
            {parentId != null && (
              <button
                type="button"
                className="btn btn-ghost btn-sm"
                onClick={() => setParentId(null)}
              >
                Cancel reply
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="panel">
        {posts.length === 0 ? (
          <p className="empty">No discussion yet.</p>
        ) : (
          posts.map((d) => (
            <div
              key={d.id}
              className={`discussion-item${d.parentId != null ? ' nested' : ''}`}
              style={d.parentId != null ? { marginLeft: '1.5rem' } : undefined}
            >
              <div className="discussion-meta">
                <strong>{d.username}</strong>
                {' · '}
                {d.createdAt ? new Date(d.createdAt).toLocaleString() : ''}
                {' · '}
                {d.upvotes} upvotes
                {d.parentId != null && (
                  <span className="muted"> · reply to #{d.parentId}</span>
                )}
              </div>
              <div style={{ whiteSpace: 'pre-wrap' }}>{d.content}</div>
              <div className="row" style={{ marginTop: '0.5rem' }}>
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => onUpvote(d.id)}
                >
                  Upvote
                </button>
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => onReply(d)}
                >
                  Reply
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
