package com.kokona.scheduler.simulation;

import com.kokona.scheduler.generator.TaskGenerator;
import com.kokona.scheduler.metrics.SchedulerMetrics;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class QuickTest {
    public static void main(String[] args) {
    	Scanner scanner = new Scanner(System.in);
    	
        System.out.println("=== СРАВНЕНИЕ АЛГОРИТМОВ ПЛАНИРОВАНИЯ ===");
        
        System.out.print("\n Введите количество задач для тестирования: ");
        int taskCount = scanner.nextInt();
        
        // Генерируем тестовые задачи
        List<Task> testTasks = TaskGenerator.generateTasks(taskCount, 30, 15);
        
        // Печатаем понятную информацию о задачах
        printClearTaskOverview(testTasks);
        
        // Тестируем все планировщики
        List<Scheduler> schedulers = List.of(
            new FifoScheduler(),
            new LifoScheduler(), 
            new SJFScheduler(),
            new PriorityScheduler(),
            new PreemptivePriorityScheduler()
        );
        
        List<SchedulerMetrics> results = new ArrayList<>();
        
        for (Scheduler scheduler : schedulers) {
            List<Task> taskCopy = TaskGenerator.copyTasks(testTasks);
            SchedulerMetrics metrics = SchedulerSimulator.simulate(scheduler, taskCopy);
            results.add(metrics);
        }
        
        // Понятное сравнение результатов
        SchedulerSimulator.printClearComparison(results);
        SchedulerSimulator.printPerformanceAnalysis(results);
        
        scanner.close();
    }
    
    private static void printClearTaskOverview(List<Task> tasks) {
        System.out.println("\n! ОБЗОР ТЕСТОВЫХ ЗАДАЧ:");
        System.out.println("Всего задач: " + tasks.size());
        
        long cpuCount = tasks.stream().filter(t -> t.getType() == Task.TaskType.CPU_BOUND).count();
        long ioCount = tasks.stream().filter(t -> t.getType() == Task.TaskType.IO_BOUND).count();
        
        System.out.printf("CPU-bound: %d | IO-bound: %d%n", cpuCount, ioCount);
        
        System.out.println("\n Время поступления задач:");
        for (int i = 0; i < Math.min(5, tasks.size()); i++) {
            Task task = tasks.get(i);
            System.out.printf("  %s: прибытие=%d, выполнение=%d, дедлайн=%d%n",
                task.getId(), task.getArrivalTime(), 
                task.getExecutionTime(), task.getDeadline());
        }
        if (tasks.size() > 5) {
            System.out.println("  ... и еще " + (tasks.size() - 5) + " задач");
        }
    }
    
}