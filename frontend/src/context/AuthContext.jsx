import { createContext, useContext, useState } from 'react';
import api from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('user'));
    } catch {
      return null;
    }
  });

  const save = (data) => {
    const u = { id: data.id, username: data.username, role: data.role };
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(u));
    setUser(u);
  };

  const login = async (username, password) => {
    const { data } = await api.post('/auth/login', { username, password });
    save(data);
  };

  const register = async (username, email, password) => {
    const { data } = await api.post('/auth/register', { username, email, password });
    save(data);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>{children}</AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
