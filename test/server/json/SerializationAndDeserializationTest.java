package server.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import utils.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SerializationAndDeserializationTest {
    private static Gson gson;

    private static TaskManager manager;

    private static Task task1;
    private static Task task2;
    private static Task task3;

    private static Epic epic;

    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Subtask subtask3;

    @BeforeAll
    static void beforeAll() {
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        manager = Managers.getDefault();

        LocalDateTime now = LocalDateTime.now();
        task1 = new Task("t1", "td1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        task2 = new Task("t2", "td2", TaskStatus.IN_PROGRESS, Duration.ofMinutes(10), null);
        task3 = new Task("t3", "td3", TaskStatus.DONE);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        epic = new Epic("e1", "ed1");
        manager.addEpic(epic);

        subtask1 = new Subtask("s1", "sd1", TaskStatus.NEW, 4, Duration.ofMinutes(5), now.plusMinutes(10));
        subtask2 = new Subtask("s2", "sd2", TaskStatus.IN_PROGRESS, 4, Duration.ofMinutes(5), null);
        subtask3 = new Subtask("s3", "sd3", TaskStatus.DONE, 4);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);
    }

    @Test
    public void shouldBeCorrectSerializeAndDeserializeTask1() {
        String task1Json = gson.toJson(task1);
        Task task1FromJson = gson.fromJson(task1Json, Task.class);
        assertEquals(task1, task1FromJson);
        assertEquals(task1.getName(), task1FromJson.getName());
        assertEquals(task1.getDescription(), task1FromJson.getDescription());
        assertEquals(task1.getStatus(), task1FromJson.getStatus());
        assertEquals(task1.getDuration(), task1FromJson.getDuration());
        assertEquals(task1.getStartTime(), task1FromJson.getStartTime());
        assertEquals(task1.getEndTime(), task1FromJson.getEndTime());
    }

    @Test
    public void shouldBeCorrectSerializeAndDeserializeTask2() {
        String task2Json = gson.toJson(task2);
        Task task2FromJson = gson.fromJson(task2Json, Task.class);
        assertEquals(task2, task2FromJson);
        assertEquals(task2.getName(), task2FromJson.getName());
        assertEquals(task2.getDescription(), task2FromJson.getDescription());
        assertEquals(task2.getStatus(), task2FromJson.getStatus());
        assertEquals(task2.getDuration(), task2FromJson.getDuration());
        assertEquals(task2.getStartTime(), task2FromJson.getStartTime());
        assertEquals(task2.getEndTime(), task2FromJson.getEndTime());
    }

    @Test
    public void shouldBeCorrectSerializeAndDeserializeTask3() {
        String task3Json = gson.toJson(task3);
        Task task3FromJson = gson.fromJson(task3Json, Task.class);
        assertEquals(task3, task3FromJson);
        assertEquals(task3.getName(), task3FromJson.getName());
        assertEquals(task3.getDescription(), task3FromJson.getDescription());
        assertEquals(task3.getStatus(), task3FromJson.getStatus());
        assertEquals(task3.getDuration(), task3FromJson.getDuration());
        assertEquals(task3.getStartTime(), task3FromJson.getStartTime());
        assertEquals(task3.getEndTime(), task3FromJson.getEndTime());
    }

    @Test
    public void shouldBeCorrectSerializeAndDeserializeEpic() {
        String epicJson = gson.toJson(epic);
        Epic epicFromJson = gson.fromJson(epicJson, Epic.class);
        assertEquals(epic, epicFromJson);
        assertEquals(epic.getName(), epicFromJson.getName());
        assertEquals(epic.getDescription(), epicFromJson.getDescription());
        assertEquals(epic.getStatus(), epicFromJson.getStatus());
        assertEquals(epic.getDuration(), epicFromJson.getDuration());
        assertEquals(epic.getStartTime(), epicFromJson.getStartTime());
        assertEquals(epic.getEndTime(), epicFromJson.getEndTime());
        assertEquals(epic.getSubtaskIdList(), epicFromJson.getSubtaskIdList());
    }

    @Test
    public void shouldBeCorrectSerializeAndDeserializeSubtask1() {
        String subtask1Json = gson.toJson(subtask1);
        Subtask subtask1FromJson = gson.fromJson(subtask1Json, Subtask.class);
        assertEquals(subtask1, subtask1FromJson);
        assertEquals(subtask1.getName(), subtask1FromJson.getName());
        assertEquals(subtask1.getDescription(), subtask1FromJson.getDescription());
        assertEquals(subtask1.getStatus(), subtask1FromJson.getStatus());
        assertEquals(subtask1.getDuration(), subtask1FromJson.getDuration());
        assertEquals(subtask1.getStartTime(), subtask1FromJson.getStartTime());
        assertEquals(subtask1.getEndTime(), subtask1FromJson.getEndTime());
        assertEquals(subtask1.getEpicId(), subtask1FromJson.getEpicId());
    }

    @Test
    public void shouldBeCorrectSerializeAndDeserializeSubtask2() {
        String subtask2Json = gson.toJson(subtask2);
        Subtask subtask2FromJson = gson.fromJson(subtask2Json, Subtask.class);
        assertEquals(subtask2, subtask2FromJson);
        assertEquals(subtask2.getName(), subtask2FromJson.getName());
        assertEquals(subtask2.getDescription(), subtask2FromJson.getDescription());
        assertEquals(subtask2.getStatus(), subtask2FromJson.getStatus());
        assertEquals(subtask2.getDuration(), subtask2FromJson.getDuration());
        assertEquals(subtask2.getStartTime(), subtask2FromJson.getStartTime());
        assertEquals(subtask2.getEndTime(), subtask2FromJson.getEndTime());
        assertEquals(subtask2.getEpicId(), subtask2FromJson.getEpicId());
    }

    @Test
    public void shouldBeCorrectSerializeAndDeserializeSubtask3() {
        String subtask3Json = gson.toJson(subtask3);
        Subtask subtask3FromJson = gson.fromJson(subtask3Json, Subtask.class);
        assertEquals(subtask3, subtask3FromJson);
        assertEquals(subtask3.getName(), subtask3FromJson.getName());
        assertEquals(subtask3.getDescription(), subtask3FromJson.getDescription());
        assertEquals(subtask3.getStatus(), subtask3FromJson.getStatus());
        assertEquals(subtask3.getDuration(), subtask3FromJson.getDuration());
        assertEquals(subtask3.getStartTime(), subtask3FromJson.getStartTime());
        assertEquals(subtask3.getEndTime(), subtask3FromJson.getEndTime());
        assertEquals(subtask3.getEpicId(), subtask3FromJson.getEpicId());
    }
}