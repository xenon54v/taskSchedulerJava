package com.kokona.scheduler.simulation;

import com.kokona.scheduler.metrics.SchedulerMetrics;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SchedulerSimulator {

    // Длительность IO-блокировки в тиках (фиксированно)
    private static final int IO_BLOCK_DURATION = 3;

    public static SchedulerMetrics simulate(Scheduler scheduler, List<Task> tasks) {
        SchedulerMetrics metrics = new SchedulerMetrics(scheduler.getName());

        // ВАЖНО: генератор уже сортирует задачи по arrivalTime,
        // а copyTasks сохраняет порядок. Поэтому можно идти указателем.
        int arrivalIndex = 0;

        Task currentTask = null;

        while (true) {
            final int now = metrics.getCurrentTime();

            // 1) Добавляем все задачи, которые "пришли" в момент now
            while (arrivalIndex < tasks.size() && tasks.get(arrivalIndex).getArrivalTime() == now) {
                Task t = tasks.get(arrivalIndex);
                if (!t.isCompleted()) {
                    scheduler.addTask(t);
                }
                arrivalIndex++;
            }

            // 2) Если нет текущей задачи — берём следующую, но пропускаем заблокированные
            if (currentTask == null) {
                currentTask = pollNextRunnableTask(scheduler, now);
                if (currentTask != null) {
                    currentTask.markStarted(now); // корректно выставит startTime + responseTime
                }
            }

            // 3) Если есть текущая задача — выполняем один тик с учётом IO
            if (currentTask != null) {
                boolean completed = currentTask.executeOneTickWithIo(now, IO_BLOCK_DURATION);

                // Тик времени прошёл (CPU работал 1 тик)
                metrics.incrementTime();

                if (completed) {
                    currentTask.setFinishTime(metrics.getCurrentTime());
                    metrics.recordTaskCompletion(currentTask);
                    currentTask = null;
                } else {
                    // Если после тика задача стала заблокированной (IO),
                    // возвращаем её в очередь и освобождаем CPU
                    if (currentTask.isBlocked(metrics.getCurrentTime())) {
                        scheduler.addTask(currentTask);
                        currentTask = null;
                    }
                }
            } else {
                // 4) CPU простаивает — тик времени проходит, idleTime растёт
                metrics.recordIdleTime();
            }

            // 5) Условие остановки:
            // - нет текущей задачи
            // - больше нет новых прибытия (arrivalIndex дошёл до конца)
            // - все задачи завершены
            // - очередь планировщика пуста (через попытку взять runnable)
            if (currentTask == null
                    && arrivalIndex >= tasks.size()
                    && allTasksCompleted(tasks)
                    && !schedulerHasRunnableTask(scheduler, metrics.getCurrentTime())) {
                break;
            }
        }

        return metrics;
    }

    /**
     * Берёт следующую задачу, которую можно реально выполнять сейчас.
     * Если задача заблокирована IO — временно откладываем её и смотрим следующую.
     * В конце все отложенные возвращаем обратно в планировщик.
     */
    private static Task pollNextRunnableTask(Scheduler scheduler, int now) {
        List<Task> deferred = new ArrayList<>();

        while (true) {
            Optional<Task> opt = scheduler.getNextTask();
            if (opt.isEmpty()) {
                // возвращаем отложенные
                for (Task t : deferred) scheduler.addTask(t);
                return null;
            }

            Task t = opt.get();
            t.unblockIfNeeded(now);

            if (!t.isBlocked(now) && !t.isCompleted()) {
                // возвращаем отложенные
                for (Task x : deferred) scheduler.addTask(x);
                return t;
            }

            // заблокирована (или уже завершена) — отложим и посмотрим другую
            deferred.add(t);
        }
    }

    /**
     * Проверка: есть ли в очереди планировщика хотя бы одна задача, которую можно выполнять сейчас.
     * Реализовано безопасно через временное извлечение/возврат.
     */
    private static boolean schedulerHasRunnableTask(Scheduler scheduler, int now) {
        Task t = pollNextRunnableTask(scheduler, now);
        if (t == null) return false;
        // вернём обратно, чтобы не менять состояние
        scheduler.addTask(t);
        return true;
    }

    private static boolean allTasksCompleted(List<Task> tasks) {
        for (Task t : tasks) {
            if (!t.isCompleted()) return false;
        }
        return true;
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

    public static void printPerformanceAnalysis(List<SchedulerMetrics> results) {
        System.out.println("\n АНАЛИЗ РЕЗУЛЬТАТОВ:");
        System.out.println("-".repeat(50));

        if (results.size() >= 3) {
            SchedulerMetrics fifo = results.get(0);
            SchedulerMetrics lifo = results.get(1);
            SchedulerMetrics sjf = results.get(2);

            double wtImprovement = ((fifo.getAverageWaitingTime() - sjf.getAverageWaitingTime()) /
                    fifo.getAverageWaitingTime()) * 100;
            double tatImprovement = ((fifo.getAverageTurnaroundTime() - sjf.getAverageTurnaroundTime()) /
                    fifo.getAverageTurnaroundTime()) * 100;

            System.out.printf("SJF vs FIFO:%n");
            System.out.printf("  # Снижение времени ожидания: +%.1f%%%n", wtImprovement);
            System.out.printf("  # Снижение общего времени: +%.1f%%%n", tatImprovement);

            double lifoWorse = ((lifo.getAverageWaitingTime() - fifo.getAverageWaitingTime()) /
                    fifo.getAverageWaitingTime()) * 100;

            System.out.printf("LIFO vs FIFO:%n");
            System.out.printf("  # Увеличение времени ожидания: %.1f%%%n", lifoWorse);

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