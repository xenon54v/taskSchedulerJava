package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.datastructures.MinHeap;
import java.util.Comparator;

public class PriorityScheduler extends Scheduler {
	private MinHeap<Task> heap;
	
	public PriorityScheduler() {
		heap = new MinHeap<>(Comparator.comparingInt(Task::getPriority).reversed());
	}
	
	@Override
    public void addTask(Task task) {
        heap.insert(task);
        metrics.taskAdded(task);
    }
    
    @Override
    public Task getNextTask() {
        if (heap.isEmpty()) {
            return null;
        }
        return heap.extractMin();
    }
    
    @Override
    public boolean hasTasks() {
        return !heap.isEmpty();
    }
    
    @Override
    public List<Task> getAllTasks() {
        return heap.getAllElements();
    }
    
    @Override
    public void clear() {
        heap.clear();
    }
    
    @Override
    public String getName() {
        return "Priority Scheduler (Non-Preemptive)";
    }

}
