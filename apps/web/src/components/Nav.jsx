import { NavLink, Link } from 'react-router-dom';
import { useAuth } from '../auth';

export default function Nav() {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <nav className="nav">
      <Link to="/" className="nav-brand">
        Online Judge
      </Link>
      <div className="nav-links">
        <NavLink to="/problems" className={({ isActive }) => (isActive ? 'active' : '')}>
          Problems
        </NavLink>
        <NavLink to="/companies" className={({ isActive }) => (isActive ? 'active' : '')}>
          Companies
        </NavLink>
        <NavLink to="/warrooms" className={({ isActive }) => (isActive ? 'active' : '')}>
          War Rooms
        </NavLink>
        <NavLink to="/leaderboard" className={({ isActive }) => (isActive ? 'active' : '')}>
          Leaderboard
        </NavLink>
        {isAuthenticated && (
          <>
            <NavLink to="/submissions" className={({ isActive }) => (isActive ? 'active' : '')}>
              Submissions
            </NavLink>
            <NavLink to="/profile" className={({ isActive }) => (isActive ? 'active' : '')}>
              Profile
            </NavLink>
            {user?.role === 'ADMIN' && (
              <NavLink to="/admin" className={({ isActive }) => (isActive ? 'active' : '')}>
                Admin
              </NavLink>
            )}
          </>
        )}
      </div>
      <div className="nav-right">
        {isAuthenticated ? (
          <>
            <span className="nav-user">{user?.username}</span>
            <button type="button" className="btn btn-ghost btn-sm" onClick={logout}>
              Log out
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="btn btn-ghost btn-sm">
              Log in
            </Link>
            <Link to="/register" className="btn btn-sm">
              Register
            </Link>
          </>
        )}
      </div>
    </nav>
  );
}
