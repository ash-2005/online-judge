import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { get, post } from '../api';
import { useAuth } from '../auth';
import { DifficultyBadge } from './Home';

const TEMPLATES = {
  JAVA: `public class Main {
  public static void main(String[] args) {
    // your code
  }
}
`,
  PYTHON: `# your code
`,
  CPP: `#include <bits/stdc++.h>
using namespace std;

int main() {
  // your code
  return 0;
}
`,
};

const FINAL = new Set([
  'ACCEPTED',
  'WRONG_ANSWER',
  'TIME_LIMIT_EXCEEDED',
  'MEMORY_LIMIT_EXCEEDED',
  'RUNTIME_ERROR',
  'COMPILATION_ERROR',
]);

export default function ProblemDetail() {
  const { idOrSlug } = useParams();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [problem, setProblem] = useState(null);
  const [language, setLanguage] = useState('JAVA');
  const [code, setCode] = useState(TEMPLATES.JAVA);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [verdict, setVerdict] = useState(null);
  const [company, setCompany] = useState('');
  const [round, setRound] = useState('');
  const [tagMsg, setTagMsg] = useState('');
  const pollRef = useRef(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const p = await get(`/api/problems/${idOrSlug}`);
        if (!cancelled) setProblem(p);
      } catch (e) {
        if (!cancelled) setError(e.message);
      }
    })();
    return () => {
      cancelled = true;
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [idOrSlug]);

  function onLanguageChange(next) {
    setLanguage(next);
    setCode(TEMPLATES[next] || '');
  }

  const pollVerdict = useCallback((submissionId) => {
    if (pollRef.current) clearInterval(pollRef.current);
    pollRef.current = setInterval(async () => {
      try {
        const s = await get(`/api/submissions/${submissionId}`);
        setVerdict(s);
        if (FINAL.has(s.status)) {
          clearInterval(pollRef.current);
          pollRef.current = null;
          setSubmitting(false);
        }
      } catch (e) {
        clearInterval(pollRef.current);
        pollRef.current = null;
        setSubmitting(false);
        setError(e.message);
      }
    }, 1200);
  }, []);

  async function onSubmit() {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!problem) return;
    setError('');
    setSubmitting(true);
    setVerdict(null);
    try {
      const s = await post('/api/submissions', {
        problemId: problem.id,
        language,
        code,
      });
      setVerdict(s);
      if (FINAL.has(s.status)) {
        setSubmitting(false);
      } else {
        pollVerdict(s.id);
      }
    } catch (e) {
      setSubmitting(false);
      setError(e.message);
    }
  }

  async function onAddCompanyTag(e) {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    setTagMsg('');
    try {
      const body = { company: company.trim() };
      if (round.trim()) body.round = round.trim();
      const updated = await post(`/api/problems/${problem.id}/company-tags`, body);
      setProblem((p) => ({ ...p, companyTags: updated.companyTags || p.companyTags }));
      setCompany('');
      setRound('');
      setTagMsg('Company tag added.');
    } catch (err) {
      setTagMsg(err.message);
    }
  }

  if (error && !problem) {
    return <p className="error">{error}</p>;
  }

  if (!problem) {
    return <p className="muted">Loading problem…</p>;
  }

  return (
    <div>
      <div className="page-header">
        <h1>{problem.title}</h1>
        <div className="row">
          <DifficultyBadge difficulty={problem.difficulty} />
          <span className="muted mono">
            {problem.timeLimitMs}ms · {problem.memoryLimitMb}MB
          </span>
          <Link to={`/problems/${problem.id}/discussion`}>Discussion</Link>
        </div>
        <div className="tags">
          {(problem.tags || []).map((t) => (
            <span key={t} className="tag">
              {t}
            </span>
          ))}
          {(problem.companyTags || []).map((ct) => (
            <span key={ct.company} className="tag">
              {ct.company}
              {ct.count ? ` (${ct.count})` : ''}
            </span>
          ))}
        </div>
      </div>

      <div className="problem-layout">
        <div className="panel">
          <p className="panel-title">Statement</p>
          <div className="statement">{problem.statement}</div>
          {(problem.sampleTestCases || []).map((s, i) => (
            <div key={i} className="sample">
              <strong>Sample {i + 1}</strong>
              <div className="muted">Input</div>
              <pre>{s.input}</pre>
              <div className="muted">Expected</div>
              <pre>{s.expectedOutput}</pre>
            </div>
          ))}

          <form className="form-inline" onSubmit={onAddCompanyTag}>
            <div className="field">
              <label>Company</label>
              <input
                value={company}
                onChange={(e) => setCompany(e.target.value)}
                placeholder="Company name"
                required
              />
            </div>
            <div className="field">
              <label>Round</label>
              <input
                value={round}
                onChange={(e) => setRound(e.target.value)}
                placeholder="Optional"
              />
            </div>
            <button type="submit" className="btn btn-ghost btn-sm">
              Add tag
            </button>
          </form>
          {tagMsg && (
            <p className={tagMsg.includes('added') ? 'success' : 'error'}>{tagMsg}</p>
          )}
        </div>

        <div className="panel">
          <p className="panel-title">Solution</p>
          <div className="editor-toolbar">
            <div className="field">
              <label>Language</label>
              <select value={language} onChange={(e) => onLanguageChange(e.target.value)}>
                <option value="JAVA">Java</option>
                <option value="PYTHON">Python</option>
                <option value="CPP">C++</option>
              </select>
            </div>
            <button
              type="button"
              className="btn"
              onClick={onSubmit}
              disabled={submitting}
              style={{ alignSelf: 'end' }}
            >
              {submitting ? 'Judging…' : 'Submit'}
            </button>
          </div>
          <div className="editor-wrap">
            <Editor
              height="360px"
              theme="vs-dark"
              language={language === 'CPP' ? 'cpp' : language.toLowerCase()}
              value={code}
              onChange={(v) => setCode(v ?? '')}
              options={{
                minimap: { enabled: false },
                fontSize: 14,
                fontFamily: 'IBM Plex Mono, monospace',
                scrollBeyondLastLine: false,
              }}
            />
          </div>
          {error && <p className="error">{error}</p>}
          {verdict && (
            <div className="verdict-box">
              <div>
                Status:{' '}
                <span className={`badge badge-status ${verdict.status}`}>{verdict.status}</span>
              </div>
              {verdict.runtimeMs != null && <div>Runtime: {verdict.runtimeMs} ms</div>}
              {verdict.memoryKb != null && <div>Memory: {verdict.memoryKb} KB</div>}
              {verdict.errorMessage && <div>Error: {verdict.errorMessage}</div>}
              <div className="muted">Submission #{verdict.id}</div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
