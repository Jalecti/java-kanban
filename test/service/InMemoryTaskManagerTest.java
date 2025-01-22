package service;

import exceptions.TaskTimeOverlapException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
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

    @Test
    public void shouldCalculateEpicStatusCorrectly() {
        taskManager.addEpic(new Epic("n1", "d1"));
        taskManager.addSubtask(new Subtask("n2", "d2", TaskStatus.NEW, 1));
        taskManager.addSubtask(new Subtask("n3", "d3", TaskStatus.NEW, 1));
        assertEquals(TaskStatus.NEW, taskManager.getEpic(1).getStatus());

        taskManager.updateSubtask(new Subtask(2, "n2", "d2", TaskStatus.DONE, 1));
        taskManager.updateSubtask(new Subtask(3, "n3", "d3", TaskStatus.DONE, 1));
        assertEquals(TaskStatus.DONE, taskManager.getEpic(1).getStatus());

        taskManager.updateSubtask(new Subtask(2, "n2", "d2", TaskStatus.NEW, 1));
        taskManager.updateSubtask(new Subtask(3, "n3", "d3", TaskStatus.DONE, 1));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(1).getStatus());

        taskManager.updateSubtask(new Subtask(2, "n2", "d2", TaskStatus.IN_PROGRESS, 1));
        taskManager.updateSubtask(new Subtask(3, "n3", "d3", TaskStatus.IN_PROGRESS, 1));
        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getEpic(1).getStatus());
    }

    @Test
    public void shouldCalculateEpicTimeCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        taskManager.addEpic(new Epic("n1", "d1"));
        assertNull(taskManager.getEpic(1).getStartTime());
        assertNull(taskManager.getEpic(1).getDuration());
        assertNull(taskManager.getEpic(1).getEndTime());

        taskManager.addSubtask(new Subtask(2, "n2", "d2", TaskStatus.NEW, 1, Duration.ofMinutes(5), now));
        assertEquals(taskManager.getSubtask(2).getStartTime(), taskManager.getEpic(1).getStartTime());
        assertEquals(taskManager.getSubtask(2).getDuration(), taskManager.getEpic(1).getDuration());
        assertEquals(taskManager.getSubtask(2).getEndTime(), taskManager.getEpic(1).getEndTime());

        taskManager.updateSubtask(new Subtask(2, "n2", "d2", TaskStatus.NEW, 1, Duration.ofMinutes(10), now.plusMinutes(10)));
        assertEquals(taskManager.getSubtask(2).getStartTime(), taskManager.getEpic(1).getStartTime());
        assertEquals(taskManager.getSubtask(2).getDuration(), taskManager.getEpic(1).getDuration());
        assertEquals(taskManager.getSubtask(2).getEndTime(), taskManager.getEpic(1).getEndTime());

        taskManager.addSubtask(new Subtask(3, "n3", "d3", TaskStatus.NEW, 1, Duration.ofMinutes(20), now.plusMinutes(21)));
        assertEquals(taskManager.getSubtask(2).getStartTime(), taskManager.getEpic(1).getStartTime());
        assertEquals(taskManager.getSubtask(3).getEndTime(), taskManager.getEpic(1).getEndTime());

        taskManager.deleteSubtask(2);
        taskManager.addSubtask(new Subtask(0, "n4", "d4", TaskStatus.NEW, 1));
        assertEquals(taskManager.getSubtask(3).getStartTime(), taskManager.getEpic(1).getStartTime());
        assertEquals(taskManager.getSubtask(3).getDuration(), taskManager.getEpic(1).getDuration());
        assertEquals(taskManager.getSubtask(3).getEndTime(), taskManager.getEpic(1).getEndTime());

    }

    @Test
    public void shouldCalculateTaskOverlapTimeCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        taskManager.addTask(new Task(1, "n1", "d1", TaskStatus.NEW, Duration.ofMinutes(5), now));
        assertThrows(TaskTimeOverlapException.class, () -> {
            taskManager.addTask(new Task(2, "n2", "d2", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(5)));
        });
        assertDoesNotThrow(() -> {
            taskManager.addTask(new Task(2, "n2", "d2", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(6)));
        });

        taskManager.addEpic(new Epic("n3", "d3"));
        taskManager.addSubtask(new Subtask(4, "n4", "d4", TaskStatus.NEW, 3, Duration.ofMinutes(20), now.plusMinutes(20)));
        assertThrows(TaskTimeOverlapException.class, () -> {
            taskManager.addTask(new Task(5, "n5", "d5", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(21)));
        });

        taskManager.deleteEpic(3);
        assertDoesNotThrow(() -> {
            taskManager.addTask(new Task(5, "n5", "d5", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(21)));
        });
    }

    @Test
    public void shouldReturnCorrectSizeOfPrioritizedTaskList() {
        LocalDateTime now = LocalDateTime.now();
        assertEquals(0, taskManager.getPrioritizedTasks().size());

        taskManager.addTask(new Task(1, "n1", "d1", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(60)));
        assertEquals(1, taskManager.getPrioritizedTasks().size());

        taskManager.addTask(new Task(2, "n2", "d2", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(50)));
        assertEquals(2, taskManager.getPrioritizedTasks().size());
        assertEquals(taskManager.getTask(2), taskManager.getPrioritizedTasks().getFirst());

        taskManager.addEpic(new Epic("n3", "d3"));
        taskManager.addSubtask(new Subtask(4, "n4", "d4", TaskStatus.NEW, 3, Duration.ofMinutes(5), now.plusMinutes(40)));
        assertEquals(3, taskManager.getPrioritizedTasks().size());
        assertEquals(taskManager.getSubtask(4), taskManager.getPrioritizedTasks().getFirst());
        assertEquals(taskManager.getTask(1), taskManager.getPrioritizedTasks().getLast());

        taskManager.addTask(new Task(0, "n0", "d0", TaskStatus.NEW));
        assertEquals(3, taskManager.getPrioritizedTasks().size());

        taskManager.deleteEpic(3);
        assertEquals(2, taskManager.getPrioritizedTasks().size());

        taskManager.clearTaskMap();
        assertEquals(0, taskManager.getPrioritizedTasks().size());
    }

    @Test
    public void shouldBeNullEpicTimeWhenAllSubtasksTimeIsNull() {
        taskManager.addEpic(new Epic("n1", "d1"));
        assertNull(taskManager.getEpic(1).getStartTime());
        assertNull(taskManager.getEpic(1).getEndTime());
        assertNull(taskManager.getEpic(1).getDuration());

        taskManager.addSubtask(new Subtask("n2", "d2", TaskStatus.NEW, 1));
        taskManager.addSubtask(new Subtask("n3", "d3", TaskStatus.NEW, 1));
        assertNull(taskManager.getEpic(1).getStartTime());
        assertNull(taskManager.getEpic(1).getEndTime());
        assertNull(taskManager.getEpic(1).getDuration());

        taskManager.addSubtask(new Subtask(4, "n4", "d4", TaskStatus.NEW, 1, Duration.ofMinutes(5), LocalDateTime.now()));
        assertEquals(taskManager.getSubtask(4).getStartTime(), taskManager.getEpic(1).getStartTime());
        assertEquals(taskManager.getSubtask(4).getEndTime(), taskManager.getEpic(1).getEndTime());
        assertEquals(taskManager.getSubtask(4).getDuration(), taskManager.getEpic(1).getDuration());

        taskManager.updateSubtask(new Subtask(4, "n4", "d4", TaskStatus.NEW, 1, null, null));
        assertNull(taskManager.getEpic(1).getStartTime());
        assertNull(taskManager.getEpic(1).getEndTime());
        assertNull(taskManager.getEpic(1).getDuration());
    }

    @Test
    public void shouldUpdateTaskInPrioritizedTaskListWhenUpdateTask() {
        LocalDateTime now = LocalDateTime.now();
        Task t1 = new Task(1, "t1", "d1", TaskStatus.NEW);
        Task t2 = new Task(2, "t2", "d2", TaskStatus.DONE);

        taskManager.addTask(t1);
        taskManager.addTask(t2);
        assertEquals(List.of(), taskManager.getPrioritizedTasks());

        Task t1New = new Task(1, "t1New", "d1New", TaskStatus.IN_PROGRESS, Duration.ofMinutes(5), now);
        Task t2New = new Task(2, "t2New", "d2New", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(6));
        taskManager.updateTask(t1New);
        taskManager.updateTask(t2New);

        assertEquals(List.of(t1New, t2New), taskManager.getPrioritizedTasks());
    }

    @Test
    public void shouldUpdateSubtaskInPrioritizedTaskListWhenUpdateSubtask() {
        LocalDateTime now = LocalDateTime.now();
        Epic epic = new Epic("e1", "ed1");
        Subtask st1 = new Subtask(2, "st1", "d1", TaskStatus.NEW, 1);
        Subtask st2 = new Subtask(3, "st2", "d2", TaskStatus.DONE, 1);

        taskManager.addEpic(epic);
        taskManager.addSubtask(st1);
        taskManager.addSubtask(st2);
        assertEquals(List.of(), taskManager.getPrioritizedTasks());

        Subtask st1New = new Subtask(2, "t1New", "d1New", TaskStatus.IN_PROGRESS, 1, Duration.ofMinutes(5), now);
        Subtask st2New = new Subtask(3, "t2New", "d2New", TaskStatus.NEW, 1, Duration.ofMinutes(5), now.plusMinutes(6));
        taskManager.updateSubtask(st1New);
        taskManager.updateSubtask(st2New);

        assertEquals(List.of(st1New, st2New), taskManager.getPrioritizedTasks());
    }
}