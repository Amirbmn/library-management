export default function Message({ msg, onClose }) {
  if (!msg) return null;
  return (
    <div className={`message ${msg.type}`} onClick={onClose}>
      {msg.text}
    </div>
  );
}
