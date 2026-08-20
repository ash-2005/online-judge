import { createContext, useContext, useEffect, useState } from 'react';
import { get, post } from './api';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const raw = localStorage.getItem('oj_user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('oj_token'));
  const [user, setUser] = useState(readStoredUser);
  const [loading, setLoading] = useState(!!localStorage.getItem('oj_token'));

  useEffect(() => {
    if (!token) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const me = await get('/api/users/me');
        if (!cancelled) {
          setUser(me);
          localStorage.setItem('oj_user', JSON.stringify(me));
        }
      } catch {
        if (!cancelled) {
          localStorage.removeItem('oj_token');
          localStorage.removeItem('oj_user');
          setToken(null);
          setUser(null);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [token]);

  function persistAuth(auth) {
    localStorage.setItem('oj_token', auth.token);
    localStorage.setItem('oj_user', JSON.stringify(auth.user));
    setToken(auth.token);
    setUser(auth.user);
  }

  async function login(username, password) {
    const auth = await post('/api/auth/login', { username, password });
    persistAuth(auth);
    return auth;
  }

  async function register(payload) {
    const auth = await post('/api/auth/register', payload);
    persistAuth(auth);
    return auth;
  }

  function logout() {
    localStorage.removeItem('oj_token');
    localStorage.removeItem('oj_user');
    setToken(null);
    setUser(null);
  }

  function updateUser(next) {
    setUser(next);
    localStorage.setItem('oj_user', JSON.stringify(next));
  }

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        loading,
        isAuthenticated: !!token,
        login,
        register,
        logout,
        updateUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
