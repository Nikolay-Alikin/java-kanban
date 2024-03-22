package service;

import service.impl.FileBackedTaskManager;
import service.impl.InMemoryHistoryManagerServiceImpl;

public class Managers {

    public static TaskManagerService getDefault() {
        return new FileBackedTaskManager("save/saveFile.csv");
    }

    public static HistoryManagerService getDefaultHistory() {
        return new InMemoryHistoryManagerServiceImpl();
    }
}
