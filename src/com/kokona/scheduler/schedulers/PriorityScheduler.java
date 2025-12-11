package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.datastructures.MinHeap;
import java.util.Comparator;

public class PriorityScheduler extends Scheduler {
	private MinHeap<Task> heap;
	
	public PriorityScheduler() {
		heap = new MinHeap<>(Comparator.comparingInt(Task::getPriority).reversed());
	}

}
