package service;

import model.Task;
import model.Node;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node<Task>> getterCallHistory;
    private final HandMadeLinkedList<Task> getterCallHistoryList;

    public InMemoryHistoryManager() {
        getterCallHistory = new HashMap<>();
        getterCallHistoryList = new HandMadeLinkedList<>();
    }

    @Override
    public List<Task> getHistory() {
        return getterCallHistoryList.getTasks();
    }

    @Override
    public void addToHistory(Task task) {
        int taskId = task.getId();
        if (getterCallHistory.containsKey(taskId)) {
            if (getterCallHistoryList.getLast().equals(task)) return;
            Node<Task> targetNode = getterCallHistory.get(taskId);
            removeNode(targetNode);
        }
        getterCallHistory.put(taskId, getterCallHistoryList.linkLast(task));
    }

    @Override
    public void remove(int id) {
        if (getterCallHistory.containsKey(id)) {
            removeNode(getterCallHistory.get(id));
            getterCallHistory.remove(id);
        }
    }

    private void removeNode(Node<Task> node) {
        getterCallHistoryList.removeListNode(node);
    }

    static class HandMadeLinkedList<T> {

        //Указатель на первый элемент списка. Он же first
        private Node<T> head;

        //Указатель на последний элемент списка. Он же last
        private Node<T> tail;

        private int size = 0;

        public Node<T> linkFirst(T element) {
            final Node<T> oldHead = head;
            final Node<T> newNode = new Node<>(null, element, oldHead);
            head = newNode;
            if (oldHead == null)
                tail = newNode;
            else
                oldHead.prev = newNode;
            size++;
            return newNode;
        }

        public T getFirst() {
            final Node<T> curHead = head;
            if (curHead == null)
                throw new NoSuchElementException();
            return head.data;
        }

        public Node<T> linkLast(T element) {
            final Node<T> oldTail = tail;
            final Node<T> newNode = new Node<>(oldTail, element, null);
            tail = newNode;
            if (oldTail == null) {
                head = newNode;
            } else {
                oldTail.next = newNode;
            }
            size++;
            return newNode;
        }

        public T getLast() {
            final Node<T> curTail = tail;
            if (curTail == null)
                throw new NoSuchElementException();
            return tail.data;
        }

        public int size() {
            return this.size;
        }

        public List<T> getTasks() {
            List<T> taskList = new ArrayList<>();
            Node<T> currNode = head;
            for (int i = 0; i < size; i++) {
                taskList.add(currNode.data);
                currNode = currNode.next;
            }
            return taskList;
        }

        public void removeListNode(Node<T> node) {
            if (size == 1 && head.data.equals(node.data) && tail.data.equals(node.data)) {
                head = null;
                tail = null;
            } else if (head.data.equals(node.data)) {
                head = head.next;
            } else if (tail.data.equals(node.data)) {
                tail = tail.prev;
            } else {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }
            --size;
        }
    }
}
