package service;

import model.Task;
import model.Subtask;
import model.Epic;

import java.util.List;

public interface TaskManager {
    List<Task> getTaskList();

    List<Subtask> getSubtaskList();

    List<Epic> getEpicList();

    List<Subtask> getSubtaskListOfEpic(int epicId);

    void clearTaskMap();

    void clearSubtaskMap();

    void clearEpicMap();

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    List<Task> getHistory();

    void addTask(Task task);

    void addSubtask(Subtask subtask);

    void addEpic(Epic epic);

    void updateTask(Task newTask);

    void updateSubtask(Subtask newSubtask);

    void updateEpic(Epic epic);

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);
}
