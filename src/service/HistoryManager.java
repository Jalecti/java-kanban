package service;

import model.Task;

import java.util.List;

public interface HistoryManager {
    void addToHistory(Task task);

    void remove(int id);

    List<Task> getHistory();
}
