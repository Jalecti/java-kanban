package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.handlers.endpoints.EndpointHistory;
import service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndpointHistory endpointHistory = getEndpointHistory(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpointHistory) {
            case GET_HISTORY:
                sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
                break;
            default:
                sendNotFound(exchange, "Такого эндпоинта history не существует");
        }
    }

    private EndpointHistory getEndpointHistory(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (pathParts.length == 2 && pathParts[1].equals("history")) {
            if (requestMethod.equals("GET")) {
                return EndpointHistory.GET_HISTORY;
            }
        }
        return EndpointHistory.UNKNOWN;
    }
}
