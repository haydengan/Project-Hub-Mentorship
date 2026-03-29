import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getGroupDetail, leaveGroup, type GroupDetailResponse } from '../api/groups';
import {
  getGroupActivities, getGroupLogsToday, getGroupLogsWeek, createActivity, logActivity, uploadMedia,
  type ActivityResponse, type DailyGroupSummary
} from '../api/activities';
import { getGroupLeaderboard, type GroupLeaderboardEntry } from '../api/streaks';
import { getMessages, sendMessage, type MessageResponse } from '../api/messages';
import DurationPicker from '../components/DurationPicker';
import WeeklyChart from '../components/WeeklyChart';
import './GroupDetailPage.css';

const TYPE_COLORS: Record<string, { bg: string; text: string; dot: string }> = {
  GYM:        { bg: '#fef2f2', text: '#991b1b', dot: '#ef4444' },
  STUDY:      { bg: '#eff6ff', text: '#1e3a5f', dot: '#3b82f6' },
  READING:    { bg: '#fefce8', text: '#713f12', dot: '#eab308' },
  MEDITATION: { bg: '#f5f3ff', text: '#4c1d95', dot: '#8b5cf6' },
  CODING:     { bg: '#f0fdf4', text: '#14532d', dot: '#22c55e' },
  RUNNING:    { bg: '#fff7ed', text: '#7c2d12', dot: '#f97316' },
  CUSTOM:     { bg: '#f0fdfa', text: '#134e4a', dot: '#14b8a6' },
};

export default function GroupDetailPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();
  const chatEndRef = useRef<HTMLDivElement>(null);

  const [group, setGroup] = useState<GroupDetailResponse | null>(null);
  const [activities, setActivities] = useState<ActivityResponse[]>([]);
  const [todayLogs, setTodayLogs] = useState<DailyGroupSummary | null>(null);
  const [weekData, setWeekData] = useState<{ date: string; totalMins: number }[]>([]);
  const [leaderboard, setLeaderboard] = useState<GroupLeaderboardEntry[]>([]);
  const [messages, setMessages] = useState<MessageResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  const [showCreate, setShowCreate] = useState(false);
  const [actName, setActName] = useState('');
  const [actType, setActType] = useState('CUSTOM');

  const fileRef = useRef<HTMLInputElement>(null);
  const [logActId, setLogActId] = useState('');
  const [logMins, setLogMins] = useState(0);
  const [logNote, setLogNote] = useState('');
  const [logFile, setLogFile] = useState<File | null>(null);
  const [logPreview, setLogPreview] = useState('');
  const [logErr, setLogErr] = useState('');
  const [logOk, setLogOk] = useState('');

  const [msgText, setMsgText] = useState('');
  const [msgSending, setMsgSending] = useState(false);

  const [tab, setTab] = useState<'today' | 'week' | 'members' | 'board'>('today');

  useEffect(() => { if (groupId) loadAll(); }, [groupId]);

  async function loadAll() {
    try {
      const [g, acts, today, board] = await Promise.all([
        getGroupDetail(groupId!), getGroupActivities(groupId!),
        getGroupLogsToday(groupId!), getGroupLeaderboard(groupId!),
      ]);
      setGroup(g); setActivities(acts); setTodayLogs(today); setLeaderboard(board);

      // weekly data
      try {
        const week: DailyGroupSummary[] = await getGroupLogsWeek(groupId!);
        // get current user's id from the members list to filter their logs
        // For simplicity, sum all members' minutes per day
        setWeekData(week.map(d => ({
          date: d.date,
          totalMins: d.members.reduce((sum, m) => sum + m.logs.reduce((s, l) => s + l.durationMins, 0), 0),
        })));
      } catch { setWeekData([]); }

      // messages
      try { setMessages(await getMessages(groupId!)); } catch { /* no messages yet */ }
    } catch { setError('Failed to load group.'); }
    finally { setLoading(false); }
  }

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    if (!groupId) return;
    try {
      await createActivity(groupId, actName, actType);
      setActName(''); setActType('CUSTOM'); setShowCreate(false); loadAll();
    } catch { setError('Failed to create activity.'); }
  }

  function handleLogFile(e: React.ChangeEvent<HTMLInputElement>) {
    const f = e.target.files?.[0];
    if (f) { setLogFile(f); setLogPreview(URL.createObjectURL(f)); }
  }
  function clearLogFile() {
    setLogFile(null); setLogPreview('');
    if (fileRef.current) fileRef.current.value = '';
  }

  async function handleLog(e: React.FormEvent) {
    e.preventDefault();
    if (logMins <= 0) return;
    setLogErr(''); setLogOk('');
    try {
      let mediaUrl: string | undefined;
      if (logFile) {
        const uploaded = await uploadMedia(logFile);
        mediaUrl = uploaded.url;
      }
      await logActivity(logActId, logMins, logNote, mediaUrl);
      setLogOk(`${logMins}m logged!`);
      setLogActId(''); setLogMins(0); setLogNote('');
      clearLogFile();
      loadAll();
      setTimeout(() => setLogOk(''), 3000);
    } catch (err: unknown) {
      if (err !== null && typeof err === 'object' && 'response' in err && err.response !== null && typeof err.response === 'object' && 'data' in err.response && err.response.data !== null && typeof err.response.data === 'object' && 'message' in err.response.data) {
        setLogErr(String((err.response as { data: { message: string } }).data.message));
      } else { setLogErr('Failed to log.'); }
    }
  }

  async function handleSendMsg(e: React.FormEvent) {
    e.preventDefault();
    if (!msgText.trim() || !groupId) return;
    setMsgSending(true);
    try {
      const msg = await sendMessage(groupId, msgText.trim());
      setMessages(prev => [...prev, msg]);
      setMsgText('');
      setTimeout(() => chatEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 50);
    } catch { /* ignore */ }
    finally { setMsgSending(false); }
  }

  function copyCode() {
    if (!group) return;
    navigator.clipboard.writeText(group.inviteCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }

  if (loading) return <div className="loading-page"><div className="spinner" /><span>Loading...</span></div>;
  if (!group) return <div className="loading-page">Group not found.</div>;

  const MEDAL = ['\u{1F947}', '\u{1F948}', '\u{1F949}'];
  function fmt(m: number) { if (m < 60) return `${m}m`; const h = Math.floor(m/60), r = m%60; return r > 0 ? `${h}h ${r}m` : `${h}h`; }

  return (
    <div className="gp">
      {/* Hero header */}
      <header className="gp-head">
        <div className="gp-head-top">
          <Link to="/" className="gp-back">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round"><path d="M15 18l-6-6 6-6"/></svg>
          </Link>
          <button className="gp-leave" onClick={() => { if (confirm('Leave this group?')) { leaveGroup(groupId!); navigate('/'); } }}>Leave</button>
        </div>
        <div className="gp-hero">
          <h1 className="gp-group-name">{group.name}</h1>
          {group.description && <p className="gp-desc">{group.description}</p>}
          <div className="gp-meta-row">
            <button className="gp-code-btn" onClick={copyCode}>
              <span className="gp-code-val">{group.inviteCode}</span>
              <span className="gp-code-label">{copied ? 'Copied!' : 'Copy'}</span>
            </button>
            <span className="gp-members-pill">{group.members.length}/{group.maxMembers} members</span>
            <button className="gp-add-btn" onClick={() => setShowCreate(!showCreate)}>{showCreate ? 'Cancel' : '+ Add Activity'}</button>
          </div>
        </div>
      </header>

      <main className="gp-grid">
        {error && <div className="error-banner gp-full">{error}</div>}

        {/* ── LEFT COLUMN ── */}
        <div className="gp-left">

          {/* Create Activity Modal */}
          {showCreate && (
            <div className="modal-overlay" onClick={() => setShowCreate(false)}>
              <div className="modal-card" onClick={e => e.stopPropagation()}>
                <div className="modal-head">
                  <h3 className="modal-title">Add Activity</h3>
                  <button className="modal-close" onClick={() => setShowCreate(false)}>&times;</button>
                </div>
                <form className="modal-form" onSubmit={handleCreate}>
                  <label className="modal-label">Activity name</label>
                  <input type="text" placeholder="e.g. Morning run" value={actName} onChange={e => setActName(e.target.value)} required />
                  <label className="modal-label">Color &amp; type</label>
                  <div className="color-picker">
                    {Object.entries(TYPE_COLORS).map(([type, c]) => (
                      <button
                        key={type}
                        type="button"
                        className={`color-swatch ${actType === type ? 'active' : ''}`}
                        style={{ background: c.dot }}
                        onClick={() => setActType(type)}
                        title={type.charAt(0) + type.slice(1).toLowerCase()}
                      />
                    ))}
                  </div>
                  <div className="color-preview" style={{ background: (TYPE_COLORS[actType] || TYPE_COLORS.CUSTOM).bg }}>
                    <span className="act-dot" style={{ background: (TYPE_COLORS[actType] || TYPE_COLORS.CUSTOM).dot }} />
                    <span style={{ color: (TYPE_COLORS[actType] || TYPE_COLORS.CUSTOM).text, fontWeight: 600, fontSize: '0.85rem' }}>
                      {actName || 'Activity name'}
                    </span>
                  </div>
                  <button type="submit" className="modal-submit">Create Activity</button>
                </form>
              </div>
            </div>
          )}

          {/* Activities */}
          <section className="gp-sec">
            <h2 className="gp-sec-h">Activities</h2>
            {activities.length === 0
              ? <p className="gp-empty-text">No activities. Add one to start.</p>
              : <div className="act-list">{activities.map(a => {
                  const c = TYPE_COLORS[a.type] || TYPE_COLORS.CUSTOM;
                  return (
                    <div key={a.id} className="act-tile" style={{ background: c.bg }}>
                      <span className="act-dot" style={{ background: c.dot }} />
                      <span className="act-name" style={{ color: c.text }}>{a.name}</span>
                      <span className="act-type" style={{ color: c.dot }}>{a.type.charAt(0) + a.type.slice(1).toLowerCase()}</span>
                    </div>
                  );
                })}</div>
            }
          </section>

          {/* Tabs */}
          <div className="gp-tabs">
            {(['today','week','members','board'] as const).map(t => (
              <button key={t} className={`gp-tab ${tab === t ? 'active' : ''}`} onClick={() => setTab(t)}>
                {t === 'today' ? 'Today' : t === 'week' ? 'Week' : t === 'members' ? 'Members' : 'Board'}
              </button>
            ))}
          </div>

          <div className="gp-tab-body">
            {tab === 'today' && todayLogs && (
              <div className="gp-list">{todayLogs.members.map(m => {
                const tot = m.logs.reduce((s,l) => s + l.durationMins, 0);
                return (
                  <div key={m.userId} className="today-card">
                    <div className="today-header">
                      <div className="gp-av">{m.username.charAt(0).toUpperCase()}</div>
                      <div className="gp-row-info">
                        <span className="gp-row-name">{m.username}</span>
                        <span className="gp-row-sub">{tot > 0 ? `${fmt(tot)} logged` : 'Nothing yet'}</span>
                      </div>
                    </div>
                    {m.logs.length > 0 && (
                      <div className="today-logs">
                        {m.logs.map(l => (
                          <div key={l.id} className="today-log-item">
                            <div className="today-log-row">
                              <span className="today-log-dot" />
                              <span className="today-log-name">{activities.find(a => a.id === l.activityId)?.name || 'Activity'}</span>
                              <span className="today-log-mins">{l.durationMins}m</span>
                            </div>
                            {l.mediaUrl && <img src={l.mediaUrl} className="today-log-img" alt="" />}
                            {l.note && <p className="today-log-note">{l.note}</p>}
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })}</div>
            )}

            {tab === 'week' && (
              <div className="gp-week-wrap">
                {weekData.length > 0
                  ? <WeeklyChart data={weekData} />
                  : <p className="gp-empty-text">No data this week yet.</p>
                }
              </div>
            )}

            {tab === 'members' && (
              <div className="gp-list">{group.members.map(m => (
                <div key={m.userId} className="gp-row">
                  <div className="gp-av">{m.username.charAt(0).toUpperCase()}</div>
                  <div className="gp-row-info">
                    <span className="gp-row-name">{m.username}{m.role === 'ADMIN' && <span className="gp-admin">Admin</span>}</span>
                    <span className="gp-row-sub">{m.email}</span>
                  </div>
                </div>
              ))}</div>
            )}

            {tab === 'board' && (
              <div className="gp-list">
                {leaderboard.length === 0
                  ? <p className="gp-empty-text">No activity logged yet.</p>
                  : leaderboard.map((e, i) => (
                    <div key={`${e.userId}-${e.activityId}`} className={`gp-row ${i < 3 ? 'gp-row-top' : ''}`}>
                      <div className="gp-rank">{i < 3 ? MEDAL[i] : <span className="gp-rank-n">#{i+1}</span>}</div>
                      <div className="gp-row-info">
                        <span className="gp-row-name">{e.username}</span>
                        <span className="gp-row-sub">{e.activityName}</span>
                      </div>
                      <div className="gp-row-stat">
                        <span className="gp-stat-v">{fmt(e.totalMinutes)}</span>
                        {e.currentStreak > 0 && <span className="gp-stat-s">{e.currentStreak}d</span>}
                      </div>
                    </div>
                  ))
                }
              </div>
            )}
          </div>
        </div>

        {/* ── RIGHT COLUMN (Log + Chat) ── */}
        <div className="gp-right">
          {/* Log */}
          {activities.length > 0 && (
            <section className="gp-log">
              <div className="gp-log-top">
                <h2 className="gp-log-h">Log Activity</h2>
                {logOk && <span className="success-toast">{logOk}</span>}
              </div>
              <form className="gp-log-form" onSubmit={handleLog}>
                <select className="ql-sel" value={logActId} onChange={e => setLogActId(e.target.value)} required>
                  <option value="">Select activity</option>
                  {activities.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}
                </select>
                <DurationPicker value={logMins} onChange={setLogMins} />
                <div className="ql-media">
                  {logPreview ? (
                    <div className="ql-preview">
                      {logFile?.type.startsWith('video/')
                        ? <video src={logPreview} className="ql-thumb" controls />
                        : <img src={logPreview} className="ql-thumb" alt="preview" />
                      }
                      <button type="button" className="ql-remove" onClick={clearLogFile}>&times;</button>
                    </div>
                  ) : (
                    <button type="button" className="ql-attach" onClick={() => fileRef.current?.click()}>
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><path d="M21 15l-5-5L5 21"/></svg>
                      <span>Add photo/video</span>
                    </button>
                  )}
                  <input ref={fileRef} type="file" accept="image/*,video/*" onChange={handleLogFile} hidden />
                </div>
                <div className="ql-bot">
                  <input className="ql-note" type="text" placeholder="Note (optional)" value={logNote} onChange={e => setLogNote(e.target.value)} />
                  <button type="submit" className="ql-go" disabled={!logActId || logMins <= 0}>Log</button>
                </div>
                {logErr && <p className="ql-err">{logErr}</p>}
              </form>
            </section>
          )}

          {/* Chat */}
          <section className="gp-chat">
            <h2 className="gp-sec-h">Group Chat</h2>
            <div className="chat-box">
              <div className="chat-msgs">
                {messages.length === 0 && <p className="chat-empty">No messages yet. Say hi!</p>}
                {messages.map(m => (
                  <div key={m.id} className="chat-msg">
                    <div className="chat-av">{m.username.charAt(0).toUpperCase()}</div>
                    <div className="chat-bubble">
                      <span className="chat-user">{m.username}</span>
                      <span className="chat-text">{m.content}</span>
                      <span className="chat-time">{new Date(m.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                    </div>
                  </div>
                ))}
                <div ref={chatEndRef} />
              </div>
              <form className="chat-input" onSubmit={handleSendMsg}>
                <input type="text" placeholder="Type a message..." value={msgText} onChange={e => setMsgText(e.target.value)} maxLength={500} />
                <button type="submit" disabled={msgSending || !msgText.trim()}>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
                </button>
              </form>
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}
