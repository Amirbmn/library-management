import { useCallback, useEffect, useState } from 'react';
import api, { errMsg } from '../api/client';
import Message from '../components/Message';

const money = (n) => Number(n || 0).toFixed(2);

export default function MyLoans() {
  const [loans, setLoans] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [fines, setFines] = useState({ unpaidReturned: 0, accruedOverdue: 0, total: 0 });
  const [msg, setMsg] = useState(null);

  const load = useCallback(async () => {
    try {
      const [l, r, f] = await Promise.all([
        api.get('/loans/me'),
        api.get('/reservations/me'),
        api.get('/fines/me'),
      ]);
      setLoans(l.data);
      setReservations(r.data);
      setFines(f.data);
    } catch (e) {
      setMsg({ type: 'error', text: errMsg(e) });
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const act = async (fn, okText) => {
    try {
      await fn();
      setMsg({ type: 'success', text: okText });
    } catch (e) {
      setMsg({ type: 'error', text: errMsg(e) });
    }
    load();
  };

  const active = loans.filter((l) => l.status === 'ACTIVE');
  const history = loans.filter((l) => l.status === 'RETURNED');

  return (
    <div>
      <h2>My Loans</h2>
      <Message msg={msg} onClose={() => setMsg(null)} />

      <section className="card">
        <h3>Fines</h3>
        <p>
          Unpaid fines (returned books): <b>{money(fines.unpaidReturned)}</b>
        </p>
        <p>
          Accruing on overdue books: <b>{money(fines.accruedOverdue)}</b>
        </p>
        <p>
          Total debt: <b>{money(fines.total)}</b>
        </p>
        {Number(fines.unpaidReturned) > 0 && (
          <button className="btn primary" onClick={() => act(() => api.post('/fines/pay'), 'Fines paid')}>
            Pay fines
          </button>
        )}
        {Number(fines.accruedOverdue) > 0 && (
          <p className="muted">Return your overdue books first; their fine becomes payable after return.</p>
        )}
      </section>

      <h3>Currently borrowed ({active.length}/3)</h3>
      <table className="table">
        <thead>
          <tr>
            <th>Book</th>
            <th>Borrowed</th>
            <th>Due</th>
            <th>Status</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {active.map((l) => (
            <tr key={l.id}>
              <td>
                {l.bookTitle} <span className="muted">— {l.bookAuthor}</span>
              </td>
              <td>{l.borrowDate}</td>
              <td>{l.dueDate}</td>
              <td>
                {l.overdue ? (
                  <span className="badge bad">
                    Overdue {l.daysLate}d (fine {money(l.fine)})
                  </span>
                ) : (
                  <span className="badge ok">On time</span>
                )}
              </td>
              <td>
                <button
                  className="btn small primary"
                  onClick={() => act(() => api.post(`/loans/${l.id}/return`), `Returned "${l.bookTitle}"`)}
                >
                  Return
                </button>
              </td>
            </tr>
          ))}
          {active.length === 0 && (
            <tr>
              <td colSpan="5" className="muted center">
                No active loans
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <h3>Reservation queue</h3>
      <table className="table">
        <thead>
          <tr>
            <th>Book</th>
            <th>Position</th>
            <th>Requested at</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {reservations.map((r) => (
            <tr key={r.id}>
              <td>
                {r.bookTitle} <span className="muted">— {r.bookAuthor}</span>
              </td>
              <td>#{r.position}</td>
              <td>{new Date(r.createdAt).toLocaleString()}</td>
              <td>
                <button
                  className="btn small"
                  onClick={() => act(() => api.delete(`/reservations/${r.id}`), 'Reservation cancelled')}
                >
                  Cancel
                </button>
              </td>
            </tr>
          ))}
          {reservations.length === 0 && (
            <tr>
              <td colSpan="4" className="muted center">
                No reservations
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <h3>History</h3>
      <table className="table">
        <thead>
          <tr>
            <th>Book</th>
            <th>Borrowed</th>
            <th>Due</th>
            <th>Returned</th>
            <th>Fine</th>
          </tr>
        </thead>
        <tbody>
          {history.map((l) => (
            <tr key={l.id}>
              <td>{l.bookTitle}</td>
              <td>{l.borrowDate}</td>
              <td>{l.dueDate}</td>
              <td>{l.returnDate}</td>
              <td>
                {Number(l.fine) > 0 ? (
                  <span className={`badge ${l.finePaid ? 'ok' : 'bad'}`}>
                    {money(l.fine)} {l.finePaid ? '(paid)' : '(unpaid)'}
                  </span>
                ) : (
                  '-'
                )}
              </td>
            </tr>
          ))}
          {history.length === 0 && (
            <tr>
              <td colSpan="5" className="muted center">
                No history yet
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
