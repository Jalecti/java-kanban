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
        ArrayList<Integer> epicIdsToDelete = new ArrayList<>(epicMap.keySet());
        for (Integer id : epicIdsToDelete) {
            deleteEpic(id);
        }
    }

    public Task getTask(int id) {
        if (checkTask(id)) {
            return taskMap.get(id);
        } else {
            System.out.println("Указанная задача не найдена");
            return null;
        }
    }

    public Subtask getSubtask(int id) {
        if (checkSubtask(id)) {
            return subtaskMap.get(id);
        } else {
            System.out.println("Указанная подзадача не найдена");
            return null;
        }
    }

    public Epic getEpic(int id) {
        if (checkEpic(id)) {
            return epicMap.get(id);
        } else {
            System.out.println("Указанный эпик не найден");
            return null;
        }
    }

    public void createTask(Task task) {
        Task newTask = new Task(taskCount, task.getName(), task.getDescription(), task.getStatus());
        taskMap.put(newTask.getId(), newTask);
        ++taskCount;
    }

    public void createSubtask(Subtask subtask) {
        if (checkEpic(subtask.getEpicId())) {
            Subtask newSubtask = new Subtask(taskCount, subtask.getName(), subtask.getDescription(), subtask.getStatus(),
                    subtask.getEpicId());
            subtaskMap.put(newSubtask.getId(), newSubtask);
            ++taskCount;
            epicMap.get(newSubtask.getEpicId()).addSubtaskId(newSubtask.getId());
        } else {
            System.out.println("Указанный эпик не найден");
        }
    }

    public void createEpic(Epic epic) {
        Epic newEpic = new Epic(taskCount, epic.getName(), epic.getDescription(), defineEpicStatus(epic),
                epic.getSubtaskIdList());
        epicMap.put(newEpic.getId(), newEpic);
        ++taskCount;
    }

    public void updateTask(Task task) {
        if (checkTask(task.getId())) {
            taskMap.put(task.getId(), task);
        } else {
            System.out.println("Задание, помеченное для обновления, не найдено");
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (!checkEpic(subtask.getEpicId())) {
            System.out.println("Эпик, указанный в подзадании, не найден");
            return;
        }
        if (checkSubtask(subtask.getId())) {
            subtaskMap.put(subtask.getId(), subtask);
        } else {
            System.out.println("Подзадание, помеченное для обновления, не найдено");
        }
        Epic subtaskEpic = epicMap.get(subtask.getEpicId());
        updateEpic(new Epic(subtaskEpic.getId(), subtaskEpic.getName(), subtaskEpic.getDescription(),
                defineEpicStatus(subtaskEpic), subtaskEpic.getSubtaskIdList()));
    }

    public void updateEpic(Epic epic) {
        if (checkEpic(epic.getId())) {
            epicMap.put(epic.getId(), epic);
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
            updateEpic(new Epic(subtaskEpic.getId(), subtaskEpic.getName(), subtaskEpic.getDescription(),
                    defineEpicStatus(subtaskEpic), subtaskEpic.getSubtaskIdList()));
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

    private TaskStatus defineEpicStatus(Epic epic) {
        TaskStatus newStatus = TaskStatus.NEW;
        ArrayList<Subtask> epicSubtaskList = getSubtaskListOfEpic(epic);
        if (!epicSubtaskList.isEmpty()) {
            newStatus = epicSubtaskList.getFirst().getStatus();
            for (int i = 1; i < epicSubtaskList.size(); i++) {
                if (epicSubtaskList.get(i).getStatus() != newStatus) {
                    newStatus = TaskStatus.IN_PROGRESS;
                    break;
                }
            }
        }
        return newStatus;
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

