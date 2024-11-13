package main;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import service.TaskManager;
import utils.Managers;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        printTitle("Создание задач и добавление их в service.TaskManager...\n" + "Вызов геттеров для проверки истории...");
        Task task1 = new Task("Task1", "d".repeat(3), TaskStatus.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Task2", "d".repeat(4), TaskStatus.NEW);
        manager.addTask(task2);

        Epic epic1 = new Epic("Epic1", "d".repeat(5));
        manager.addEpic(epic1);
        Subtask subtask1e1 = new Subtask("Subtask1", "d".repeat(6), TaskStatus.NEW, 3);
        manager.addSubtask(subtask1e1);
        Subtask subtask2e1 = new Subtask("Subtask2", "d".repeat(7), TaskStatus.NEW, 3);
        manager.addSubtask(subtask2e1);

        Epic epic2 = new Epic("Epic2", "d".repeat(8));
        manager.addEpic(epic2);
        Subtask subtask1e2 = new Subtask("Subtask3", "d".repeat(9), TaskStatus.NEW, 6);
        manager.addSubtask(subtask1e2);

        for (int i = 0; i < 1; i++) {
            manager.getTask(1);
            manager.getSubtask(4);
            manager.getEpic(3);
        }
        manager.getEpic(6);
        manager.getEpic(6);

        printAllTasks(manager);

        printTitle("Обновление старых задач на новые с другим описанием и статусом...\n" +
                "Вызов дополнительных геттеров для проверки истории...");
        task1 = new Task(1, "Task1", "d".repeat(10), TaskStatus.DONE);
        manager.updateTask(task1);
        task2 = new Task(2, "Task2", "d".repeat(11), TaskStatus.IN_PROGRESS);
        manager.updateTask(task2);

        subtask1e1 = new Subtask(4, "Subtask1", "d".repeat(12), TaskStatus.DONE, 3);
        manager.updateSubtask(subtask1e1);

        subtask2e1 = new Subtask(5, "Subtask2", "d".repeat(13), TaskStatus.NEW, 3);
        manager.updateSubtask(subtask2e1);

        subtask1e2 = new Subtask(7, "Subtask3", "d".repeat(14), TaskStatus.DONE, 6);
        manager.updateSubtask(subtask1e2);

        for (int i = 0; i < 6; i++) {
            manager.getTask(1);
        }
        printAllTasks(manager);
    }

    static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTaskList()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpicList()) {
            System.out.println(epic);

            for (Task task : manager.getSubtaskListOfEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtaskList()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

    static void printTitle(String str) {
        System.out.println("=".repeat(200));
        System.out.println(str);
        System.out.println("=".repeat(200));
    }
}
