package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import java.util.Optional;
import java.util.Stack;

	/**
	 * Реализация планировщика LIFO (Last-In-First-Out)
	 * Последняя добавленная задача выполняется первой
	 * Использует структуру данных Stack
	 */
	public class LifoScheduler implements Scheduler {
	    private final Stack<Task> stack;  // Структура данных Stack для LIFO
	    private final String name;
	    
	    public LifoScheduler() {
	        this.stack = new Stack<>();
	        this.name = "LIFO";
	    }
	    
	    @Override
	    public void addTask(Task task) {
	        stack.push(task);  // Добавляем задачу в вершину стека
	        System.out.println("Added task: " + task.getId() + " (Stack size: " + stack.size() + ")");
	    }
	    
	    @Override
	    public Optional<Task> getNextTask() {
	        if (stack.isEmpty()) {
	            return Optional.empty();
	        }
	        Task task = stack.pop();  // Извлекаем задачу с вершины стека
	        System.out.println("Processing task: " + task.getId() + " (Stack size: " + stack.size() + ")");
	        return Optional.of(task);
	    }
	    
	    @Override
	    public boolean hasTasks() {
	        return !stack.isEmpty();
	    }
	    
	    @Override
	    public String getName() {
	        return name;
	    }
	    
	    /**
	     * Дополнительный метод для просмотра вершины стека без извлечения
	     */
	    public Optional<Task> peek() {
	        return stack.isEmpty() ? Optional.empty() : Optional.of(stack.peek());
	    }
	    
	    /**
	     * Возвращает текущий размер стека
	     */
	    public int getSize() {
	        return stack.size();
	    }

	}
