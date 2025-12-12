package com.kokona.scheduler.gui;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.metrics.SchedulerMetrics;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ResultsWindow extends JFrame {
    
    public ResultsWindow(List<TaskTablePanel.SimulationResult> results) {
        setTitle("Результаты симуляции - Таблица задач");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); // Центрируем окно
        
        // Просто используем нашу панель с таблицами
        add(new TaskTablePanel(results), BorderLayout.CENTER);
        
        // Кнопка закрытия
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dispose());
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
}