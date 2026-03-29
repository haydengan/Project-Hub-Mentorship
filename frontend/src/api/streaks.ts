import client from './client';

export interface StreakResponse {
  userId: string;
  activityId: string;
  activityName: string;
  currentStreak: number;
  longestStreak: number;
  totalMinutes: number;
  lastLoggedDate: string | null;
}

export interface GroupLeaderboardEntry {
  userId: string;
  username: string;
  activityId: string;
  activityName: string;
  currentStreak: number;
  longestStreak: number;
  totalMinutes: number;
}

export async function getMyStreaks(): Promise<StreakResponse[]> {
  const res = await client.get('/api/users/me/streaks');
  return res.data;
}

export async function getGroupLeaderboard(groupId: string): Promise<GroupLeaderboardEntry[]> {
  const res = await client.get(`/api/groups/${groupId}/streaks`);
  return res.data;
}
