package service;

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
    public void shouldDeleteFirstWhenOverflow() {
        Task firstTask = new Task(1, "name", "descr", TaskStatus.NEW);
        historyManager.addToHistory(firstTask);
        Task task = new Task(2, "name", "descr", TaskStatus.NEW);
        for (int i = 0; i < 9; i++) {
            historyManager.addToHistory(task);
        }
        assertEquals(10, historyManager.getHistory().size());
        assertEquals(firstTask, historyManager.getHistory().getFirst());
        historyManager.addToHistory(task);
        assertNotEquals(firstTask, historyManager.getHistory().getFirst());
    }
}