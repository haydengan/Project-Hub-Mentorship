import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getGroupDetail, leaveGroup, type GroupDetailResponse } from '../api/groups';
import {
  getGroupActivities, getGroupLogsToday, createActivity, logActivity,
  type ActivityResponse, type DailyGroupSummary
} from '../api/activities';
import { getGroupLeaderboard, type GroupLeaderboardEntry } from '../api/streaks';
import './GroupDetailPage.css';

export default function GroupDetailPage() {
  const { groupId } = useParams<{ groupId: string }>();
  const navigate = useNavigate();

  const [group, setGroup] = useState<GroupDetailResponse | null>(null);
  const [activities, setActivities] = useState<ActivityResponse[]>([]);
  const [todayLogs, setTodayLogs] = useState<DailyGroupSummary | null>(null);
  const [leaderboard, setLeaderboard] = useState<GroupLeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Create activity form
  const [showCreateActivity, setShowCreateActivity] = useState(false);
  const [activityName, setActivityName] = useState('');
  const [activityType, setActivityType] = useState('CUSTOM');

  // Log activity form
  const [logActivityId, setLogActivityId] = useState('');
  const [logDuration, setLogDuration] = useState('');
  const [logNote, setLogNote] = useState('');
  const [logError, setLogError] = useState('');

  const [tab, setTab] = useState<'today' | 'members' | 'leaderboard'>('today');

  useEffect(() => {
    if (!groupId) return;
    loadGroupData();
  }, [groupId]);

  async function loadGroupData() {
    try {
      const [groupData, activitiesData, todayData, leaderboardData] = await Promise.all([
        getGroupDetail(groupId!),
        getGroupActivities(groupId!),
        getGroupLogsToday(groupId!),
        getGroupLeaderboard(groupId!),
      ]);
      setGroup(groupData);
      setActivities(activitiesData);
      setTodayLogs(todayData);
      setLeaderboard(leaderboardData);
    } catch {
      setError('Failed to load group.');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateActivity(e: React.FormEvent) {
    e.preventDefault();
    if (!groupId) return;
    try {
      await createActivity(groupId, activityName, activityType);
      setActivityName('');
      setActivityType('CUSTOM');
      setShowCreateActivity(false);
      loadGroupData();
    } catch {
      setError('Failed to create activity.');
    }
  }

  async function handleLogActivity(e: React.FormEvent) {
    e.preventDefault();
    setLogError('');
    try {
      await logActivity(logActivityId, parseInt(logDuration), logNote);
      setLogActivityId('');
      setLogDuration('');
      setLogNote('');
      loadGroupData();
    } catch (err: unknown) {
      if (
        err !== null && typeof err === 'object' && 'response' in err &&
        err.response !== null && typeof err.response === 'object' &&
        'data' in err.response && err.response.data !== null &&
        typeof err.response.data === 'object' && 'message' in err.response.data
      ) {
        setLogError(String((err.response as { data: { message: string } }).data.message));
      } else {
        setLogError('Failed to log activity.');
      }
    }
  }

  async function handleLeaveGroup() {
    if (!groupId || !confirm('Are you sure you want to leave this group?')) return;
    try {
      await leaveGroup(groupId);
      navigate('/');
    } catch {
      setError('Failed to leave group.');
    }
  }

  if (loading) return <div className="dashboard-loading">Loading...</div>;
  if (!group) return <div className="dashboard-loading">Group not found.</div>;

  return (
    <div className="group-detail">
      <header className="group-detail-header">
        <Link to="/" className="back-link">Back</Link>
        <h1>{group.name}</h1>
        <button className="btn-leave" onClick={handleLeaveGroup}>Leave</button>
      </header>

      {error && <p className="dashboard-error">{error}</p>}

      <div className="group-info-bar">
        {group.description && <p className="group-info-desc">{group.description}</p>}
        <span className="group-info-code">Invite Code: <strong>{group.inviteCode}</strong></span>
        <span className="group-info-members">
          {group.members.length}/{group.maxMembers} members
        </span>
      </div>

      {/* Log Activity Form */}
      {activities.length > 0 && (
        <section className="log-section">
          <h2>Log Activity</h2>
          <form className="log-form" onSubmit={handleLogActivity}>
            <select
              value={logActivityId}
              onChange={e => setLogActivityId(e.target.value)}
              required
            >
              <option value="">Select activity...</option>
              {activities.map(a => (
                <option key={a.id} value={a.id}>{a.name}</option>
              ))}
            </select>
            <input
              type="number"
              placeholder="Minutes"
              min="1"
              value={logDuration}
              onChange={e => setLogDuration(e.target.value)}
              required
            />
            <input
              type="text"
              placeholder="Note (optional)"
              value={logNote}
              onChange={e => setLogNote(e.target.value)}
            />
            <button type="submit" className="btn-log">Log</button>
          </form>
          {logError && <p className="log-error">{logError}</p>}
        </section>
      )}

      {/* Activities */}
      <section className="activities-section">
        <div className="section-header">
          <h2>Activities</h2>
          <button
            className="btn-secondary"
            onClick={() => setShowCreateActivity(!showCreateActivity)}
          >
            {showCreateActivity ? 'Cancel' : '+ Add Activity'}
          </button>
        </div>

        {showCreateActivity && (
          <form className="create-activity-form" onSubmit={handleCreateActivity}>
            <input
              type="text"
              placeholder="Activity name"
              value={activityName}
              onChange={e => setActivityName(e.target.value)}
              required
            />
            <select value={activityType} onChange={e => setActivityType(e.target.value)}>
              <option value="GYM">Gym</option>
              <option value="STUDY">Study</option>
              <option value="READING">Reading</option>
              <option value="MEDITATION">Meditation</option>
              <option value="CODING">Coding</option>
              <option value="RUNNING">Running</option>
              <option value="CUSTOM">Custom</option>
            </select>
            <button type="submit" className="btn-primary-sm">Create</button>
          </form>
        )}

        {activities.length === 0 ? (
          <p className="empty-hint">No activities yet. Create one to start tracking.</p>
        ) : (
          <div className="activity-chips">
            {activities.map(a => (
              <span key={a.id} className="activity-chip">{a.name}</span>
            ))}
          </div>
        )}
      </section>

      {/* Tabs: Today / Members / Leaderboard */}
      <div className="tab-bar">
        <button
          className={`tab-btn ${tab === 'today' ? 'active' : ''}`}
          onClick={() => setTab('today')}
        >Today</button>
        <button
          className={`tab-btn ${tab === 'members' ? 'active' : ''}`}
          onClick={() => setTab('members')}
        >Members</button>
        <button
          className={`tab-btn ${tab === 'leaderboard' ? 'active' : ''}`}
          onClick={() => setTab('leaderboard')}
        >Streaks</button>
      </div>

      {/* Today Tab */}
      {tab === 'today' && todayLogs && (
        <section className="tab-content">
          {todayLogs.members.map(member => (
            <div key={member.userId} className="member-row">
              <div className="member-name">{member.username}</div>
              <div className="member-logs">
                {member.logs.length === 0 ? (
                  <span className="status-not-logged">Not yet</span>
                ) : (
                  member.logs.map(log => (
                    <span key={log.id} className="status-logged">
                      {activities.find(a => a.id === log.activityId)?.name || 'Activity'}{' '}
                      - {log.durationMins}min
                    </span>
                  ))
                )}
              </div>
            </div>
          ))}
        </section>
      )}

      {/* Members Tab */}
      {tab === 'members' && (
        <section className="tab-content">
          {group.members.map(member => (
            <div key={member.userId} className="member-row">
              <div className="member-name">
                {member.username}
                {member.role === 'ADMIN' && <span className="role-badge">Admin</span>}
              </div>
              <div className="member-meta">{member.email}</div>
            </div>
          ))}
        </section>
      )}

      {/* Leaderboard Tab */}
      {tab === 'leaderboard' && (
        <section className="tab-content">
          {leaderboard.length === 0 ? (
            <p className="empty-hint">No streaks yet. Start logging to build streaks.</p>
          ) : (
            leaderboard.map((entry, i) => (
              <div key={`${entry.userId}-${entry.activityId}`} className="leaderboard-row">
                <span className="leaderboard-rank">#{i + 1}</span>
                <div className="leaderboard-info">
                  <span className="leaderboard-name">{entry.username}</span>
                  <span className="leaderboard-activity">{entry.activityName}</span>
                </div>
                <div className="leaderboard-streak">
                  <span className="streak-current">{entry.currentStreak} days</span>
                  <span className="streak-record">Best: {entry.longestStreak}</span>
                </div>
              </div>
            ))
          )}
        </section>
      )}
    </div>
  );
}
