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

        String tasksString = String.format("%s%n%s%n%s%n%s%n%s%n",
                "id,type,name,status,description,epicId,duration,startTime",
                "1,TASK,Task1,NEW,Description task1,," + task1.getDuration().toMinutes() + "," + task1.getStartTime(),
                "2,EPIC,Epic1,DONE,Description epic1,," + epic1.getDuration().toMinutes() + "," + epic1.getStartTime(),
                "3,SUBTASK,Sub Task1,DONE,Description sub task1,2," + subtask1.getDuration().toMinutes() + "," + subtask1.getStartTime(),
                "4,SUBTASK,Sub Task2,DONE,Description sub task2,2," + subtask2.getDuration().toMinutes() + "," + subtask2.getStartTime());
        fileString = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertEquals(tasksString, fileString);
    }

    @Test
    public void shouldBeEmptyManagerWhenLoadFromEmptyFile() throws IOException {
        String fileString = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertTrue(fileString.isEmpty());
        assertDoesNotThrow(() -> fileBackedTaskManager = FileBackedTaskManager.loadFromFile(tempFile));
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
        String tasksString = String.format("%s%n%s%n%s%n%s%n%s%n",
                "id,type,name,status,description,epicId,duration,startTime",
                "1,TASK,Task1,NEW,Description task1,," + task1.getDuration().toMinutes() + "," + task1.getStartTime(),
                "2,EPIC,Epic1,DONE,Description epic1,," + epic1.getDuration().toMinutes() + "," + epic1.getStartTime(),
                "3,SUBTASK,Sub Task1,DONE,Description sub task1,2," + subtask1.getDuration().toMinutes() + "," + subtask1.getStartTime(),
                "4,SUBTASK,Sub Task2,DONE,Description sub task2,2," + subtask2.getDuration().toMinutes() + "," + subtask2.getStartTime());
        try (FileWriter fileWriter = new FileWriter(tempFile, StandardCharsets.UTF_8);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(tasksString);
        }
        String fileString = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8);
        assertEquals(tasksString, fileString);

        assertDoesNotThrow(() -> fileBackedTaskManager = FileBackedTaskManager.loadFromFile(tempFile));
        assertEquals(new ArrayList<>(List.of(task1)), fileBackedTaskManager.getTaskList());
        assertEquals(new ArrayList<>(List.of(epic1)), fileBackedTaskManager.getEpicList());
        assertEquals(new ArrayList<>(List.of(subtask1, subtask2)), fileBackedTaskManager.getSubtaskList());
    }
}