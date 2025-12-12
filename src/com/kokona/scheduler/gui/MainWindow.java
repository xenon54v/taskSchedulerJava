package com.kokona.scheduler.gui;

import com.kokona.scheduler.simulation.SchedulerSimulator;
import com.kokona.scheduler.generator.TaskGenerator;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.*;
import com.kokona.scheduler.metrics.SchedulerMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MainWindow extends JFrame {
    private JSpinner taskCountSpinner;
    private JCheckBox fifoCheck, lifoCheck, sjfCheck;
    private JButton runButton;
    private JTextArea logArea;
    
    public MainWindow() {
        setTitle("Task Scheduler Simulator");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        initComponents();
        setVisible(true);
    }
    
    private void initComponents() {
        // 1. Панель настроек (верх)
        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Настройки симуляции"));
        
        settingsPanel.add(new JLabel("Количество задач:"));
        taskCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        settingsPanel.add(taskCountSpinner);
        
        settingsPanel.add(new JLabel("Макс. время прибытия:"));
        JSpinner arrivalSpinner = new JSpinner(new SpinnerNumberModel(50, 1, 200, 1));
        settingsPanel.add(arrivalSpinner);
        
        settingsPanel.add(new JLabel("Макс. время выполнения:"));
        JSpinner execSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 50, 1));
        settingsPanel.add(execSpinner);
        
        add(settingsPanel, BorderLayout.NORTH);
        
        // 2. Панель выбора алгоритмов (центр)
        JPanel algoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        algoPanel.setBorder(BorderFactory.createTitledBorder("Алгоритмы планирования"));
        
        fifoCheck = new JCheckBox("FIFO (First In, First Out)", true);
        lifoCheck = new JCheckBox("LIFO (Last In, First Out)", true);
        sjfCheck = new JCheckBox("SJF (Shortest Job First)", true);
        
        algoPanel.add(fifoCheck);
        algoPanel.add(lifoCheck);
        algoPanel.add(sjfCheck);
        
        add(algoPanel, BorderLayout.CENTER);
        
        // 3. Панель кнопок (юг)
        JPanel buttonPanel = new JPanel();
        runButton = new JButton("▶ Запустить симуляцию");
        runButton.setFont(new Font("Arial", Font.BOLD, 14));
        runButton.setBackground(new Color(46, 204, 113));
        runButton.setForeground(Color.WHITE);
        
        runButton.addActionListener(e -> runSimulation(
            (Integer) taskCountSpinner.getValue(),
            (Integer) arrivalSpinner.getValue(),
            (Integer) execSpinner.getValue()
        ));
        
        buttonPanel.add(runButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void runSimulation(int taskCount, int maxArrival, int maxExec) {
        // Генерируем задачи
        List<Task> tasks = TaskGenerator.generateTasks(taskCount, maxArrival, maxExec);
        
        // Создаем выбранные планировщики
        java.util.List<Scheduler> schedulers = new java.util.ArrayList<>();
        if (fifoCheck.isSelected()) schedulers.add(new FifoScheduler());
        if (lifoCheck.isSelected()) schedulers.add(new LifoScheduler());
        if (sjfCheck.isSelected()) schedulers.add(new SJFScheduler());
        
        // Запускаем симуляцию для каждого планировщика
        java.util.List<SchedulerMetrics> results = new java.util.ArrayList<>();
        
        for (Scheduler scheduler : schedulers) {
            // Клонируем задачи для каждого планировщика
            List<Task> clonedTasks = TaskGenerator.copyTasks(tasks);
            
            // Запускаем симуляцию
            SchedulerMetrics metrics = SchedulerSimulator.simulate(scheduler, clonedTasks);
            results.add(metrics);
        }
        
        // Открываем окно с результатами
        new ResultsWindow(tasks, results);
    }
}