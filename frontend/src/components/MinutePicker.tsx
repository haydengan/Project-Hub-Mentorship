import { useRef, useEffect } from 'react';
import './MinutePicker.css';

const PRESETS = [5, 10, 15, 20, 25, 30, 45, 60, 90, 120];

interface MinutePickerProps {
  value: number | null;
  onChange: (mins: number) => void;
}

export default function MinutePicker({ value, onChange }: MinutePickerProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (value && scrollRef.current) {
      const active = scrollRef.current.querySelector('.mp-pill.active');
      if (active) {
        active.scrollIntoView({ behavior: 'smooth', inline: 'center', block: 'nearest' });
      }
    }
  }, [value]);

  function formatLabel(mins: number) {
    if (mins < 60) return `${mins}m`;
    const h = mins / 60;
    return h === Math.floor(h) ? `${h}h` : `${Math.floor(h)}h${mins % 60}m`;
  }

  return (
    <div className="mp-wrapper">
      <div className="mp-scroll" ref={scrollRef}>
        {PRESETS.map(mins => (
          <button
            key={mins}
            type="button"
            className={`mp-pill ${value === mins ? 'active' : ''}`}
            onClick={() => onChange(mins)}
          >
            {formatLabel(mins)}
          </button>
        ))}
      </div>
    </div>
  );
}
