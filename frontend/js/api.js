const BASE = '/api/tasks';

async function request(url, options = {}) {
  const response = await fetch(url, options);
  if (!response.ok) {
    let message = `Erro ${response.status}`;
    try {
      const body = await response.json();
      if (body && body.message) {
        message = body.message;
      }
    } catch (_) {
      // Corpo não-JSON: mantém a mensagem padrão.
    }
    throw new Error(message);
  }
  if (response.status === 204) {
    return null;
  }
  return response.json();
}

export function listTasks() {
  return request(BASE);
}

export function createTask(task) {
  return request(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(task),
  });
}

export function updateTask(id, task) {
  return request(`${BASE}/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(task),
  });
}

export function deleteTask(id) {
  return request(`${BASE}/${id}`, { method: 'DELETE' });
}