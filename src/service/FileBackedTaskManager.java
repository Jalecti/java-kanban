package service;

import model.*;
import exceptions.ManagerSaveException;
import utils.Managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
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

    private static String toString(Task task) {
        String epicId = "";
        TaskTypes taskType = TaskTypes.TASK;
        if (task instanceof Subtask) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
            taskType = TaskTypes.SUBTASK;
        } else if (task instanceof Epic) {
            taskType = TaskTypes.EPIC;
        }
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(), taskType, task.getName(), task.getStatus(), task.getDescription(), epicId, task.getDuration().toMinutes(), task.getStartTime());
    }

    private static Task fromString(String value) {
        //value = "id,type,name,status,description,epicId,duration,startTime" - CSV
        String[] taskInfo = value.split(",");
        int taskId = Integer.parseInt(taskInfo[0]);
        String taskName = taskInfo[2];
        TaskStatus taskStatus = TaskStatus.valueOf(taskInfo[3]);
        String taskDescription = taskInfo[4];
        Duration taskDuration = Duration.ofMinutes(Integer.parseInt(taskInfo[6]));
        LocalDateTime taskStartTime = LocalDateTime.parse(taskInfo[7]);

        if (taskInfo[1].equals(TaskTypes.SUBTASK.toString())) {
            return new Subtask(taskId, taskName, taskDescription, taskStatus, Integer.parseInt(taskInfo[5]), taskDuration, taskStartTime);
        } else if (taskInfo[1].equals(TaskTypes.EPIC.toString())) {
            return new Epic(taskId, taskName, taskDescription, taskStatus, new ArrayList<>());
        }

        return new Task(taskId, taskName, taskDescription, taskStatus, taskDuration, taskStartTime);
    }

    private void save() {
        try (FileWriter fileWriter = new FileWriter(saveFile, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            Set<Task> allTasks = new TreeSet<>(Comparator.comparingInt(Task::getId));

            allTasks.addAll(getTaskList());
            allTasks.addAll(getEpicList());
            allTasks.addAll(getSubtaskList());

            String header = "id,type,name,status,description,epicId,duration,startTime";
            bufferedWriter.write(header);
            bufferedWriter.newLine();

            allTasks.stream().forEach(task -> {
                String taskInCSV = toString(task);
                try {
                    bufferedWriter.write(taskInCSV);
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) throws FileNotFoundException {
        List<Task> allTasksFromSave = new ArrayList<>();
        try (FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            boolean isFirst = true;
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                allTasksFromSave.add(fromString(line));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла");
        }

        FileBackedTaskManager newManager = Managers.getFileBacked(file);
        allTasksFromSave.stream().forEach(newManager::addTaskFromFile);
        if (!allTasksFromSave.isEmpty()) {
            newManager.setTaskCount(allTasksFromSave.getLast().getId());
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
        LocalDateTime now = LocalDateTime.now();
        Task task1 = new Task(1, "Task1", "Description task1", TaskStatus.NEW,
                Duration.ofMinutes(5), now);
        manager.addTask(task1);
        Epic epic1 = new Epic("Epic1", "Description epic1");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask(3, "Sub Task1", "Description sub task1", TaskStatus.DONE,
                2, Duration.ofMinutes(5), now.plusMinutes(10));
        manager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask(4, "Sub Task2", "Description sub task2", TaskStatus.DONE,
                2, Duration.ofMinutes(5), now.plusMinutes(20));
        manager.addSubtask(subtask2);

        for (int i = 0; i < 3; i++) {
            manager.addTask(new Task((5 + i), "UNIQname" + i, "UNIQdescr" + i, TaskStatus.IN_PROGRESS,
                    Duration.ofMinutes(5), now.plusMinutes(30 + 10 * i)));
        }

        TaskManager manager2 = FileBackedTaskManager.loadFromFile(saveFile);

        System.out.println("Задачи:");
        manager2.getTaskList().stream().forEach(System.out::println);
        System.out.println("Эпики:");
        manager2.getEpicList().stream().forEach(epic -> {
            System.out.println(epic);
            manager2.getSubtaskListOfEpic(epic.getId()).stream().forEach(task -> System.out.println("--> " + task));
        });
        System.out.println("Подзадачи:");
        manager2.getSubtaskList().stream().forEach(System.out::println);

        System.out.println("Соответствие списков у двух менеджеров:");
        System.out.println("TaskLists - " + manager.getTaskList().equals(manager2.getTaskList()));
        System.out.println("EpicLists - " + manager.getEpicList().equals(manager2.getEpicList()));
        System.out.println("SubtaskLists - " + manager.getSubtaskList().equals(manager2.getSubtaskList()));
    }
}
