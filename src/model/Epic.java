package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIdList;

    private LocalDateTime endTime;

    public Epic(int id, String name, String description, TaskStatus status, ArrayList<Integer> subtaskIdList,
                Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.subtaskIdList = subtaskIdList;
        endTime = null;
    }

    public Epic(int id, String name, String description, TaskStatus status, ArrayList<Integer> subtaskIdList) {
        this(id, name, description, status, subtaskIdList, null, null);
    }

    public Epic(String name, String description) {
        this(-1, name, description, TaskStatus.NEW, new ArrayList<>());
    }

    public ArrayList<Integer> getSubtaskIdList() {
        return new ArrayList<>(subtaskIdList);
    }

    public void clearSubtaskIdList() {
        subtaskIdList.clear();
    }

    public void addSubtaskId(int id) {
        if (this.id != id) {
            subtaskIdList.add(id);
        }
    }

    public void removeSubtaskId(Integer id) {
        subtaskIdList.remove(id);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }


    @Override
    public String toString() {
        return "Epic{" +
                "subtaskIdList=" + subtaskIdList +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + getEndTime() +
                '}';
    }
}