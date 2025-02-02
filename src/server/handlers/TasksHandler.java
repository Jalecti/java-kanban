package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeOverlapException;
import model.Task;
import server.handlers.endpoints.EndpointTasks;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Task task = getTaskFromExchange(exchange);
            boolean hasId = checkTaskId(task);
            EndpointTasks endpointTasks = getEndpointTasks(exchange.getRequestURI().getPath(), exchange.getRequestMethod(), hasId);
            int id;
            switch (endpointTasks) {
                case GET_TASKS:
                    sendText(exchange, gson.toJson(taskManager.getTaskList()), 200);
                    break;
                case GET_TASK_BY_ID:
                    id = getIdFromPath(exchange);
                    sendText(exchange, gson.toJson(taskManager.getTask(id)), 200);
                    break;
                case POST_TASK_CREATE:
                    taskManager.addTask(task);
                    sendText(exchange, "Задача " + task.getName() + " успешно добавлена", 201);
                    break;
                case POST_TASK_UPDATE:
                    taskManager.updateTask(task);
                    sendText(exchange, "Задача " + task.getName() + " успешно обновлена", 201);
                    break;
                case DELETE_TASK:
                    id = getIdFromPath(exchange);
                    taskManager.deleteTask(id);
                    sendText(exchange, "Задача с id=" + id + " успешно удалена из менеджера", 200);
                    break;
                default:
                    sendNotFound(exchange, "Такого эндпоинта tasks не существует");
            }
        } catch (TaskNotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TaskTimeOverlapException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, e.getMessage(), 500);
        }
    }

    private EndpointTasks getEndpointTasks(String requestPath, String requestMethod, boolean hasId) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return EndpointTasks.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                if (hasId) {
                    return EndpointTasks.POST_TASK_UPDATE;
                } else {
                    return EndpointTasks.POST_TASK_CREATE;
                }
            }
        }

        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            try {
                Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                return EndpointTasks.UNKNOWN;
            }
            if (requestMethod.equals("GET")) {
                return EndpointTasks.GET_TASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return EndpointTasks.DELETE_TASK;
            }
        }

        return EndpointTasks.UNKNOWN;
    }

    private Task getTaskFromExchange(HttpExchange httpExchange) throws IOException {
        Task task = null;
        byte[] body = httpExchange.getRequestBody().readAllBytes();
        if (body.length != 0) {
            task = gson.fromJson(new String(body, StandardCharsets.UTF_8), Task.class);
        }
        return task;
    }
}