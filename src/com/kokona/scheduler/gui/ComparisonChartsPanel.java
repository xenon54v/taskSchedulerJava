package com.kokona.scheduler.gui;

import com.kokona.scheduler.metrics.SchedulerMetrics;
import javax.swing.*;
import java.awt.*;

public class ComparisonChartsPanel extends JPanel {
    private java.util.List<TaskTablePanel.SimulationResult> results;
    
    public ComparisonChartsPanel(java.util.List<TaskTablePanel.SimulationResult> results) {
        this.results = results;
        setLayout(new GridLayout(2, 2, 10, 10));
        
        add(createChart("⏱️ Среднее время ожидания", 
            r -> r.metrics.getAverageWaitingTime()));
        add(createChart("🔄 Среднее время выполнения", 
            r -> r.metrics.getAverageTurnaroundTime()));
        add(createChart("⏰ Пропуски дедлайнов (%)", 
            r -> r.metrics.getDeadlineMissRate()));
        add(createChart("⚡ Эффективность (%)", 
            r -> 100.0 - (100.0 * getTotalIdleTime(r.metrics) / r.metrics.getCurrentTime())));
    }
    
    private double getTotalIdleTime(SchedulerMetrics metrics) {
        return metrics.getCurrentTime() == 0
                ? 0
                : metrics.getTotalIdleTime();
    }
    
    private JPanel createChart(String title, 
                              java.util.function.Function<TaskTablePanel.SimulationResult, 
                              Double> valueExtractor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                int width = getWidth();
                int height = getHeight();
                
                // Фон
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, width, height);
                
                // Заголовок
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString(title, 10, 20);
                
                if (results.isEmpty()) return;
                
                // Данные
                double[] values = new double[results.size()];
                String[] labels = new String[results.size()];
                
                for (int i = 0; i < results.size(); i++) {
                    values[i] = valueExtractor.apply(results.get(i));
                    labels[i] = results.get(i).schedulerName;
                }
                
                // Находим максимум
                double max = 0;
                for (double v : values) if (v > max) max = v;
                if (max == 0) max = 1;
                
                // Рисуем столбцы
                int barWidth = Math.min(40, (width - 100) / values.length);
                int spacing = 20;
                
                for (int i = 0; i < values.length; i++) {
                    int barHeight = (int) ((values[i] / max) * (height - 80));
                    int x = 50 + i * (barWidth + spacing);
                    int y = height - barHeight - 40;
                    
                    // Цвет в зависимости от алгоритма
                    Color color = getAlgorithmColor(labels[i]);
                    g2d.setColor(color);
                    g2d.fillRect(x, y, barWidth, barHeight);
                    
                    // Обводка
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, barWidth, barHeight);
                    
                    // Значение
                    g2d.drawString(String.format("%.1f", values[i]), 
                        x + barWidth/2 - 10, y - 5);
                    
                    // Подпись
                    g2d.drawString(labels[i], x, height - 20);
                }
            }
        };
    }
    
    private Color getAlgorithmColor(String name) {
        if (name.contains("FIFO")) return new Color(70, 130, 180);
        if (name.contains("LIFO")) return new Color(220, 20, 60);
        if (name.contains("SJF")) return new Color(46, 204, 113);
        return Color.GRAY;
    }
}