import { API_BASE_URL } from '../config.js';

async function parseResponse(response) {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export function createApiClient(token) {
  async function request(path, options = {}, withAuth = true) {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(withAuth && token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.headers || {})
      }
    });

    const data = await parseResponse(response);

    if (!response.ok) {
      const message = data?.message || data?.error || `API ${response.status}`;
      throw new Error(message);
    }

    return data;
  }

  return {
    get: (path, withAuth = true) => request(path, {}, withAuth),
    post: (path, body, withAuth = true) => request(
      path,
      { method: 'POST', body: JSON.stringify(body) },
      withAuth
    ),
    put: (path, body = {}, withAuth = true) => request(
      path,
      { method: 'PUT', body: JSON.stringify(body) },
      withAuth
    ),
    delete: (path, withAuth = true) => request(path, { method: 'DELETE' }, withAuth)
  };
}
