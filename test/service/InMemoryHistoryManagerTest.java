package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private static HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void add() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", TaskStatus.NEW);
        historyManager.addToHistory(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    public void shouldBeSameTaskAfterAdd() {
        Task task = new Task(1, "name", "descr", TaskStatus.NEW);
        historyManager.addToHistory(task);
        Task taskFromHistory = historyManager.getHistory().getFirst();

        assertEquals(task.getName(), taskFromHistory.getName());
        assertEquals(task.getDescription(), taskFromHistory.getDescription());
        assertEquals(task.getStatus(), taskFromHistory.getStatus());
    }

    @Test
    public void shouldReturnSameTaskWhenTaskGetAgainFromHistory() {
        Task task1 = new Task(1, "name1", "d".repeat(1), TaskStatus.NEW);
        Task task2 = new Task(2, "name2", "d".repeat(2), TaskStatus.NEW);
        historyManager.addToHistory(task1);
        historyManager.addToHistory(task2);
        assertEquals(2, historyManager.getHistory().size());
        assertEquals(task2, historyManager.getHistory().getLast());
        historyManager.addToHistory(task1);
        historyManager.addToHistory(task1);
        assertEquals(task2, historyManager.getHistory().getFirst());
        assertEquals(task1, historyManager.getHistory().getLast());
        assertEquals(2, historyManager.getHistory().size());

    }

    @Test
    public void shouldRemoveTaskFromHistory() {
        Task task1 = new Task(1, "name", "descr", TaskStatus.NEW);
        Task task2 = new Task(2, "name", "descr", TaskStatus.NEW);
        historyManager.addToHistory(task1);
        historyManager.addToHistory(task2);
        assertEquals(task2, historyManager.getHistory().getLast());
        historyManager.remove(2);
        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task1, historyManager.getHistory().getFirst());
        assertEquals(task1, historyManager.getHistory().getLast());
        historyManager.remove(1);
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void shouldLinkFirstInEmptyHandMadeLinkedList() {
        Task task1 = new Task(1, "name", "descr", TaskStatus.NEW);
        InMemoryHistoryManager.HandMadeLinkedList<Task> handMadeLinkedList = new InMemoryHistoryManager.HandMadeLinkedList<>();
        var node = handMadeLinkedList.linkFirst(task1);
        assertEquals(task1, handMadeLinkedList.getFirst());
        assertEquals(task1, handMadeLinkedList.getLast());
    }

    @Test
    public void shouldLinkLastInEmptyHandMadeLinkedList() {
        Task task1 = new Task(1, "name", "descr", TaskStatus.NEW);
        InMemoryHistoryManager.HandMadeLinkedList<Task> handMadeLinkedList = new InMemoryHistoryManager.HandMadeLinkedList<>();
        handMadeLinkedList.linkLast(task1);
        assertEquals(task1, handMadeLinkedList.getFirst());
        assertEquals(task1, handMadeLinkedList.getLast());
    }

    @Test
    public void shouldReturnSize3OfHandMadeLinkedList() {
        Task task1 = new Task(1, "name", "descr", TaskStatus.NEW);
        Task task2 = new Task(2, "name", "descr", TaskStatus.NEW);
        Task task3 = new Task(3, "name", "descr", TaskStatus.NEW);
        InMemoryHistoryManager.HandMadeLinkedList<Task> handMadeLinkedList = new InMemoryHistoryManager.HandMadeLinkedList<>();
        handMadeLinkedList.linkLast(task1);
        handMadeLinkedList.linkLast(task2);
        handMadeLinkedList.linkLast(task3);
        assertEquals(task3, handMadeLinkedList.getLast());
        assertEquals(task1, handMadeLinkedList.getFirst());
        assertEquals(3, handMadeLinkedList.size());
    }

    @Test
    public void shouldReturnHistorySize3() {
        Task task1 = new Task(1, "name", "descr", TaskStatus.NEW);
        Task task2 = new Task(2, "name", "descr", TaskStatus.NEW);
        Task task3 = new Task(3, "name", "descr", TaskStatus.NEW);
        historyManager.addToHistory(task1);
        historyManager.addToHistory(task2);
        historyManager.addToHistory(task3);
        assertEquals(3, historyManager.getHistory().size());
    }

    @Test
    public void shouldReturnEmptyHistory() {
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void shouldReturnHistorySize2AfterRemove() {
        Task task1 = new Task(1, "name", "descr", TaskStatus.NEW);
        Task task2 = new Task(2, "name", "descr", TaskStatus.NEW);
        Task task3 = new Task(3, "name", "descr", TaskStatus.NEW);
        historyManager.addToHistory(task1);
        historyManager.addToHistory(task2);
        historyManager.addToHistory(task3);
        assertEquals(3, historyManager.getHistory().size());
        historyManager.remove(2);
        assertEquals(2, historyManager.getHistory().size());
        historyManager.remove(3);
        assertEquals(1, historyManager.getHistory().size());
        historyManager.remove(1);
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void shouldReturnHistorySize0AfterRemoveFromEmptyHistory() {
        assertEquals(0, historyManager.getHistory().size());
        historyManager.remove(2);
        assertEquals(0, historyManager.getHistory().size());
    }

    @Test
    public void shouldDeleteSubtasksFromHistoryWhenDeleteEpic() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("name1", "descr1");
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("name", "descr", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask("name", "descr", TaskStatus.NEW, 1);
        Subtask subtask3 = new Subtask("name", "descr", TaskStatus.NEW, 1);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
        taskManager.getEpic(1);
        taskManager.getSubtask(2);
        taskManager.getSubtask(3);
        taskManager.getSubtask(4);
        assertEquals(epic, taskManager.getHistory().getFirst());
        assertEquals(subtask3, taskManager.getHistory().getLast());
        assertEquals(4, taskManager.getHistory().size());
        taskManager.deleteSubtask(4);
        assertEquals(subtask2, taskManager.getHistory().getLast());
        assertEquals(3, taskManager.getHistory().size());

        taskManager.deleteEpic(1);
        assertEquals(0, taskManager.getHistory().size());
    }
}