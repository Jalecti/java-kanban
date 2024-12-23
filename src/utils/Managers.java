package utils;

import service.*;

import java.io.File;
import java.io.FileNotFoundException;

public class Managers {
    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTaskManager getFileBacked(File saveFile) throws FileNotFoundException {
        return new FileBackedTaskManager(saveFile);
    }
}
