import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE, get, post } from '../api';
import { useAuth } from '../auth';

const TEMPLATES = {
  JAVA: `public class Main {
  public static void main(String[] args) {
  }
}
`,
  PYTHON: `# solve here
`,
  CPP: `#include <bits/stdc++.h>
using namespace std;
int main() { return 0; }
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

function formatEvent(event) {
  const type = event?.type || event?.eventType || 'EVENT';
  const user = event?.username || (event?.userId != null ? `user #${event.userId}` : null);
  const status = event?.status;
  const submissionId = event?.submissionId;
  const winnerId = event?.winnerId;

  switch (type) {
    case 'JOIN':
      return `${user || 'Someone'} joined the room`;
    case 'STATUS':
      return `Room status → ${status || event?.roomStatus || 'updated'}`;
    case 'SUBMISSION_UPDATE':
      return `${user || 'A player'} submission #${submissionId ?? '?'} → ${status || 'updated'}`;
    case 'WINNER':
      return `Winner: ${user || (winnerId != null ? `user #${winnerId}` : 'decided')}`;
    default:
      return typeof event === 'string' ? event : JSON.stringify(event);
  }
}

export default function WarRoomLive() {
  const { code } = useParams();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const [problem, setProblem] = useState(null);
  const [language, setLanguage] = useState('JAVA');
  const [source, setSource] = useState(TEMPLATES.JAVA);
  const [error, setError] = useState('');
  const [verdict, setVerdict] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [events, setEvents] = useState([]);
  const [winnerId, setWinnerId] = useState(null);
  const pollRef = useRef(null);
  const stompRef = useRef(null);
  const roomIdRef = useRef(null);

  const pushEvent = useCallback((raw) => {
    const entry = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      at: new Date().toISOString(),
      type: raw?.type || raw?.eventType || 'EVENT',
      text: formatEvent(raw),
      raw,
    };
    setEvents((list) => [entry, ...list].slice(0, 50));

    if (entry.type === 'WINNER' || raw?.winnerId != null) {
      setWinnerId((prev) => raw.winnerId ?? prev);
    }
    if (raw?.room) {
      setRoom(raw.room);
      if (raw.room.winnerId != null) setWinnerId(raw.room.winnerId);
    }
    if (entry.type === 'STATUS' && raw?.status) {
      setRoom((r) => (r ? { ...r, status: raw.status } : r));
    }
  }, []);

  const refreshRoom = useCallback(async () => {
    try {
      const found = await get(`/api/warrooms/code/${encodeURIComponent(code)}`);
      setRoom(found);
      if (found?.winnerId != null) setWinnerId(found.winnerId);
      return found;
    } catch {
      try {
        const joined = await post(`/api/warrooms/${encodeURIComponent(code)}/join`, {});
        setRoom(joined);
        if (joined?.winnerId != null) setWinnerId(joined.winnerId);
        return joined;
      } catch (e) {
        setError(e.message);
      }
    }
    return null;
  }, [code]);

  useEffect(() => {
    if (!isAuthenticated) return;
    let cancelled = false;

    (async () => {
      try {
        let r;
        try {
          r = await get(`/api/warrooms/code/${encodeURIComponent(code)}`);
        } catch {
          r = await post(`/api/warrooms/${encodeURIComponent(code)}/join`, {});
        }
        if (cancelled || !r) return;
        setRoom(r);
        if (r.winnerId != null) setWinnerId(r.winnerId);
        const p = await get(`/api/problems/${r.problemId}`);
        if (!cancelled) setProblem(p);
      } catch (e) {
        if (!cancelled) setError(e.message);
      }
    })();

    return () => {
      cancelled = true;
      if (pollRef.current) clearInterval(pollRef.current);
    };
  }, [code, isAuthenticated]);

  useEffect(() => {
    if (!room?.id || !isAuthenticated) return;
    if (roomIdRef.current === room.id && stompRef.current) return;

    if (stompRef.current) {
      try {
        stompRef.current.deactivate();
      } catch {
        /* ignore */
      }
      stompRef.current = null;
    }

    roomIdRef.current = room.id;
    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE}/ws`),
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe(`/topic/warroom/${room.id}`, (message) => {
          try {
            const body = JSON.parse(message.body);
            pushEvent(body);
            if (body?.type === 'WINNER' || body?.eventType === 'WINNER' || body?.winnerId != null) {
              refreshRoom();
            }
            if (body?.type === 'STATUS' || body?.eventType === 'STATUS') {
              refreshRoom();
            }
          } catch {
            pushEvent({ type: 'EVENT', message: message.body });
          }
        });
      },
      onStompError: (frame) => {
        setError(frame.headers?.message || 'WebSocket error');
      },
    });

    client.activate();
    stompRef.current = client;

    return () => {
      roomIdRef.current = null;
      try {
        client.deactivate();
      } catch {
        /* ignore */
      }
      if (stompRef.current === client) stompRef.current = null;
    };
  }, [room?.id, isAuthenticated, pushEvent, refreshRoom]);

  function onLanguageChange(next) {
    setLanguage(next);
    setSource(TEMPLATES[next] || '');
  }

  function pollVerdict(submissionId) {
    if (pollRef.current) clearInterval(pollRef.current);
    pollRef.current = setInterval(async () => {
      try {
        const s = await get(`/api/submissions/${submissionId}`);
        setVerdict(s);
        if (FINAL.has(s.status)) {
          clearInterval(pollRef.current);
          pollRef.current = null;
          setSubmitting(false);
          refreshRoom();
        }
      } catch (e) {
        clearInterval(pollRef.current);
        pollRef.current = null;
        setSubmitting(false);
        setError(e.message);
      }
    }, 1200);
  }

  async function onSubmit() {
    if (!room || !problem) return;
    setSubmitting(true);
    setError('');
    setVerdict(null);
    try {
      const s = await post('/api/submissions', {
        problemId: problem.id,
        language,
        code: source,
        warRoomId: room.id,
      });
      setVerdict(s);
      if (FINAL.has(s.status)) {
        setSubmitting(false);
        refreshRoom();
      } else {
        pollVerdict(s.id);
      }
    } catch (e) {
      setSubmitting(false);
      setError(e.message);
    }
  }

  const effectiveWinnerId = winnerId ?? room?.winnerId;

  if (authLoading) return <p className="muted">Loading…</p>;
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return (
    <div>
      <div className="page-header">
        <h1>War room {code}</h1>
        {room && (
          <div className="row">
            <span className={`badge badge-status ${room.status}`}>{room.status}</span>
            <span className="muted mono">
              {room.participantCount}/{room.maxParticipants} players
            </span>
            <Link to="/warrooms">Lobby</Link>
          </div>
        )}
      </div>

      {effectiveWinnerId != null && (
        <div className="winner-banner">Winner — user #{effectiveWinnerId}</div>
      )}

      {error && <p className="error">{error}</p>}

      {!room || !problem ? (
        <p className="muted">Loading room…</p>
      ) : (
        <div className="problem-layout">
          <div className="panel">
            <p className="panel-title">Problem</p>
            <h2 style={{ marginTop: 0 }}>
              <Link to={`/problems/${problem.slug || problem.id}`}>{problem.title}</Link>
            </h2>
            <div className="statement">{problem.statement}</div>

            <p className="panel-title" style={{ marginTop: '1.25rem' }}>
              Live feed
            </p>
            <div className="event-feed">
              {events.length === 0 ? (
                <p className="muted empty">Waiting for events…</p>
              ) : (
                events.map((ev) => (
                  <div key={ev.id} className="event-item">
                    <span className={`badge badge-status ${ev.type}`}>{ev.type}</span>
                    <span>{ev.text}</span>
                    <span className="muted mono event-time">
                      {new Date(ev.at).toLocaleTimeString()}
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="panel">
            <p className="panel-title">Your solution</p>
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
                disabled={submitting}
                onClick={onSubmit}
                style={{ alignSelf: 'end' }}
              >
                {submitting ? 'Judging…' : 'Submit to room'}
              </button>
            </div>
            <div className="editor-wrap">
              <Editor
                height="360px"
                theme="vs-dark"
                language={language === 'CPP' ? 'cpp' : language.toLowerCase()}
                value={source}
                onChange={(v) => setSource(v ?? '')}
                options={{
                  minimap: { enabled: false },
                  fontSize: 14,
                  fontFamily: 'IBM Plex Mono, monospace',
                  scrollBeyondLastLine: false,
                }}
              />
            </div>
            {verdict && (
              <div className="verdict-box">
                <div>
                  Status:{' '}
                  <span className={`badge badge-status ${verdict.status}`}>{verdict.status}</span>
                </div>
                {verdict.runtimeMs != null && <div>Runtime: {verdict.runtimeMs} ms</div>}
                {verdict.errorMessage && <div>Error: {verdict.errorMessage}</div>}
              </div>
            )}
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              style={{ marginTop: '0.75rem' }}
              onClick={() => navigate('/warrooms')}
            >
              Leave to lobby
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
