public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Task1", "d".repeat(3), TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Task2", "d".repeat(4), TaskStatus.NEW);
        taskManager.addTask(task2);


        Epic epic1 = new Epic("Epic1", "d".repeat(5));
        taskManager.addEpic(epic1);
        Subtask subtask1e1 = new Subtask("Subtask1", "d".repeat(6), TaskStatus.NEW, 3);
        taskManager.addSubtask(subtask1e1);
        Subtask subtask2e1 = new Subtask("Subtask2", "d".repeat(7), TaskStatus.NEW, 3);
        taskManager.addSubtask(subtask2e1);


        Epic epic2 = new Epic("Epic2", "d".repeat(8));
        taskManager.addEpic(epic2);
        Subtask subtask1e2 = new Subtask("Subtask3", "d".repeat(9), TaskStatus.NEW, 6);
        taskManager.addSubtask(subtask1e2);

        printAll(taskManager);

        task1 = new Task(1, "Task1", "d".repeat(10), TaskStatus.DONE);
        taskManager.updateTask(task1);
        task2 = new Task(2, "Task2", "d".repeat(11), TaskStatus.IN_PROGRESS);
        taskManager.updateTask(task2);

        subtask1e1 = new Subtask(4, "Subtask1", "d".repeat(12), TaskStatus.DONE, 3);
        taskManager.updateSubtask(subtask1e1);

        subtask2e1 = new Subtask(5, "Subtask2", "d".repeat(13), TaskStatus.NEW, 3);
        taskManager.updateSubtask(subtask2e1);

        subtask1e2 = new Subtask(7, "Subtask3", "d".repeat(14), TaskStatus.DONE, 6);
        taskManager.updateSubtask(subtask1e2);

        printAll(taskManager);

        //Subtask subtask123 = new Subtask("123", "Desc123", TaskStatus.NEW, 6);
        //taskManager.addSubtask(subtask123);
        //printAll(taskManager);
        //taskManager.deleteSubtask(8);
        //Epic epicToUpdate = taskManager.getEpic(6);
//        epicToUpdate.setDescription("asd");
//        epicToUpdate.setName("UPDATED EPIC2");
//        taskManager.updateEpic(epicToUpdate);
//        printAll(taskManager);

        //taskManager.clearTaskMap();
        //taskManager.clearSubtaskMap();
        //taskManager.clearEpicMap();
        //taskManager.getTask(1).setName("1234");
        //printAll(taskManager);


    }

    static void printAll(TaskManager taskManager) {
        System.out.println("=".repeat(200));
        System.out.println(taskManager.getTaskList());
        System.out.println(taskManager.getSubtaskList());
        System.out.println(taskManager.getEpicList());
    }
}
