package com.kokona.scheduler.schedulers;

import com.kokona.scheduler.model.Task;
import java.util.Optional;

public interface Scheduler {
	void addTask(Task task);
    Optional<Task> getNextTask();
    boolean hasTasks();
    String getName();

}
