package com.kokona.scheduler.model;

public class Task {
    private final String id;
    private final int arrivalTime;
    private final int executionTime;
    private int remainingTime;
    private int startTime;
    private int finishTime;
    
    // === ПОЛЯ ДЛЯ МЕТРИК ===
    private int priority;           // приоритет задачи (1-10)
    private TaskType type;          // тип задачи
    private int deadline;           // дедлайн
    private double weight;          // вес задачи
    private boolean hasJitter;      // наличие джиттера
    private int ioInterval;         // интервал IO операций
    
    // Метрики выполнения
    private int responseTime = -1;  // время первого ответа
    private int totalExecutionTime = 0; // общее время выполнения
    private int contextSwitches = 0;    // количество переключений контекста
    
    public enum TaskType {
        CPU_BOUND, IO_BOUND
    }
    
    // === СУЩЕСТВУЮЩИЕ КОНСТРУКТОРЫ (сохраняем для обратной совместимости) ===
    public Task(String id, int arrivalTime, int executionTime) {
        this(id, arrivalTime, executionTime, 5, TaskType.CPU_BOUND, 
             arrivalTime + executionTime * 2, 1.0, false, Integer.MAX_VALUE);
    }
    
    // === НОВЫЙ ПОЛНЫЙ КОНСТРУКТОР ===
    public Task(String id, int arrivalTime, int executionTime, int priority, 
                TaskType type, int deadline, double weight, boolean hasJitter, int ioInterval) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.executionTime = executionTime;
        this.remainingTime = executionTime;
        this.startTime = -1;
        this.finishTime = -1;
        
        // Новые поля
        this.priority = priority;
        this.type = type;
        this.deadline = deadline;
        this.weight = weight;
        this.hasJitter = hasJitter;
        this.ioInterval = ioInterval;
    }
    
    // === СУЩЕСТВУЮЩИЕ ГЕТТЕРЫ ===
    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getExecutionTime() { return executionTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getStartTime() { return startTime; }
    public int getFinishTime() { return finishTime; }
    
    // === НОВЫЕ ГЕТТЕРЫ ===
    public int getPriority() { return priority; }
    public TaskType getType() { return type; }
    public int getDeadline() { return deadline; }
    public double getWeight() { return weight; }
    public boolean hasJitter() { return hasJitter; }
    public int getIoInterval() { return ioInterval; }
    public int getResponseTime() { return responseTime; }
    public int getTotalExecutionTime() { return totalExecutionTime; }
    public int getContextSwitches() { return contextSwitches; }
    
    // === СУЩЕСТВУЮЩИЕ СЕТТЕРЫ ===
    public void setRemainingTime(int remainingTime) { 
        this.remainingTime = remainingTime; 
    }
    public void setStartTime(int startTime) { 
        this.startTime = startTime; 
    }
    public void setFinishTime(int finishTime) { 
        this.finishTime = finishTime; 
    }
    public void setPriority(int priority) { 
        this.priority = priority;
    }
    
    /**
     * Выполняет задачу в течение указанного кванта времени
     * @param timeQuantum квант времени для выполнения
     * @return true если задача завершена, false если еще осталось время
     */
    public boolean execute(int timeQuantum) {
        if (remainingTime <= timeQuantum) {
            totalExecutionTime += remainingTime;
            remainingTime = 0;
            return true; // задача завершена
        } else {
            remainingTime -= timeQuantum;
            totalExecutionTime += timeQuantum;
            return false; // задача не завершена
        }
    }
    
    /**
     * Вычисляет время ожидания задачи
     * @return время ожидания (startTime - arrivalTime)
     */
    public int getWaitingTime() {
        if (startTime == -1) return 0; // задача еще не начиналась
        return startTime - arrivalTime;
    }
    
    /**
     * Вычисляет общее время выполнения (от поступления до завершения)
     * @return общее время выполнения (finishTime - arrivalTime)
     */
    public int getTurnaroundTime() {
        if (finishTime == -1) return 0; // задача еще не завершена
        return finishTime - arrivalTime;
    }
    
    /**
     * Проверяет, пропущен ли дедлайн
     * @return true если дедлайн пропущен
     */
    public boolean isDeadlineMissed() {
        if (finishTime == -1) return false; // задача еще не завершена
        return finishTime > deadline;
    }
    
    /**
     * Вычисляет насколько сильно пропущен дедлайн
     * @return величина пропуска дедлайна (0 если не пропущен)
     */
    public int getDeadlineMiss() {
        if (finishTime == -1 || finishTime <= deadline) return 0;
        return finishTime - deadline;
    }
    
    /**
     * Взвешенное время выполнения (для weighted scheduling)
     * @return executionTime / weight
     */
    public double getWeightedExecutionTime() {
        return executionTime / weight;
    }
    
    /**
     * Проверяет, готова ли задача к выполнению в текущий момент времени
     * @param currentTime текущее время симуляции
     * @return true если задача может быть выполнена
     */
    public boolean isReady(int currentTime) {
        return currentTime >= arrivalTime && !isCompleted();
    }
    
    /**
     * Проверяет, завершена ли задача
     * @return true если задача завершена
     */
    public boolean isCompleted() {
        return remainingTime == 0;
    }
    
    /**
     * Проверяет, началась ли задача
     * @return true если задача уже начиналась
     */
    public boolean isStarted() {
        return startTime != -1;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Task{id='%s', arrival=%d, execution=%d, remaining=%d, priority=%d, type=%s, deadline=%d}", 
            id, arrivalTime, executionTime, remainingTime, priority, type, deadline
        );
    }
    
    /**
     * Детальная информация для отладки
     */
    public String toDetailedString() {
        return String.format(
            "Task{id='%s', arrival=%d, exec=%d, rem=%d, prio=%d, type=%s, deadline=%d, weight=%.2f, jitter=%s, io=%d}", 
            id, arrivalTime, executionTime, remainingTime, priority, type, 
            deadline, weight, hasJitter, ioInterval
        );
    }
}