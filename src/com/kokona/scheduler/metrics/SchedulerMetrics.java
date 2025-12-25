package com.kokona.scheduler.metrics;

import com.kokona.scheduler.model.Task;
import java.util.ArrayList;
import java.util.List;

public class SchedulerMetrics {
    private final String schedulerName;
    private int currentTime = 0;
    private int totalWaitingTime = 0;
    private int totalTurnaroundTime = 0;
    private int completedTasks = 0;
    private int deadlineMisses = 0;
    private int idleTime = 0;
    private final List<Task> completedTasksList = new ArrayList<>();
    
    public SchedulerMetrics(String schedulerName) {
        this.schedulerName = schedulerName;
    }
    
    public int getDeadlineMisses() {
        return deadlineMisses;
    }
    

    public double getDeadlineMissRate() {
        return completedTasks > 0 ? (deadlineMisses * 100.0 / completedTasks) : 0;
    }
    
    public void incrementTime() {
        currentTime++;
    }
    
    public void recordIdleTime() {
        idleTime++;
        incrementTime(); // Убедитесь, что время увеличивается и при простое
    }
    
    public void recordTaskCompletion(Task task) {
        completedTasks++;
        totalWaitingTime += task.getWaitingTime();
        totalTurnaroundTime += task.getTurnaroundTime();
        completedTasksList.add(task);
        
        if (task.isDeadlineMissed()) {
            deadlineMisses++;
        }
    }
    
    
    // Геттеры
    public String getSchedulerName() { return schedulerName; }
    public int getCurrentTime() { return currentTime; }
    public int getCompletedTasks() { return completedTasks; }
    public double getAverageWaitingTime() { 
        return completedTasks > 0 ? (double) totalWaitingTime / completedTasks : 0; 
    }
    public double getAverageTurnaroundTime() { 
        return completedTasks > 0 ? (double) totalTurnaroundTime / completedTasks : 0; 
    }
    public int getTotalIdleTime() {
        return idleTime;
    }

}
