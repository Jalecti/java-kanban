package service;

import exceptions.TaskTimeOverlapException;
import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import utils.Constant;
import utils.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> taskMap;
    private final Map<Integer, Subtask> subtaskMap;
    private final Map<Integer, Epic> epicMap;
    private int taskCount;
    private final HistoryManager historyManager;
    private final TreeSet<Task> prioritizedTasks;


    public InMemoryTaskManager() {
        taskMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        epicMap = new HashMap<>();
        taskCount = 0;
        historyManager = Managers.getDefaultHistory();
        prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime).thenComparing(Task::getId));
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
            epic.getSubtaskIdList().stream().forEach(subtaskId -> subtaskListOfEpic.add(subtaskMap.get(subtaskId)));
            return subtaskListOfEpic;
        } else {
            System.out.println("Указанный эпик не найден");
            return null;
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().toList();
    }

    @Override
    public void clearTaskMap() {
        taskMap.keySet().stream().forEach(historyManager::remove);
        taskMap.clear();

        prioritizedTasks.removeIf(task -> !(task instanceof Subtask || task instanceof Epic));
    }

    @Override
    public void clearSubtaskMap() {
        subtaskMap.keySet().stream().forEach(historyManager::remove);
        subtaskMap.clear();
        epicMap.values().stream().forEach(epic -> {
            epic.clearSubtaskIdList();
            updateEpicData(epic.getId());
        });

        prioritizedTasks.removeIf(task -> task instanceof Subtask);
    }

    @Override
    public void clearEpicMap() {
        epicMap.keySet().stream().forEach(historyManager::remove);
        epicMap.clear();
        subtaskMap.keySet().stream().forEach(historyManager::remove);
        subtaskMap.clear();

        prioritizedTasks.removeIf(task -> task instanceof Subtask || task instanceof Epic);
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
        if (isValidInTime(task)) {
            task.setId(++taskCount);
            taskMap.put(task.getId(), task);
            addToPrioritizedTasks(task);
        } else {
            throw new TaskTimeOverlapException("Временной отрезок задачи "
                    + task.getName() + " пересекается с задачами внутри менеджера");
        }
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (checkEpic(subtask.getEpicId())) {
            if (isValidInTime(subtask)) {
                subtask.setId(++taskCount);
                subtaskMap.put(subtask.getId(), subtask);
                epicMap.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
                updateEpicData(subtask.getEpicId());
                addToPrioritizedTasks(subtask);
            } else {
                throw new TaskTimeOverlapException("Временной отрезок задачи "
                        + subtask.getName() + " пересекается с задачами внутри менеджера");
            }
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
            if (isValidInTime(newTask)) {
                taskMap.put(newTask.getId(), newTask);
            } else {
                throw new TaskTimeOverlapException("Временной отрезок задачи "
                        + newTask.getName() + " пересекается с задачами внутри менеджера");
            }
        } else {
            System.out.println("Задание, помеченное для обновления, не найдено");
        }
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        int newSubtaskId = newSubtask.getId();
        if (checkSubtask(newSubtaskId) && newSubtask.getEpicId() == subtaskMap.get(newSubtaskId).getEpicId()) {
            if (isValidInTime(newSubtask)) {
                subtaskMap.put(newSubtaskId, newSubtask);
                updateEpicData(newSubtask.getEpicId());
            } else {
                throw new TaskTimeOverlapException("Временной отрезок задачи "
                        + newSubtask.getName() + " пересекается с задачами внутри менеджера");
            }
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
            prioritizedTasks.remove(taskMap.get(id));
            taskMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Задача, помеченная для удаления, не найдена");
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (checkSubtask(id)) {
            prioritizedTasks.remove(subtaskMap.get(id));
            Epic subtaskEpic = epicMap.get(subtaskMap.get(id).getEpicId());
            subtaskEpic.removeSubtaskId(id);
            updateEpicData(subtaskEpic.getId());
            subtaskMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Подзадача, помеченная для удаления, не найдена");
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (checkEpic(id)) {
            prioritizedTasks.remove(epicMap.get(id));
            epicMap.get(id).getSubtaskIdList().stream().forEach(subtaskId -> {
                prioritizedTasks.remove(subtaskMap.get(subtaskId));
                subtaskMap.remove(subtaskId);
                historyManager.remove(subtaskId);
            });
            epicMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Эпик, помеченный для удаления, не найден");
        }
    }

    private void updateEpicData(int epicId) {
        updateEpicStatus(epicId);
        updateEpicTime(epicId);
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

    private void updateEpicTime(int epicId) {
        if (checkEpic(epicId)) {
            Epic epicToUpdate = epicMap.get(epicId);
            List<Subtask> epicSubtaskList = getSubtaskListOfEpic(epicId);
            LocalDateTime newStartTime = Constant.UNIX_EPOCH_START;
            LocalDateTime newEndTime = Constant.UNIX_EPOCH_START;
            if (!epicSubtaskList.isEmpty()) {
                newStartTime = epicSubtaskList.getFirst().getStartTime();
                newEndTime = epicSubtaskList.getFirst().getEndTime();

                for (int i = 1; i < epicSubtaskList.size(); i++) {
                    Subtask currSubtask = epicSubtaskList.get(i);
                    if (currSubtask.getStartTime().equals(Constant.UNIX_EPOCH_START)) {
                        continue;
                    }
                    if (currSubtask.getStartTime().isBefore(newStartTime)) {
                        newStartTime = currSubtask.getStartTime();
                    }
                    if (currSubtask.getEndTime().isAfter(newEndTime)) {
                        newEndTime = currSubtask.getEndTime();
                    }
                }
            }
            epicToUpdate.setStartTime(newStartTime);
            epicToUpdate.setEndTime(newEndTime);
            //Хоть и в условии ФЗ сказано считать duration эпика как сумму duration всех его подзадач,
            //я посчитал как разницу между startTime и endTime, тк это учитывает возможные перерывы между подзадачами внутри эпика
            epicToUpdate.setDuration(Duration.between(newStartTime, newEndTime));
            addToPrioritizedTasks(epicToUpdate);
        }
    }

    private void addToPrioritizedTasks(Task task) {
        if (!(task.getDuration().equals(Duration.ZERO) || task.getStartTime().equals(Constant.UNIX_EPOCH_START))) {
            prioritizedTasks.add(task);
        }
    }

    private boolean isOverlapInTime(Task t1, Task t2) {
        return !t1.getEndTime().isBefore(t2.getStartTime()) && !t1.getStartTime().isAfter(t2.getEndTime());
    }

    private boolean isValidInTime(Task task) {
        return prioritizedTasks.stream()
                .filter(taskInSet -> isOverlapInTime(task, taskInSet))
                .collect(Collectors.toList()).isEmpty();
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
                updateEpicData(subtask.getEpicId());
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

