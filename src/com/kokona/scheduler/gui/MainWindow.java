package com.kokona.scheduler.gui;

import com.kokona.scheduler.simulation.SchedulerSimulator;
import com.kokona.scheduler.generator.TaskGenerator;
import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.schedulers.*;
import com.kokona.scheduler.metrics.SchedulerMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
	private JSpinner taskCountSpinner;
    private JSpinner arrivalSpinner;
    private JSpinner execSpinner;
    private JCheckBox fifoCheck, lifoCheck, sjfCheck;
    private JButton runButton;
    private JTextArea logArea;
    
    public MainWindow() {
        setTitle("Task Scheduler Simulator - Сценарии");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        initComponents();
        setVisible(true);
    }
    
    private void initComponents() {
        // 1. Заголовок
        JLabel title = new JLabel("🧠 Планировщик задач с готовыми сценариями", 
                                  SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));
        add(title, BorderLayout.NORTH);
        
        // 2. Основная панель (центр)
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        
        // Панель настроек
        mainPanel.add(createSettingsPanel());
        
        // Панель сценариев
        mainPanel.add(createScenariosPanel());
        
        // Панель алгоритмов
        mainPanel.add(createAlgorithmsPanel());
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 3. Кнопка запуска (юг)
        JPanel buttonPanel = new JPanel();
        runButton = new JButton("🚀 Запустить симуляцию");
        runButton.setFont(new Font("Arial", Font.BOLD, 14));
        runButton.setBackground(new Color(46, 204, 113));
        runButton.setForeground(Color.WHITE);
        runButton.setPreferredSize(new Dimension(200, 40));
        
        runButton.addActionListener(e -> runSimulation());
        
        buttonPanel.add(runButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("⚙️ Основные параметры"));
        
        // Количество задач
        panel.add(new JLabel("Количество задач:"));
        taskCountSpinner = new JSpinner(new SpinnerNumberModel(15, 1, 100, 1));
        panel.add(taskCountSpinner);
        
        // Макс. время прибытия
        panel.add(new JLabel("Макс. время прибытия:"));
        arrivalSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));
        panel.add(arrivalSpinner);
        
        // Макс. время выполнения
        panel.add(new JLabel("Макс. время выполнения:"));
        execSpinner = new JSpinner(new SpinnerNumberModel(25, 1, 100, 1));
        panel.add(execSpinner);
        
        return panel;
    }
    
    private JPanel createScenariosPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("🎭 Готовые сценарии"));
        
        // Сценарий 1: Демонстрация SJF
        JButton scenario1 = createScenarioButton("SJF vs FIFO", 
            "Показать преимущество Shortest Job First",
            new Color(70, 130, 180), 
            () -> {
                taskCountSpinner.setValue(15);
                arrivalSpinner.setValue(20);
                execSpinner.setValue(30);
                fifoCheck.setSelected(true);
                lifoCheck.setSelected(true);
                sjfCheck.setSelected(true);
            });
        
        // Сценарий 2: Справедливость FIFO
        JButton scenario2 = createScenarioButton("Справедливый FIFO", 
            "Все алгоритмы работают примерно одинаково",
            new Color(46, 204, 113),
            () -> {
                taskCountSpinner.setValue(10);
                arrivalSpinner.setValue(50);
                execSpinner.setValue(15);
                fifoCheck.setSelected(true);
                lifoCheck.setSelected(true);
                sjfCheck.setSelected(true);
            });
        
        // Сценарий 3: Провал LIFO
        JButton scenario3 = createScenarioButton("Провал LIFO", 
            "LIFO показывает худший результат",
            new Color(220, 20, 60),
            () -> {
                taskCountSpinner.setValue(20);
                arrivalSpinner.setValue(5);
                execSpinner.setValue(40);
                fifoCheck.setSelected(true);
                lifoCheck.setSelected(true);
                sjfCheck.setSelected(true);
            });
        
        // Сценарий 4: Реалистичный день
        JButton scenario4 = createScenarioButton("Рабочий день", 
            "Реалистичное распределение задач",
            new Color(155, 89, 182),
            () -> {
                taskCountSpinner.setValue(12);
                arrivalSpinner.setValue(100);
                execSpinner.setValue(40);
                fifoCheck.setSelected(true);
                lifoCheck.setSelected(false); // LIFO обычно не используется
                sjfCheck.setSelected(true);
            });
        
        panel.add(scenario1);
        panel.add(scenario2);
        panel.add(scenario3);
        panel.add(scenario4);
        
        return panel;
    }
    
    private JButton createScenarioButton(String title, String tooltip, 
                                        Color color, Runnable action) {
        JButton button = new JButton("<html><center>" + title + "</center></html>");
        button.setToolTipText(tooltip);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setPreferredSize(new Dimension(120, 60));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        
        button.addActionListener(e -> {
            action.run();
            JOptionPane.showMessageDialog(this, 
                "Сценарий '" + title + "' загружен!\n" + tooltip,
                "Сценарий установлен",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        return button;
    }
    
    private JPanel createAlgorithmsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panel.setBorder(BorderFactory.createTitledBorder("📋 Выберите алгоритмы для сравнения"));
        
        fifoCheck = new JCheckBox("FIFO", true);
        lifoCheck = new JCheckBox("LIFO", true);
        sjfCheck = new JCheckBox("SJF", true);
        
        // Стилизуем чекбоксы
        styleCheckbox(fifoCheck, new Color(70, 130, 180));
        styleCheckbox(lifoCheck, new Color(220, 20, 60));
        styleCheckbox(sjfCheck, new Color(46, 204, 113));
        
        panel.add(fifoCheck);
        panel.add(lifoCheck);
        panel.add(sjfCheck);
        
        // Кнопка "Выбрать все"
        JButton selectAll = new JButton("Выбрать все");
        selectAll.addActionListener(e -> {
            fifoCheck.setSelected(true);
            lifoCheck.setSelected(true);
            sjfCheck.setSelected(true);
        });
        
        // Кнопка "Сбросить"
        JButton deselectAll = new JButton("Сбросить");
        deselectAll.addActionListener(e -> {
            fifoCheck.setSelected(false);
            lifoCheck.setSelected(false);
            sjfCheck.setSelected(false);
        });
        
        panel.add(Box.createHorizontalStrut(20));
        panel.add(selectAll);
        panel.add(deselectAll);
        
        return panel;
    }
    
    private void styleCheckbox(JCheckBox checkbox, Color color) {
        checkbox.setFont(new Font("Arial", Font.BOLD, 12));
        checkbox.setForeground(color);
    }
    
    private void runSimulation() {
        // Проверяем, что выбран хотя бы один алгоритм
        if (!fifoCheck.isSelected() && !lifoCheck.isSelected() && !sjfCheck.isSelected()) {
            JOptionPane.showMessageDialog(this,
                    "Пожалуйста, выберите хотя бы один алгоритм планирования!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int taskCount = (Integer) taskCountSpinner.getValue();
        final int maxArrival = (Integer) arrivalSpinner.getValue();
        final int maxExec = (Integer) execSpinner.getValue();

        // Блокируем кнопку, чтобы не запустить несколько раз
        runButton.setEnabled(false);

        // Диалог прогресса (модальный), но показываем его корректно — через invokeLater,
        // а саму работу делаем в SwingWorker.
        final JDialog loadingDialog = createLoadingDialog();

        SwingWorker<List<TaskTablePanel.SimulationResult>, Void> worker =
                new SwingWorker<>() {
                    @Override
                    protected List<TaskTablePanel.SimulationResult> doInBackground() {
                        // Генерируем задачи
                        List<Task> tasks = TaskGenerator.generateTasks(taskCount, maxArrival, maxExec);

                        // Создаем выбранные планировщики
                        List<Scheduler> schedulers = new ArrayList<>();
                        if (fifoCheck.isSelected()) schedulers.add(new FifoScheduler());
                        if (lifoCheck.isSelected()) schedulers.add(new LifoScheduler());
                        if (sjfCheck.isSelected()) schedulers.add(new SJFScheduler());

                        // Запускаем симуляцию для каждого планировщика
                        List<TaskTablePanel.SimulationResult> allResults = new ArrayList<>();

                        for (Scheduler scheduler : schedulers) {
                            List<Task> clonedTasks = TaskGenerator.copyTasks(tasks);
                            SchedulerMetrics metrics = SchedulerSimulator.simulate(scheduler, clonedTasks);

                            allResults.add(new TaskTablePanel.SimulationResult(
                                    scheduler.getName(),
                                    metrics,
                                    clonedTasks
                            ));
                        }

                        return allResults;
                    }

                    @Override
                    protected void done() {
                        // done() всегда вызывается в EDT — тут безопасно трогать UI
                        try {
                            List<TaskTablePanel.SimulationResult> allResults = get();

                            loadingDialog.dispose();
                            runButton.setEnabled(true);

                            // Открываем окно с результатами
                            new ResultsWindow(allResults);

                            // Показываем статистику
                            showQuickStats(allResults);

                        } catch (Exception e) {
                            loadingDialog.dispose();
                            runButton.setEnabled(true);

                            String msg = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
                            JOptionPane.showMessageDialog(MainWindow.this,
                                    "Ошибка при запуске симуляции: " + msg,
                                    "Ошибка",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

        worker.execute();

        // Показываем диалог ПОСЛЕ старта worker (и корректно из EDT)
        SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
    }

    private JDialog createLoadingDialog() {
        JDialog dialog = new JDialog(this, "Запуск симуляции…", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(360, 140);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel label = new JLabel("⏳ Генерирую задачи и запускаю симуляцию…", SwingConstants.CENTER);
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        panel.add(label, BorderLayout.CENTER);
        panel.add(bar, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        return dialog;
    }
    
    private void showQuickStats(List<TaskTablePanel.SimulationResult> results) {
        if (results.size() >= 2) {
            StringBuilder stats = new StringBuilder();
            stats.append("📊 Краткая статистика:\n\n");
            
            for (TaskTablePanel.SimulationResult result : results) {
                stats.append(result.schedulerName).append(":\n");
                stats.append("  Среднее ожидание: ").append(
                    String.format("%.2f", result.metrics.getAverageWaitingTime())).append("\n");
                stats.append("  Дедлайнов: ").append(
                    result.metrics.getDeadlineMisses()).append("/").append(
                    result.metrics.getCompletedTasks()).append("\n\n");
            }
            
            // Находим лучший алгоритм по времени ожидания
            TaskTablePanel.SimulationResult best = results.stream()
                .min((a, b) -> Double.compare(
                    a.metrics.getAverageWaitingTime(), 
                    b.metrics.getAverageWaitingTime()))
                .orElse(results.get(0));
            
            stats.append("🏆 Лучший алгоритм: ").append(best.schedulerName)
                 .append(" (ожидание: ").append(
                     String.format("%.2f", best.metrics.getAverageWaitingTime())).append(")");
            
            JOptionPane.showMessageDialog(this,
                stats.toString(),
                "Результаты симуляции",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
}