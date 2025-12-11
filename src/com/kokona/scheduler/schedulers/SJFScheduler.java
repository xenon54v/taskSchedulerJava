package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.datastructures.MinHeap;
import java.util.Optional;

/**
 * реализация планировщика SJF (Shortest Job First)
 * всегда выбирает задачу с наименьшим временем выполнения
 * использует MinHeap для эффективного извлечения минимума
 */
public class SJFScheduler implements Scheduler {
    private final MinHeap<Task> heap;  //  Добавлена типизация <Task>
    private final String name;
    
    public SJFScheduler() {
        this.heap = new MinHeap<>();  //  Типизация
        this.name = "SJF";
    }
    
    @Override
    public void addTask(Task task) {
        heap.insert(task);
        System.out.println("  [SJF] Added task: " + task.getId() + " (exec time: " + task.getExecutionTime() + ")");
    }
    
    @Override
    public Optional<Task> getNextTask() {
        if (heap.isEmpty()) {
            return Optional.empty();
        }
        Task task = heap.extractMin();
        System.out.println("  [SJF] Processing task: " + task.getId() + " (shortest time: " + task.getExecutionTime() + ")");
        return Optional.of(task);
    }
    
    @Override
    public boolean hasTasks() {
        return !heap.isEmpty();
    }
    
    @Override
    public String getName() {
        return name;
    }
}