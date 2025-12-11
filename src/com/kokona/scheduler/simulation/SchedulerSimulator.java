package com.kokona.scheduler.simulation;

import com.kokona.scheduler.metrics.SchedulerMetrics;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.PreemptivePriorityScheduler;
import com.kokona.scheduler.schedulers.Scheduler;
import java.util.*;

public class SchedulerSimulator {
    
	public static SchedulerMetrics simulate(Scheduler scheduler, List<Task> tasks) {
	    SchedulerMetrics metrics = new SchedulerMetrics(scheduler.getName());
	    int maxTime = calculateMaxSimulationTime(tasks);
	    Task currentTask = null;
	    
	    // Проверяем тип планировщика
	    boolean isPreemptive = scheduler instanceof PreemptivePriorityScheduler;
	    PreemptivePriorityScheduler preemptiveScheduler = null;
	    if (isPreemptive) {
	        preemptiveScheduler = (PreemptivePriorityScheduler) scheduler;
	    }
	    
	    System.out.println("\n=== Starting " + scheduler.getName() + " simulation ===");
	    
	    while (metrics.getCurrentTime() <= maxTime) {
	        int currentTime = metrics.getCurrentTime();
	        
	        // 1. Добавляем задачи, которые прибыли в текущий момент
	        for (Task task : tasks) {
	            if (task.getArrivalTime() == currentTime && !task.isCompleted()) {
	                scheduler.addTask(task);
	                System.out.printf("Time %d: %s arrived (priority=%d, exec=%d)%n", 
	                    currentTime, task.getId(), task.getPriority(), task.getExecutionTime());
	                
	                // ⚡ ПРОВЕРКА ВЫТЕСНЕНИЯ (только для preemptive)
	                if (isPreemptive && currentTask != null && !currentTask.isCompleted()) {
	                    if (preemptiveScheduler.shouldPreempt(task)) {
	                        System.out.printf("  ⚡ PREEMPTION: Task %s (priority=%d) preempts %s (priority=%d)%n",
	                            task.getId(), task.getPriority(),
	                            currentTask.getId(), currentTask.getPriority());
	                        
	                        // Вытесняем текущую задачу
	                        preemptiveScheduler.preemptCurrentTask();
	                        currentTask = null;  // Сбрасываем текущую задачу
	                    }
	                }
	            }
	        }
	        
	        // 2. Если нет текущей задачи или она завершена, берём следующую
	        if (currentTask == null || currentTask.isCompleted()) {
	            // Записываем завершение предыдущей задачи
	            if (currentTask != null && currentTask.isCompleted()) {
	                currentTask.setFinishTime(currentTime);
	                metrics.recordTaskCompletion(currentTask);
	                
	                if (isPreemptive) {
	                    preemptiveScheduler.notifyTaskCompleted();
	                }
	            }
	            
	            // Берём следующую задачу
	            Optional<Task> nextTaskOpt = scheduler.getNextTask();
	            if (nextTaskOpt.isPresent()) {
	                currentTask = nextTaskOpt.get();
	                if (!currentTask.isStarted()) {
	                    currentTask.setStartTime(currentTime);
	                }
	                System.out.printf("Time %d: %s started (priority=%d, remaining=%d)%n",
	                    currentTime, currentTask.getId(), 
	                    currentTask.getPriority(), currentTask.getRemainingTime());
	            } else {
	                currentTask = null;
	            }
	        }
	        
	        // 3. Выполняем текущую задачу
	        if (currentTask != null) {
	            boolean completed = currentTask.execute(1);
	            metrics.incrementTime();
	            
	            if (completed) {
	                currentTask.setFinishTime(metrics.getCurrentTime());
	                metrics.recordTaskCompletion(currentTask);
	                
	                if (isPreemptive) {
	                    preemptiveScheduler.notifyTaskCompleted();
	                }
	                currentTask = null;
	            }
	        } else {
	            // Нет задач - простой
	            metrics.recordIdleTime();
	        }
	        
	        // 4. Проверяем условие остановки
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
                System.out.println("  # LIFO не рекомендуется для рабочих систем");
            }
            System.out.println("  # FIFO обеспечивает справедливость, но не оптимальность");
        }
    }
}
