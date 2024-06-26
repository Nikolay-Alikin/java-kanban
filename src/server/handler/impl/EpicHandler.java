package server.handler.impl;

import com.sun.net.httpserver.HttpExchange;
import model.Epic;
import model.Subtask;
import server.handler.AbstractHttpHandler;
import server.model.RequestParams;
import service.TaskManagerService;

import java.util.List;
import java.util.Map;


public class EpicHandler extends AbstractHttpHandler<Epic> {

    private final TaskManagerService taskManagerService;

    public EpicHandler(TaskManagerService taskManagerService) {
        this.taskManagerService = taskManagerService;
    }

    @Override
    public void handle(HttpExchange exchange) {
        RequestParams result = getRequestParams(exchange);

        Epic epic = getTaskFromJson(result.requestBodyInString(), Epic.class);

        boolean haveEpicId = result.requestParam().isPresent();

        if (haveEpicId && exchange.getRequestURI().getPath().contains("subtasks")) {
            processGetEpicSubtasksReq(result.requestParam().get());
            return;
        }

        Map<Integer, List<Epic>> response = processRequest(
                result.httpMethod(),
                result.requestParam().isPresent() ? result.requestParam().get() : null,
                epic,
                taskManagerService::getEpicById,
                taskManagerService::getEpics,
                taskManagerService::createEpic,
                taskManagerService::removeAllEpics
        );
        sendResponse(response);
    }


    private void processGetEpicSubtasksReq(int epicId) {
        List<Subtask> epicSubTasks = taskManagerService.getEpicSubTasks(epicId);
        if (epicSubTasks.isEmpty()) {
            sendResponse(Map.of(notFound, List.of("Подзадачи для эпика с id " + epicId + " не найдены")));
            return;
        }
        sendResponse(Map.of(httpOk, epicSubTasks));
    }
}
