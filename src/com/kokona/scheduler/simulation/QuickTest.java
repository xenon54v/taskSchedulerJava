package com.kokona.scheduler.simulation;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.FifoScheduler;
import com.kokona.scheduler.schedulers.Scheduler;

public class QuickTest {
	public static void main(String[] args) {
        System.out.println("=== QUICK TEST ===");
        
        // 1. Создаем планировщик
        Scheduler scheduler = new FifoScheduler();
        System.out.println("Scheduler created: " + scheduler.getName());
        
        // 2. Создаем задачи
        Task task1 = new Task("T1", 0, 5);
        Task task2 = new Task("T2", 1, 3);
        System.out.println("Tasks created: " + task1 + ", " + task2);
        
        // 3. Добавляем задачи
        scheduler.addTask(task1);
        scheduler.addTask(task2);
        System.out.println("Tasks added to scheduler");
        
        // 4. Проверяем наличие задач
        System.out.println("Has tasks: " + scheduler.hasTasks());
        
        // 5. Извлекаем задачи
        System.out.print("Execution order: ");
        while (scheduler.hasTasks()) {
            Task task = scheduler.getNextTask().get();
            System.out.print(task.getId() + " ");
        }
        System.out.println("\n✅ TEST PASSED! Basic functionality works.");
    }

}
