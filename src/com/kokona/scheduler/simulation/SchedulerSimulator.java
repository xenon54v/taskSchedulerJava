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
    
    public static void printClearComparison(List<SchedulerMetrics> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" СРАВНЕНИЕ ПРОИЗВОДИТЕЛЬНОСТИ");
        System.out.println("=".repeat(80));
        
        System.out.printf("%-12s %-8s %-10s %-12s %-12s %-18s%n", 
            "Алгоритм", "Задачи", "Время", "Ср. ожидание", "Ср. выполнение", "Пропуски дедлайнов");
        System.out.println("-".repeat(80));
        
        for (SchedulerMetrics metrics : results) {
            String deadlineInfo = String.format("%d/%-2d (%.0f%%)", 
                metrics.getDeadlineMisses(), 
                metrics.getCompletedTasks(),
                metrics.getDeadlineMissRate());
            
            System.out.printf("%-12s %-8d %-10d %-12.1f %-12.1f %-18s%n",
                metrics.getSchedulerName(),
                metrics.getCompletedTasks(),
                metrics.getCurrentTime(),
                metrics.getAverageWaitingTime(),
                metrics.getAverageTurnaroundTime(),
                deadlineInfo);
        }
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
    
    
    public static void printPerformanceAnalysis(List<SchedulerMetrics> results) {
        System.out.println("\n АНАЛИЗ РЕЗУЛЬТАТОВ:");
        System.out.println("-".repeat(50));
        
        if (results.size() >= 3) {
            SchedulerMetrics fifo = results.get(0);
            SchedulerMetrics lifo = results.get(1);
            SchedulerMetrics sjf = results.get(2);
            
            // Сравнение SJF vs FIFO
            double wtImprovement = ((fifo.getAverageWaitingTime() - sjf.getAverageWaitingTime()) / 
                                   fifo.getAverageWaitingTime()) * 100;
            double tatImprovement = ((fifo.getAverageTurnaroundTime() - sjf.getAverageTurnaroundTime()) / 
                                    fifo.getAverageTurnaroundTime()) * 100;
            
            System.out.printf("SJF vs FIFO:%n");
            System.out.printf("  # Снижение времени ожидания: +%.1f%%%n", wtImprovement);
            System.out.printf("  # Снижение общего времени: +%.1f%%%n", tatImprovement);
            
            // Сравнение LIFO vs FIFO
            double lifoWorse = ((lifo.getAverageWaitingTime() - fifo.getAverageWaitingTime()) / 
                               fifo.getAverageWaitingTime()) * 100;
            
            System.out.printf("LIFO vs FIFO:%n");
            System.out.printf("  # Увеличение времени ожидания: %.1f%%%n", lifoWorse);
            
            // Рекомендации
            System.out.println("\n РЕКОМЕНДАЦИИ:");
            if (wtImprovement > 15) {
                System.out.println("  # SJF значительно эффективнее для коротких задач");
            }
            if (lifoWorse > 0) {
                System.out.println("  # LIFO не рекомендуется для production систем");
            }
            System.out.println("  # FIFO обеспечивает справедливость, но не оптимальность");
        }
    }
}
