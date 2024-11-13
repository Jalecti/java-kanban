package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> getterCallHistory;

    public InMemoryHistoryManager() {
        getterCallHistory = new ArrayList<>(10);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(getterCallHistory);
    }

    @Override
    public void addToHistory(Task task) {
        if (getterCallHistory.size() == 10) {
            getterCallHistory.removeFirst();
        }
        getterCallHistory.add(task);
    }
}
