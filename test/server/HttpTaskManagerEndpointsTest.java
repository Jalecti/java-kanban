package server;

import com.google.gson.Gson;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import server.json.EpicListTypeToken;
import server.json.SubtaskListTypeToken;
import server.json.TaskListTypeToken;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEndpointsTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    LocalDateTime now = LocalDateTime.now();

    public HttpTaskManagerEndpointsTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.clearTaskMap();
        manager.clearSubtaskMap();
        manager.clearEpicMap();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    //=================================================TASKS============================================================
    @Test
    public void shouldAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTaskList();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldReturn406WhenAddTaskWithOverlap() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        manager.addTask(task1);

        Task task2 = new Task("T2", "TD2", TaskStatus.DONE, Duration.ofMinutes(5), now.plusMinutes(3));
        String task2Json = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(task2Json)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void shouldGetTasks() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        Task task2 = new Task("T2", "TD2", TaskStatus.DONE, Duration.ofMinutes(5), now.plusMinutes(6));
        manager.addTask(task1);
        manager.addTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromResponse = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        List<Task> tasksFromManager = manager.getTaskList();

        assertEquals(2, tasksFromResponse.size());
        assertEquals(2, tasksFromManager.size());
        assertEquals(tasksFromManager, tasksFromResponse);
    }

    @Test
    public void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task taskFromServer = gson.fromJson(new String(response.body().getBytes(), StandardCharsets.UTF_8), Task.class);
        assertEquals(1, taskFromServer.getId());
    }

    @Test
    public void shouldReturn404WhenGetTaskWithWrongId() throws IOException, InterruptedException {
        List<Task> tasksFromManager = manager.getTaskList();
        assertEquals(0, tasksFromManager.size());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        manager.addTask(task);

        Task taskToUpdt = new Task(1, "T1updt", "TD1updt", TaskStatus.IN_PROGRESS, Duration.ofMinutes(6), now.plusMinutes(5));
        String taskToUpdtJson = gson.toJson(taskToUpdt);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskToUpdtJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task task1 = manager.getTask(1);
        assertEquals("T1updt", task1.getName());
        assertEquals("TD1updt", task1.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, task1.getStatus());
        assertEquals(Duration.ofMinutes(6), task1.getDuration());
        assertEquals(now.plusMinutes(5), task1.getStartTime());
    }

    @Test
    public void shouldReturn406WhenUpdateTaskWithOverlap() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        Task task2 = new Task("T2", "TD2", TaskStatus.DONE, Duration.ofMinutes(5), now.plusMinutes(6));
        manager.addTask(task1);
        manager.addTask(task2);

        Task task2ToUpdate = new Task(2, "T2", "TD2", TaskStatus.DONE, Duration.ofMinutes(5), now.plusMinutes(3));
        String task2ToUpdateJson = gson.toJson(task2ToUpdate);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(task2ToUpdateJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void shouldReturn404WhenUpdateTaskWithWrongId() throws IOException, InterruptedException {
        Task task = new Task(1, "T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getTaskList().size());
    }

    @Test
    public void shouldReturn404WhenDeleteTaskWithWrongId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldReturn404WhenUnknownTasksEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/unknown");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Такого эндпоинта tasks не существует", new String(response.body().getBytes()));
    }

    //=================================================EPICS============================================================
    @Test
    public void shouldAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("en1", "ed1");
        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicList = manager.getEpicList();
        assertNotNull(epicList, "Задачи не возвращаются");
        assertEquals(1, epicList.size(), "Некорректное количество задач");
        assertEquals("en1", epicList.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("en1", "ed1");
        Epic epic2 = new Epic("en2", "ed2");
        manager.addEpic(epic1);
        manager.addEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromResponse = gson.fromJson(response.body(), new EpicListTypeToken().getType());
        List<Epic> epicsFromManager = manager.getEpicList();
        assertEquals(2, epicsFromResponse.size());
        assertEquals(2, epicsFromManager.size());
        assertEquals(epicsFromManager, epicsFromResponse);
    }

    @Test
    public void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic1 = new Epic("en1", "ed1");
        manager.addEpic(epic1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic epicFromServer = gson.fromJson(new String(response.body().getBytes(), StandardCharsets.UTF_8), Epic.class);
        assertEquals(1, epicFromServer.getId());
    }

    @Test
    public void shouldReturn404WhenGetEpicByIdWithWrongId() throws IOException, InterruptedException {
        Epic epic1 = new Epic("en1", "ed1");
        manager.addEpic(epic1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("en1", "ed1");
        manager.addEpic(epic1);

        Epic epic1ToUpdt = new Epic(1, "T1updt", "TD1updt", TaskStatus.NEW, new ArrayList<>());
        String epic1ToUpdtJson = gson.toJson(epic1ToUpdt);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epic1ToUpdtJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Epic task1 = manager.getEpic(1);
        assertEquals("T1updt", task1.getName());
        assertEquals("TD1updt", task1.getDescription());
        assertEquals(TaskStatus.NEW, task1.getStatus());
        assertNull(task1.getDuration());
        assertNull(task1.getStartTime());
    }

    @Test
    public void shouldReturn404WhenUpdateEpicWithWrongId() throws IOException, InterruptedException {
        Epic epic1 = new Epic("en1", "ed1");
        manager.addEpic(epic1);

        Epic epic1ToUpdt = new Epic(2, "T1updt", "TD1updt", TaskStatus.NEW, new ArrayList<>());
        String epic1ToUpdtJson = gson.toJson(epic1ToUpdt);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epic1ToUpdtJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldDeleteEpic() throws IOException, InterruptedException {
        Epic epic1 = new Epic("en1", "ed1");
        manager.addEpic(epic1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getEpicList().size());
    }

    @Test
    public void shouldReturn404WhenDeleteEpicWithWrongId() throws IOException, InterruptedException {
        Epic epic1 = new Epic("en1", "ed1");
        manager.addEpic(epic1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals(1, manager.getEpicList().size());
    }

    @Test
    public void shouldGetSubtasksIdList() throws IOException, InterruptedException {
        manager.addEpic(new Epic("en1", "ed1"));
        manager.addSubtask(new Subtask("s1", "d1", TaskStatus.NEW, 1));
        manager.addSubtask(new Subtask("s2", "d2", TaskStatus.NEW, 1));
        manager.addSubtask(new Subtask("s3", "d3", TaskStatus.NEW, 1));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(3, manager.getEpic(1).getSubtaskIdList().size());
    }

    @Test
    public void shouldReturn404WhenGetSubtasksIdListWithWrongEpicId() throws IOException, InterruptedException {
        manager.addEpic(new Epic("en1", "ed1"));
        manager.addSubtask(new Subtask("s1", "d1", TaskStatus.NEW, 1));
        manager.addSubtask(new Subtask("s2", "d2", TaskStatus.NEW, 1));
        manager.addSubtask(new Subtask("s3", "d3", TaskStatus.NEW, 1));

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/2/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldReturn404WhenUnknownEpicsEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/unknown");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Такого эндпоинта epics не существует", new String(response.body().getBytes()));
    }

    //==============================================SUBTASKS============================================================
    @Test
    public void shouldAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 1);
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getSubtaskList();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("sn1", subtasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldReturn406WhenAddSubtaskWithOverlap() throws IOException, InterruptedException {
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 1, Duration.ofMinutes(5), now);
        manager.addSubtask(subtask);

        Subtask subtask2 = new Subtask("sn123", "sd123", TaskStatus.IN_PROGRESS, 1, Duration.ofMinutes(5), now.plusMinutes(3));
        String subtask2Json = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtask2Json)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void shouldGetSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 1, Duration.ofMinutes(5), now);
        manager.addSubtask(subtask);
        Subtask subtask2 = new Subtask("sn2", "sd2", TaskStatus.DONE, 1, Duration.ofMinutes(5), now.plusMinutes(6));
        manager.addSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> tasksFromResponse = gson.fromJson(response.body(), new SubtaskListTypeToken().getType());
        List<Subtask> tasksFromManager = manager.getSubtaskList();

        assertEquals(2, tasksFromResponse.size());
        assertEquals(2, tasksFromManager.size());
        assertEquals(tasksFromManager, tasksFromResponse);
    }

    @Test
    public void shouldGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 1, Duration.ofMinutes(5), now);
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask taskFromServer = gson.fromJson(new String(response.body().getBytes(), StandardCharsets.UTF_8), Subtask.class);
        assertEquals(2, taskFromServer.getId());
    }

    @Test
    public void shouldReturn404WhenGetSubtaskWithWrongId() throws IOException, InterruptedException {
        List<Subtask> tasksFromManager = manager.getSubtaskList();
        assertEquals(0, tasksFromManager.size());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 1, Duration.ofMinutes(5), now);
        manager.addSubtask(subtask);

        Subtask subtaskToUpdt = new Subtask(2, "sn2updt", "sd2updt", TaskStatus.DONE, 1, Duration.ofMinutes(5), now.plusMinutes(6));
        String subtaskToUpdtJson = gson.toJson(subtaskToUpdt);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskToUpdtJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask task1 = manager.getSubtask(2);
        assertEquals("sn2updt", task1.getName());
        assertEquals("sd2updt", task1.getDescription());
        assertEquals(TaskStatus.DONE, task1.getStatus());
        assertEquals(Duration.ofMinutes(5), task1.getDuration());
        assertEquals(now.plusMinutes(6), task1.getStartTime());
        assertEquals(1, task1.getEpicId());
    }

    @Test
    public void shouldReturn406WhenUpdateSubtaskWithOverlap() throws IOException, InterruptedException {
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 1, Duration.ofMinutes(5), now);
        manager.addSubtask(subtask);
        Subtask subtask2 = new Subtask("sn2", "sd2", TaskStatus.NEW, 1, Duration.ofMinutes(5), now.plusMinutes(6));
        manager.addSubtask(subtask2);

        Subtask subtaskToUpdt = new Subtask(3, "sn2updt", "sd2updt", TaskStatus.DONE, 1, Duration.ofMinutes(5), now.plusMinutes(3));
        String subtaskToUpdtJson = gson.toJson(subtaskToUpdt);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskToUpdtJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void shouldReturn404WhenUpdateSubtaskWithWrongId() throws IOException, InterruptedException {
        Subtask subtask = new Subtask(2, "sn1", "sd1", TaskStatus.NEW, 1, Duration.ofMinutes(5), now);
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 1, Duration.ofMinutes(5), now);
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getSubtaskList().size());
    }

    @Test
    public void shouldReturn404WhenDeleteSubtaskWithWrongId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldReturn404WhenUnknownSubtasksEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/unknown");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Такого эндпоинта subtasks не существует", new String(response.body().getBytes()));
    }

    //==============================================HISTORY=============================================================
    @Test
    public void shouldGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        Task task2 = new Task("T2", "TD2", TaskStatus.DONE, Duration.ofMinutes(5), now.plusMinutes(6));
        manager.addTask(task1);
        manager.addTask(task2);
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 3, Duration.ofMinutes(5), now.plusMinutes(12));
        manager.addSubtask(subtask);

        manager.getTask(2);
        manager.getTask(1);
        manager.getSubtask(4);
        manager.getEpic(3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> historyFromResponse = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        List<Task> historyFromManager = manager.getHistory();

        assertEquals(4, historyFromResponse.size());
        assertEquals(4, historyFromManager.size());
        assertEquals(historyFromManager.get(0).getId(), historyFromResponse.get(0).getId());
        assertEquals(historyFromManager.get(1).getId(), historyFromResponse.get(1).getId());
        assertEquals(historyFromManager.get(2).getId(), historyFromResponse.get(2).getId());
        assertEquals(historyFromManager.get(3).getId(), historyFromResponse.get(3).getId());
    }

    @Test
    public void shouldReturn404WhenUnknownHistoryEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history/unknown");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Такого эндпоинта history не существует", new String(response.body().getBytes()));
    }

    //==========================================PRIORITIZED=============================================================
    @Test
    public void shouldGetPrioritized() throws IOException, InterruptedException {
        Task task1 = new Task("T1", "TD1", TaskStatus.NEW, Duration.ofMinutes(5), now);
        Task task2 = new Task("T2", "TD2", TaskStatus.DONE, Duration.ofMinutes(5), now.plusMinutes(6));
        manager.addTask(task1);
        manager.addTask(task2);
        Epic epic = new Epic("en", "ed");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("sn1", "sd1", TaskStatus.NEW, 3, Duration.ofMinutes(5), now.plusMinutes(12));
        manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> prioritizedFromResponse = gson.fromJson(response.body(), new TaskListTypeToken().getType());
        List<Task> prioritizedFromManager = manager.getPrioritizedTasks();

        assertEquals(3, prioritizedFromResponse.size());
        assertEquals(3, prioritizedFromManager.size());
        assertEquals(prioritizedFromManager.get(0).getId(), prioritizedFromResponse.get(0).getId());
        assertEquals(prioritizedFromManager.get(1).getId(), prioritizedFromResponse.get(1).getId());
        assertEquals(prioritizedFromManager.get(2).getId(), prioritizedFromResponse.get(2).getId());
    }

    @Test
    public void shouldReturn404WhenUnknownPrioritizedEndpoint() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized/unknown");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        assertEquals("Такого эндпоинта prioritized не существует", new String(response.body().getBytes()));
    }
}