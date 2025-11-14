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
    
    public void incrementTime() {
        currentTime++;
    }
    
    public void recordIdleTime() {
        idleTime++;
        incrementTime(); // Убедитесь, что время увеличивается и при простое
        System.out.printf("Time %d: IDLE (no tasks to execute)%n", currentTime);
    }
    
    public void recordTaskCompletion(Task task) {
        completedTasks++;
        totalWaitingTime += task.getWaitingTime();
        totalTurnaroundTime += task.getTurnaroundTime();
        completedTasksList.add(task);
        
        if (task.isDeadlineMissed()) {
            deadlineMisses++;
        }
        
        System.out.printf("Time %d: %s completed %s (WT=%d, TAT=%d)%n",
            currentTime, schedulerName, task.getId(), 
            task.getWaitingTime(), task.getTurnaroundTime());
    }
    
    public void printMetrics() {
        System.out.println("\n=== " + schedulerName + " METRICS ===");
        System.out.println("Total simulation time: " + currentTime);
        System.out.println("Completed tasks: " + completedTasks);
        System.out.println("Idle time: " + idleTime + " (" + 
            String.format("%.1f%%", (idleTime * 100.0 / currentTime)) + ")");
        
        if (completedTasks > 0) {
            double avgWaiting = (double) totalWaitingTime / completedTasks;
            double avgTurnaround = (double) totalTurnaroundTime / completedTasks;
            
            System.out.printf("Average Waiting Time: %.2f%n", avgWaiting);
            System.out.printf("Average Turnaround Time: %.2f%n", avgTurnaround);
            System.out.printf("Deadline Miss Rate: %.1f%%%n", 
                (deadlineMisses * 100.0 / completedTasks));
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

}
