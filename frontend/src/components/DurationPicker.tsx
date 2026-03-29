import './DurationPicker.css';

interface DurationPickerProps {
  value: number; // total minutes
  onChange: (mins: number) => void;
}

export default function DurationPicker({ value, onChange }: DurationPickerProps) {
  const hours = Math.floor(value / 60);
  const mins = value % 60;

  function setHours(h: number) {
    onChange(Math.max(0, Math.min(23, h)) * 60 + mins);
  }

  function setMins(m: number) {
    // step by 5
    const clamped = Math.max(0, Math.min(55, m));
    onChange(hours * 60 + clamped);
  }

  return (
    <div className="dp">
      {/* Hours */}
      <div className="dp-col">
        <button type="button" className="dp-btn" onClick={() => setHours(hours + 1)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M18 15l-6-6-6 6"/></svg>
        </button>
        <span className="dp-val">{hours}</span>
        <button type="button" className="dp-btn" onClick={() => setHours(hours - 1)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M6 9l6 6 6-6"/></svg>
        </button>
        <span className="dp-label">hr</span>
      </div>

      <span className="dp-sep">:</span>

      {/* Minutes */}
      <div className="dp-col">
        <button type="button" className="dp-btn" onClick={() => setMins(mins + 5)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M18 15l-6-6-6 6"/></svg>
        </button>
        <span className="dp-val">{String(mins).padStart(2, '0')}</span>
        <button type="button" className="dp-btn" onClick={() => setMins(mins - 5)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M6 9l6 6 6-6"/></svg>
        </button>
        <span className="dp-label">min</span>
      </div>
    </div>
  );
}
