package server.handler.impl;

import com.sun.net.httpserver.HttpExchange;
import model.Subtask;
import server.handler.AbstractHttpHandler;
import server.model.RequestParams;
import service.TaskManagerService;

import java.util.List;
import java.util.Map;

public class SubTaskHandler extends AbstractHttpHandler<Subtask> {

    private final TaskManagerService taskManagerService;

    public SubTaskHandler(TaskManagerService taskManagerService) {
        this.taskManagerService = taskManagerService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        RequestParams requestParams = getRequestParams(exchange);

        Subtask subtask = getTaskFromJson(requestParams.requestBodyInString(), Subtask.class);

        Map<Integer, List<Subtask>> response = processRequest(
                requestParams.httpMethod(),
                requestParams.requestParam().isPresent() ? requestParams.requestParam().get() : null,
                subtask,
                taskManagerService::getSubTaskById,
                taskManagerService::getSubTasks,
                taskManagerService::createSubtask,
                taskManagerService::removeAllSubTasks
        );

        sendResponse(response);
    }
}
