package server.handler.impl;

import com.sun.net.httpserver.HttpExchange;
import model.Task;
import server.handler.AbstractHttpHandler;
import service.TaskManagerService;

import java.util.Map;

public class PrioritizedHandler extends AbstractHttpHandler<Task> {

    private final TaskManagerService taskManagerService;

    public PrioritizedHandler(TaskManagerService taskManagerService) {
        this.taskManagerService = taskManagerService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        getRequestParams(exchange);
        sendResponse(Map.of(HTTP_OK, taskManagerService.getPrioritizedTasks()));
    }
}
