package com.kokona.scheduler.simulation;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.FifoScheduler;
import com.kokona.scheduler.schedulers.Scheduler;

public class SimpleTest {
	public static void main(String[] args) {
        System.out.println("=== Testing FIFO Scheduler ===");
        
        Scheduler scheduler = new FifoScheduler();
        
        // Создаем задачи
        Task task1 = new Task("1", 0, 5);
        Task task2 = new Task("2", 1, 3);
        Task task3 = new Task("3", 2, 4);
        
        // Добавляем в планировщик
        scheduler.addTask(task1);
        scheduler.addTask(task2);
        scheduler.addTask(task3);
        
        // Извлекаем и выводим порядок
        System.out.print("Execution order: ");
        while (scheduler.hasTasks()) {
            Task task = scheduler.getNextTask().get();
            System.out.print(task.getId() + " ");
        }
        System.out.println("\nExpected: 1 2 3");
        System.out.println("Test completed!");
    }

}
