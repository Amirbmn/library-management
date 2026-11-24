import { useCallback, useEffect, useState } from 'react';
import api, { errMsg } from '../api/client';
import BookForm from '../components/BookForm';
import Pagination from '../components/Pagination';
import Message from '../components/Message';

export default function AdminBooks() {
  const [search, setSearch] = useState('');
  const [applied, setApplied] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [editing, setEditing] = useState(null);
  const [msg, setMsg] = useState(null);

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/books', { params: { search: applied, page, size: 8 } });
      setData(data);
    } catch (e) {
      setMsg({ type: 'error', text: errMsg(e) });
    }
  }, [applied, page]);

  useEffect(() => {
    load();
  }, [load]);

  const save = async (payload) => {
    try {
      if (editing) {
        await api.put(`/books/${editing.id}`, payload);
        setMsg({ type: 'success', text: 'Book updated' });
      } else {
        await api.post('/books', payload);
        setMsg({ type: 'success', text: 'Book added' });
      }
      setEditing(null);
      load();
    } catch (e) {
      setMsg({ type: 'error', text: errMsg(e) });
    }
  };

  const remove = async (b) => {
    if (!window.confirm(`Delete "${b.title}"?`)) return;
    try {
      await api.delete(`/books/${b.id}`);
      setMsg({ type: 'success', text: 'Book deleted' });
      load();
    } catch (e) {
      setMsg({ type: 'error', text: errMsg(e) });
    }
  };

  return (
    <div>
      <h2>Manage Books</h2>
      <Message msg={msg} onClose={() => setMsg(null)} />
      <div className="admin-grid">
        <div>
          <form
            className="card filters"
            onSubmit={(e) => {
              e.preventDefault();
              setPage(0);
              setApplied(search);
            }}
          >
            <input placeholder="Search title or author" value={search} onChange={(e) => setSearch(e.target.value)} />
            <button className="btn primary" type="submit">
              Search
            </button>
          </form>
          <table className="table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Author</th>
                <th>ISBN</th>
                <th>Category</th>
                <th>Copies</th>
                <th>Waiting</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {data.content.map((b) => (
                <tr key={b.id}>
                  <td>{b.title}</td>
                  <td>{b.author}</td>
                  <td>{b.isbn}</td>
                  <td>{b.categoryCode}</td>
                  <td>
                    {b.availableCopies} / {b.totalCopies}
                  </td>
                  <td>{b.waitingCount}</td>
                  <td className="actions">
                    <button className="btn small" onClick={() => setEditing(b)}>
                      Edit
                    </button>
                    <button className="btn small danger" onClick={() => remove(b)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr>
                  <td colSpan="7" className="muted center">
                    No books found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />
        </div>
        <BookForm editing={editing} onSubmit={save} onCancel={() => setEditing(null)} />
      </div>
    </div>
  );
}
