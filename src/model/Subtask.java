package model;

import utils.Constant;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        if (epicId != id) {
            this.epicId = epicId;
        } else {
            this.epicId = -1;
        }
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        this(id, name, description, status, epicId, Duration.ZERO, Constant.UNIX_EPOCH_START);
    }

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        this(-1, name, description, status, epicId);
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + duration.toMinutes() + "min" +
                ", startTime=" + startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) +
                ", endTime=" + getEndTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) +
                '}';
    }
}