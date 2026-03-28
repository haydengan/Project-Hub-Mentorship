import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import client from '../api/client';
import { getMyGroups, type GroupResponse } from '../api/groups';
import { getMyStreaks, type StreakResponse } from '../api/streaks';
import './DashboardPage.css';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [groups, setGroups] = useState<GroupResponse[]>([]);
  const [streaks, setStreaks] = useState<StreakResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [groupsData, streaksData] = await Promise.all([
          getMyGroups(),
          getMyStreaks(),
        ]);
        setGroups(groupsData);
        setStreaks(streaksData);
      } catch {
        setError('Failed to load dashboard. Please log in again.');
        navigate('/login');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [navigate]);

  async function handleLogout() {
    try {
      await client.post('/api/auth/logout');
    } finally {
      navigate('/login');
    }
  }

  if (loading) {
    return <div className="dashboard-loading">Loading...</div>;
  }

  // Top streaks (active ones only)
  const activeStreaks = streaks.filter(s => s.currentStreak > 0);

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1 className="dashboard-brand">Togetherly</h1>
        <button className="btn-logout" onClick={handleLogout}>Logout</button>
      </header>

      {error && <p className="dashboard-error">{error}</p>}

      {/* Streak Summary */}
      {activeStreaks.length > 0 && (
        <section className="dashboard-section">
          <h2>My Streaks</h2>
          <div className="streak-cards">
            {activeStreaks.map(s => (
              <div key={s.activityId} className="streak-card">
                <div className="streak-number">{s.currentStreak}</div>
                <div className="streak-label">day{s.currentStreak !== 1 ? 's' : ''}</div>
                <div className="streak-activity">{s.activityName}</div>
                <div className="streak-best">Best: {s.longestStreak}</div>
              </div>
            ))}
          </div>
        </section>
      )}

      {/* Groups */}
      <section className="dashboard-section">
        <div className="section-header">
          <h2>My Groups</h2>
          <div className="section-actions">
            <Link to="/groups/join" className="btn-secondary">Join Group</Link>
            <Link to="/groups/create" className="btn-primary-sm">Create Group</Link>
          </div>
        </div>

        {groups.length === 0 ? (
          <div className="empty-state">
            <p>You're not in any groups yet.</p>
            <p>Create a group or join one with an invite code to get started.</p>
          </div>
        ) : (
          <div className="group-cards">
            {groups.map(group => (
              <Link to={`/groups/${group.id}`} key={group.id} className="group-card">
                <div className="group-card-header">
                  <h3>{group.name}</h3>
                  <span className="member-count">
                    {group.memberCount}/{group.maxMembers}
                  </span>
                </div>
                {group.description && (
                  <p className="group-desc">{group.description}</p>
                )}
                <div className="group-card-footer">
                  <span className="invite-code">Code: {group.inviteCode}</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
