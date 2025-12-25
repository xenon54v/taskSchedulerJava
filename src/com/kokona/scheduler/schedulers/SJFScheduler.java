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
	private final MinHeap heap;
	private final String name;
	
	public SJFScheduler() {
        this.heap = new MinHeap();
        this.name = "SJF";
    }
	
	public SJFScheduler(int initialCapacity) {
        this.heap = new MinHeap(initialCapacity);
        this.name = "SJF";
    }
	
	@Override
    public void addTask(Task task) {
        heap.insert(task);
    }
	
	@Override
    public Optional<Task> getNextTask() {
        if (heap.isEmpty()) {
            return Optional.empty();
        }
        Task task = heap.extractMin();
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
