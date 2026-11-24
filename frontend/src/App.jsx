import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Books from './pages/Books';
import MyLoans from './pages/MyLoans';
import AdminBooks from './pages/AdminBooks';

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/books"
            element={
              <ProtectedRoute>
                <Books />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my"
            element={
              <ProtectedRoute role="USER">
                <MyLoans />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/books"
            element={
              <ProtectedRoute role="ADMIN">
                <AdminBooks />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/books" replace />} />
        </Routes>
      </main>
    </>
  );
}
