package com.kokona.scheduler.simulation;

import com.kokona.scheduler.generator.TaskGenerator;
import com.kokona.scheduler.metrics.SchedulerMetrics;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Специальный тест для демонстрации работы приоритетных планировщиков
 */
public class PrioritySchedulerTest {
    public static void main(String[] args) {
        System.out.println("╔" + "═".repeat(68) + "╗");
        System.out.println("║" + " ".repeat(15) + "ТЕСТ ПРИОРИТЕТНЫХ ПЛАНИРОВЩИКОВ" + " ".repeat(21) + "║");
        System.out.println("╚" + "═".repeat(68) + "╝\n");
        
        // Генерируем задачи с разными приоритетами
        List<Task> testTasks = TaskGenerator.generateTasks(12, 25, 12);
        
        // Печатаем информацию о задачах
        System.out.println("📋 ЗАДАЧИ ДЛЯ ТЕСТИРОВАНИЯ:");
        System.out.println("─".repeat(70));
        System.out.printf("%-6s %-10s %-10s %-10s %-10s%n", 
            "ID", "Arrival", "Exec Time", "Priority", "Deadline");
        System.out.println("─".repeat(70));
        
        for (Task task : testTasks) {
            System.out.printf("%-6s %-10d %-10d %-10d %-10d%n",
                task.getId(), 
                task.getArrivalTime(), 
                task.getExecutionTime(), 
                task.getPriority(),
                task.getDeadline());
        }
        
        System.out.println("\n" + "═".repeat(70) + "\n");
        
        // Тестируем оба планировщика
        List<Scheduler> schedulers = List.of(
            new PriorityScheduler(),
            new PreemptivePriorityScheduler()
        );
        
        List<SchedulerMetrics> results = new ArrayList<>();
        
        for (Scheduler scheduler : schedulers) {
            List<Task> taskCopy = TaskGenerator.copyTasks(testTasks);
            SchedulerMetrics metrics = SchedulerSimulator.simulate(scheduler, taskCopy);
            results.add(metrics);
            
            // Подсчитываем переключения контекста
            int totalSwitches = taskCopy.stream()
                .mapToInt(Task::getContextSwitches)
                .sum();
            
            System.out.println("\n📊 СТАТИСТИКА " + scheduler.getName() + ":");
            System.out.println("   Переключений контекста: " + totalSwitches);
            
            System.out.println("\n" + "═".repeat(70) + "\n");
        }
        
        // Сравнение результатов
        SchedulerSimulator.printClearComparison(results);
        
        // Детальный анализ
        printPriorityAnalysis(results);
    }
    
    private static void printPriorityAnalysis(List<SchedulerMetrics> results) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("📈 АНАЛИЗ ПРИОРИТЕТНЫХ ПЛАНИРОВЩИКОВ");
        System.out.println("═".repeat(70));
        
        if (results.size() >= 2) {
            SchedulerMetrics nonPreemptive = results.get(0);
            SchedulerMetrics preemptive = results.get(1);
            
            double responseImprovement = 
                ((nonPreemptive.getAverageWaitingTime() - preemptive.getAverageWaitingTime()) / 
                 nonPreemptive.getAverageWaitingTime()) * 100;
            
            System.out.println("\n🔍 Preemptive vs Non-Preemptive:");
            System.out.printf("   ▸ Изменение среднего времени ожидания: %.1f%%%n", responseImprovement);
            
            if (preemptive.getDeadlineMisses() < nonPreemptive.getDeadlineMisses()) {
                System.out.printf("   ▸ Пропущено меньше дедлайнов: %d vs %d%n",
                    preemptive.getDeadlineMisses(), nonPreemptive.getDeadlineMisses());
            }
            
            System.out.println("\n💡 РЕКОМЕНДАЦИИ:");
            System.out.println("   • Preemptive - для real-time систем с жесткими дедлайнами");
            System.out.println("   • Non-Preemptive - для систем с низкой стоимостью переключений");
            System.out.println("   • Приоритеты должны отражать важность задач");
        }
    }
}