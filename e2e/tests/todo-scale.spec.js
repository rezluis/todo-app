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

test('permanece funcional com 100 tarefas (FR-012, SC-005)', async ({ page, request }) => {
  for (let i = 1; i <= 100; i += 1) {
    await request.post('/api/tasks', { data: { title: `Tarefa ${i}`, description: `desc ${i}` } });
  }

  await page.goto('/');
  await expect(page.locator('.task')).toHaveCount(100);

  const first = page.locator('.task').first();
  await first.locator('.task__toggle').check();
  await expect(first).toHaveClass(/task--completed/);

  await page.locator('.task .task__delete').nth(1).click();
  await expect(page.locator('.task')).toHaveCount(99);
});