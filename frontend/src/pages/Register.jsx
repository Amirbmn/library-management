import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { errMsg } from '../api/client';
import Message from '../components/Message';

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [msg, setMsg] = useState(null);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    try {
      await register(form.username, form.email, form.password);
      navigate('/books');
    } catch (err) {
      setMsg({ type: 'error', text: errMsg(err) });
    }
  };

  return (
    <div className="auth-box card">
      <h2>Register</h2>
      <Message msg={msg} onClose={() => setMsg(null)} />
      <form className="form" onSubmit={submit}>
        <input placeholder="Username (min 3 chars)" value={form.username} onChange={set('username')} required />
        <input type="email" placeholder="Email" value={form.email} onChange={set('email')} required />
        <input
          type="password"
          placeholder="Password (min 6 chars)"
          value={form.password}
          onChange={set('password')}
          required
        />
        <button className="btn primary" type="submit">
          Create account
        </button>
      </form>
      <p>
        Already registered? <Link to="/login">Login</Link>
      </p>
    </div>
  );
}
