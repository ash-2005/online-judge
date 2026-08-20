import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Problems from './pages/Problems';
import ProblemDetail from './pages/ProblemDetail';
import Profile from './pages/Profile';
import Submissions from './pages/Submissions';
import Discussion from './pages/Discussion';
import Companies from './pages/Companies';
import CompanyProblems from './pages/CompanyProblems';
import WarRoomLobby from './pages/WarRoomLobby';
import WarRoomLive from './pages/WarRoomLive';
import Admin from './pages/Admin';
import Leaderboard from './pages/Leaderboard';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route path="problems" element={<Problems />} />
        <Route path="problems/:idOrSlug" element={<ProblemDetail />} />
        <Route path="problems/:id/discussion" element={<Discussion />} />
        <Route path="profile" element={<Profile />} />
        <Route path="submissions" element={<Submissions />} />
        <Route path="companies" element={<Companies />} />
        <Route path="companies/:name" element={<CompanyProblems />} />
        <Route path="warrooms" element={<WarRoomLobby />} />
        <Route path="warrooms/:code" element={<WarRoomLive />} />
        <Route path="admin" element={<Admin />} />
        <Route path="leaderboard" element={<Leaderboard />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
