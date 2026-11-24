import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { errMsg } from '../api/client';
import Message from '../components/Message';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [msg, setMsg] = useState(null);

  const submit = async (e) => {
    e.preventDefault();
    try {
      await login(username, password);
      navigate('/books');
    } catch (err) {
      setMsg({ type: 'error', text: errMsg(err) });
    }
  };

  return (
    <div className="auth-box card">
      <h2>Login</h2>
      <Message msg={msg} onClose={() => setMsg(null)} />
      <form className="form" onSubmit={submit}>
        <input placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button className="btn primary" type="submit">
          Login
        </button>
      </form>
      <p>
        No account? <Link to="/register">Register</Link>
      </p>
    </div>
  );
}
