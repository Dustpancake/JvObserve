package Observe;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JFrame;

public class Main extends JFrame implements KeyListener {

    private final BlockingQueue<String> queue;

    public Main() {
        queue = new LinkedBlockingQueue<>();
        initUI();
    }

    private void initUI() {

        add(new Viewer(queue));
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(true);

        setResizable(false);
        pack();

        setTitle("Observe");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void printHelp() {
        System.out.println("invalid commands! exiting...");
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(0);
        }
        String command = args[0];

        if (command.equals("view")) {
            EventQueue.invokeLater(() -> {
                JFrame ex = new Main();
                ex.setVisible(true);
            });
        } else {

            CommandLine cl = new CommandLine();
            switch (command) {

                case "single":
                    System.out.println("Starting single picture...");
                    cl.singleImage();
                    break;

                case "expose":
                    if (args.length > 1) {
                        int time = 0;
                        try {
                            time = Integer.parseInt(args[1]);
                        } catch (Exception e) {
                            System.out.println("Could not convert time parameter to integer.");
                            System.exit(1);
                        }
                        if (time > 0) {
                            System.out.println("Starting long exposure...");
                            cl.longExposure(time, cl.ACT_AVERAGE);
                        }
                    }
                    break;

                default:
                    printHelp();
                    break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        switch (c) {
            case 'c':
                queue.add("capture");
                System.out.println("captured!");
                break;
            case 'q':
                System.exit(0);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}