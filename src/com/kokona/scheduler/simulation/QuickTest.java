package com.kokona.scheduler.simulation;

import com.kokona.scheduler.generator.TaskGenerator;
import com.kokona.scheduler.metrics.SchedulerMetrics;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.*;

import java.util.ArrayList;
import java.util.List;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("=== СРАВНЕНИЕ АЛГОРИТМОВ ПЛАНИРОВАНИЯ ===");
        
        // Генерируем тестовые задачи
        List<Task> testTasks = TaskGenerator.generateTasks(10, 30, 15);
        
        // Печатаем понятную информацию о задачах
        printClearTaskOverview(testTasks);
        
        // Тестируем все планировщики
        List<Scheduler> schedulers = List.of(
            new FifoScheduler(),
            new LifoScheduler(), 
            new SJFScheduler(20)
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
    }
    
    private static void printClearTaskOverview(List<Task> tasks) {
        System.out.println("\n📊 ОБЗОР ТЕСТОВЫХ ЗАДАЧ:");
        System.out.println("Всего задач: " + tasks.size());
        
        long cpuCount = tasks.stream().filter(t -> t.getType() == Task.TaskType.CPU_BOUND).count();
        long ioCount = tasks.stream().filter(t -> t.getType() == Task.TaskType.IO_BOUND).count();
        
        System.out.printf("CPU-bound: %d | IO-bound: %d%n", cpuCount, ioCount);
        
        System.out.println("\n⏰ Время поступления задач:");
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
    
    
    
    private static void printPerformanceAnalysis(List<SchedulerMetrics> results) {
        System.out.println("\n💡 АНАЛИЗ РЕЗУЛЬТАТОВ:");
        System.out.println("-".repeat(50));
        
        if (results.size() < 3) {
            System.out.println("Недостаточно данных для анализа");
            return;
        }
        
        SchedulerMetrics fifo = results.get(0);
        SchedulerMetrics lifo = results.get(1);
        SchedulerMetrics sjf = results.get(2);
        
        // Находим ЛУЧШИЙ алгоритм по времени ожидания
        SchedulerMetrics bestByWaitingTime = results.stream()
            .min((a, b) -> Double.compare(a.getAverageWaitingTime(), b.getAverageWaitingTime()))
            .orElse(fifo);
        
        // Находим ЛУЧШИЙ алгоритм по дедлайнам
        SchedulerMetrics bestByDeadlines = results.stream()
            .min((a, b) -> Double.compare(a.getDeadlineMissRate(), b.getDeadlineMissRate()))
            .orElse(fifo);
        
        // Анализ эффективности
        System.out.println("📊 ОБЩАЯ ЭФФЕКТИВНОСТЬ:");
        
        // Лучший по времени ожидания
        if (bestByWaitingTime.getSchedulerName().equals("SJF")) {
            double improvementVsFifo = ((fifo.getAverageWaitingTime() - sjf.getAverageWaitingTime()) / 
                                       fifo.getAverageWaitingTime()) * 100;
            System.out.printf("• %s - лучший по времени ожидания (на %.1f%% лучше FIFO)%n",
                bestByWaitingTime.getSchedulerName(), improvementVsFifo);
        } else if (bestByWaitingTime.getSchedulerName().equals("LIFO")) {
            double improvementVsFifo = ((fifo.getAverageWaitingTime() - lifo.getAverageWaitingTime()) / 
                                       fifo.getAverageWaitingTime()) * 100;
            System.out.printf("• %s - лучший по времени ожидания (на %.1f%% лучше FIFO)%n",
                bestByWaitingTime.getSchedulerName(), improvementVsFifo);
        } else {
            System.out.printf("• %s - показывает сбалансированные результаты%n", bestByWaitingTime.getSchedulerName());
        }
        
        // Лучший по дедлайнам
        if (!bestByDeadlines.getSchedulerName().equals(bestByWaitingTime.getSchedulerName())) {
            System.out.printf("• %s - лучший по соблюдению дедлайнов%n", bestByDeadlines.getSchedulerName());
        }
        
        // Анализ проблем
        System.out.println("\n⚠️  ВЫЯВЛЕННЫЕ ПРОБЛЕМЫ:");
        
        // Анализ пропусков дедлайнов
        SchedulerMetrics worstByDeadlines = results.stream()
            .max((a, b) -> Double.compare(a.getDeadlineMissRate(), b.getDeadlineMissRate()))
            .orElse(fifo);
        
        if (worstByDeadlines.getDeadlineMissRate() > 50) {
            System.out.printf("• %s имеет высокий процент пропусков дедлайнов (%.0f%%)%n",
                worstByDeadlines.getSchedulerName(), worstByDeadlines.getDeadlineMissRate());
        }
        
        // Анализ времени ожидания
        SchedulerMetrics worstByWaiting = results.stream()
            .max((a, b) -> Double.compare(a.getAverageWaitingTime(), b.getAverageWaitingTime()))
            .orElse(fifo);
        
        if (worstByWaiting.getAverageWaitingTime() > bestByWaitingTime.getAverageWaitingTime() * 1.5) {
            System.out.printf("• %s имеет значительно большее время ожидания%n",
                worstByWaiting.getSchedulerName());
        }
        
        // Рекомендации на основе реальных данных
        System.out.println("\n🎯 РЕКОМЕНДАЦИИ:");
        
        // Для SJF
        if (sjf.getAverageWaitingTime() < fifo.getAverageWaitingTime() * 0.7) {
            System.out.println("• SJF эффективен для workloads с короткими задачами");
        }
        
        // Для LIFO
        if (lifo.getAverageWaitingTime() > fifo.getAverageWaitingTime()) {
            System.out.println("• LIFO может увеличивать starvation длинных задач");
        } else if (lifo.getAverageWaitingTime() < fifo.getAverageWaitingTime()) {
            System.out.println("• LIFO неожиданно эффективен в данном тесте");
        }
        
        // Общие рекомендации
        if (bestByDeadlines.getDeadlineMissRate() > 30) {
            System.out.println("• Рассмотрите алгоритмы с учетом дедлайнов (EDF)");
        }
        
        if (results.stream().anyMatch(m -> m.getAverageWaitingTime() > 30)) {
            System.out.println("• Высокое время ожидания suggests неоптимальное планирование");
        }
        
        // Финальный вывод
        System.out.printf("%n🏆 ВЫВОД: %s показал наилучшие результаты в данном тесте%n",
            bestByWaitingTime.getSchedulerName());
    }
}