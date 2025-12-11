package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import java.util.*;

public class PriorityScheduler extends Scheduler {
    private List<Task> taskList;
    
    public PriorityScheduler() {
        super();  // Вызываем конструктор родителя
        taskList = new ArrayList<>();
        initMetrics();  // ← ВАЖНО! Инициализируем метрики ЗДЕСЬ
    }
    
    @Override
    public String getName() {
        return "Priority Scheduler";
    }
    
    @Override
    public void addTask(Task task) {
        taskList.add(task);
        // Сортируем по приоритету (1 - самый высокий)
        taskList.sort((t1, t2) -> Integer.compare(t1.getPriority(), t2.getPriority()));
        metrics.taskAdded(task);
    }
    
    @Override
    public Task getNextTask() {
        if (taskList.isEmpty()) {
            return null;
        }
        return taskList.remove(0);
    }
    
    @Override
    public boolean hasTasks() {
        return !taskList.isEmpty();
    }
    
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskList);
    }
    
    @Override
    public void clear() {
        taskList.clear();
    }
}
