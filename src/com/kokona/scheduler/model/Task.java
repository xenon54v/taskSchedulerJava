package com.kokona.scheduler.model;

public class Task {
    private final String id;
    private final int arrivalTime;
    private final int executionTime;

    private int remainingTime;
    private int startTime;
    private int finishTime;

    // === ПОЛЯ ДЛЯ МЕТРИК ===
    private int priority;           // 1-10
    private TaskType type;          // CPU/IO
    private int deadline;           // дедлайн
    private double weight;          // вес задачи
    private boolean hasJitter;      // наличие джиттера
    private int ioInterval;         // интервал IO операций (для IO_BOUND)

    // Метрики выполнения
    private int responseTime = -1;       // время первого ответа (start - arrival)
    private int totalExecutionTime = 0;  // фактическое время CPU, отданное задаче
    private int contextSwitches = 0;     // можно использовать позже

    // === IO-блокировка (для IO_BOUND) ===
    private int cpuSinceLastIo = 0;
    private int ioBlockedUntil = -1;

    public enum TaskType {
        CPU_BOUND, IO_BOUND
    }

    // === СОВМЕСТИМОСТЬ: старый конструктор ===
    public Task(String id, int arrivalTime, int executionTime) {
        this(id, arrivalTime, executionTime, 5, TaskType.CPU_BOUND,
                arrivalTime + executionTime * 2, 1.0, false, Integer.MAX_VALUE);
    }

    // === Полный конструктор ===
    public Task(String id, int arrivalTime, int executionTime, int priority,
                TaskType type, int deadline, double weight, boolean hasJitter, int ioInterval) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.executionTime = executionTime;

        this.remainingTime = executionTime;
        this.startTime = -1;
        this.finishTime = -1;

        this.priority = priority;
        this.type = type;
        this.deadline = deadline;
        this.weight = weight;
        this.hasJitter = hasJitter;
        this.ioInterval = ioInterval;
    }

    // === БАЗОВЫЕ ГЕТТЕРЫ ===
    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getExecutionTime() { return executionTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getStartTime() { return startTime; }
    public int getFinishTime() { return finishTime; }

    // === ДОП. ГЕТТЕРЫ ===
    public int getPriority() { return priority; }
    public TaskType getType() { return type; }
    public int getDeadline() { return deadline; }
    public double getWeight() { return weight; }
    public boolean hasJitter() { return hasJitter; }
    public int getIoInterval() { return ioInterval; }
    public int getResponseTime() { return responseTime; }
    public int getTotalExecutionTime() { return totalExecutionTime; }
    public int getContextSwitches() { return contextSwitches; }

    // === СЕТТЕРЫ (оставляем для совместимости) ===
    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    /**
     * ВАЖНО: setStartTime оставляем, но лучше использовать markStarted(now),
     * чтобы корректно выставлялся responseTime.
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
        // Если кто-то по старинке вызывает setStartTime — responseTime тоже выставим
        if (this.responseTime == -1 && startTime >= 0) {
            this.responseTime = startTime - arrivalTime;
        }
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    // === УДОБНЫЕ МЕТОДЫ ДЛЯ СИМУЛЯТОРА ===

    /**
     * Корректно помечает старт задачи (и ставит responseTime один раз).
     */
    public void markStarted(int currentTime) {
        if (startTime == -1) {
            startTime = currentTime;
        }
        if (responseTime == -1) {
            responseTime = startTime - arrivalTime;
        }
    }

    /**
     * IO-блокировка: задача считается заблокированной до момента ioBlockedUntil.
     */
    public boolean isBlocked(int currentTime) {
        return ioBlockedUntil != -1 && currentTime < ioBlockedUntil;
    }

    /**
     * Если время пришло — снимаем блокировку.
     */
    public void unblockIfNeeded(int currentTime) {
        if (ioBlockedUntil != -1 && currentTime >= ioBlockedUntil) {
            ioBlockedUntil = -1;
        }
    }

    /**
     * Выполняет задачу на 1 тик CPU (или на timeQuantum), БЕЗ IO-логики.
     * Сохраняем для совместимости с текущим SchedulerSimulator.
     */
    public boolean execute(int timeQuantum) {
        if (remainingTime <= 0) return true;

        if (remainingTime <= timeQuantum) {
            totalExecutionTime += remainingTime;
            remainingTime = 0;
            return true;
        } else {
            remainingTime -= timeQuantum;
            totalExecutionTime += timeQuantum;
            return false;
        }
    }

    /**
     * НОВОЕ: выполнение одного тика С УЧЁТОМ IO.
     * - если задача заблокирована -> CPU тик не тратится, возвращаем false
     * - если IO_BOUND и накопили ioInterval -> уходим в блокировку на ioBlockDuration тиков
     *
     * ioBlockDuration — задаёшь в симуляторе (например 3).
     *
     * @return true если задача завершилась на этом тике CPU
     */
    public boolean executeOneTickWithIo(int currentTime, int ioBlockDuration) {
        // Снимаем блокировку, если пора
        unblockIfNeeded(currentTime);

        // Если заблокирована — не выполняем CPU
        if (isBlocked(currentTime)) {
            return false;
        }

        // Выполняем 1 тик CPU
        boolean completed = execute(1);

        // Если не завершена и IO_BOUND — имитируем IO операции
        if (!completed && type == TaskType.IO_BOUND) {
            cpuSinceLastIo++;

            // Защита: если ioInterval некорректный, считаем, что IO не бывает
            if (ioInterval > 0 && ioInterval != Integer.MAX_VALUE && cpuSinceLastIo >= ioInterval) {
                cpuSinceLastIo = 0;
                ioBlockedUntil = currentTime + ioBlockDuration; // блокируем до этого момента
            }
        }

        return completed;
    }

    // === МЕТРИКИ (честные) ===

    /**
     * Waiting time (универсально): turnaround - фактическое CPU-время.
     * Корректно даже при IO-блокировках/простоях/вытеснении.
     */
    public int getWaitingTime() {
        if (finishTime == -1) return 0;
        return Math.max(0, getTurnaroundTime() - totalExecutionTime);
    }

    public int getTurnaroundTime() {
        if (finishTime == -1) return 0;
        return finishTime - arrivalTime;
    }

    public boolean isDeadlineMissed() {
        if (finishTime == -1) return false;
        return finishTime > deadline;
    }

    public int getDeadlineMiss() {
        if (finishTime == -1 || finishTime <= deadline) return 0;
        return finishTime - deadline;
    }

    public double getWeightedExecutionTime() {
        return executionTime / weight;
    }

    public boolean isReady(int currentTime) {
        return currentTime >= arrivalTime && !isCompleted();
    }

    public boolean isCompleted() {
        return remainingTime == 0;
    }

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

    public String toDetailedString() {
        return String.format(
                "Task{id='%s', arrival=%d, exec=%d, rem=%d, prio=%d, type=%s, deadline=%d, weight=%.2f, jitter=%s, io=%d, resp=%d, cpu=%d}",
                id, arrivalTime, executionTime, remainingTime, priority, type,
                deadline, weight, hasJitter, ioInterval, responseTime, totalExecutionTime
        );
    }
}