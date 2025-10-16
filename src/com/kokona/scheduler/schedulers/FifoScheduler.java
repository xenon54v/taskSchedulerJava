package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * реализация планировщика FIFO (First-In-First-Out)
 * первая добавленная задача выполняется первой
 * использует структуру данных Queue
 */

public class FifoScheduler implements Scheduler {
private final Queue<Task> queue = new LinkedList<>();
    
    @Override
    public void addTask(Task task) {
        queue.offer(task);
    }
    
    @Override
    public Optional<Task> getNextTask() {
        return Optional.ofNullable(queue.poll());
    }
    
    @Override
    public boolean hasTasks() {
        return !queue.isEmpty();
    }
    
    @Override
    public String getName() {
        return "FIFO";
    }

}
