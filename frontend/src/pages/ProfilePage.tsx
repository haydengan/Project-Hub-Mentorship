import { useEffect, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { getMyProfile, updateUsername, type UserProfile } from '../api/user';
import { getMyStreaks, type StreakResponse } from '../api/streaks';
import { getMyGroups } from '../api/groups';
import { getGroupLogsWeek, type DailyGroupSummary } from '../api/activities';
import ActivityCalendar from '../components/ActivityCalendar';
import './ProfilePage.css';

export default function ProfilePage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [streaks, setStreaks] = useState<StreakResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [newName, setNewName] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [calMonth, setCalMonth] = useState(new Date());
  const [calData, setCalData] = useState<Record<string, number>>({});
  const [selectedDay, setSelectedDay] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        const [p, s] = await Promise.all([getMyProfile(), getMyStreaks()]);
        setProfile(p);
        setStreaks(s);
        setNewName(p.username);
      } catch {
        navigate('/login');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [navigate]);

  // Load calendar data when month changes
  useEffect(() => {
    if (!profile) return;
    loadCalendar();
  }, [calMonth, profile]);

  async function loadCalendar() {
    try {
      const groups = await getMyGroups();
      const year = calMonth.getFullYear();
      const month = calMonth.getMonth();
      const startDate = `${year}-${String(month + 1).padStart(2, '0')}-01`;
      const lastDay = new Date(year, month + 1, 0).getDate();
      const endDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;

      const allDays: Record<string, number> = {};

      // Fetch logs from all groups for this month
      for (const g of groups) {
        try {
          const weeks: DailyGroupSummary[] = await getGroupLogsWeek(g.id, startDate, endDate);
          for (const day of weeks) {
            for (const member of day.members) {
              if (member.userId === profile!.id) {
                const mins = member.logs.reduce((s, l) => s + l.durationMins, 0);
                allDays[day.date] = (allDays[day.date] || 0) + mins;
              }
            }
          }
        } catch { /* skip */ }
      }

      setCalData(allDays);
    } catch { /* ignore */ }
  }

  async function handleSave(e: FormEvent) {
    e.preventDefault();
    if (!newName.trim()) return;
    setSaving(true); setError(''); setSuccess('');
    try {
      const updated = await updateUsername(newName.trim());
      setProfile(updated);
      setEditing(false);
      setSuccess('Username updated!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: unknown) {
      if (err !== null && typeof err === 'object' && 'response' in err && err.response !== null && typeof err.response === 'object' && 'data' in err.response && err.response.data !== null && typeof err.response.data === 'object' && 'message' in err.response.data) {
        setError(String((err.response as { data: { message: string } }).data.message));
      } else { setError('Failed to update.'); }
    } finally { setSaving(false); }
  }

  function fmt(m: number) {
    if (m < 60) return `${m}m`;
    const h = Math.floor(m / 60), r = m % 60;
    return r > 0 ? `${h}h ${r}m` : `${h}h`;
  }

  if (loading) return <div className="loading-page"><div className="spinner" /><span>Loading...</span></div>;
  if (!profile) return null;

  const totalMins = streaks.reduce((s, st) => s + st.totalMinutes, 0);
  const totalActivities = streaks.length;
  const bestStreak = Math.max(0, ...streaks.map(s => s.longestStreak));

  return (
    <div className="pr">
      <header className="pr-head">
        <Link to="/" className="pr-back">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M15 18l-6-6 6-6"/></svg>
        </Link>
        <h1 className="pr-title">Profile</h1>
        <div style={{ width: 34 }} />
      </header>

      <main className="pr-main">
        <div className="pr-hero">
          <div className="pr-big-av">{profile.username.charAt(0).toUpperCase()}</div>
          {!editing ? (
            <div className="pr-name-row">
              <h2 className="pr-name">{profile.username}</h2>
              <button className="pr-edit-btn" onClick={() => setEditing(true)}>Edit</button>
            </div>
          ) : (
            <form className="pr-edit-form" onSubmit={handleSave}>
              <input type="text" value={newName} onChange={e => setNewName(e.target.value)} maxLength={32} autoFocus />
              <button type="submit" className="btn-brand" disabled={saving}>{saving ? '...' : 'Save'}</button>
              <button type="button" className="btn-ghost" onClick={() => { setEditing(false); setNewName(profile.username); }}>Cancel</button>
            </form>
          )}
          <p className="pr-email">{profile.email}</p>
          {error && <p className="ql-err">{error}</p>}
          {success && <span className="success-toast">{success}</span>}
        </div>

        <div className="pr-stats">
          <div className="pr-stat">
            <span className="pr-stat-val">{fmt(totalMins)}</span>
            <span className="pr-stat-label">Total time</span>
          </div>
          <div className="pr-stat">
            <span className="pr-stat-val">{totalActivities}</span>
            <span className="pr-stat-label">Activities</span>
          </div>
          <div className="pr-stat">
            <span className="pr-stat-val">{bestStreak}d</span>
            <span className="pr-stat-label">Best streak</span>
          </div>
        </div>

        {/* Activity Calendar */}
        <section className="pr-sec">
          <h3 className="pr-sec-h">Activity Calendar</h3>
          <ActivityCalendar
            data={calData}
            month={calMonth}
            onPrevMonth={() => setCalMonth(new Date(calMonth.getFullYear(), calMonth.getMonth() - 1))}
            onNextMonth={() => setCalMonth(new Date(calMonth.getFullYear(), calMonth.getMonth() + 1))}
            onDayClick={(d) => setSelectedDay(selectedDay === d ? null : d)}
          />
          {selectedDay && calData[selectedDay] && (
            <div className="pr-day-detail">
              <span className="pr-day-date">{new Date(selectedDay + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}</span>
              <span className="pr-day-mins">{fmt(calData[selectedDay])}</span>
            </div>
          )}
        </section>

        {/* Activity breakdown */}
        {streaks.length > 0 && (
          <section className="pr-sec">
            <h3 className="pr-sec-h">Activity Breakdown</h3>
            <div className="pr-breakdown">
              {streaks.map(s => (
                <div key={s.activityId} className="pr-act-row">
                  <span className="pr-act-name">{s.activityName}</span>
                  <div className="pr-act-bar-wrap">
                    <div className="pr-act-bar" style={{ width: `${totalMins > 0 ? (s.totalMinutes / totalMins) * 100 : 0}%` }} />
                  </div>
                  <span className="pr-act-val">{fmt(s.totalMinutes)}</span>
                </div>
              ))}
            </div>
          </section>
        )}

        <section className="pr-sec">
          <h3 className="pr-sec-h">Account</h3>
          <div className="pr-info-rows">
            <div className="pr-info-row">
              <span className="pr-info-label">Email</span>
              <span className="pr-info-val">{profile.email}</span>
            </div>
            <div className="pr-info-row">
              <span className="pr-info-label">Joined</span>
              <span className="pr-info-val">{new Date(profile.registeredAt).toLocaleDateString()}</span>
            </div>
            <div className="pr-info-row">
              <span className="pr-info-label">Role</span>
              <span className="pr-info-val">{profile.role}</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
