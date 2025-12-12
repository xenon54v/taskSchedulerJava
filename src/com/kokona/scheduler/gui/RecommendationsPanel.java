package com.kokona.scheduler.gui;

import javax.swing.*;
import java.awt.*;

public class RecommendationsPanel extends JPanel {
    private java.util.List<TaskTablePanel.SimulationResult> results;
    
    public RecommendationsPanel(java.util.List<TaskTablePanel.SimulationResult> results) {
        this.results = results;
        setLayout(new BorderLayout());
        
        JTextArea recommendations = new JTextArea();
        recommendations.setEditable(false);
        recommendations.setFont(new Font("Arial", Font.PLAIN, 14));
        recommendations.setLineWrap(true);
        recommendations.setWrapStyleWord(true);
        
        recommendations.setText(generateRecommendations());
        
        add(new JScrollPane(recommendations), BorderLayout.CENTER);
    }
    
    private String generateRecommendations() {
        if (results.size() < 2) return "Недостаточно данных для анализа";
        
        StringBuilder sb = new StringBuilder();
        sb.append("🎯 РЕКОМЕНДАЦИИ ПО РЕЗУЛЬТАТАМ СИМУЛЯЦИИ\n");
        sb.append("=".repeat(50)).append("\n\n");
        
        // Используем массивы для обхода проблемы с final в лямбде
        TaskTablePanel.SimulationResult bestWaiting = results.get(0);
        TaskTablePanel.SimulationResult bestTurnaround = results.get(0);
        TaskTablePanel.SimulationResult bestDeadlines = results.get(0);
        
        double minWaiting = Double.MAX_VALUE;
        double minTurnaround = Double.MAX_VALUE;
        double minDeadlines = Double.MAX_VALUE;
        
        // Старый добрый цикл for вместо stream
        for (TaskTablePanel.SimulationResult result : results) {
            double waiting = result.metrics.getAverageWaitingTime();
            double turnaround = result.metrics.getAverageTurnaroundTime();
            double deadlines = result.metrics.getDeadlineMissRate();
            
            if (waiting < minWaiting) {
                minWaiting = waiting;
                bestWaiting = result;
            }
            if (turnaround < minTurnaround) {
                minTurnaround = turnaround;
                bestTurnaround = result;
            }
            if (deadlines < minDeadlines) {
                minDeadlines = deadlines;
                bestDeadlines = result;
            }
        }
        
        // Анализ
        sb.append("📊 АНАЛИЗ ПРОИЗВОДИТЕЛЬНОСТИ:\n");
        sb.append("- Лучший по времени ожидания: ").append(bestWaiting.schedulerName)
          .append(" (").append(String.format("%.2f", minWaiting)).append(")\n");
        sb.append("- Лучший по общему времени: ").append(bestTurnaround.schedulerName)
          .append(" (").append(String.format("%.2f", minTurnaround)).append(")\n");
        sb.append("- Лучший по дедлайнам: ").append(bestDeadlines.schedulerName)
          .append(" (").append(String.format("%.1f", minDeadlines)).append("% пропущено)\n\n");
        
        sb.append("💡 РЕКОМЕНДАЦИИ:\n");
        
        // Логика рекомендаций
        if (bestWaiting.schedulerName.contains("SJF")) {
            sb.append("✅ SJF показал лучшие результаты по времени ожидания.\n");
            sb.append("   Используйте его для систем, где важна отзывчивость.\n");
        }
        
        if (bestDeadlines.schedulerName.contains("FIFO")) {
            sb.append("✅ FIFO лучше справляется с дедлайнами.\n");
            sb.append("   Используйте для систем реального времени.\n");
        }
        
        // Проверяем LIFO
        boolean lifoIsWorst = false;
        for (TaskTablePanel.SimulationResult result : results) {
            if (result.schedulerName.contains("LIFO")) {
                double avgWaiting = result.metrics.getAverageWaitingTime();
                if (avgWaiting > minWaiting * 1.5) {
                    lifoIsWorst = true;
                    break;
                }
            }
        }
        
        if (lifoIsWorst) {
            sb.append("⚠️  LIFO показал наихудшие результаты.\n");
            sb.append("   Не рекомендуется для production систем.\n");
        }
        
        sb.append("\n🎭 ВЫВОД:\n");
        if (results.size() == 3) {
            sb.append("Для данной конфигурации задач оптимальным является ");
            if (minWaiting < minTurnaround * 0.9) {
                sb.append(bestWaiting.schedulerName).append(" (лучшая отзывчивость)");
            } else {
                sb.append(bestTurnaround.schedulerName).append(" (лучшая пропускная способность)");
            }
        }
        
        return sb.toString();
    }
}