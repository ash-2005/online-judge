import { useState } from 'react';
import { Navigate } from 'react-router-dom';
import { post } from '../api';
import { useAuth } from '../auth';

const emptyProblem = {
  title: '',
  slug: '',
  statement: '',
  difficulty: 'EASY',
  timeLimitMs: 2000,
  memoryLimitMb: 256,
  tags: '',
};

const emptyTestcase = {
  problemId: '',
  input: '',
  expectedOutput: '',
  sample: false,
};

export default function Admin() {
  const { user, isAuthenticated, loading } = useAuth();
  const [problem, setProblem] = useState(emptyProblem);
  const [testcase, setTestcase] = useState(emptyTestcase);
  const [problemMsg, setProblemMsg] = useState('');
  const [testcaseMsg, setTestcaseMsg] = useState('');
  const [problemErr, setProblemErr] = useState('');
  const [testcaseErr, setTestcaseErr] = useState('');
  const [busyProblem, setBusyProblem] = useState(false);
  const [busyTestcase, setBusyTestcase] = useState(false);

  if (loading) return <p className="muted">Loading…</p>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role !== 'ADMIN') {
    return (
      <div>
        <div className="page-header">
          <h1>Admin</h1>
          <p className="error">Admin role required.</p>
        </div>
      </div>
    );
  }

  async function onCreateProblem(e) {
    e.preventDefault();
    setBusyProblem(true);
    setProblemMsg('');
    setProblemErr('');
    try {
      const tags = problem.tags
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean);
      const created = await post('/api/admin/problems', {
        title: problem.title.trim(),
        slug: problem.slug.trim(),
        statement: problem.statement,
        difficulty: problem.difficulty,
        timeLimitMs: Number(problem.timeLimitMs),
        memoryLimitMb: Number(problem.memoryLimitMb),
        tags,
      });
      setProblemMsg(`Created problem #${created?.id ?? created?.slug ?? ''}`);
      setProblem(emptyProblem);
      if (created?.id != null) {
        setTestcase((t) => ({ ...t, problemId: String(created.id) }));
      }
    } catch (err) {
      setProblemErr(err.message);
    } finally {
      setBusyProblem(false);
    }
  }

  async function onAddTestcase(e) {
    e.preventDefault();
    setBusyTestcase(true);
    setTestcaseMsg('');
    setTestcaseErr('');
    try {
      const created = await post('/api/admin/testcases', {
        problemId: Number(testcase.problemId),
        input: testcase.input,
        expectedOutput: testcase.expectedOutput,
        sample: !!testcase.sample,
      });
      setTestcaseMsg(`Added testcase #${created?.id ?? ''}`);
      setTestcase((t) => ({ ...emptyTestcase, problemId: t.problemId }));
    } catch (err) {
      setTestcaseErr(err.message);
    } finally {
      setBusyTestcase(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>Admin</h1>
        <p className="muted">Create problems and test cases.</p>
      </div>

      <div className="problem-layout">
        <div className="panel">
          <p className="panel-title">Create problem</p>
          <form className="stack" onSubmit={onCreateProblem}>
            <div className="field">
              <label>Title</label>
              <input
                value={problem.title}
                onChange={(e) => setProblem((p) => ({ ...p, title: e.target.value }))}
                required
              />
            </div>
            <div className="field">
              <label>Slug</label>
              <input
                value={problem.slug}
                onChange={(e) => setProblem((p) => ({ ...p, slug: e.target.value }))}
                required
              />
            </div>
            <div className="field">
              <label>Statement</label>
              <textarea
                rows={8}
                value={problem.statement}
                onChange={(e) => setProblem((p) => ({ ...p, statement: e.target.value }))}
                required
              />
            </div>
            <div className="row">
              <div className="field" style={{ flex: 1 }}>
                <label>Difficulty</label>
                <select
                  value={problem.difficulty}
                  onChange={(e) => setProblem((p) => ({ ...p, difficulty: e.target.value }))}
                >
                  <option value="EASY">EASY</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HARD">HARD</option>
                </select>
              </div>
              <div className="field" style={{ flex: 1 }}>
                <label>Time limit (ms)</label>
                <input
                  type="number"
                  min={100}
                  value={problem.timeLimitMs}
                  onChange={(e) => setProblem((p) => ({ ...p, timeLimitMs: e.target.value }))}
                  required
                />
              </div>
              <div className="field" style={{ flex: 1 }}>
                <label>Memory (MB)</label>
                <input
                  type="number"
                  min={16}
                  value={problem.memoryLimitMb}
                  onChange={(e) => setProblem((p) => ({ ...p, memoryLimitMb: e.target.value }))}
                  required
                />
              </div>
            </div>
            <div className="field">
              <label>Tags (comma-separated)</label>
              <input
                value={problem.tags}
                onChange={(e) => setProblem((p) => ({ ...p, tags: e.target.value }))}
                placeholder="arrays, dp"
              />
            </div>
            {problemMsg && <p className="success">{problemMsg}</p>}
            {problemErr && <p className="error">{problemErr}</p>}
            <button className="btn" type="submit" disabled={busyProblem}>
              {busyProblem ? 'Creating…' : 'Create problem'}
            </button>
          </form>
        </div>

        <div className="panel">
          <p className="panel-title">Add testcase</p>
          <form className="stack" onSubmit={onAddTestcase}>
            <div className="field">
              <label>Problem ID</label>
              <input
                type="number"
                value={testcase.problemId}
                onChange={(e) => setTestcase((t) => ({ ...t, problemId: e.target.value }))}
                required
              />
            </div>
            <div className="field">
              <label>Input</label>
              <textarea
                rows={5}
                value={testcase.input}
                onChange={(e) => setTestcase((t) => ({ ...t, input: e.target.value }))}
                required
                className="mono"
              />
            </div>
            <div className="field">
              <label>Expected output</label>
              <textarea
                rows={5}
                value={testcase.expectedOutput}
                onChange={(e) => setTestcase((t) => ({ ...t, expectedOutput: e.target.value }))}
                required
                className="mono"
              />
            </div>
            <div className="field row">
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <input
                  type="checkbox"
                  checked={testcase.sample}
                  onChange={(e) => setTestcase((t) => ({ ...t, sample: e.target.checked }))}
                />
                Sample testcase
              </label>
            </div>
            {testcaseMsg && <p className="success">{testcaseMsg}</p>}
            {testcaseErr && <p className="error">{testcaseErr}</p>}
            <button className="btn" type="submit" disabled={busyTestcase}>
              {busyTestcase ? 'Adding…' : 'Add testcase'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
