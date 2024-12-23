package service;

import model.*;
import exceptions.ManagerSaveException;
import utils.Managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) throws FileNotFoundException {
        super();
        if (saveFile == null || !saveFile.exists()) {
            throw new FileNotFoundException("Файл не найден: " + saveFile);
        }
        this.saveFile = saveFile;
    }

    public static String toString(Task task) {
        String epicId = "";
        TaskTypes taskType = TaskTypes.TASK;
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
            taskType = TaskTypes.SUBTASK;
        } else if (task instanceof Epic) {
            taskType = TaskTypes.EPIC;
        }
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(), taskType, task.getName(), task.getStatus(), task.getDescription(), epicId);
    }

    public static Task fromString(String value) {
        //value = "id,type,name,status,description,epicId" - CSV
        String[] taskInfo = value.split(",");
        int taskId = Integer.parseInt(taskInfo[0]);
        String taskName = taskInfo[2];
        TaskStatus taskStatus = TaskStatus.valueOf(taskInfo[3]);
        String taskDescription = taskInfo[4];

        if (taskInfo[1].equals(TaskTypes.SUBTASK.toString())) {
            return new Subtask(taskId, taskName, taskDescription, taskStatus, Integer.parseInt(taskInfo[5]));
        } else if (taskInfo[1].equals(TaskTypes.EPIC.toString())) {
            return new Epic(taskId, taskName, taskDescription, taskStatus, new ArrayList<>());
        }

        return new Task(taskId, taskName, taskDescription, taskStatus);
    }

    private void save() {
        try (FileWriter fileWriter = new FileWriter(saveFile, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            Set<Task> allTasks = new TreeSet<>(new Comparator<>() {
                @Override
                public int compare(Task o1, Task o2) {
                    return o1.getId() - o2.getId();
                }
            });

            allTasks.addAll(getTaskList());
            allTasks.addAll(getEpicList());
            allTasks.addAll(getSubtaskList());

            for (Task task : allTasks) {
                String taskInCSV = toString(task);
                bufferedWriter.write(taskInCSV);
                bufferedWriter.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) throws FileNotFoundException {
        List<Task> allTasksFromSave = new ArrayList<>();
        try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                allTasksFromSave.add(fromString(line));
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения из файла: " + e.getMessage());
        }

        FileBackedTaskManager newManager = Managers.getFileBacked(file);

        for (Task task : allTasksFromSave) {
            if (task instanceof Epic) {
                newManager.addEpic((Epic) task);
            } else if (task instanceof Subtask) {
                newManager.addSubtask((Subtask) task);
            } else {
                newManager.addTask(task);
            }
        }

        return newManager;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task newTask) {
        super.updateTask(newTask);
        save();
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void clearTaskMap() {
        super.clearTaskMap();
        save();
    }

    @Override
    public void clearSubtaskMap() {
        super.clearSubtaskMap();
        save();
    }

    @Override
    public void clearEpicMap() {
        super.clearEpicMap();
        save();
    }

    static void main(String[] args) throws FileNotFoundException {
        File saveFile = new File("saves\\save.csv");
        TaskManager manager = Managers.getFileBacked(saveFile);
        Task task1 = new Task("Task1", "Description task1", TaskStatus.NEW);
        manager.addTask(task1);
        Epic epic1 = new Epic("Epic1", "Description epic1");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Sub Task1", "Description sub task1", TaskStatus.DONE, 2);
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Sub Task2", "Description sub task2", TaskStatus.DONE, 2);
        manager.addSubtask(subtask2);

        for (int i = 0; i < 3; i++) {
            manager.addTask(new Task("UNIQname" + i, "UNIQdescr" + i, TaskStatus.IN_PROGRESS));
        }

        TaskManager manager2 = FileBackedTaskManager.loadFromFile(saveFile);

        System.out.println("Задачи:");
        for (Task task : manager2.getTaskList()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager2.getEpicList()) {
            System.out.println(epic);

            for (Task task : manager2.getSubtaskListOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager2.getSubtaskList()) {
            System.out.println(subtask);
        }

        System.out.println("Соответствие списков у двух менеджеров:");
        System.out.println("TaskLists - " + manager.getTaskList().equals(manager2.getTaskList()));
        System.out.println("EpicLists - " + manager.getEpicList().equals(manager2.getEpicList()));
        System.out.println("SubtaskLists - " + manager.getSubtaskList().equals(manager2.getSubtaskList()));
    }
}
