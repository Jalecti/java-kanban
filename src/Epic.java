import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIdList;

    public Epic(int id, String name, String description, TaskStatus status, ArrayList<Integer> subtaskIdList) {
        super(id, name, description, status);
        this.subtaskIdList = subtaskIdList;
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
        subtaskIdList.add(id);
    }

    public void removeSubtaskId(Integer id) {
        subtaskIdList.remove(id);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtaskIdList=" + subtaskIdList +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description.length()='" + description.length() + '\'' +
                ", status=" + status +
                '}';
    }
}