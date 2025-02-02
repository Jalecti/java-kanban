package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeOverlapException;
import model.Subtask;
import server.handlers.endpoints.EndpointSubtasks;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Subtask subtask = getSubtaskFromExchange(exchange);
            boolean hasId = checkTaskId(subtask);
            EndpointSubtasks endpointSubtasks = getEndpointSubtasks(exchange.getRequestURI().getPath(), exchange.getRequestMethod(), hasId);
            int id;

            switch (endpointSubtasks) {
                case GET_SUBTASKS:
                    sendText(exchange, gson.toJson(taskManager.getSubtaskList()), 200);
                    break;
                case GET_SUBTASK_BY_ID:
                    id = getIdFromPath(exchange);
                    sendText(exchange, gson.toJson(taskManager.getSubtask(id)), 200);
                    break;
                case POST_SUBTASK_CREATE:
                    taskManager.addSubtask(subtask);
                    sendText(exchange, "Подзадача " + subtask.getName() + " успешно добавлена под идентификатором id=" + subtask.getId(), 201);
                    break;
                case POST_SUBTASK_UPDATE:
                    taskManager.updateSubtask(subtask);
                    sendText(exchange, "Подзадача " + subtask.getName() + " успешно обновлена", 201);
                    break;
                case DELETE_SUBTASK:
                    id = getIdFromPath(exchange);
                    taskManager.deleteSubtask(id);
                    sendText(exchange, "Подзадача с id=" + id + " успешно удалена из менеджера", 200);
                    break;
                default:
                    sendNotFound(exchange, "Такого эндпоинта subtasks не существует");
            }
        } catch (TaskNotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TaskTimeOverlapException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, e.getMessage(), 500);
        }
    }

    private EndpointSubtasks getEndpointSubtasks(String requestPath, String requestMethod, boolean hasId) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return EndpointSubtasks.GET_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                if (hasId) {
                    return EndpointSubtasks.POST_SUBTASK_UPDATE;
                } else {
                    return EndpointSubtasks.POST_SUBTASK_CREATE;
                }
            }
        }

        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            try {
                Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                return EndpointSubtasks.UNKNOWN;
            }
            if (requestMethod.equals("GET")) {
                return EndpointSubtasks.GET_SUBTASK_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return EndpointSubtasks.DELETE_SUBTASK;
            }
        }

        return EndpointSubtasks.UNKNOWN;
    }

    private Subtask getSubtaskFromExchange(HttpExchange httpExchange) throws IOException {
        Subtask subtask = null;
        byte[] body = httpExchange.getRequestBody().readAllBytes();
        if (body.length != 0) {
            subtask = gson.fromJson(new String(body, StandardCharsets.UTF_8), Subtask.class);
        }
        return subtask;
    }
}