package com.kokona.scheduler.generator;

import com.kokona.scheduler.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskGenerator {
	
	public static List<Task> copyTasks(List<Task> originalTasks) {
	    List<Task> copies = new ArrayList<>();
	    for (Task original : originalTasks) {
	        // Создаем новую задачу с теми же параметрами
	        Task copy = new Task(
	            original.getId(),
	            original.getArrivalTime(),
	            original.getExecutionTime(),
	            original.getPriority(),
	            original.getType(),
	            original.getDeadline(),
	            original.getWeight(),
	            original.hasJitter(),
	            original.getIoInterval()
	        );
	        copies.add(copy);
	    }
	    return copies;
	}

    private static final Random random = new Random();

    /**
     * Генерирует список случайных задач с расширенными метриками
     *
     * @param count      количество задач
     * @param maxArrival максимальное время появления (0..maxArrival)
     * @param maxExec    максимальное время выполнения (1..maxExec)
     * @return список задач, отсортированный по времени появления
     */
    public static List<Task> generateTasks(int count, int maxArrival, int maxExec) {
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Базовые параметры
            int arrival = random.nextInt(maxArrival);
            int exec = 1 + random.nextInt(maxExec);
            int priority = 1 + random.nextInt(10); // приоритет 1-10

            // Дополнительные параметры
            Task.TaskType type = random.nextBoolean() ? Task.TaskType.CPU_BOUND : Task.TaskType.IO_BOUND;
            int deadline = arrival + exec * (2 + random.nextInt(3)); // дедлайн = arrival + exec * (2-4)
            double weight = 0.5 + random.nextDouble() * 1.5; // вес 0.5-2.0
            boolean jitter = random.nextDouble() < 0.4; // 40% задач имеют джиттер
            int ioInterval = (type == Task.TaskType.IO_BOUND)
                    ? 1 + random.nextInt(3)   // 1..3: блокировки будут заметнее
                    : Integer.MAX_VALUE;
            
            // Создаем задачу через полный конструктор
            Task task = new Task(
                "T" + i,                    // id
                arrival,                    // arrivalTime
                exec,                       // executionTime
                priority,                   // priority
                type,                       // type
                deadline,                   // deadline
                weight,                     // weight
                jitter,                     // hasJitter
                ioInterval                  // ioInterval
            );

            tasks.add(task);
        }

        // Сортируем по времени появления
        tasks.sort((a, b) -> Integer.compare(a.getArrivalTime(), b.getArrivalTime()));
        return tasks;
    }

    /**
     * Упрощенная генерация задач (только базовые параметры)
     *
     * @param count количество задач
     * @return список простых задач
     */
    public static List<Task> generateSimple(int count) {
        return generateTasks(count, 50, 20);
    }

    /**
     * Генерация задач только одного типа
     *
     * @param count количество задач
     * @param type  тип всех задач
     * @return список задач одного типа
     */
    public static List<Task> generateTasksByType(int count, Task.TaskType type) {
        List<Task> tasks = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int arrival = random.nextInt(50);
            int exec = 1 + random.nextInt(20);
            int priority = 1 + random.nextInt(10);
            int deadline = arrival + exec * 3;
            double weight = 1.0;
            boolean jitter = false;
            int ioInterval = (type == Task.TaskType.IO_BOUND)
                    ? 1 + random.nextInt(3)
                    : Integer.MAX_VALUE;

            
            Task task = new Task(
                type.name().charAt(0) + "" + i, // ID типа "C0", "I1" и т.д.
                arrival, exec, priority, type, deadline, weight, jitter, ioInterval
            );

            tasks.add(task);
        }

        tasks.sort((a, b) -> Integer.compare(a.getArrivalTime(), b.getArrivalTime()));
        return tasks;
    }

    /**
     * Генерация задач с высоким приоритетом
     */
    public static List<Task> generateHighPriorityTasks(int count) {
        List<Task> tasks = generateTasks(count, 30, 15);
        
        // Устанавливаем высокий приоритет (1-3)
        for (Task task : tasks) {
            task.setPriority(1 + random.nextInt(3));
        }
        
        return tasks;
    }

    /**
     * Генерация задач с жесткими дедлайнами
     */
    public static List<Task> generateTightDeadlineTasks(int count) {
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int arrival = random.nextInt(40);
            int exec = 5 + random.nextInt(10);
            int priority = 1 + random.nextInt(10);
            
            // Жесткий дедлайн: arrival + exec * (1-2)
            int deadline = arrival + exec * (1 + random.nextInt(2));
            
            Task task = new Task(
                "TD" + i, // Tight Deadline
                arrival, exec, priority, Task.TaskType.CPU_BOUND, 
                deadline, 1.0, false, Integer.MAX_VALUE
            );

            tasks.add(task);
        }

        tasks.sort((a, b) -> Integer.compare(a.getArrivalTime(), b.getArrivalTime()));
        return tasks;
    }

    /**
     * Печатает статистику по списку задач
     */
    public static void printTaskStats(List<Task> tasks) {
        System.out.println("\n=== СТАТИСТИКА ЗАДАЧ ===");
        System.out.println("Всего задач: " + tasks.size());
        
        long cpuCount = tasks.stream().filter(t -> t.getType() == Task.TaskType.CPU_BOUND).count();
        long ioCount = tasks.stream().filter(t -> t.getType() == Task.TaskType.IO_BOUND).count();
        
        System.out.printf("CPU-bound: %d (%.1f%%)\n", cpuCount, (cpuCount * 100.0 / tasks.size()));
        System.out.printf("IO-bound: %d (%.1f%%)\n", ioCount, (ioCount * 100.0 / tasks.size()));
        
        double avgExec = tasks.stream().mapToInt(Task::getExecutionTime).average().orElse(0);
        double avgPriority = tasks.stream().mapToInt(Task::getPriority).average().orElse(0);
        
        System.out.printf("Среднее время выполнения: %.1f\n", avgExec);
        System.out.printf("Средний приоритет: %.1f\n", avgPriority);
    }
}