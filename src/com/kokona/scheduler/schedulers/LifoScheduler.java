package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import java.util.Optional;
import java.util.Stack;

	/**
	 * реализация планировщика LIFO (Last-In-First-Out)
	 * последняя добавленная задача выполняется первой
	 * использует структуру данных Stack
	 */
	public class LifoScheduler implements Scheduler {
	    private final Stack<Task> stack;  // структура данных Stack для LIFO
	    private final String name;
	    
	    public LifoScheduler() {
	        this.stack = new Stack<>();
	        this.name = "LIFO";
	    }
	    
	    @Override
	    public void addTask(Task task) {
	        stack.push(task);  // добавляем задачу в вершину стека
	    }
	    
	    @Override
	    public Optional<Task> getNextTask() {
	        if (stack.isEmpty()) {
	            return Optional.empty();
	        }
	        Task task = stack.pop();  // извлекаем задачу с вершины стека
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
	    
	    // дополнительный метод для просмотра вершины стека без извлечения
	    public Optional<Task> peek() {
	        return stack.isEmpty() ? Optional.empty() : Optional.of(stack.peek());
	    }
	    
	    //возвращает текущий размер стека
	    public int getSize() {
	        return stack.size();
	    }

	}
