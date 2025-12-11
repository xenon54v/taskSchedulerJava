package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;

public class PreemptivePriorityScheduler extends PriorityScheduler {
	
	private Task currentTask = null;
    private boolean isCurrentTaskPreempted = false;
    
    @Override
    public void addTask(Task newTask) {
        super.addTask(newTask);
        
        // Вытеснение: если новая задача имеет более высокий приоритет, чем текущая
        if (currentTask != null && 
            !currentTask.isCompleted() && 
            newTask.getPriority() > currentTask.getPriority()) {
            
            // Возвращаем текущую задачу обратно в кучу
            super.addTask(currentTask);
            currentTask.setContextSwitches(currentTask.getContextSwitches() + 1);
            isCurrentTaskPreempted = true;
            
            // Начинаем выполнять новую задачу
            currentTask = newTask;
        }
    }
    
    @Override
    public Task getNextTask() {
        // Если текущая задача была вытеснена, нужно сначала вернуть новую задачу
        if (isCurrentTaskPreempted) {
            isCurrentTaskPreempted = false;
            return currentTask;
        }
        
        // Иначе получаем следующую задачу из кучи
        currentTask = super.getNextTask();
        return currentTask;
    }
    
    @Override
    public void taskCompleted(Task task) {
        currentTask = null;
        super.taskCompleted(task);
    }
    
    @Override
    public String getName() {
        return "Preemptive Priority Scheduler";
    }

}
