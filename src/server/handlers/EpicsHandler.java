package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskNotFoundException;
import exceptions.TaskTimeOverlapException;
import model.Epic;
import server.handlers.endpoints.EndpointEpics;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    public EpicsHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            Epic epic = getEpicFromExchange(exchange);

            boolean hasId = checkTaskId(epic);
            EndpointEpics endpointEpics = getEndpointEpics(exchange.getRequestURI().getPath(), exchange.getRequestMethod(), hasId);
            int id;
            switch (endpointEpics) {
                case GET_EPICS:
                    sendText(exchange, gson.toJson(taskManager.getEpicList()), 200);
                    break;
                case GET_EPIC_BY_ID:
                    id = getIdFromPath(exchange);
                    sendText(exchange, gson.toJson(taskManager.getEpic(id)), 200);
                    break;
                case POST_EPIC_CREATE:
                    taskManager.addEpic(epic);
                    sendText(exchange, "Эпик " + epic.getName() + " успешно добавлен под идентификатором id=" + epic.getId(), 201);
                    break;
                case POST_EPIC_UPDATE:
                    taskManager.updateEpic(epic);
                    sendText(exchange, "Эпик " + epic.getName() + " успешно обновлен", 201);
                    break;
                case GET_EPIC_SUBTASKS:
                    id = getIdFromPath(exchange);
                    sendText(exchange, gson.toJson(taskManager.getEpic(id).getSubtaskIdList()), 200);
                    break;
                case DELETE_EPIC:
                    id = getIdFromPath(exchange);
                    taskManager.deleteEpic(id);
                    sendText(exchange, "Эпик с id=" + id + " успешно удален из менеджера", 200);
                    break;
                default:
                    sendNotFound(exchange, "Такого эндпоинта epics не существует");
            }
        } catch (TaskNotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        } catch (TaskTimeOverlapException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendText(exchange, e.getMessage(), 500);
        }
    }

    private EndpointEpics getEndpointEpics(String requestPath, String requestMethod, boolean hasId) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return EndpointEpics.GET_EPICS;
            }
            if (requestMethod.equals("POST")) {
                if (hasId) {
                    return EndpointEpics.POST_EPIC_UPDATE;
                } else {
                    return EndpointEpics.POST_EPIC_CREATE;
                }
            }
        }

        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            try {
                Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                return EndpointEpics.UNKNOWN;
            }
            if (requestMethod.equals("GET")) {
                return EndpointEpics.GET_EPIC_BY_ID;
            }
            if (requestMethod.equals("DELETE")) {
                return EndpointEpics.DELETE_EPIC;
            }
        }

        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            try {
                Integer.parseInt(pathParts[2]);
            } catch (NumberFormatException e) {
                return EndpointEpics.UNKNOWN;
            }
            if (requestMethod.equals("GET")) {
                return EndpointEpics.GET_EPIC_SUBTASKS;
            }
        }

        return EndpointEpics.UNKNOWN;
    }

    private Epic getEpicFromExchange(HttpExchange httpExchange) throws IOException {
        Epic epic = null;
        byte[] body = httpExchange.getRequestBody().readAllBytes();
        if (body.length != 0) {
            epic = gson.fromJson(new String(body, StandardCharsets.UTF_8), Epic.class);
        }
        return epic;
    }
}
