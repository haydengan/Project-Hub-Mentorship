import client from './client';

export interface UserProfile {
  id: string;
  username: string;
  email: string;
  role: string;
  isActive: boolean;
  registeredAt: string;
}

export async function getMyProfile(): Promise<UserProfile> {
  const res = await client.get('/api/user/profile');
  return res.data;
}

export async function updateUsername(username: string): Promise<UserProfile> {
  const res = await client.put('/api/user/profile', { username });
  return res.data;
}
