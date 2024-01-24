package service;

import model.Task;

import java.util.List;

public interface HistoryManagerService {

    List<Task> getHistory();

    void add(Task task);

}
