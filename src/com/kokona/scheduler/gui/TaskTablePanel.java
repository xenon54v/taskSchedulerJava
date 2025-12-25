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

        JTabbedPane tabbedPane = new JTabbedPane();

        for (SimulationResult result : results) {
            tabbedPane.addTab(result.schedulerName, createTableForScheduler(result));
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JScrollPane createTableForScheduler(SimulationResult result) {
        // Новые заголовки таблицы (чтобы видеть "правду" симуляции)
        String[] columns = {
                "Задача",
                "Тип",
                "Прибыл",
                "Длительность",
                "IO",
                "Response",
                "CPU",
                "Начало",
                "Конец",
                "Ожидание",
                "Turnaround",
                "Дедлайн",
                "Статус"
        };

        Object[][] data = new Object[result.executedTasks.size()][columns.length];

        for (int i = 0; i < result.executedTasks.size(); i++) {
            Task task = result.executedTasks.get(i);

            int col = 0;
            data[i][col++] = task.getId();
            data[i][col++] = task.getType(); // CPU_BOUND / IO_BOUND
            data[i][col++] = task.getArrivalTime();
            data[i][col++] = task.getExecutionTime();

            // IO-интервал имеет смысл только для IO_BOUND
            data[i][col++] = (task.getType() == Task.TaskType.IO_BOUND) ? task.getIoInterval() : "—";

            // Response time — когда задача впервые получила CPU
            data[i][col++] = (task.getResponseTime() != -1) ? task.getResponseTime() : "—";

            // Реально отработанное CPU-время (должно быть == executionTime для корректного завершения)
            data[i][col++] = task.getTotalExecutionTime();

            data[i][col++] = task.getStartTime() != -1 ? task.getStartTime() : "—";
            data[i][col++] = task.getFinishTime() != -1 ? task.getFinishTime() : "—";

            data[i][col++] = task.getWaitingTime();
            data[i][col++] = task.getTurnaroundTime();
            data[i][col++] = task.getDeadline();

            // Статус
            if (task.isCompleted()) {
                data[i][col++] = task.isDeadlineMissed() ? "❌ Пропущен" : "✅ Выполнен";
            } else if (task.isStarted()) {
                data[i][col++] = "⏳ Выполняется";
            } else {
                data[i][col++] = "⏱️ Ожидает";
            }
        }

        JTable table = new JTable(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));

        // Ширины столбцов (под 13 колонок)
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // Задача
        table.getColumnModel().getColumn(1).setPreferredWidth(70);   // Тип
        table.getColumnModel().getColumn(2).setPreferredWidth(55);   // Прибыл
        table.getColumnModel().getColumn(3).setPreferredWidth(80);   // Длительность
        table.getColumnModel().getColumn(4).setPreferredWidth(45);   // IO
        table.getColumnModel().getColumn(5).setPreferredWidth(70);   // Response
        table.getColumnModel().getColumn(6).setPreferredWidth(55);   // CPU
        table.getColumnModel().getColumn(7).setPreferredWidth(55);   // Начало
        table.getColumnModel().getColumn(8).setPreferredWidth(55);   // Конец
        table.getColumnModel().getColumn(9).setPreferredWidth(70);   // Ожидание
        table.getColumnModel().getColumn(10).setPreferredWidth(85);  // Turnaround
        table.getColumnModel().getColumn(11).setPreferredWidth(65);  // Дедлайн
        table.getColumnModel().getColumn(12).setPreferredWidth(110); // Статус

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                Task task = result.executedTasks.get(row);

                // Фон по статусу
                if (task.isDeadlineMissed()) {
                    c.setBackground(new Color(255, 230, 230));
                } else if (task.isCompleted()) {
                    c.setBackground(new Color(230, 255, 230));
                } else if (task.isStarted()) {
                    c.setBackground(new Color(230, 240, 255));
                } else {
                    c.setBackground(Color.WHITE);
                }

                // Жирный для ID и статуса
                if (column == 0 || column == 12) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }

                // Центрируем числа (колонки 2..11) + IO/Response/CPU тоже числа
                if (c instanceof JLabel label) {
                    if (column >= 2 && column <= 11) {
                        label.setHorizontalAlignment(SwingConstants.CENTER);
                    } else {
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }

                return c;
            }
        });

        return new JScrollPane(table);
    }
}
