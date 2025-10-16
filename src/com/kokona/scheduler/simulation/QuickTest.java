package com.kokona.scheduler.simulation;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.FifoScheduler;
import com.kokona.scheduler.schedulers.LifoScheduler;
import com.kokona.scheduler.schedulers.Scheduler;

public class QuickTest {
    public static void main(String[] args) {
        System.out.println("=== QUICK TEST ===");
        
        testFIFO();
        testLIFO();
    }
    
    private static void testFIFO() {
        System.out.println("\n--- Testing FIFO Scheduler ---");
        Scheduler scheduler = new FifoScheduler();
        
        Task task1 = new Task("T1", 0, 5);
        Task task2 = new Task("T2", 1, 3);
        Task task3 = new Task("T3", 2, 4);
        
        scheduler.addTask(task1);
        scheduler.addTask(task2);
        scheduler.addTask(task3);
        
        System.out.print("FIFO Execution order: ");
        while (scheduler.hasTasks()) {
            Task task = scheduler.getNextTask().get();
            System.out.print(task.getId() + " ");
        }
        System.out.println("\nExpected: T1 T2 T3");
    }
    
    private static void testLIFO() {
        System.out.println("\n--- Testing LIFO Scheduler ---");
        LifoScheduler scheduler = new LifoScheduler();
        
        Task task1 = new Task("T1", 0, 5);
        Task task2 = new Task("T2", 1, 3);
        Task task3 = new Task("T3", 2, 4);
        
        scheduler.addTask(task1);
        scheduler.addTask(task2);
        scheduler.addTask(task3);
        
        System.out.print("LIFO Execution order: ");
        while (scheduler.hasTasks()) {
            Task task = scheduler.getNextTask().get();
            System.out.print(task.getId() + " ");
        }
        System.out.println("\nExpected: T3 T2 T1");
        
        System.out.println("!LIFO test completed!");
    }
}
