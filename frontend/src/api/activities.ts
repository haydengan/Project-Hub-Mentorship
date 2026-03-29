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
  mediaUrl: string | null;
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
  activityId: string, durationMins: number, note: string, mediaUrl?: string
): Promise<ActivityLogResponse> {
  const res = await client.post('/api/logs', { activityId, durationMins, note, mediaUrl });
  return res.data;
}

export async function uploadMedia(file: File): Promise<{ url: string; filename: string }> {
  const formData = new FormData();
  formData.append('file', file);
  const res = await client.post('/api/media/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
}

export async function getGroupLogsToday(groupId: string): Promise<DailyGroupSummary> {
  const res = await client.get(`/api/groups/${groupId}/logs/today`);
  return res.data;
}

export async function getGroupLogsWeek(groupId: string, startDate?: string, endDate?: string): Promise<DailyGroupSummary[]> {
  const params: Record<string, string> = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  const res = await client.get(`/api/groups/${groupId}/logs/week`, { params });
  return res.data;
}
