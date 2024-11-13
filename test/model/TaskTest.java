package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    private static Task task1;
    private static Task task1SameId;
    private static Task task2;

    @BeforeAll
    static void beforeAll() {
        task1 = new Task(4, "Task1", "Descr1", TaskStatus.NEW);
        task1SameId = new Task(4, "Task1SameID", "Descr1SameID", TaskStatus.IN_PROGRESS);
        task2 = new Task(8, "Task2", "Descr2", TaskStatus.DONE);
    }

    @Test
    public void shouldBeEqualsWhenIdsEquals() {
        assertEquals(task1, task1SameId, "Не равны, когда равны ID");
    }

    @Test
    public void shouldBeEqualsWhenSetName() {
        task1.setName("TaskNewName");
        assertEquals("TaskNewName", task1.getName());
    }

    @Test
    public void shouldBeEqualsWhenSetDescription() {
        task1.setDescription("TaskNewDescr");
        assertEquals("TaskNewDescr", task1.getDescription());
    }

    @Test
    public void shouldBeEqualsWhenSetStatus() {
        task1.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task1.getStatus());
    }
}