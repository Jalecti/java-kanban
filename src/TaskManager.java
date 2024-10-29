import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private final HashMap<Integer, Task> taskMap;
    private final HashMap<Integer, Subtask> subtaskMap;
    private final HashMap<Integer, Epic> epicMap;
    private int taskCount;

    public TaskManager() {
        taskMap = new HashMap<>();
        subtaskMap = new HashMap<>();
        epicMap = new HashMap<>();
        taskCount = 0;
    }

    public ArrayList<Task> getTaskList() {
        return new ArrayList<>(taskMap.values());
    }

    public ArrayList<Subtask> getSubtaskList() {
        return new ArrayList<>(subtaskMap.values());
    }

    public ArrayList<Epic> getEpicList() {
        return new ArrayList<>(epicMap.values());
    }

    public void clearTaskMap() {
        taskMap.clear();
    }

    public void clearSubtaskMap() {
        ArrayList<Integer> subtaskIdsToDelete = new ArrayList<>(subtaskMap.keySet());
        for (Integer id : subtaskIdsToDelete) {
            deleteSubtask(id);
        }
    }

    public void clearEpicMap() {
        epicMap.clear();
        subtaskMap.clear();
    }

    public Task getTask(int id) {
        if (checkTask(id)) {
            return new Task(taskMap.get(id));
        } else {
            System.out.println("Указанная задача не найдена");
            return null;
        }
    }

    public Subtask getSubtask(int id) {
        if (checkSubtask(id)) {
            return new Subtask(subtaskMap.get(id));
        } else {
            System.out.println("Указанная подзадача не найдена");
            return null;
        }
    }

    public Epic getEpic(int id) {
        if (checkEpic(id)) {
            return new Epic(epicMap.get(id));
        } else {
            System.out.println("Указанный эпик не найден");
            return null;
        }
    }

    public void addTask(Task task) {
        task.setId(++taskCount);
        taskMap.put(task.getId(), task);
    }

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

    public void addEpic(Epic epic) {
        epic.setId(++taskCount);
        epicMap.put(epic.getId(), epic);
    }

    public void updateTask(Task newTask) {
        if (checkTask(newTask.getId())) {
            taskMap.put(newTask.getId(), newTask);
        } else {
            System.out.println("Задание, помеченное для обновления, не найдено");
        }
    }

    public void updateSubtask(Subtask newSubtask) {
        int newSubtaskId = newSubtask.getId();
        if (checkSubtask(newSubtaskId) && newSubtask.getEpicId() == subtaskMap.get(newSubtaskId).getEpicId()) {
            subtaskMap.put(newSubtaskId, newSubtask);
            updateEpicStatus(newSubtask.getEpicId());
        }
    }

    public void updateEpic(Epic epic) {
        if (checkEpic(epic.getId())) {
            Epic epicToUpdate = epicMap.get(epic.getId());
            epicToUpdate.setName(epic.getName());
            epicToUpdate.setDescription(epic.getDescription());
        } else {
            System.out.println("Эпик, помеченный для обновления, не найден");
        }
    }

    public void deleteTask(int id) {
        if (checkTask(id)) {
            taskMap.remove(id);
        } else {
            System.out.println("Задача, помеченная для удаления, не найдена");
        }
    }

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

    public ArrayList<Subtask> getSubtaskListOfEpic(Epic epic) {
        ArrayList<Subtask> subtaskListOfEpic = new ArrayList<>();
        for (int subtaskId : epic.getSubtaskIdList()) {
            subtaskListOfEpic.add(subtaskMap.get(subtaskId));
        }
        return subtaskListOfEpic;
    }

    private void updateEpicStatus(int epicId) {
        if (checkEpic(epicId)) {
            Epic epicToUpdate = epicMap.get(epicId);
            TaskStatus newStatus = TaskStatus.NEW;
            ArrayList<Subtask> epicSubtaskList = getSubtaskListOfEpic(epicToUpdate);
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

