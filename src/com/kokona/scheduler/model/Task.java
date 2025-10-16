package com.kokona.scheduler.model;

public class Task {
	private final String id;
	private final int arrivalTime;
	private final int executionTime;
	private int remainingTime;
	private int startTime;
	private int finishTime;
	
	public Task(String id, int arrivalTime, int executionTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.executionTime = executionTime;
        this.remainingTime = executionTime;
        this.startTime = -1;   // -1 означает, что задача еще не начиналась
        this.finishTime = -1;  // -1 означает, что задача еще не завершена
    }
	
	// геттеры
    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getExecutionTime() { return executionTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getStartTime() { return startTime; }
    public int getFinishTime() { return finishTime; }
    
    // сеттеры
    public void setRemainingTime(int remainingTime) { 
        this.remainingTime = remainingTime; 
    }
    public void setStartTime(int startTime) { 
        this.startTime = startTime; 
    }
    public void setFinishTime(int finishTime) { 
        this.finishTime = finishTime; 
    }
    
    /**
     * выполняет задачу в течение указанного кванта времени
     * @param timeQuantum квант времени для выполнения
     * @return true если задача завершена, false если еще осталось время
     */
    
    public boolean execute(int timeQuantum) {
        if (remainingTime <= timeQuantum) {
            remainingTime = 0;
            return true; // задача завершена
        } else {
            remainingTime -= timeQuantum;
            return false; // задача не завершена
        }
    }
    
    /**
     * вычисляет время ожидания задачи
     * @return время ожидания (startTime - arrivalTime)
     */
    
    public int getWaitingTime() {
        if (startTime == -1) return 0; // задача еще не начиналась
        return startTime - arrivalTime;
    }
    
    /**
     * вычисляет общее время выполнения (от поступления до завершения)
     * @return общее время выполнения (finishTime - arrivalTime)
     */
    public int getTurnaroundTime() {
        if (finishTime == -1) return 0; // задача еще не завершена
        return finishTime - arrivalTime;
    }
    
    /**
     * проверяет, завершена ли задача
     * @return true если задача завершена
     */
    public boolean isCompleted() {
        return remainingTime == 0;
    }
    
    @Override
    public String toString() {
        return String.format("Task{id='%s', arrival=%d, execution=%d, remaining=%d}", 
                           id, arrivalTime, executionTime, remainingTime);
    }

}
