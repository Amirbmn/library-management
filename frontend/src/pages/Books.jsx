import { useCallback, useEffect, useState } from 'react';
import api, { errMsg } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Pagination from '../components/Pagination';
import Message from '../components/Message';

const PAGE_SIZE = 8;

export default function Books() {
  const { user } = useAuth();
  const isUser = user?.role === 'USER';

  const [filters, setFilters] = useState({ search: '', category: '', availableOnly: false });
  const [applied, setApplied] = useState(filters);
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [categories, setCategories] = useState([]);
  const [msg, setMsg] = useState(null);

  const load = useCallback(async () => {
    try {
      const { data } = await api.get('/books', { params: { ...applied, page, size: PAGE_SIZE } });
      setData(data);
    } catch (e) {
      setMsg({ type: 'error', text: errMsg(e) });
    }
  }, [applied, page]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    api
      .get('/books/categories')
      .then((r) => setCategories(r.data))
      .catch(() => {});
  }, []);

  const search = (e) => {
    e.preventDefault();
    setPage(0);
    setApplied({ ...filters });
  };

  const act = async (fn, okText) => {
    try {
      await fn();
      setMsg({ type: 'success', text: okText });
    } catch (e) {
      setMsg({ type: 'error', text: errMsg(e) });
    }
    load();
  };

  const borrow = (b) => act(() => api.post(`/loans/borrow/${b.id}`), `Borrowed "${b.title}" (due in 14 days)`);
  const reserve = (b) => act(() => api.post(`/reservations/${b.id}`), `You joined the queue for "${b.title}"`);

  return (
    <div>
      <h2>Books</h2>
      <Message msg={msg} onClose={() => setMsg(null)} />

      <form className="card filters" onSubmit={search}>
        <input
          placeholder="Search by title or author"
          value={filters.search}
          onChange={(e) => setFilters({ ...filters, search: e.target.value })}
        />
        <select value={filters.category} onChange={(e) => setFilters({ ...filters, category: e.target.value })}>
          <option value="">All categories</option>
          {categories.map((c) => (
            <option key={c} value={c}>
              {c}
            </option>
          ))}
        </select>
        <label className="check">
          <input
            type="checkbox"
            checked={filters.availableOnly}
            onChange={(e) => setFilters({ ...filters, availableOnly: e.target.checked })}
          />
          Available only
        </label>
        <button className="btn primary" type="submit">
          Search
        </button>
      </form>

      <table className="table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Author</th>
            <th>Category</th>
            <th>ISBN</th>
            <th>Availability</th>
            {isUser && <th>Action</th>}
          </tr>
        </thead>
        <tbody>
          {data.content.map((b) => (
            <tr key={b.id}>
              <td>{b.title}</td>
              <td>{b.author}</td>
              <td>{b.categoryCode}</td>
              <td>{b.isbn}</td>
              <td>
                {b.availableCopies > 0 ? (
                  <span className="badge ok">
                    {b.availableCopies} / {b.totalCopies} available
                  </span>
                ) : (
                  <span className="badge bad">
                    Unavailable{b.waitingCount > 0 ? ` (${b.waitingCount} waiting)` : ''}
                  </span>
                )}
              </td>
              {isUser && (
                <td>
                  {b.borrowedByMe ? (
                    <span className="muted">Borrowed by you</span>
                  ) : b.reservedByMe ? (
                    <span className="muted">In queue</span>
                  ) : b.availableCopies > 0 ? (
                    <button className="btn small primary" onClick={() => borrow(b)}>
                      Borrow
                    </button>
                  ) : (
                    <button className="btn small" onClick={() => reserve(b)}>
                      Reserve
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
          {data.content.length === 0 && (
            <tr>
              <td colSpan={isUser ? 6 : 5} className="muted center">
                No books found
              </td>
            </tr>
          )}
        </tbody>
      </table>
      <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />
    </div>
  );
}
