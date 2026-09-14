import { listTasks, createTask, updateTask, deleteTask } from './api.js';

const form = document.getElementById('task-form');
const titleInput = document.getElementById('title-input');
const descriptionInput = document.getElementById('description-input');
const listEl = document.getElementById('task-list');
const emptyEl = document.getElementById('empty-state');
const errorEl = document.getElementById('error-message');

let tasks = [];
let errorTimer = null;

function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function showError(message) {
  errorEl.textContent = message;
  errorEl.hidden = false;
  clearTimeout(errorTimer);
  errorTimer = setTimeout(() => {
    errorEl.hidden = true;
  }, 5000);
}

function taskListItem(task) {
  const completed = task.status === 'completed';
  const toggleLabel = completed ? 'Marcar como pendente' : 'Marcar como concluída';
  const description = task.description
    ? `<span class="task__description">${escapeHtml(task.description)}</span>`
    : '';
  return `
    <li class="task ${completed ? 'task--completed' : ''}" data-id="${task.id}">
      <input type="checkbox" class="task__toggle" aria-label="${toggleLabel}" ${completed ? 'checked' : ''}>
      <div class="task__body">
        <span class="task__title">${escapeHtml(task.title)}</span>
        ${description}
      </div>
      <button type="button" class="task__edit" aria-label="Editar tarefa">Editar</button>
      <button type="button" class="task__delete" aria-label="Excluir tarefa">Excluir</button>
    </li>
  `;
}

function render() {
  listEl.innerHTML = tasks.map(taskListItem).join('');
  emptyEl.hidden = tasks.length > 0;
  errorEl.hidden = true;
}

async function reloadTasks() {
  try {
    tasks = await listTasks();
  } catch (err) {
    showError(`Não foi possível carregar as tarefas: ${err.message}`);
  }
  render();
}

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  const payload = {
    title: titleInput.value,
    description: descriptionInput.value || null,
  };
  try {
    await createTask(payload);
    titleInput.value = '';
    descriptionInput.value = '';
    await reloadTasks();
  } catch (err) {
    showError(err.message);
  }
});

listEl.addEventListener('change', async (event) => {
  const toggle = event.target.closest('.task__toggle');
  if (!toggle) {
    return;
  }
  const li = toggle.closest('.task');
  const id = Number(li.dataset.id);
  const task = tasks.find((item) => item.id === id);
  if (!task) {
    return;
  }
  const nextStatus = task.status === 'completed' ? 'pending' : 'completed';
  task.status = nextStatus;
  render();
  try {
    await updateTask(id, { status: nextStatus });
  } catch (err) {
    showError(err.message);
    await reloadTasks();
  }
});

listEl.addEventListener('click', async (event) => {
  if (event.target.closest('.task__delete')) {
    const li = event.target.closest('.task');
    const id = Number(li.dataset.id);
    try {
      await deleteTask(id);
      tasks = tasks.filter((item) => item.id !== id);
      render();
    } catch (err) {
      showError(err.message);
      await reloadTasks();
    }
    return;
  }

  if (event.target.closest('.task__edit')) {
    const li = event.target.closest('.task');
    const task = tasks.find((item) => item.id === Number(li.dataset.id));
    if (task) {
      startEdit(li, task);
    }
  }
});

function startEdit(li, task) {
  const originalHtml = taskListItem(task);
  li.innerHTML = `
    <form class="task__edit-form">
      <input type="text" class="task__edit-title" maxlength="120" value="${escapeHtml(task.title)}" aria-label="Título">
      <input type="text" class="task__edit-description" value="${task.description ? escapeHtml(task.description) : ''}" aria-label="Descrição">
      <button type="submit" class="task-form__submit">Salvar</button>
      <button type="button" class="task__delete js-edit-cancel">Cancelar</button>
    </form>
  `;
  const titleField = li.querySelector('.task__edit-title');
  titleField.focus();
  titleField.setSelectionRange(titleField.value.length, titleField.value.length);

  const editForm = li.querySelector('.task__edit-form');
  editForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    const payload = {
      title: titleField.value,
      description: editForm.querySelector('.task__edit-description').value || null,
    };
    try {
      await updateTask(task.id, payload);
      await reloadTasks();
    } catch (err) {
      showError(err.message);
    }
  });

  li.querySelector('.js-edit-cancel').addEventListener('click', () => {
    li.innerHTML = originalHtml;
  });
}

await reloadTasks();