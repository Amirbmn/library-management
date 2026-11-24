import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <span className="brand">Library</span>
      {user && (
        <>
          <Link to="/books">Books</Link>
          {user.role === 'USER' && <Link to="/my">My Loans</Link>}
          {user.role === 'ADMIN' && <Link to="/admin/books">Manage Books</Link>}
          <span className="spacer" />
          <span className="who">
            {user.username} ({user.role})
          </span>
          <button className="btn small" onClick={handleLogout}>
            Logout
          </button>
        </>
      )}
      {!user && (
        <>
          <span className="spacer" />
          <Link to="/login">Login</Link>
          <Link to="/register">Register</Link>
        </>
      )}
    </nav>
  );
}
