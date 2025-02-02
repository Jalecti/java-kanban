package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.handlers.endpoints.EndpointPrioritized;
import service.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        EndpointPrioritized endpointPrioritized = getEndpointPrioritized(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpointPrioritized) {
            case GET_PRIORITIZED:
                sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
                break;
            default:
                sendNotFound(exchange, "Такого эндпоинта prioritized не существует");
        }
    }

    private EndpointPrioritized getEndpointPrioritized(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");
        if (pathParts.length == 2 && pathParts[1].equals("prioritized")) {
            if (requestMethod.equals("GET")) {
                return EndpointPrioritized.GET_PRIORITIZED;
            }
        }
        return EndpointPrioritized.UNKNOWN;
    }
}
