import axios from 'axios';

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  withCredentials: true,
});

let isRefreshing = false;
let pendingRequests: (() => void)[] = [];

function onRefreshComplete() {
  pendingRequests.forEach((cb) => cb());
  pendingRequests = [];
}

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status !== 401 ||
      originalRequest._retry ||
      originalRequest.url?.includes('/auth/login') ||
      originalRequest.url?.includes('/auth/refresh') ||
      originalRequest.url?.includes('/auth/register') ||
      originalRequest.url?.includes('/auth/issueVerificationCode')
    ) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve) => {
        pendingRequests.push(() => {
          originalRequest._retry = true;
          resolve(client(originalRequest));
        });
      });
    }

    isRefreshing = true;
    originalRequest._retry = true;

    try {
      await axios.post(
        (import.meta.env.VITE_API_URL || '') + '/web/api/auth/refresh',
        null,
        { withCredentials: true }
      );
      onRefreshComplete();
      return client(originalRequest);
    } catch {
      pendingRequests = [];
      window.location.href = '/login';
      return Promise.reject(error);
    } finally {
      isRefreshing = false;
    }
  }
);

export default client;
