import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { get, post } from '../api';
import { useAuth } from '../auth';

export default function WarRoomLobby() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [problemId, setProblemId] = useState('');
  const [maxParticipants, setMaxParticipants] = useState(4);
  const [joinCode, setJoinCode] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function loadRooms() {
    const list = await get('/api/warrooms?status=WAITING');
    setRooms(list || []);
  }

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        await loadRooms();
      } catch (e) {
        if (!cancelled) setError(e.message);
      }
    })();
    const t = setInterval(() => {
      loadRooms().catch(() => {});
    }, 5000);
    return () => {
      cancelled = true;
      clearInterval(t);
    };
  }, []);

  async function onCreate(e) {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const room = await post('/api/warrooms', {
        problemId: Number(problemId),
        maxParticipants: Number(maxParticipants),
      });
      navigate(`/warrooms/${room.roomCode}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function onJoin(e) {
    e.preventDefault();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    setBusy(true);
    setError('');
    try {
      const code = joinCode.trim();
      const room = await post(`/api/warrooms/${encodeURIComponent(code)}/join`, {});
      navigate(`/warrooms/${room.roomCode || code}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <h1>War rooms</h1>
        <p>Create or join a timed coding duel. Waiting rooms refresh every few seconds.</p>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="problem-layout" style={{ marginBottom: '1.5rem' }}>
        <div className="panel">
          <p className="panel-title">Create room</p>
          <form className="stack" onSubmit={onCreate}>
            <div className="field">
              <label>Problem ID</label>
              <input
                type="number"
                min="1"
                value={problemId}
                onChange={(e) => setProblemId(e.target.value)}
                required
              />
            </div>
            <div className="field">
              <label>Max participants</label>
              <input
                type="number"
                min="2"
                max="20"
                value={maxParticipants}
                onChange={(e) => setMaxParticipants(e.target.value)}
                required
              />
            </div>
            <button className="btn" type="submit" disabled={busy}>
              Create
            </button>
          </form>
        </div>

        <div className="panel">
          <p className="panel-title">Join by code</p>
          <form className="stack" onSubmit={onJoin}>
            <div className="field">
              <label>Room code</label>
              <input
                value={joinCode}
                onChange={(e) => setJoinCode(e.target.value)}
                required
                placeholder="e.g. AB12CD"
              />
            </div>
            <button className="btn" type="submit" disabled={busy}>
              Join
            </button>
          </form>
        </div>
      </div>

      <h2>Waiting rooms</h2>
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Problem</th>
              <th>Players</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {rooms.length === 0 ? (
              <tr>
                <td colSpan={5} className="empty">
                  No waiting rooms.
                </td>
              </tr>
            ) : (
              rooms.map((r) => (
                <tr key={r.id}>
                  <td className="mono">{r.roomCode}</td>
                  <td>
                    <Link to={`/problems/${r.problemId}`}>{r.problemTitle || r.problemId}</Link>
                  </td>
                  <td className="mono">
                    {r.participantCount}/{r.maxParticipants}
                  </td>
                  <td>
                    <span className="badge badge-status">{r.status}</span>
                  </td>
                  <td>
                    <Link className="btn btn-ghost btn-sm" to={`/warrooms/${r.roomCode}`}>
                      Open
                    </Link>
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
