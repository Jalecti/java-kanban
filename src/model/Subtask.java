package model;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        if (epicId != id) {
            this.epicId = epicId;
        } else {
            this.epicId = -1;
        }
    }

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        this(-1, name, description, status, epicId);
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "model.Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description.length()='" + description.length() + '\'' +
                ", status=" + status +
                '}';
    }
}