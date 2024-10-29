public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Task1", "d".repeat(3), TaskStatus.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Task2", "d".repeat(4), TaskStatus.NEW);
        taskManager.createTask(task2);


        Epic epic1 = new Epic("Epic1", "d".repeat(5));
        taskManager.createEpic(epic1);
        Subtask subtask1e1 = new Subtask("Subtask1", "d".repeat(6), TaskStatus.NEW, 2);
        taskManager.createSubtask(subtask1e1);
        Subtask subtask2e1 = new Subtask("Subtask2", "d".repeat(7), TaskStatus.NEW, 2);
        taskManager.createSubtask(subtask2e1);


        Epic epic2 = new Epic("Epic2", "d".repeat(8));
        taskManager.createEpic(epic2);
        Subtask subtask1e2 = new Subtask("Subtask3", "d".repeat(9), TaskStatus.NEW, 5);
        taskManager.createSubtask(subtask1e2);

        printAll(taskManager);

        task1 = new Task(0, "Task1", "d".repeat(10), TaskStatus.DONE);
        taskManager.updateTask(task1);
        task2 = new Task(1, "Task2", "d".repeat(11), TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task2);

        subtask1e1 = new Subtask(3, "Subtask1", "d".repeat(12), TaskStatus.NEW, 2);
        taskManager.updateSubtask(subtask1e1);

        subtask2e1 = new Subtask(4, "Subtask2", "d".repeat(13), TaskStatus.DONE, 2);
        taskManager.updateSubtask(subtask2e1);

        subtask1e2 = new Subtask(6, "Subtask3", "d".repeat(14), TaskStatus.IN_PROGRESS, 5);
        taskManager.updateSubtask(subtask1e2);

        printAll(taskManager);

//        Epic epicToUpdate = taskManager.getEpic(5);
//        Epic newEpic = new Epic(2, epicToUpdate.getName(), epicToUpdate.description, epicToUpdate.getStatus(),
//                epicToUpdate.getSubtaskIdList());
//        taskManager.updateEpic(newEpic);
        taskManager.deleteTask(taskManager.getTaskList().getFirst().getId());
        taskManager.deleteEpic(taskManager.getEpicList().getFirst().getId());

        printAll(taskManager);

        //taskManager.clearSubtaskMap();
        //taskManager.clearEpicMap();
        //printAll(taskManager);

    }

    static void printAll(TaskManager taskManager) {
        System.out.println("=".repeat(200));
        System.out.println(taskManager.getTaskList());
        System.out.println(taskManager.getSubtaskList());
        System.out.println(taskManager.getEpicList());
    }
}
