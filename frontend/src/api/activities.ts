import client from './client';

export interface ActivityResponse {
  id: string;
  name: string;
  type: string;
  groupId: string;
  createdBy: string;
  createdAt: string;
}

export interface ActivityLogResponse {
  id: string;
  userId: string;
  activityId: string;
  durationMins: number;
  note: string;
  loggedDate: string;
  createdAt: string;
}

export interface MemberDailyLog {
  userId: string;
  username: string;
  logs: ActivityLogResponse[];
}

export interface DailyGroupSummary {
  date: string;
  members: MemberDailyLog[];
}

export async function getGroupActivities(groupId: string): Promise<ActivityResponse[]> {
  const res = await client.get(`/api/groups/${groupId}/activities`);
  return res.data;
}

export async function createActivity(
  groupId: string, name: string, type: string
): Promise<ActivityResponse> {
  const res = await client.post(`/api/groups/${groupId}/activities`, { name, type });
  return res.data;
}

export async function logActivity(
  activityId: string, durationMins: number, note: string
): Promise<ActivityLogResponse> {
  const res = await client.post('/api/logs', { activityId, durationMins, note });
  return res.data;
}

export async function getGroupLogsToday(groupId: string): Promise<DailyGroupSummary> {
  const res = await client.get(`/api/groups/${groupId}/logs/today`);
  return res.data;
}

export async function getGroupLogsWeek(groupId: string): Promise<DailyGroupSummary[]> {
  const res = await client.get(`/api/groups/${groupId}/logs/week`);
  return res.data;
}
