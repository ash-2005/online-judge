import { Outlet } from 'react-router-dom';
import Nav from './Nav';

export default function Layout({ wide }) {
  return (
    <div className="layout">
      <Nav />
      <main className={wide ? 'main wide' : 'main'}>
        <Outlet />
      </main>
    </div>
  );
}
