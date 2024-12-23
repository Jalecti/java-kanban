package service;

import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Managers;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private static File tempFile;
    private static TaskManager fileBackedTaskManager;

    @BeforeAll
    public static void beforeAll() throws IOException {
        tempFile = File.createTempFile("tempSaveFile", ".csv");
        tempFile.deleteOnExit();
    }

    @BeforeEach
    public void beforeEach() throws IOException {
        if (tempFile.delete()) {
            tempFile = File.createTempFile("tempSaveFile", ".csv");
            tempFile.deleteOnExit();
        }
        fileBackedTaskManager = Managers.getFileBacked(tempFile);
    }

    @Test
    public void shouldBeRightStringWhenCallToStringTask() {
        Task task1 = new Task(1, "Task1", "Description task1", TaskStatus.NEW);
        String task1String = FileBackedTaskManager.toString(task1);
        assertEquals("1,TASK,Task1,NEW,Description task1,", task1String);
    }

    @Test
    public void shouldBeRightStringWhenCallToStringSubtask() {
        Subtask subtask1 = new Subtask(1, "Sub Task1", "Description sub task1", TaskStatus.DONE, 2);
        String subtask1String = FileBackedTaskManager.toString(subtask1);
        assertEquals("1,SUBTASK,Sub Task1,DONE,Description sub task1,2", subtask1String);
    }

    @Test
    public void shouldBeRightTaskWhenCallTaskFromString() {
        Task task1 = new Task(1, "Task1", "Description task1", TaskStatus.NEW);
        String task1String = "1,TASK,Task1,NEW,Description task1,";
        Task task1FromString = FileBackedTaskManager.fromString(task1String);
        assertEquals(task1.getId(), task1FromString.getId());
        assertEquals(task1.getName(), task1FromString.getName());
        assertEquals(task1.getDescription(), task1FromString.getDescription());
        assertEquals(task1.getStatus(), task1FromString.getStatus());
    }

    @Test
    public void shouldBeRightSubtaskWhenCallSubtaskFromStirng() {
        Subtask subtask1 = new Subtask(1, "Sub Task1", "Description sub task1", TaskStatus.DONE, 2);
        String subtask1String = "1,SUBTASK,Sub Task1,DONE,Description sub task1,2";
        Subtask subtask1FromString = (Subtask) FileBackedTaskManager.fromString(subtask1String);
        assertEquals(subtask1.getId(), subtask1FromString.getId());
        assertEquals(subtask1.getName(), subtask1FromString.getName());
        assertEquals(subtask1.getDescription(), subtask1FromString.getDescription());
        assertEquals(subtask1.getStatus(), subtask1FromString.getStatus());
        assertEquals(subtask1.getEpicId(), subtask1FromString.getEpicId());
    }

    @Test
    public void shouldSaveManagerInEmptyFile() throws IOException {
        String fileString = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(fileString.isEmpty());

        Task task1 = new Task("Task1", "Description task1", TaskStatus.NEW);
        fileBackedTaskManager.addTask(task1);
        Epic epic1 = new Epic("Epic1", "Description epic1");
        fileBackedTaskManager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Sub Task1", "Description sub task1", TaskStatus.DONE, 2);
        fileBackedTaskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Sub Task2", "Description sub task2", TaskStatus.DONE, 2);
        fileBackedTaskManager.addSubtask(subtask2);

        String tasksString = String.format("%s%n%s%n%s%n%s%n",
                FileBackedTaskManager.toString(task1),
                FileBackedTaskManager.toString(epic1),
                FileBackedTaskManager.toString(subtask1),
                FileBackedTaskManager.toString(subtask2));
        fileString = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertEquals(tasksString, fileString);
    }

    @Test
    public void shouldBeEmptyManagerWhenLoadFromEmptyFile() throws IOException {
        String fileString = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(fileString.isEmpty());
        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(fileBackedTaskManager.getTaskList().isEmpty());
        assertTrue(fileBackedTaskManager.getEpicList().isEmpty());
        assertTrue(fileBackedTaskManager.getSubtaskList().isEmpty());
    }

    @Test
    public void shouldLoadFromFile() throws IOException {
        Task task1 = new Task(1, "Task1", "Description task1", TaskStatus.NEW);
        Epic epic1 = new Epic(2, "Epic1", "Description epic1", TaskStatus.DONE, new ArrayList<>());
        Subtask subtask1 = new Subtask(3, "Sub Task1", "Description sub task1", TaskStatus.DONE, 2);
        Subtask subtask2 = new Subtask(4, "Sub Task2", "Description sub task2", TaskStatus.DONE, 2);
        String tasksString = String.format("%s%n%s%n%s%n%s%n",
                FileBackedTaskManager.toString(task1),
                FileBackedTaskManager.toString(epic1),
                FileBackedTaskManager.toString(subtask1),
                FileBackedTaskManager.toString(subtask2));
        try (FileWriter fileWriter = new FileWriter(tempFile, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(tasksString);
        }
        String fileString = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertEquals(tasksString, fileString);

        fileBackedTaskManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(new ArrayList<>(List.of(task1)), fileBackedTaskManager.getTaskList());
        assertEquals(new ArrayList<>(List.of(epic1)), fileBackedTaskManager.getEpicList());
        assertEquals(new ArrayList<>(List.of(subtask1, subtask2)), fileBackedTaskManager.getSubtaskList());
    }
}