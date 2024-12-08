package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Managers;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

    @Test
    void addNewTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        taskManager.addTask(task);

        final Task savedTask = taskManager.getTask(task.getId());

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTaskList();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    public void shouldAddAndFoundTask() {
        Task task = new Task(1, "name", "descr", TaskStatus.NEW);
        taskManager.addTask(task);
        assertEquals(task, taskManager.getTaskList().getFirst());
        assertEquals(task, taskManager.getTask(task.getId()));
    }

    @Test
    public void shouldAddAndFoundSubtask() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        assertEquals(subtask, taskManager.getSubtask(subtask.getId()));
        assertEquals(subtask, taskManager.getSubtaskList().getFirst());
    }

    @Test
    public void shouldAddAndFoundEpic() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        taskManager.addEpic(epic);

        assertEquals(epic, taskManager.getEpic(epic.getId()));
        assertEquals(epic, taskManager.getEpicList().getFirst());
    }

    @Test
    public void shouldBeNoConflictBtwIds() {
        Task task = new Task(2, "name", "descr", TaskStatus.NEW);
        Epic epic = new Epic(2, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(1, "name", "descr", TaskStatus.NEW, 2);
        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);

        assertEquals(1, taskManager.getTaskList().getFirst().getId());
        assertEquals(2, taskManager.getEpicList().getFirst().getId());
        assertEquals(3, taskManager.getSubtaskList().getFirst().getId());
    }

    @Test
    public void shouldBeImmutableWhenAddToTaskManager() {
        Task task = new Task(1, "name1", "descr1", TaskStatus.DONE);
        taskManager.addTask(task);
        Task taskFromManager = taskManager.getTask(1);

        assertEquals(task.getName(), taskFromManager.getName());
        assertEquals(task.getDescription(), taskFromManager.getDescription());
        assertEquals(task.getStatus(), taskFromManager.getStatus());
    }

    @Test
    public void shouldBeEmptyWhenClearTaskMap() {
        Task task = new Task(1, "name1", "descr1", TaskStatus.DONE);
        taskManager.addTask(task);
        taskManager.clearTaskMap();
        assertTrue(taskManager.getTaskList().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenClearSubtaskMap() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        taskManager.clearSubtaskMap();
        assertTrue(taskManager.getSubtaskList().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenClearEpicMap() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        taskManager.clearEpicMap();
        assertTrue(taskManager.getSubtaskList().isEmpty());
        assertTrue(taskManager.getEpicList().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenDeleteTask() {
        Task task = new Task(1, "name1", "descr1", TaskStatus.DONE);
        taskManager.addTask(task);
        taskManager.deleteTask(1);
        assertTrue(taskManager.getTaskList().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenDeleteSubtask() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        taskManager.deleteSubtask(2);
        assertTrue(taskManager.getSubtaskList().isEmpty());
        assertTrue(taskManager.getSubtaskListOfEpic(1).isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenDeleteEpic() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        taskManager.deleteEpic(1);
        assertTrue(taskManager.getEpicList().isEmpty());
        assertTrue(taskManager.getSubtaskList().isEmpty());
    }

    @Test
    public void shouldBeUpdatedAfterTaskUpdate() {
        Task task = new Task(1, "name1", "descr1", TaskStatus.NEW);
        taskManager.addTask(task);
        Task taskToUpdate = new Task(1, "new_name", "new_descr", TaskStatus.IN_PROGRESS);
        taskManager.updateTask(taskToUpdate);
        Task updatedTaskFromManager = taskManager.getTask(1);
        assertEquals(taskToUpdate.getName(), updatedTaskFromManager.getName());
        assertEquals(taskToUpdate.getDescription(), updatedTaskFromManager.getDescription());
        assertEquals(taskToUpdate.getStatus(), updatedTaskFromManager.getStatus());
    }

    @Test
    public void shouldBeUpdatedAfterSubtaskUpdate() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Subtask subtaskToUpdate = new Subtask(2, "new_name", "new_descr", TaskStatus.IN_PROGRESS, 1);
        taskManager.updateSubtask(subtaskToUpdate);
        Subtask updatedSubtaskFromManager = taskManager.getSubtask(2);
        assertEquals(subtaskToUpdate.getName(), updatedSubtaskFromManager.getName());
        assertEquals(subtaskToUpdate.getDescription(), updatedSubtaskFromManager.getDescription());
        assertEquals(subtaskToUpdate.getStatus(), updatedSubtaskFromManager.getStatus());
    }

    @Test
    public void shouldBeUpdatedAfterEpicUpdate() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        Epic epicToUpdate = new Epic(1, "new_name", "new_descr", TaskStatus.NEW, new ArrayList<>());
        taskManager.updateEpic(epicToUpdate);
        Epic updatedEpicFromManager = taskManager.getEpic(1);
        assertEquals(epicToUpdate.getName(), updatedEpicFromManager.getName());
        assertEquals(epicToUpdate.getDescription(), updatedEpicFromManager.getDescription());
    }

    @Test
    public void shouldBeUpdatedStatusInEpicAfterSubtaskStatusUpdate() {
        Epic epic = new Epic(1, "name", "descr", TaskStatus.NEW, new ArrayList<>());
        Subtask subtask = new Subtask(2, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        assertEquals(TaskStatus.NEW, taskManager.getEpic(1).getStatus());
        Subtask subtaskToUpdate = new Subtask(2, "name", "descr", TaskStatus.DONE, 1);
        taskManager.updateSubtask(subtaskToUpdate);
        assertEquals(TaskStatus.DONE, taskManager.getEpic(1).getStatus());
        Subtask subtask2 = new Subtask(3, "name", "descr", TaskStatus.NEW, 1);
        taskManager.addSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(1).getStatus());
    }

    @Test
    public void shouldEmptyHistoryWhenClearTaskMap() {
        for (int i = 1; i <= 10; i++) {
            taskManager.addTask(new Task("name" + i, "desc" + i, TaskStatus.NEW));
        }
        assertEquals(10, taskManager.getTaskList().size());
        for (int i = 1; i <= 10; i++) {
            taskManager.getTask(i);
        }
        assertEquals(10, taskManager.getHistory().size());

        taskManager.clearTaskMap();
        assertEquals(0, taskManager.getTaskList().size());
        assertEquals(0, taskManager.getHistory().size());
    }

    @Test
    public void shouldEmptyHistoryWhenClearEpicMap() {
        for (int i = 1; i <= 10; i++) {
            taskManager.addEpic(new Epic("name" + i, "desc" + i));
        }
        assertEquals(10, taskManager.getEpicList().size());
        for (int i = 1; i <= 10; i++) {
            taskManager.getEpic(i);
        }
        assertEquals(10, taskManager.getHistory().size());

        taskManager.clearEpicMap();
        assertEquals(0, taskManager.getEpicList().size());
        assertEquals(0, taskManager.getHistory().size());
    }

    @Test
    public void shouldEmptyHistoryWhenClearSubtaskMap() {
        taskManager.addEpic(new Epic("epic", "desc"));
        for (int i = 1; i <= 10; i++) {
            taskManager.addSubtask(new Subtask("name" + i, "desc" + i, TaskStatus.NEW, 1));
        }
        assertEquals(1, taskManager.getEpicList().size());
        assertEquals(10, taskManager.getSubtaskList().size());
        for (int i = 2; i <= 11; i++) {
            taskManager.getSubtask(i);
        }
        assertEquals(10, taskManager.getHistory().size());

        taskManager.clearSubtaskMap();
        assertEquals(0, taskManager.getSubtaskList().size());
        assertEquals(0, taskManager.getHistory().size());
    }

    @Test
    public void shouldEmptyHistoryWhenGetSubtasksAndClearEpicMap() {
        taskManager.addEpic(new Epic("epic", "desc"));
        for (int i = 1; i <= 10; i++) {
            taskManager.addSubtask(new Subtask("name" + i, "desc" + i, TaskStatus.NEW, 1));
        }
        assertEquals(1, taskManager.getEpicList().size());
        assertEquals(10, taskManager.getSubtaskList().size());
        for (int i = 2; i <= 11; i++) {
            taskManager.getSubtask(i);
        }
        assertEquals(10, taskManager.getHistory().size());

        taskManager.clearEpicMap();
        assertEquals(0, taskManager.getSubtaskList().size());
        assertEquals(0, taskManager.getHistory().size());
    }

}