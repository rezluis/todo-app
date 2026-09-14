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

async function createViaUi(page, title, description) {
  await page.fill('#title-input', title);
  if (description !== undefined) {
    await page.fill('#description-input', description);
  }
  await page.click('button[type="submit"]');
}

test.beforeEach(async ({ request }) => {
  await clearTasks(request);
});

test('cria tarefa com título e descrição', async ({ page }) => {
  await page.goto('/');
  await createViaUi(page, 'Comprar leite', '2 litros');

  const item = page.locator('.task').first();
  await expect(item.locator('.task__title')).toHaveText('Comprar leite');
  await expect(item.locator('.task__description')).toHaveText('2 litros');
  await expect(item).not.toHaveClass(/task--completed/);
  await expect(item.locator('.task__toggle')).not.toBeChecked();
});

test('cria tarefa sem título usando padrão "Sem título"', async ({ page }) => {
  await page.goto('/');
  await createViaUi(page, '');

  await expect(page.locator('.task').first().locator('.task__title')).toHaveText('Sem título');
});

test('marca tarefa como concluída com um clique e desmarca', async ({ page }) => {
  await page.goto('/');
  await createViaUi(page, 'Estudar', '');

  const item = page.locator('.task').first();
  const toggle = item.locator('.task__toggle');

  await toggle.check();
  await expect(item).toHaveClass(/task--completed/);
  await expect(toggle).toBeChecked();

  await toggle.uncheck();
  await expect(item).not.toHaveClass(/task--completed/);
  await expect(toggle).not.toBeChecked();
});

test('edita título e descrição de uma tarefa', async ({ page }) => {
  await page.goto('/');
  await createViaUi(page, 'Título antigo', 'desc antiga');

  const item = page.locator('.task').first();
  await item.locator('.task__edit').click();
  await item.locator('.task__edit-title').fill('Título novo');
  await item.locator('.task__edit-description').fill('desc nova');
  await item.locator('.task__edit-form button[type="submit"]').click();

  await expect(item.locator('.task__title')).toHaveText('Título novo');
  await expect(item.locator('.task__description')).toHaveText('desc nova');
});

test('exclui tarefa e mostra estado vazio na última exclusão', async ({ page }) => {
  await page.goto('/');
  await createViaUi(page, 'Remover', '');

  await page.locator('.task .task__delete').click();
  await expect(page.locator('.task')).toHaveCount(0);
  await expect(page.locator('#empty-state')).toBeVisible();
});

test('formulário limpa os campos após criar', async ({ page }) => {
  await page.goto('/');
  await createViaUi(page, 'Limpar campos', '');

  await expect(page.locator('#title-input')).toHaveValue('');
  await expect(page.locator('#description-input')).toHaveValue('');
});