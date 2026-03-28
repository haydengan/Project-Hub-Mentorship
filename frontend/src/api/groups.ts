import client from './client';

export interface GroupResponse {
  id: string;
  name: string;
  description: string;
  inviteCode: string;
  maxMembers: number;
  memberCount: number;
  createdAt: string;
}

export interface GroupMemberResponse {
  userId: string;
  username: string;
  email: string;
  role: 'ADMIN' | 'MEMBER';
  joinedAt: string;
}

export interface GroupDetailResponse {
  id: string;
  name: string;
  description: string;
  inviteCode: string;
  maxMembers: number;
  createdBy: string;
  createdAt: string;
  members: GroupMemberResponse[];
}

export async function getMyGroups(): Promise<GroupResponse[]> {
  const res = await client.get('/api/groups');
  return res.data;
}

export async function getGroupDetail(groupId: string): Promise<GroupDetailResponse> {
  const res = await client.get(`/api/groups/${groupId}`);
  return res.data;
}

export async function createGroup(name: string, description: string): Promise<GroupDetailResponse> {
  const res = await client.post('/api/groups', { name, description });
  return res.data;
}

export async function joinGroup(inviteCode: string): Promise<GroupResponse> {
  const res = await client.post('/api/groups/join', { inviteCode });
  return res.data;
}

export async function leaveGroup(groupId: string): Promise<void> {
  await client.delete(`/api/groups/${groupId}`);
}
