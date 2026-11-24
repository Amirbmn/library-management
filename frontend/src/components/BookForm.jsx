import { useEffect, useState } from 'react';

const empty = { title: '', author: '', isbn: '', categoryCode: '', totalCopies: 1 };

export default function BookForm({ editing, onSubmit, onCancel }) {
  const [form, setForm] = useState(empty);

  useEffect(() => {
    setForm(editing ? { ...editing } : empty);
  }, [editing]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = (e) => {
    e.preventDefault();
    onSubmit({
      title: form.title,
      author: form.author,
      isbn: form.isbn,
      categoryCode: form.categoryCode,
      totalCopies: Number(form.totalCopies),
    });
  };

  return (
    <form className="card form" onSubmit={submit}>
      <h3>{editing ? 'Edit book' : 'Add new book'}</h3>
      <input placeholder="Title" value={form.title} onChange={set('title')} required />
      <input placeholder="Author" value={form.author} onChange={set('author')} required />
      <input placeholder="ISBN" value={form.isbn} onChange={set('isbn')} required />
      <input placeholder="Category code" value={form.categoryCode} onChange={set('categoryCode')} required />
      <input
        type="number"
        min="0"
        placeholder="Total copies"
        value={form.totalCopies}
        onChange={set('totalCopies')}
        required
      />
      <div className="row">
        <button className="btn primary" type="submit">
          {editing ? 'Save' : 'Add'}
        </button>
        {editing && (
          <button className="btn" type="button" onClick={onCancel}>
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
