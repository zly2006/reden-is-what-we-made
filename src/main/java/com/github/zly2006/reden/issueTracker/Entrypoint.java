package com.github.zly2006.reden.issueTracker;

import javax.swing.*;
import java.awt.*;

public class Entrypoint {
    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Failed to start: headless environment detected.");
            return;
        }
        JFrame frame = new JFrame("Reden crashed");
        frame.setVisible(true);
    }
}
