import { useMemo } from 'react';
import './ActivityCalendar.css';

interface ActivityCalendarProps {
  /** Map of "YYYY-MM-DD" -> total minutes logged that day */
  data: Record<string, number>;
  /** Currently viewed month (Date object) */
  month: Date;
  onPrevMonth: () => void;
  onNextMonth: () => void;
  onDayClick?: (date: string) => void;
}

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

export default function ActivityCalendar({ data, month, onPrevMonth, onNextMonth, onDayClick }: ActivityCalendarProps) {
  const cells = useMemo(() => {
    const year = month.getFullYear();
    const m = month.getMonth();
    const firstDay = new Date(year, m, 1);
    const lastDay = new Date(year, m + 1, 0);
    const totalDays = lastDay.getDate();

    // Monday=0 ... Sunday=6
    let startDow = firstDay.getDay() - 1;
    if (startDow < 0) startDow = 6;

    const rows: { date: string; day: number; inMonth: boolean; mins: number }[][] = [];
    let row: typeof rows[0] = [];

    // pad before
    for (let i = 0; i < startDow; i++) {
      row.push({ date: '', day: 0, inMonth: false, mins: 0 });
    }

    for (let d = 1; d <= totalDays; d++) {
      const dateStr = `${year}-${String(m + 1).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
      row.push({ date: dateStr, day: d, inMonth: true, mins: data[dateStr] || 0 });
      if (row.length === 7) {
        rows.push(row);
        row = [];
      }
    }

    // pad after
    if (row.length > 0) {
      while (row.length < 7) row.push({ date: '', day: 0, inMonth: false, mins: 0 });
      rows.push(row);
    }

    return rows;
  }, [data, month]);

  const todayStr = new Date().toISOString().split('T')[0];
  const monthLabel = month.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  const maxMins = Math.max(1, ...Object.values(data));

  function intensityClass(mins: number) {
    if (mins <= 0) return '';
    const ratio = mins / maxMins;
    if (ratio < 0.25) return 'cal-l1';
    if (ratio < 0.5) return 'cal-l2';
    if (ratio < 0.75) return 'cal-l3';
    return 'cal-l4';
  }

  return (
    <div className="cal">
      <div className="cal-head">
        <button type="button" className="cal-nav" onClick={onPrevMonth}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M15 18l-6-6 6-6"/></svg>
        </button>
        <span className="cal-month">{monthLabel}</span>
        <button type="button" className="cal-nav" onClick={onNextMonth}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M9 18l6-6-6-6"/></svg>
        </button>
      </div>

      <div className="cal-grid">
        {DAYS.map(d => <span key={d} className="cal-dow">{d}</span>)}
        {cells.flat().map((c, i) => (
          <button
            key={i}
            type="button"
            className={`cal-cell ${!c.inMonth ? 'cal-out' : ''} ${c.date === todayStr ? 'cal-today' : ''} ${intensityClass(c.mins)}`}
            onClick={() => c.inMonth && c.date && onDayClick?.(c.date)}
            disabled={!c.inMonth}
          >
            {c.inMonth ? c.day : ''}
            {c.mins > 0 && <span className="cal-dot" />}
          </button>
        ))}
      </div>

      <div className="cal-legend">
        <span className="cal-legend-label">Less</span>
        <span className="cal-swatch" />
        <span className="cal-swatch cal-l1" />
        <span className="cal-swatch cal-l2" />
        <span className="cal-swatch cal-l3" />
        <span className="cal-swatch cal-l4" />
        <span className="cal-legend-label">More</span>
      </div>
    </div>
  );
}
