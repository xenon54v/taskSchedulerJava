package com.kokona.scheduler.gui;

import javax.swing.*;
import java.awt.*;

public class ResultsWindow extends JFrame {
    
    public ResultsWindow(java.util.List<TaskTablePanel.SimulationResult> results) {
        setTitle(" Результаты сравнения алгоритмов");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Создаём вкладки
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Вкладка 1: Детальная таблица задач
        tabbedPane.addTab(" Таблица выполнения", new TaskTablePanel(results));
        
        // Вкладка 2: Графики сравнения
        tabbedPane.addTab(" Графики сравнения", new ComparisonChartsPanel(results));
        
        // Вкладка 3: Рекомендации
        tabbedPane.addTab(" Рекомендации", new RecommendationsPanel(results));
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Панель с кнопками
        JPanel buttonPanel = new JPanel();
        
        JButton saveButton = new JButton("Сохранить отчёт");
        saveButton.addActionListener(e -> saveReport(results));
        
        JButton closeButton = new JButton("❌ Закрыть");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void saveReport(java.util.List<TaskTablePanel.SimulationResult> results) {
        JOptionPane.showMessageDialog(this,
            "Функция сохранения отчёта будет реализована позже",
            "В разработке",
            JOptionPane.INFORMATION_MESSAGE);
    }
}