const { test, expect } = require('@playwright/test');

async function clearTasks(request) {
  const response = await request.get('/api/tasks');
  if (response.status() === 200) {
    const tasks = await response.json();
    for (const task of tasks) {
      await request.delete(`/api/tasks/${task.id}`);
    }
  }
}

test.beforeEach(async ({ request }) => {
  await clearTasks(request);
});

test('persiste tarefas e status após recarregar a página', async ({ page, request }) => {
  await request.post('/api/tasks', { data: { title: 'Pendente viva', description: 'd1' } });
  await request.post('/api/tasks', { data: { title: 'Concluída viva', description: 'd2' } });
  const list = await (await request.get('/api/tasks')).json();
  const completed = list.find((t) => t.title === 'Concluída viva');
  await request.put(`/api/tasks/${completed.id}`, { data: { status: 'completed' } });

  await page.goto('/');
  await expect(page.locator('.task')).toHaveCount(2);

  await page.reload();

  await expect(page.locator('.task')).toHaveCount(2);
  await expect(page.locator('.task').filter({ hasText: 'Pendente viva' })).toHaveCount(1);
  const doneItem = page.locator('.task').filter({ hasText: 'Concluída viva' });
  await expect(doneItem.locator('.task__toggle')).toBeChecked();
});

test('exibe ordenação determinística pendentes primeiro e preserva após recarga', async ({ request, page }) => {
  const created = [];
  for (const title of ['A', 'B', 'C']) {
    created.push((await (await request.post('/api/tasks', { data: { title } })).json()));
  }
  // Conclui "B": agora ordem esperada na UI = C, A (pendentes) e depois B (concluída).
  await request.put(`/api/tasks/${created[1].id}`, { data: { status: 'completed' } });

  await page.goto('/');
  let titles = await page.locator('.task__title').allTextContents();
  expect(titles).toEqual(['C', 'A', 'B']);

  await page.reload();
  titles = await page.locator('.task__title').allTextContents();
  expect(titles).toEqual(['C', 'A', 'B']);
});