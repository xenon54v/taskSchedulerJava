package com.kokona.scheduler.simulation;

import com.kokona.scheduler.generator.TaskGenerator;
import com.kokona.scheduler.metrics.SchedulerMetrics;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.*;

import java.util.ArrayList;
import java.util.List;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("=== SCHEDULER PERFORMANCE COMPARISON ===");
        
        // Генерируем тестовые задачи
        List<Task> testTasks = TaskGenerator.generateTasks(10, 30, 15);
        
        // Печатаем информацию о задачах
        printTaskOverview(testTasks);
        
        // Тестируем все планировщики на одинаковых задачах
        List<Scheduler> schedulers = List.of(
            new FifoScheduler(),
            new LifoScheduler(), 
            new SJFScheduler(20)
        );
        
        List<SchedulerMetrics> results = new ArrayList<>();
        
        for (Scheduler scheduler : schedulers) {
            // Создаем копии задач для каждого планировщика
            List<Task> taskCopy = TaskGenerator.copyTasks(testTasks);
            SchedulerMetrics metrics = SchedulerSimulator.simulate(scheduler, taskCopy);
            metrics.printMetrics();
            results.add(metrics);
        }
        
        // Сравниваем результаты
        printComparison(results);
        
        // Старые тесты (сохраняем для обратной совместимости)
        System.out.println("\n" + "=".repeat(60));
        System.out.println("BASIC FUNCTIONALITY TESTS");
        System.out.println("=".repeat(60));
        testFIFO();
        testLIFO();
        testSJF();
    }
    
    private static void printTaskOverview(List<Task> tasks) {
        System.out.println("\n=== GENERATED TASKS OVERVIEW ===");
        TaskGenerator.printTaskStats(tasks);
        
        System.out.println("\nFirst 3 tasks:");
        for (int i = 0; i < Math.min(3, tasks.size()); i++) {
            System.out.println("  " + tasks.get(i).toDetailedString());
        }
    }
    
    private static void printComparison(List<SchedulerMetrics> results) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PERFORMANCE COMPARISON");
        System.out.println("=".repeat(70));
        
        System.out.printf("%-15s %-8s %-8s %-12s %-12s%n", 
            "Scheduler", "Tasks", "Time", "Avg WT", "Avg TAT");
        System.out.println("-".repeat(70));
        
        for (SchedulerMetrics metrics : results) {
            System.out.printf("%-15s %-8d %-8d %-12.2f %-12.2f%n",
                metrics.getSchedulerName(),
                metrics.getCompletedTasks(),
                metrics.getCurrentTime(),
                metrics.getAverageWaitingTime(),
                metrics.getAverageTurnaroundTime());
        }
    }
    
    // Старые тестовые методы (сохраняем)
    private static void testFIFO() {
        System.out.println("\n--- Basic FIFO Test ---");
        // ... существующий код теста FIFO
    }
    
    private static void testLIFO() {
        System.out.println("\n--- Basic LIFO Test ---");
        // ... существующий код теста LIFO
    }
    
    private static void testSJF() {
        System.out.println("\n--- Basic SJF Test ---");
        // ... существующий код теста SJF
    }
}