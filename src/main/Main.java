package main;

import model.Task;
import model.Subtask;
import model.Epic;
import model.TaskStatus;
import service.TaskManager;
import utils.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    static int printHistoryCount = 0;

    public static void main(String[] args) {
        firstScenario();
        //secondScenario();
    }

    static void firstScenario() {
        TaskManager manager = Managers.getDefault();
        //1: Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        //id = 1
        Task task1 = new Task("t1", "d1", TaskStatus.NEW);
        manager.addTask(task1);
        //id = 2
        Task task2 = new Task("t2", "d2", TaskStatus.NEW);
        manager.addTask(task2);
        //id = 3
        Epic epic1 = new Epic("e1", "d3");
        manager.addEpic(epic1);
        //id = 4
        Subtask subtask1 = new Subtask("st1", "d4", TaskStatus.NEW, 3);
        manager.addSubtask(subtask1);
        //id = 5
        Subtask subtask2 = new Subtask("st2", "d5", TaskStatus.IN_PROGRESS, 3);
        manager.addSubtask(subtask2);
        //id = 6
        Subtask subtask3 = new Subtask("st3", "d6", TaskStatus.DONE, 3);
        manager.addSubtask(subtask3);
        //id = 7
        Epic epic2 = new Epic("e2", "d7");
        manager.addEpic(epic2);

        //2: Запросите созданные задачи несколько раз в разном порядке.
        //&&
        //3: После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
        manager.getTask(1);
        manager.getTask(1);
        manager.getTask(2);
        manager.getEpic(3);
        manager.getSubtask(4);
        manager.getSubtask(5);
        manager.getSubtask(6);
        manager.getEpic(7);
        manager.getEpic(7);
        printHistory(manager); //История 1

        manager.getTask(1);
        manager.getTask(1);
        manager.getTask(1);
        printHistory(manager); //История 2

        //4: Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        manager.deleteTask(1);
        printHistory(manager); //История 3

        //5: Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        manager.deleteEpic(3);
        printHistory(manager); //История 4
    }

    static void secondScenario() {
        //Второй сценарий
        LocalDateTime now = LocalDateTime.now();
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task(1, "t1", "d1", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(40));
        Task task2 = new Task(2, "t2", "d2", TaskStatus.NEW, Duration.ofMinutes(5), now.plusMinutes(30));
        manager.addTask(task1);
        manager.addTask(task2);
        Epic epic1 = new Epic("e1", "d3");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask(4, "st1", "d1", TaskStatus.NEW, 3, Duration.ofMinutes(7), now.plusMinutes(20));
        manager.addSubtask(subtask1);
        //id = 5
        Subtask subtask2 = new Subtask(5, "st2", "d2", TaskStatus.IN_PROGRESS, 3, Duration.ofMinutes(5), now.plusMinutes(10));
        manager.addSubtask(subtask2);
        //id = 6
        Subtask subtask3 = new Subtask(6, "st3", "d3", TaskStatus.DONE, 3, Duration.ofMinutes(5), now);
        manager.addSubtask(subtask3);
        printAllTasks(manager);

        System.out.println("Приоритетный порядок задач:");
        manager.getPrioritizedTasks().stream().forEach(System.out::println);
        System.out.println("Длительность эпика:");
        System.out.println(manager.getEpic(3).getDuration());
    }

    static void printHistory(TaskManager manager) {
        System.out.printf("История %s:\n", ++printHistoryCount);
        manager.getHistory().stream().forEach(System.out::println);
    }

    static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        manager.getTaskList().stream().forEach(System.out::println);
        System.out.println("Эпики:");

        manager.getEpicList().stream().forEach(epic -> {
            System.out.println(epic);
            manager.getSubtaskListOfEpic(epic.getId()).stream().forEach(task -> System.out.println("--> " + task));
        });
        System.out.println("Подзадачи:");
        manager.getSubtaskList().stream().forEach(System.out::println);

        System.out.println("История:");
        manager.getHistory().stream().forEach(System.out::println);
    }

    static void printTitle(String str) {
        System.out.println("=".repeat(200));
        System.out.println(str);
        System.out.println("=".repeat(200));
    }
}
