package service.impl;

import enums.TaskType;
import exception.ManagerSaveException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import model.Epic;
import model.Subtask;
import model.Task;
import service.HistoryManagerService;

public class FileBackedTaskManager extends InMemoryTaskManagerServiceImpl {

    private final String saveFile;

    public FileBackedTaskManager(String saveFile) {
        this.saveFile = saveFile;
        if (Files.exists(Paths.get(saveFile))) {
            loadFromFile(saveFile);
        }
    }

    @Override
    public Task getTaskById(int taskId) {
        Task task = super.getTaskById(taskId);
        save();
        return task;
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Task updateTask(Task taskForUpdate) {
        Task updatedTask = super.updateTask(taskForUpdate);
        save();
        return updatedTask;
    }

    @Override
    public Task removeTask(int taskId) {
        Task removedTask = super.removeTask(taskId);
        save();
        return removedTask;
    }

    @Override
    public Subtask getSubTaskById(int subTaskId) {
        Subtask subtask = super.getSubTaskById(subTaskId);
        save();
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask createdSubtask = super.createSubtask(subtask);
        save();
        return createdSubtask;
    }

    @Override
    public Subtask updateSubTask(Subtask subTaskForUpdate) {
        Subtask updatedSubTask = super.updateSubTask(subTaskForUpdate);
        save();
        return updatedSubTask;
    }

    @Override
    public Subtask removeSubTask(int subTaskId) {
        Subtask removedSubTask = super.removeSubTask(subTaskId);
        save();
        return removedSubTask;
    }

    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = super.getEpicById(epicId);
        save();
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public Epic removeEpicById(int epicId) {
        Epic removedEpic = super.removeEpicById(epicId);
        save();
        return removedEpic;
    }

    private void save() {
        final String tasksFields = "id,type,name,status,description,epic";
        Path path = Paths.get(this.saveFile);
        List<Task> tasksToSave = new ArrayList<>();
        tasksToSave.addAll(getTasks());
        tasksToSave.addAll(getEpics());
        tasksToSave.addAll(getSubTasks());

        try {
            Files.deleteIfExists(path);
            Files.writeString(path, tasksFields + "\n", StandardOpenOption.CREATE);
            Files.write(path, tasksToSave.stream()
                    .map(Task::toString)
                    .collect(Collectors.toList()), StandardOpenOption.APPEND);
            Files.writeString(path, historyToString(historyManagerService), StandardOpenOption.APPEND);
        } catch (IOException e) {
            super.logger.log(java.util.logging.Level.WARNING, "Can't save tasks to " + saveFile + "\n", e);
            throw new ManagerSaveException(e);
        }
    }

    private static String historyToString(HistoryManagerService historyManagerService) {
        String historyMarker = "HISTORY,";
        return historyMarker + new StringBuilder().append(historyManagerService
                .getHistory()
                .stream()
                .map(Task::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","))).reverse();

    }

    private void historyFromString(String history) {
        if (history.isBlank()) {
            return;
        }
        Arrays.stream(history.split(","))
                .skip(1)
                .map(Integer::parseInt)
                .toList()
                .forEach(id -> {
                    if (tasks.containsKey(id)) {
                        historyManagerService.add(tasks.get(id));
                    }
                    if (epics.containsKey(id)) {
                        historyManagerService.add(epics.get(id));
                    }
                    if (subtasks.containsKey(id)) {
                        historyManagerService.add(subtasks.get(id));
                    }
                });
    }

    private void loadFromFile(String saveFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(saveFile));
        } catch (IOException e) {
            super.logger.log(java.util.logging.Level.WARNING, "Can't load tasks from " + saveFile + "\n", e);
            throw new ManagerSaveException(e);
        }

        if (lines.isEmpty()) {
            return;
        }

        lines.removeFirst();
        lines.forEach(line -> {
            if (line.isEmpty() || line.isBlank()) {
                logger.info("Все задачи десериализованы");
                return;
            }
            if (line.startsWith("HISTORY")) {
                historyFromString(line);
            } else {
                String[] parts = line.split(",");
                if (parts[1].equals(TaskType.EPIC.toString())) {
                    Epic epic = Epic.fromString(line);
                    setTasksCount(epic.getId());
                    epics.put(epic.getId(), epic);
                } else if (parts[1].equals(TaskType.SUBTASK.toString())) {
                    Subtask subtask = Subtask.fromString(line);
                    setTasksCount(subtask.getId());
                    subtasks.put(subtask.getId(), subtask);
                } else {
                    Task task = Task.fromString(line);
                    setTasksCount(task.getId());
                    tasks.put(task.getId(), task);
                }
            }
        });
        super.subtasks.values().forEach(subtask -> {
            if (epics.containsKey(subtask.getEpicId())) {
                epics.get(subtask.getEpicId()).addSubTaskId(subtask.getId());
            }
        });
    }

    private void setTasksCount(int taskId) {
        if (super.taskCount < taskId) {
            super.taskCount = taskId;
        }
    }
}
