package model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private static Epic epic1;
    private static Epic epic1SameId;
    private static Epic epic2;

    @BeforeAll
    static void beforeAll() {
        epic1 = new Epic(4, "Subtask1", "Descr1", TaskStatus.NEW, new ArrayList<>());
        epic1SameId = new Epic(4, "Subtask1SameID", "Descr1SameID", TaskStatus.IN_PROGRESS, new ArrayList<>());
        epic2 = new Epic(8, "Subtask2", "Descr2", TaskStatus.DONE, new ArrayList<>());
    }

    @Test
    public void shouldBeEqualsWhenIdsEquals() {
        assertEquals(epic1, epic1SameId, "Не равны, когда равны ID");
    }

    @Test
    public void shouldNotAddEpicAsSubtaskToHimself() {
        epic1.addSubtaskId(4);
        ArrayList<Integer> subtaskIdList = new ArrayList<>();
        subtaskIdList.add(4);
        assertNotEquals(epic1.getSubtaskIdList(), subtaskIdList, "Epic не должен добавлять в самого себя в виде подзадачи");
    }

    @Test
    public void shouldBeEmptyListAfterClear() {
        epic1.clearSubtaskIdList();
        epic1.addSubtaskId(7);
        epic1.addSubtaskId(8);
        epic1.addSubtaskId(9);
        assertEquals(new ArrayList<>(Arrays.asList(7, 8, 9)), epic1.getSubtaskIdList());
        epic1.clearSubtaskIdList();
        assertTrue(epic1.getSubtaskIdList().isEmpty());
    }

    @Test
    public void shouldBeEmptyListAfterRemove() {
        epic1.clearSubtaskIdList();
        epic1.addSubtaskId(7);
        epic1.addSubtaskId(8);
        epic1.addSubtaskId(9);
        assertEquals(new ArrayList<>(Arrays.asList(7, 8, 9)), epic1.getSubtaskIdList());
        epic1.removeSubtaskId(7);
        epic1.removeSubtaskId(8);
        epic1.removeSubtaskId(9);
        assertTrue(epic1.getSubtaskIdList().isEmpty());
    }


}