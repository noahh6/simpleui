import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class SUI {

    private static JFrame frame;
    private static JPanel panel;
    private static JPanel buttonPanel;
    private static final List<Consumer<Graphics>> drawCommands = new ArrayList<>();
    private static final List<Consumer<Graphics>> persistentCommands = new ArrayList<>();
    private static Timer gameLoop;

    private static Consumer<int[]> onMouseMoved;
    private static Consumer<int[]> onMousePressedLeft;
    private static Consumer<int[]> onMousePressedRight;
    private static Consumer<int[]> onMousePressedMiddle;
    private static Consumer<int[]> onMouseReleased;
    private static Consumer<int[]> onMouseDragged;
    private static Consumer<Character> onKeyTyped;
    private static Consumer<Integer> onKeyPressed;

    public static void init(int width, int height) {
        init(width, height, "Simple UI", true);
    }

    public static void init(int width, int height, boolean resizable) {
        init(width, height, "Simple UI", resizable);
    }

    public static void init(int width, int height, String title) {
        init(width, height, title, true);
    }

    public static void init(int width, int height, String title, boolean resizable) {
        frame = new JFrame(title);
        frame.setLayout(new BorderLayout());
        frame.setResizable(resizable);

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (onKeyTyped != null)
                    onKeyTyped.accept(e.getKeyChar());
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if (onKeyPressed != null)
                    onKeyPressed.accept(e.getKeyCode());
            }
        });
        frame.setFocusable(true);
        frame.requestFocusInWindow();

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (var cmd : persistentCommands) {
                    cmd.accept(g);
                }
                for (var cmd : drawCommands) {
                    cmd.accept(g);
                }
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int[] pos = new int[]{e.getX(), e.getY()};
                if (e.getButton() == MouseEvent.BUTTON1 && onMousePressedLeft != null)
                    onMousePressedLeft.accept(pos);
                if (e.getButton() == MouseEvent.BUTTON2 && onMousePressedMiddle != null)
                    onMousePressedMiddle.accept(pos);
                if (e.getButton() == MouseEvent.BUTTON3 && onMousePressedRight != null)
                    onMousePressedRight.accept(pos);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (onMouseReleased != null)
                    onMouseReleased.accept(new int[]{e.getX(), e.getY()});
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (onMouseMoved != null)
                    onMouseMoved.accept(new int[]{e.getX(), e.getY()});
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (onMouseDragged != null)
                    onMouseDragged.accept(new int[]{e.getX(), e.getY()});
            }
        });

        buttonPanel = new JPanel(new FlowLayout());

        frame.add(panel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Mouse listeners
    public static void onMouseMoved(Consumer<int[]> callback) { onMouseMoved = callback; }
    public static void onMousePressedLeft(Consumer<int[]> callback) { onMousePressedLeft = callback; }
    public static void onMousePressedRight(Consumer<int[]> callback) { onMousePressedRight = callback; }
    public static void onMousePressedMiddle(Consumer<int[]> callback) { onMousePressedMiddle = callback; }
    public static void onMouseReleased(Consumer<int[]> callback) { onMouseReleased = callback; }
    public static void onMouseDragged(Consumer<int[]> callback) { onMouseDragged = callback; }

    // Key listeners
    public static void onKeyTyped(Consumer<Character> callback) { onKeyTyped = callback; }
    public static void onKeyPressed(Consumer<Integer> callback) { onKeyPressed = callback; }

    // Panels
    public static JPanel addPanel(String position, int size) {
        JPanel p = new JPanel();
        return getT(position, size, p);
    }

    public static JPanel addPanel(String position, int size, boolean divider) {
        JPanel p = addPanel(position, size);
        if (divider) {
            switch (position.toLowerCase()) {
                case "north" -> drawLinePersistent(0, size, frame.getWidth(), size);
                case "south" -> drawLinePersistent(0, frame.getHeight() - size, frame.getWidth(), frame.getHeight() - size);
                case "east"  -> drawLinePersistent(frame.getWidth() - size, 0, frame.getWidth() - size, frame.getHeight());
                case "west"  -> drawLinePersistent(size, 0, size, frame.getHeight());
            }
        }
        return p;
    }

    public static <T extends JPanel> T addPanel(String position, int size, T panel) {
        return getT(position, size, panel);
    }

    private static <T extends JPanel> T getT(String position, int size, T panel) {
        switch (position.toLowerCase()) {
            case "north" -> { panel.setPreferredSize(new Dimension(0, size)); frame.add(panel, BorderLayout.NORTH); }
            case "south" -> { panel.setPreferredSize(new Dimension(0, size)); frame.add(panel, BorderLayout.SOUTH); }
            case "east"  -> { panel.setPreferredSize(new Dimension(size, 0)); frame.add(panel, BorderLayout.EAST); }
            case "west"  -> { panel.setPreferredSize(new Dimension(size, 0)); frame.add(panel, BorderLayout.WEST); }
        }
        frame.revalidate();
        return panel;
    }

    public static void refresh(JPanel p) {
        p.revalidate();
        p.repaint();
    }

    // Buttons
    public static void addButton(String label, Runnable onClick) {
        addButton(label, onClick, buttonPanel);
    }

    public static void addButton(String label, Runnable onClick, JPanel targetPanel) {
        JButton button = new JButton(label);
        button.addActionListener(e -> onClick.run());
        targetPanel.add(button);
        targetPanel.revalidate();
    }

    // Drawing
    public static void drawOval(int x, int y, int w, int h) {
        drawCommands.add(g -> g.drawOval(x, y, w, h));
        panel.repaint();
    }

    public static void fillOval(int x, int y, int w, int h) {
        drawCommands.add(g -> g.fillOval(x, y, w, h));
        panel.repaint();
    }

    public static void drawRect(int x, int y, int w, int h) {
        drawCommands.add(g -> g.drawRect(x, y, w, h));
        panel.repaint();
    }

    public static void fillRect(int x, int y, int w, int h) {
        drawCommands.add(g -> g.fillRect(x, y, w, h));
        panel.repaint();
    }

    public static void drawRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        drawCommands.add(g -> g.drawRoundRect(x, y, w, h, arcW, arcH));
        panel.repaint();
    }

    public static void fillRoundRect(int x, int y, int w, int h, int arcW, int arcH) {
        drawCommands.add(g -> g.fillRoundRect(x, y, w, h, arcW, arcH));
        panel.repaint();
    }

    public static void drawLine(int x1, int y1, int x2, int y2) {
        drawCommands.add(g -> g.drawLine(x1, y1, x2, y2));
        panel.repaint();
    }

    private static void drawLinePersistent(int x1, int y1, int x2, int y2) {
        persistentCommands.add(g -> g.drawLine(x1, y1, x2, y2));
        panel.repaint();
    }

    public static void drawPolygon(int[] xPoints, int[] yPoints) {
        drawCommands.add(g -> g.drawPolygon(xPoints, yPoints, xPoints.length));
        panel.repaint();
    }

    public static void fillPolygon(int[] xPoints, int[] yPoints) {
        drawCommands.add(g -> g.fillPolygon(xPoints, yPoints, xPoints.length));
        panel.repaint();
    }

    public static void drawString(String text, int x, int y) {
        drawCommands.add(g -> g.drawString(text, x, y));
        panel.repaint();
    }

    public static void drawImage(String path, int x, int y, int w, int h) {
        Image img = new ImageIcon(path).getImage();
        drawCommands.add(g -> g.drawImage(img, x, y, w, h, null));
        panel.repaint();
    }

    // Styling
    public static void setColor(Color color) {
        drawCommands.add(g -> g.setColor(color));
        panel.repaint();
    }

    public static void setBackground(Color color) {
        panel.setBackground(color);
        panel.repaint();
    }

    /**
     * style: Font.PLAIN (0), Font.BOLD (1), Font.ITALIC (2), Font.BOLD + Font.ITALIC (3)
     */

    public static void setFont(String name, int style, int size) {
        drawCommands.add(g -> g.setFont(new Font(name, style, size)));
        panel.repaint();
    }

    public static void setStroke(int thickness) {
        drawCommands.add(g -> ((Graphics2D) g).setStroke(new BasicStroke(thickness)));
        panel.repaint();
    }

    public static void setTitle(String title) {
        frame.setTitle(title);
    }

    // Utility
    public static int getWidth() { return panel.getWidth(); }
    public static int getHeight() { return panel.getHeight(); }

    public static int[] toGrid(int x, int y, int cols, int rows) {
        int cellWidth = panel.getWidth() / cols;
        int cellHeight = panel.getHeight() / rows;
        return new int[]{x / cellWidth, y / cellHeight};
    }

    public static void loop(int fps, Runnable callback) {
        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(1000 / fps, e -> callback.run());
        gameLoop.start();
    }

    public static void stopLoop() {
        if (gameLoop != null) gameLoop.stop();
    }

    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void clear() {
        drawCommands.clear();
        panel.repaint();
    }

}