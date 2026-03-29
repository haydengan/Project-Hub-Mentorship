import './WeeklyChart.css';

interface WeeklyChartProps {
  data: { date: string; totalMins: number }[];
}

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

export default function WeeklyChart({ data }: WeeklyChartProps) {
  const max = Math.max(...data.map(d => d.totalMins), 1);

  function formatLabel(mins: number) {
    if (mins === 0) return '';
    if (mins < 60) return `${mins}m`;
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return m > 0 ? `${h}h${m}` : `${h}h`;
  }

  return (
    <div className="wc">
      <div className="wc-bars">
        {data.map((d, i) => {
          const pct = max > 0 ? (d.totalMins / max) * 100 : 0;
          const dayName = DAYS[new Date(d.date + 'T00:00:00').getDay() === 0 ? 6 : new Date(d.date + 'T00:00:00').getDay() - 1];
          const isToday = d.date === new Date().toISOString().split('T')[0];
          return (
            <div key={d.date} className={`wc-col ${isToday ? 'wc-today' : ''}`}>
              <span className="wc-val">{formatLabel(d.totalMins)}</span>
              <div className="wc-track">
                <div
                  className="wc-fill"
                  style={{ height: `${Math.max(pct, d.totalMins > 0 ? 8 : 0)}%` }}
                />
              </div>
              <span className="wc-day">{dayName}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
