package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.datastructures.MinHeap;
import java.util.Comparator;
import java.util.Optional;

/**
 * Неприоритетный планировщик на основе приоритетов
 * Задачи с МЕНЬШИМ числовым значением priority имеют БОЛЬШИЙ приоритет
 * Пример: priority=1 выполняется раньше priority=10
 */
public class PriorityScheduler implements Scheduler {
    protected MinHeap<Task> heap;
    
    public PriorityScheduler() {
        // Меньший приоритет = выше в очереди (priority=1 раньше priority=10)
        heap = new MinHeap<>(Comparator.comparingInt(Task::getPriority));
    }
    
    @Override
    public void addTask(Task task) {
        heap.insert(task);
        System.out.printf("  [Priority] Task %s added (priority=%d, exec=%d)%n",
            task.getId(), task.getPriority(), task.getExecutionTime());
    }
    
    @Override
    public Optional<Task> getNextTask() {
        if (heap.isEmpty()) {
            return Optional.empty();
        }
        Task task = heap.extractMin();
        System.out.printf("  [Priority] Processing task %s (priority=%d)%n",
            task.getId(), task.getPriority());
        return Optional.of(task);
    }
    
    @Override
    public boolean hasTasks() {
        return !heap.isEmpty();
    }
    
    @Override
    public String getName() {
        return "Priority (Non-Preemptive)";
    }
}