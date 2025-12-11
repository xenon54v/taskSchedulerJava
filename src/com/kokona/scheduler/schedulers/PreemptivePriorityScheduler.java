package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.datastructures.MinHeap;
import java.util.Comparator;
import java.util.Optional;

/**
 * Приоритетный планировщик с вытеснением (Preemptive Priority Scheduling)
 */
public class PreemptivePriorityScheduler implements Scheduler {
    
    private MinHeap<Task> heap;
    private Task currentTask = null;
    
    public PreemptivePriorityScheduler() {
        heap = new MinHeap<>(Comparator.comparingInt(Task::getPriority));
    }
    
    @Override
    public void addTask(Task newTask) {
        heap.insert(newTask);
        System.out.printf("  [Preemptive Priority] Task %s added (priority=%d, exec=%d)%n",
            newTask.getId(), newTask.getPriority(), newTask.getExecutionTime());
    }
    
    @Override
    public Optional<Task> getNextTask() {
        if (heap.isEmpty()) {
            currentTask = null;
            return Optional.empty();
        }
        
        currentTask = heap.extractMin();
        System.out.printf("  [Preemptive Priority] Processing task %s (priority=%d, remaining=%d)%n",
            currentTask.getId(), currentTask.getPriority(), currentTask.getRemainingTime());
        return Optional.of(currentTask);
    }
    
    @Override
    public boolean hasTasks() {
        return !heap.isEmpty();
    }
    
    @Override
    public String getName() {
        return "Priority (Preemptive)";
    }
    
    /**
     * Проверяет, нужно ли вытеснить текущую задачу новой
     * Вызывается симулятором при добавлении новой задачи
     */
    public boolean shouldPreempt(Task newTask) {
        return currentTask != null && 
               !currentTask.isCompleted() && 
               newTask.getPriority() < currentTask.getPriority();
    }
    
    /**
     * Вытесняет текущую задачу и возвращает её в очередь
     */
    public Task preemptCurrentTask() {
        if (currentTask == null) {
            return null;
        }
        
        System.out.printf("  ⚡ PREEMPTION: Preempting task %s (priority=%d)%n",
            currentTask.getId(), currentTask.getPriority());
        
        Task preempted = currentTask;
        preempted.incrementContextSwitches();
        heap.insert(preempted);  // Возвращаем в очередь
        currentTask = null;
        
        return preempted;
    }
    
    /**
     * Получает текущую выполняемую задачу
     */
    public Task getCurrentTask() {
        return currentTask;
    }
    
    /**
     * Уведомляет планировщик о завершении задачи
     */
    public void notifyTaskCompleted() {
        if (currentTask != null) {
            System.out.printf("  [Preemptive Priority] Task %s completed (context switches: %d)%n",
                currentTask.getId(), currentTask.getContextSwitches());
        }
        currentTask = null;
    }
}