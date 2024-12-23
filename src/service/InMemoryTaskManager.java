package service;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import utils.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> taskMap;
    private final Map<Integer, Subtask> subtaskMap;
    private final Map<Integer, Epic> epicMap;
    private int taskCount;
    private final HistoryManager historyManager;


    public InMemoryTaskManager() {
        taskMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        epicMap = new HashMap<>();
        taskCount = 0;
        historyManager = Managers.getDefaultHistory();
    }

    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public List<Subtask> getSubtaskList() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public List<Epic> getEpicList() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public List<Subtask> getSubtaskListOfEpic(int epicId) {
        List<Subtask> subtaskListOfEpic = new ArrayList<>();
        if (checkEpic(epicId)) {
            Epic epic = epicMap.get(epicId);
            for (int subtaskId : epic.getSubtaskIdList()) {
                subtaskListOfEpic.add(subtaskMap.get(subtaskId));
            }
            return subtaskListOfEpic;
        } else {
            System.out.println("Указанный эпик не найден");
            return null;
        }
    }

    @Override
    public void clearTaskMap() {
        for (int id : taskMap.keySet()) {
            historyManager.remove(id);
        }
        taskMap.clear();
    }

    @Override
    public void clearSubtaskMap() {
        for (int id : subtaskMap.keySet()) {
            historyManager.remove(id);
        }
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.clearSubtaskIdList();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void clearEpicMap() {
        for (int id : epicMap.keySet()) {
            historyManager.remove(id);
        }
        epicMap.clear();
        for (int id : subtaskMap.keySet()) {
            historyManager.remove(id);
        }
        subtaskMap.clear();
    }

    @Override
    public Task getTask(int id) {
        if (checkTask(id)) {
            Task targetTask = taskMap.get(id);
            historyManager.addToHistory(targetTask);
            return targetTask;
        } else {
            System.out.println("Указанная задача не найдена");
            return null;
        }
    }

    @Override
    public Subtask getSubtask(int id) {
        if (checkSubtask(id)) {
            Subtask targetSubtask = subtaskMap.get(id);
            historyManager.addToHistory(targetSubtask);
            return targetSubtask;
        } else {
            System.out.println("Указанная подзадача не найдена");
            return null;
        }
    }

    @Override
    public Epic getEpic(int id) {
        if (checkEpic(id)) {
            Epic targetEpic = epicMap.get(id);
            historyManager.addToHistory(targetEpic);
            return targetEpic;
        } else {
            System.out.println("Указанный эпик не найден");
            return null;
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public void addTask(Task task) {
        task.setId(++taskCount);
        taskMap.put(task.getId(), task);
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (checkEpic(subtask.getEpicId())) {
            subtask.setId(++taskCount);
            subtaskMap.put(subtask.getId(), subtask);
            epicMap.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
        } else {
            System.out.println("Указанный эпик не найден");
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(++taskCount);
        epicMap.put(epic.getId(), epic);
    }

    @Override
    public void updateTask(Task newTask) {
        if (checkTask(newTask.getId())) {
            taskMap.put(newTask.getId(), newTask);
        } else {
            System.out.println("Задание, помеченное для обновления, не найдено");
        }
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        int newSubtaskId = newSubtask.getId();
        if (checkSubtask(newSubtaskId) && newSubtask.getEpicId() == subtaskMap.get(newSubtaskId).getEpicId()) {
            subtaskMap.put(newSubtaskId, newSubtask);
            updateEpicStatus(newSubtask.getEpicId());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (checkEpic(epic.getId())) {
            Epic epicToUpdate = epicMap.get(epic.getId());
            epicToUpdate.setName(epic.getName());
            epicToUpdate.setDescription(epic.getDescription());
        } else {
            System.out.println("Эпик, помеченный для обновления, не найден");
        }
    }

    @Override
    public void deleteTask(int id) {
        if (checkTask(id)) {
            taskMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Задача, помеченная для удаления, не найдена");
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (checkSubtask(id)) {
            Epic subtaskEpic = epicMap.get(subtaskMap.get(id).getEpicId());
            subtaskEpic.removeSubtaskId(id);
            updateEpicStatus(subtaskEpic.getId());
            subtaskMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Подзадача, помеченная для удаления, не найдена");
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (checkEpic(id)) {
            for (Integer subtaskId : epicMap.get(id).getSubtaskIdList()) {
                subtaskMap.remove(subtaskId);
                historyManager.remove(subtaskId);
            }
            epicMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Эпик, помеченный для удаления, не найден");
        }
    }

    private void updateEpicStatus(int epicId) {
        if (checkEpic(epicId)) {
            Epic epicToUpdate = epicMap.get(epicId);
            TaskStatus newStatus = TaskStatus.NEW;
            List<Subtask> epicSubtaskList = getSubtaskListOfEpic(epicId);
            if (!epicSubtaskList.isEmpty()) {
                newStatus = epicSubtaskList.getFirst().getStatus();
                for (int i = 1; i < epicSubtaskList.size(); i++) {
                    if (epicSubtaskList.get(i).getStatus() != newStatus) {
                        newStatus = TaskStatus.IN_PROGRESS;
                        break;
                    }
                }
            }
            epicToUpdate.setStatus(newStatus);
        }
    }

    private boolean checkTask(int id) {
        return taskMap.containsKey(id);
    }

    private boolean checkSubtask(int id) {
        return subtaskMap.containsKey(id);
    }

    private boolean checkEpic(int id) {
        return epicMap.containsKey(id);
    }

    protected void addTaskFromFile(Task task) {
        if (task instanceof Epic) {
            epicMap.put(task.getId(), (Epic) task);
        } else if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            if (checkEpic(subtask.getEpicId())) {
                subtaskMap.put(subtask.getId(), subtask);
                epicMap.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
                updateEpicStatus(subtask.getEpicId());
            } else {
                System.out.println("Указанный эпик не найден");
            }
        } else {
            taskMap.put(task.getId(), task);
        }
    }

    protected void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

}

