package utils;

import service.TaskManager;
import service.InMemoryTaskManager;
import service.HistoryManager;
import service.InMemoryHistoryManager;

public class Managers {
    private Managers() {}

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
