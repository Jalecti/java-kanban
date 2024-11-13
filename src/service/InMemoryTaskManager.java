package service;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import utils.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> taskMap;
    private final HashMap<Integer, Subtask> subtaskMap;
    private final HashMap<Integer, Epic> epicMap;
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
    public ArrayList<Task> getTaskList() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public ArrayList<Subtask> getSubtaskList() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public ArrayList<Epic> getEpicList() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public ArrayList<Subtask> getSubtaskListOfEpic(int epicId) {
        ArrayList<Subtask> subtaskListOfEpic = new ArrayList<>();
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
        taskMap.clear();
    }

    @Override
    public void clearSubtaskMap() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.clearSubtaskIdList();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void clearEpicMap() {
        epicMap.clear();
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
        } else {
            System.out.println("Подзадача, помеченная для удаления, не найдена");
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (checkEpic(id)) {
            for (Integer subtaskId : epicMap.get(id).getSubtaskIdList()) {
                subtaskMap.remove(subtaskId);
            }
            epicMap.remove(id);
        } else {
            System.out.println("Эпик, помеченный для удаления, не найден");
        }
    }

    private void updateEpicStatus(int epicId) {
        if (checkEpic(epicId)) {
            Epic epicToUpdate = epicMap.get(epicId);
            TaskStatus newStatus = TaskStatus.NEW;
            ArrayList<Subtask> epicSubtaskList = getSubtaskListOfEpic(epicId);
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
}

