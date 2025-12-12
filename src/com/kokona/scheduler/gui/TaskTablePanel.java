package com.kokona.scheduler.gui;

import com.kokona.scheduler.model.Task;
import com.kokona.scheduler.metrics.SchedulerMetrics;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TaskTablePanel extends JPanel {
    
    // Вспомогательный класс для хранения результатов
    public static class SimulationResult {
        public String schedulerName;
        public SchedulerMetrics metrics;
        public java.util.List<Task> executedTasks;
        
        public SimulationResult(String name, SchedulerMetrics metrics, java.util.List<Task> tasks) {
            this.schedulerName = name;
            this.metrics = metrics;
            this.executedTasks = tasks;
        }
    }
    
    public TaskTablePanel(java.util.List<SimulationResult> results) {
        setLayout(new BorderLayout());
        
        if (results.isEmpty()) {
            add(new JLabel("Нет данных для отображения", SwingConstants.CENTER));
            return;
        }
        
        // Создаём вкладки для каждого планировщика
        JTabbedPane tabbedPane = new JTabbedPane();
        
        for (SimulationResult result : results) {
            tabbedPane.addTab(result.schedulerName, createTableForScheduler(result));
        }
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JScrollPane createTableForScheduler(SimulationResult result) {
        // Заголовки таблицы
        String[] columns = {
            "Задача", 
            "Прибыл", 
            "Длительность", 
            "Начало", 
            "Конец", 
            "Ожидание", 
            "Выполнение",
            "Дедлайн",
            "Статус"
        };
        
        // Создаём данные для таблицы
        Object[][] data = new Object[result.executedTasks.size()][columns.length];
        
        for (int i = 0; i < result.executedTasks.size(); i++) {
            Task task = result.executedTasks.get(i);
            data[i][0] = task.getId();
            data[i][1] = task.getArrivalTime();
            data[i][2] = task.getExecutionTime();
            data[i][3] = task.getStartTime() != -1 ? task.getStartTime() : "—";
            data[i][4] = task.getFinishTime() != -1 ? task.getFinishTime() : "—";
            data[i][5] = task.getWaitingTime();
            data[i][6] = task.getTurnaroundTime();
            data[i][7] = task.getDeadline();
            
            // Статус с иконкой
            if (task.isCompleted()) {
                if (task.isDeadlineMissed()) {
                    data[i][8] = "❌ Пропущен";
                } else {
                    data[i][8] = "✅ Выполнен";
                }
            } else if (task.isStarted()) {
                data[i][8] = "⏳ Выполняется";
            } else {
                data[i][8] = "⏱️ Ожидает";
            }
        }
        
        // Создаём таблицу
        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Нельзя редактировать
            }
        };
        
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Настраиваем ширину столбцов
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // Задача
        table.getColumnModel().getColumn(1).setPreferredWidth(60);  // Прибыл
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Длительность
        table.getColumnModel().getColumn(3).setPreferredWidth(60);  // Начало
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // Конец
        table.getColumnModel().getColumn(5).setPreferredWidth(70);  // Ожидание
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Выполнение
        table.getColumnModel().getColumn(7).setPreferredWidth(70);  // Дедлайн
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Статус
        
        // Раскрашиваем строки
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
                
                Task task = result.executedTasks.get(row);
                
                // Цвет фона в зависимости от статуса
                if (task.isDeadlineMissed()) {
                    c.setBackground(new Color(255, 230, 230)); // светло-красный
                } else if (task.isCompleted()) {
                    c.setBackground(new Color(230, 255, 230)); // светло-зелёный
                } else if (task.isStarted()) {
                    c.setBackground(new Color(230, 240, 255)); // светло-синий
                } else {
                    c.setBackground(Color.WHITE);
                }
                
                // Жирный шрифт для важных колонок
                if (column == 0 || column == 8) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                
                // Центрируем числа
                if (column >= 1 && column <= 7) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                
                return c;
            }
        });
        
        return new JScrollPane(table);
    }
}