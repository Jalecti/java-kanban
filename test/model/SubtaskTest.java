package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    private static Subtask subtask1;
    private static Subtask subtask1SameId;
    private static Subtask subtask2;

    @BeforeAll
    static void beforeAll() {
        subtask1 = new Subtask(4, "Subtask1", "Descr1", TaskStatus.NEW, 1);
        subtask1SameId = new Subtask(4, "Subtask1SameID", "Descr1SameID", TaskStatus.IN_PROGRESS, 1);
        subtask2 = new Subtask(8, "Subtask2", "Descr2", TaskStatus.DONE, 1);
    }

    @Test
    public void shouldBeEqualsWhenIdsEquals() {
        assertEquals(subtask1, subtask1SameId, "Не равны, когда равны ID");
    }

    @Test
    public void shouldNotAddEpicIdOfSubtaskToHimself() {
        Subtask subtask = new Subtask(4, "name", "descr", TaskStatus.NEW, 4);
        assertEquals(-1, subtask.getEpicId());
    }

}