package com.kokona.scheduler.simulation;

import com.kokona.scheduler.metrics.SchedulerMetrics;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.Scheduler;
import java.util.*;

public class SchedulerSimulator {
    
    public static SchedulerMetrics simulate(Scheduler scheduler, List<Task> tasks) {
        SchedulerMetrics metrics = new SchedulerMetrics(scheduler.getName());
        int maxTime = calculateMaxSimulationTime(tasks);
        Task currentTask = null; // Текущая выполняемая задача
        int taskRemainingTime = 0; // Оставшееся время выполнения текущей задачи
        
        System.out.println("\nStarting " + scheduler.getName() + " simulation...");
        
        while (metrics.getCurrentTime() <= maxTime) {
            int currentTime = metrics.getCurrentTime();
            
            // Добавляем задачи по времени прибытия
            for (Task task : tasks) {
                if (task.getArrivalTime() == currentTime && !task.isCompleted()) {
                    scheduler.addTask(task);
                    System.out.printf("Time %d: %s arrived - %s%n", 
                        currentTime, task.getId(), task.toDetailedString());
                }
            }
            
            // Если текущая задача завершена или её нет, берём следующую
            if (currentTask == null || currentTask.isCompleted()) {
                Optional<Task> nextTaskOpt = scheduler.getNextTask();
                if (nextTaskOpt.isPresent()) {
                    currentTask = nextTaskOpt.get();
                    if (!currentTask.isStarted()) {
                        currentTask.setStartTime(currentTime);
                    }
                    taskRemainingTime = currentTask.getRemainingTime();
                    System.out.printf("Time %d: %s started executing (remaining: %d)%n",
                        currentTime, currentTask.getId(), taskRemainingTime);
                } else {
                    currentTask = null;
                }
            }
            
            // Выполняем текущую задачу
            if (currentTask != null) {
                boolean completed = currentTask.execute(1);
                metrics.incrementTime();
                
                if (completed) {
                    currentTask.setFinishTime(metrics.getCurrentTime());
                    metrics.recordTaskCompletion(currentTask);
                    currentTask = null; // Освобождаем текущую задачу
                }
            } else {
                // Нет задач для выполнения - idle time
                metrics.recordIdleTime();
            }
            
            // Проверяем завершение симуляции
            if (shouldStopSimulation(scheduler, tasks, metrics.getCurrentTime(), maxTime)) {
                break;
            }
        }
        
        return metrics;
    }
    
    private static int calculateMaxSimulationTime(List<Task> tasks) {
        // Более консервативная оценка - учитываем, что могут быть простои
        int totalExecutionTime = tasks.stream()
            .mapToInt(Task::getExecutionTime)
            .sum();
        int maxArrivalTime = tasks.stream()
            .mapToInt(Task::getArrivalTime)
            .max()
            .orElse(0);
        return maxArrivalTime + totalExecutionTime * 2; // Удваиваем для надежности
    }
    
    private static boolean shouldStopSimulation(Scheduler scheduler, List<Task> tasks, 
            int currentTime, int maxTime) {
    		// Останавливаем когда ВСЕ задачи завершены
    	boolean allTasksCompleted = tasks.stream().allMatch(Task::isCompleted);
		return allTasksCompleted || currentTime >= maxTime * 2; // Увеличиваем лимит на всякий случай
    }
    
    private static void printDetailedComparison(List<SchedulerMetrics> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DETAILED PERFORMANCE ANALYSIS");
        System.out.println("=".repeat(80));
        
        System.out.printf("%-15s %-8s %-8s %-12s %-12s %-12s%n", 
            "Scheduler", "Tasks", "Time", "Avg WT", "Avg TAT", "WT Improvement");
        System.out.println("-".repeat(80));
        
        double baseWT = results.get(0).getAverageWaitingTime();
        
        for (SchedulerMetrics metrics : results) {
            double improvement = ((baseWT - metrics.getAverageWaitingTime()) / baseWT) * 100;
            System.out.printf("%-15s %-8d %-8d %-12.2f %-12.2f %-11.1f%%%n",
                metrics.getSchedulerName(),
                metrics.getCompletedTasks(),
                metrics.getCurrentTime(),
                metrics.getAverageWaitingTime(),
                metrics.getAverageTurnaroundTime(),
                improvement);
        }
        
        // Анализ эффективности
        System.out.println("\n--- KEY INSIGHTS ---");
        System.out.printf("SJF reduces waiting time by %.1f%% compared to FIFO%n", 
            ((results.get(0).getAverageWaitingTime() - results.get(2).getAverageWaitingTime()) / 
             results.get(0).getAverageWaitingTime()) * 100);
        System.out.printf("SJF reduces turnaround time by %.1f%% compared to FIFO%n",
            ((results.get(0).getAverageTurnaroundTime() - results.get(2).getAverageTurnaroundTime()) / 
             results.get(0).getAverageTurnaroundTime()) * 100);
    }
}
