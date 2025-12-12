package com.kokona.scheduler.gui;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.metrics.SchedulerMetrics;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ResultsWindow extends JFrame {
    private List<Task> tasks;
    private List<SchedulerMetrics> results;
    
    public ResultsWindow(List<Task> tasks, List<SchedulerMetrics> results) {
        this.tasks = tasks;
        this.results = results;
        
        setTitle("Результаты симуляции");
        setSize(1200, 800);
        setLayout(new BorderLayout());
        
        initComponents();
        setVisible(true);
    }
    
    private void initComponents() {
        // 1. Вкладки
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Вкладка 1: Диаграмма Ганта
        tabbedPane.addTab("📊 Диаграмма Ганта", createGanttPanel());
        
        // Вкладка 2: Таблица результатов
        tabbedPane.addTab("📋 Статистика", createStatsPanel());
        
        // Вкладка 3: Графики сравнения
        tabbedPane.addTab("📈 Графики", createChartsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 2. Кнопка закрытия
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createGanttPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Для каждого планировщика создаем свою диаграмму
        JPanel chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        
        for (SchedulerMetrics metrics : results) {
            chartsPanel.add(new JLabel(metrics.getSchedulerName() + ":", SwingConstants.LEFT));
            chartsPanel.add(createGanttChartForScheduler(metrics));
            chartsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }
        
        JScrollPane scrollPane = new JScrollPane(chartsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createGanttChartForScheduler(SchedulerMetrics metrics) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                int width = getWidth();
                int height = 100;
                setPreferredSize(new Dimension(width, height));
                
                // Находим максимальное время
                int maxTime = metrics.getCurrentTime();
                if (maxTime == 0) maxTime = 100;
                
                // Рисуем шкалу времени
                g2d.setColor(Color.BLACK);
                g2d.drawLine(50, 30, width - 50, 30);
                
                for (int i = 0; i <= maxTime; i += 10) {
                    int x = 50 + (i * (width - 100) / maxTime);
                    g2d.drawLine(x, 25, x, 35);
                    g2d.drawString(String.valueOf(i), x - 5, 45);
                }
                
                // Рисуем задачи (упрощённо)
                // В реальности нужно получить задачи из метрик
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(100, 50, 200, 20);
                g2d.drawString("Task1", 105, 65);
                
                g2d.setColor(new Color(220, 20, 60));
                g2d.fillRect(350, 50, 150, 20);
                g2d.drawString("Task2", 355, 65);
            }
        };
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Таблица результатов
        String[] columns = {"Планировщик", "Задач", "Общ. время", "Ср. ожидание", 
                           "Ср. выполнение", "Простой", "Дедлайнов"};
        
        Object[][] data = new Object[results.size()][columns.length];
        
        for (int i = 0; i < results.size(); i++) {
            SchedulerMetrics m = results.get(i);
            data[i][0] = m.getSchedulerName();
            data[i][1] = m.getCompletedTasks();
            data[i][2] = m.getCurrentTime();
            data[i][3] = String.format("%.2f", m.getAverageWaitingTime());
            data[i][4] = String.format("%.2f", m.getAverageTurnaroundTime());
            data[i][5] = String.format("%.1f%%", 
                (m.getCurrentTime() > 0 ? (100.0 - (m.getCompletedTasks() * 100.0 / m.getCurrentTime())) : 0));
            data[i][6] = m.getDeadlineMisses() + "/" + m.getCompletedTasks();
        }
        
        JTable table = new JTable(data, columns);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        
        // График 1: Среднее время ожидания
        panel.add(createBarChart("Среднее время ожидания", 
            results.stream().mapToDouble(m -> m.getAverageWaitingTime()).toArray(),
            results.stream().map(m -> m.getSchedulerName()).toArray(String[]::new),
            new Color(70, 130, 180)));
        
        // График 2: Среднее время выполнения
        panel.add(createBarChart("Среднее время выполнения",
            results.stream().mapToDouble(m -> m.getAverageTurnaroundTime()).toArray(),
            results.stream().map(m -> m.getSchedulerName()).toArray(String[]::new),
            new Color(46, 204, 113)));
        
        return panel;
    }
    
    private JPanel createBarChart(String title, double[] values, String[] labels, Color color) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                int width = getWidth();
                int height = getHeight();
                
                // Заголовок
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString(title, 10, 20);
                
                // Находим максимум
                double max = 0;
                for (double v : values) if (v > max) max = v;
                if (max == 0) max = 1;
                
                // Рисуем столбцы
                int barWidth = width / (values.length * 2);
                
                for (int i = 0; i < values.length; i++) {
                    int barHeight = (int) ((values[i] / max) * (height - 100));
                    int x = i * barWidth * 2 + 50;
                    int y = height - barHeight - 50;
                    
                    g2d.setColor(color);
                    g2d.fillRect(x, y, barWidth, barHeight);
                    
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, barWidth, barHeight);
                    
                    // Подпись
                    g2d.drawString(labels[i], x, height - 25);
                    g2d.drawString(String.format("%.2f", values[i]), x, y - 5);
                }
            }
        };
    }
}