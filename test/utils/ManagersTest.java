package utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.HistoryManager;
import service.TaskManager;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    private static TaskManager taskManager;
    private static HistoryManager historyManager;

    @BeforeAll
    public static void beforeAll() {
        taskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void shouldReturnNonNullTaskManager() {
        assertNotNull(taskManager);
    }

    @Test
    public void shouldReturnNonNullHistoryManager() {
        assertNotNull(historyManager);
    }

}