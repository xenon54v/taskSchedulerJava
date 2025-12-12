package com.kokona.scheduler.gui;

import javax.swing.*;

import com.kokona.scheduler.gui.MainWindow;

public class SchedulerApp {
    public static void main(String[] args) {
        // Устанавливаем красивый стиль
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Запускаем в потоке событий Swing
        SwingUtilities.invokeLater(() -> {
            new MainWindow();
        });
    }
}