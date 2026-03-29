import { useEffect, useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client from '../api/client';
import { getMyGroups, type GroupResponse } from '../api/groups';
import { getMyStreaks, type StreakResponse } from '../api/streaks';
import { getGroupActivities, getGroupLogsWeek, logActivity, uploadMedia, type ActivityResponse, type DailyGroupSummary } from '../api/activities';
import { getMyProfile } from '../api/user';
import DurationPicker from '../components/DurationPicker';
import ActivityCalendar from '../components/ActivityCalendar';
import './DashboardPage.css';

export default function DashboardPage() {
  const navigate = useNavigate();
  const fileRef = useRef<HTMLInputElement>(null);
  const [groups, setGroups] = useState<GroupResponse[]>([]);
  const [streaks, setStreaks] = useState<StreakResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [allActivities, setAllActivities] = useState<(ActivityResponse & { groupName: string })[]>([]);
  const [qlActivity, setQlActivity] = useState('');
  const [qlMinutes, setQlMinutes] = useState(0);
  const [qlNote, setQlNote] = useState('');
  const [qlFile, setQlFile] = useState<File | null>(null);
  const [qlPreview, setQlPreview] = useState('');
  const [qlLoading, setQlLoading] = useState(false);
  const [qlSuccess, setQlSuccess] = useState('');
  const [qlError, setQlError] = useState('');

  const [userId, setUserId] = useState('');
  const [calMonth, setCalMonth] = useState(new Date());
  const [calData, setCalData] = useState<Record<string, number>>({});
  const [calLogs, setCalLogs] = useState<Record<string, { name: string; mins: number }[]>>({});
  const [selectedDay, setSelectedDay] = useState<string | null>(null);

  useEffect(() => { load(); }, []);

  async function load() {
    try {
      const [groupsData, streaksData, profile] = await Promise.all([getMyGroups(), getMyStreaks(), getMyProfile()]);
      setGroups(groupsData);
      setStreaks(streaksData);
      setUserId(profile.id);
      const actPromises = groupsData.map(async (g) => {
        const acts = await getGroupActivities(g.id);
        return acts.map(a => ({ ...a, groupName: g.name }));
      });
      setAllActivities((await Promise.all(actPromises)).flat());
    } catch {
      setError('Session expired.');
      navigate('/login');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!userId || groups.length === 0) return;
    loadCalendar();
  }, [calMonth, userId, groups.length]);

  async function loadCalendar() {
    const year = calMonth.getFullYear(), month = calMonth.getMonth();
    const startDate = `${year}-${String(month + 1).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month + 1, 0).getDate();
    const endDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
    const days: Record<string, number> = {};
    const logs: Record<string, { name: string; mins: number }[]> = {};
    for (const g of groups) {
      try {
        const acts = allActivities.filter(a => a.groupId === g.id);
        const weeks: DailyGroupSummary[] = await getGroupLogsWeek(g.id, startDate, endDate);
        for (const d of weeks) for (const m of d.members) if (m.userId === userId) {
          for (const l of m.logs) {
            days[d.date] = (days[d.date] || 0) + l.durationMins;
            if (!logs[d.date]) logs[d.date] = [];
            const actName = acts.find(a => a.id === l.activityId)?.name || 'Activity';
            logs[d.date].push({ name: actName, mins: l.durationMins });
          }
        }
      } catch { /* skip */ }
    }
    setCalData(days);
    setCalLogs(logs);
  }

  function handleFileChange(e: React.ChangeEvent<HTMLInputElement>) {
    const f = e.target.files?.[0];
    if (f) { setQlFile(f); setQlPreview(URL.createObjectURL(f)); }
  }
  function clearFile() { setQlFile(null); setQlPreview(''); if (fileRef.current) fileRef.current.value = ''; }

  async function handleQuickLog(e: React.FormEvent) {
    e.preventDefault();
    if (!qlActivity || qlMinutes <= 0) return;
    setQlLoading(true); setQlError(''); setQlSuccess('');
    try {
      let mediaUrl: string | undefined;
      if (qlFile) { mediaUrl = (await uploadMedia(qlFile)).url; }
      await logActivity(qlActivity, qlMinutes, qlNote, mediaUrl);
      const actName = allActivities.find(a => a.id === qlActivity)?.name || 'Activity';
      setQlSuccess(`${qlMinutes}m of ${actName} logged!`);
      setQlActivity(''); setQlMinutes(0); setQlNote(''); clearFile();
      setStreaks(await getMyStreaks()); loadCalendar();
      setTimeout(() => setQlSuccess(''), 3000);
    } catch (err: unknown) {
      if (err !== null && typeof err === 'object' && 'response' in err && err.response !== null && typeof err.response === 'object' && 'data' in err.response && err.response.data !== null && typeof err.response.data === 'object' && 'message' in err.response.data) {
        setQlError(String((err.response as { data: { message: string } }).data.message));
      } else { setQlError('Failed to log activity.'); }
    } finally { setQlLoading(false); }
  }

  async function handleLogout() { try { await client.post('/api/auth/logout'); } finally { navigate('/login'); } }

  if (loading) return <div className="loading-page"><div className="spinner" /><span>Loading...</span></div>;

  const tracked = streaks.filter(s => s.totalMinutes > 0);
  function fmt(mins: number) { if (mins < 60) return `${mins}m`; const h = Math.floor(mins / 60), m = mins % 60; return m > 0 ? `${h}h ${m}m` : `${h}h`; }

  return (
    <div className="d">
      <header className="d-head">
        <h1 className="d-logo">Togetherly</h1>
        <div className="d-head-actions">
          <Link to="/profile" className="d-avatar-btn">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          </Link>
          <button className="d-logout" onClick={handleLogout}>Log out</button>
        </div>
      </header>

      <main className="d-grid">
        {error && <div className="error-banner d-full">{error}</div>}

        {/* LEFT */}
        <div className="d-left">
          {tracked.length > 0 && (
            <section className="d-sec">
              <h2 className="d-sec-h">Progress</h2>
              <div className="stat-grid">
                {tracked.map((s, i) => (
                  <div key={s.activityId} className={`stat-card stat-c${(i % 5) + 1}`}>
                    <span className="stat-val">{fmt(s.totalMinutes)}</span>
                    <span className="stat-name">{s.activityName}</span>
                    {s.currentStreak > 0 && <span className="stat-streak">{s.currentStreak}d streak</span>}
                  </div>
                ))}
              </div>
            </section>
          )}

          <section className="d-sec">
            <h2 className="d-sec-h">Groups</h2>
            {groups.length === 0 ? (
              <div className="d-empty"><p className="d-empty-t">No groups yet</p><p className="d-empty-d">Create or join a group to start.</p></div>
            ) : (
              <div className="g-list">
                {groups.map(g => (
                  <Link to={`/groups/${g.id}`} key={g.id} className="g-card">
                    <div className="g-card-top">
                      <div className="g-ico">{g.name.charAt(0)}</div>
                      <div className="g-card-info"><span className="g-name">{g.name}</span><span className="g-meta">{g.memberCount} member{g.memberCount !== 1 ? 's' : ''}</span></div>
                      <span className="g-arrow">&rsaquo;</span>
                    </div>
                    {g.description && <p className="g-desc">{g.description}</p>}
                  </Link>
                ))}
              </div>
            )}
            <div className="action-cards">
              <Link to="/groups/join" className="action-card">
                <div className="action-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg></div>
                <span className="action-label">Join Group</span>
                <span className="action-desc">Enter an invite code</span>
              </Link>
              <Link to="/groups/create" className="action-card action-card-brand">
                <div className="action-icon"><svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg></div>
                <span className="action-label">Create Group</span>
                <span className="action-desc">Start something new</span>
              </Link>
            </div>
          </section>
        </div>

        {/* RIGHT */}
        <div className="d-right">
          {allActivities.length > 0 ? (
            <section className="ql-card">
              <div className="ql-top"><h2 className="ql-h">Log Activity</h2>{qlSuccess && <span className="success-toast">{qlSuccess}</span>}</div>
              <form className="ql-form" onSubmit={handleQuickLog}>
                <select className="ql-sel" value={qlActivity} onChange={e => setQlActivity(e.target.value)} required>
                  <option value="">What did you do?</option>
                  {allActivities.map(a => <option key={a.id} value={a.id}>{a.name} &middot; {a.groupName}</option>)}
                </select>
                <DurationPicker value={qlMinutes} onChange={setQlMinutes} />
                <div className="ql-media">
                  {qlPreview ? (
                    <div className="ql-preview">
                      {qlFile?.type.startsWith('video/') ? <video src={qlPreview} className="ql-thumb" controls /> : <img src={qlPreview} className="ql-thumb" alt="" />}
                      <button type="button" className="ql-remove" onClick={clearFile}>&times;</button>
                    </div>
                  ) : (
                    <button type="button" className="ql-attach" onClick={() => fileRef.current?.click()}>
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
                      <span>Add photo/video</span>
                    </button>
                  )}
                  <input ref={fileRef} type="file" accept="image/*,video/*" onChange={handleFileChange} hidden />
                </div>
                <div className="ql-bot">
                  <input className="ql-note" type="text" placeholder="Note (optional)" value={qlNote} onChange={e => setQlNote(e.target.value)} />
                  <button type="submit" className="ql-go" disabled={qlLoading || !qlActivity || qlMinutes <= 0}>{qlLoading ? '...' : 'Log'}</button>
                </div>
                {qlError && <p className="ql-err">{qlError}</p>}
              </form>
            </section>
          ) : (
            <div className="ql-card ql-empty-card"><p className="d-empty-t">No activities yet</p><p className="d-empty-d">Join a group and add activities to start logging.</p></div>
          )}

          <section className="d-cal">
            <ActivityCalendar data={calData} month={calMonth}
              onPrevMonth={() => setCalMonth(new Date(calMonth.getFullYear(), calMonth.getMonth() - 1))}
              onNextMonth={() => setCalMonth(new Date(calMonth.getFullYear(), calMonth.getMonth() + 1))}
              onDayClick={(d) => setSelectedDay(selectedDay === d ? null : d)} />
            {selectedDay && calData[selectedDay] > 0 && (
              <div className="d-cal-detail">
                <div className="d-cal-date">{new Date(selectedDay + 'T00:00:00').toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}</div>
                <div className="d-cal-logs">
                  {(calLogs[selectedDay] || []).map((l, i) => (
                    <div key={i} className="d-cal-log">
                      <span className="d-cal-log-name">{l.name}</span>
                      <span className="d-cal-log-mins">{l.mins}m</span>
                    </div>
                  ))}
                </div>
                <div className="d-cal-total">Total: {fmt(calData[selectedDay])}</div>
              </div>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}
