import client from './client';

export interface MessageResponse {
  id: string;
  groupId: string;
  userId: string;
  username: string;
  content: string;
  createdAt: string;
}

export async function getMessages(groupId: string): Promise<MessageResponse[]> {
  const res = await client.get(`/api/groups/${groupId}/messages`);
  return res.data;
}

export async function sendMessage(groupId: string, content: string): Promise<MessageResponse> {
  const res = await client.post(`/api/groups/${groupId}/messages`, { content });
  return res.data;
}
