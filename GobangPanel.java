import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class GobangPanel extends JPanel {

    private final int OFFSET = 45;
    private final int CELL_WIDTH = 40;
    private int computerSide = Stone.BLACK;
    private final int RATE = 650 / 15;
    private final int X_OFFSET = 23;
    private final int Y_OFFSET = 28;
    private int humanSide = Stone.WHITE;
    private int cx = Board.CENTER, cy = Board.CENTER;
    private boolean isShowOrder = false;
    private int[] lastStep;
    private Board bd;
    private Robot br;
    public static final int MANUAL = 0;
    public static final int HALF = 1;
    public static final int TREE = 2;
    private int mode;
    private int intel;
    private boolean isGameOver = true;
    private BufferedImage table;
    private BufferedImage blackstone;
    private BufferedImage whitestone;

    public void undo() {
        Point p = bd.undo();
        lastStep[0] = p.x;
        lastStep[1] = p.y;
        repaint();
    }

    public GobangPanel() throws Exception{
        table = ImageIO.read(new File("images/table.jpg"));
        blackstone = ImageIO.read(new File("images/black.png"));
        whitestone = ImageIO.read(new File("images/white.png"));
        lastStep = new int[2];
        addMouseMotionListener(mouseMotionListener);
        addMouseListener(mouseListener);
        this.setBackground(Color.ORANGE);
        setPreferredSize(new Dimension(650, 700));
        bd = new Board();
    }

    public void paintComponent(Graphics g){

        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        g2d.setStroke(new BasicStroke(2));
        g2d.setFont(new Font("April", Font.BOLD, 12));
        // draw board
        ((Graphics2D) g).drawImage(table,0,0,650,661,null);     
        // draw selected cell
        drawCell(g2d, cx, cy, 0);

        if (!isGameOver) {
            // draw stones
            for (int x = 1; x <= Board.BOARD_SIZE; ++x) {
                for (int y = 1; y <= Board.BOARD_SIZE; ++y) {
                    drawChess(g2d, x, y, bd.getBoard()[x][y].getSide());
                }
            }
            // draw order of stone
            if (isShowOrder)
                drawOrder(g2d);
            else {
                if (lastStep[0] > 0 && lastStep[1] > 0) {
                    g2d.setColor(Color.RED);
                    g2d.fillRect((lastStep[0] - 1) * CELL_WIDTH + OFFSET
                                    - CELL_WIDTH / 10, (lastStep[1] - 1) * CELL_WIDTH
                                    + OFFSET - CELL_WIDTH / 10, CELL_WIDTH / 5,
                            CELL_WIDTH / 5);

                }
            }
        }
    }

    // draw stone
    private void drawChess(Graphics g2d, int cx, int cy, int player) {
        if (player == 0)
            return;
        int size = CELL_WIDTH * 5 / 6;
        g2d.setColor(player == Stone.BLACK ? Color.BLACK : Color.WHITE);
        g2d.fillOval((cx - 1) * RATE + X_OFFSET - size / 2, (cy - 1)
                * RATE - size / 2 + Y_OFFSET, size, size);
    }

    // draw selected cell
    private void drawCell(Graphics g2d, int x, int y, int c) {// c 是style
        int length = CELL_WIDTH / 4;
        int xx = (x - 1) * RATE + X_OFFSET;
        int yy = (y - 1) * RATE + Y_OFFSET;
        int x1, y1, x2, y2, x3, y3, x4, y4;
        x1 = x4 = xx - CELL_WIDTH / 2;
        x2 = x3 = xx + CELL_WIDTH / 2;
        y1 = y2 = yy - CELL_WIDTH / 2;
        y3 = y4 = yy + CELL_WIDTH / 2;
        g2d.setColor(Color.RED);
        g2d.drawLine(x1, y1, x1 + length, y1);
        g2d.drawLine(x1, y1, x1, y1 + length);
        g2d.drawLine(x2, y2, x2 - length, y2);
        g2d.drawLine(x2, y2, x2, y2 + length);
        g2d.drawLine(x3, y3, x3 - length, y3);
        g2d.drawLine(x3, y3, x3, y3 - length);
        g2d.drawLine(x4, y4, x4 + length, y4);
        g2d.drawLine(x4, y4, x4, y4 - length);
    }

    // draw order
    private void drawOrder(Graphics g2d) {
        int[][] history = bd.getHistory();
        if (history.length > 0) {
            g2d.setColor(Color.RED);
            for (int i = 0; i < history.length; i++) {
                int x = history[i][0];
                int y = history[i][1];
                String text = String.valueOf(i + 1);                
                FontMetrics fm = g2d.getFontMetrics();
                int stringWidth = fm.stringWidth(text);
                int stringAscent = fm.getAscent();
                g2d.drawString(text, (x - 1) * CELL_WIDTH + OFFSET
                        - stringWidth / 2, (y - 1) * CELL_WIDTH + OFFSET
                        + stringAscent / 2);
            }
        }
    }

    // start game
    public void startGame(int mode, int intel, int level, int node) {
        //if (isGameOver) {
            this.mode = mode;
            this.intel = intel;
            bd.reset();
            //area.setText("");
            lastStep[0] = lastStep[1] = Board.CENTER;
            br = new Robot(bd, level, node);
            bd.start();
            isGameOver = false;
            JOptionPane.showMessageDialog(GobangPanel.this, "Game Starts！");
            repaint();


    }

   
    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter() {
        public void mouseMoved(MouseEvent e) {
            int tx = Math.round((e.getX() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            int ty = Math.round((e.getY() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            if (tx != cx || ty != cy) {
                if (tx >= 1 && tx <= Board.BOARD_SIZE && ty >= 1
                        && ty <= Board.BOARD_SIZE) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                    repaint();
                } else
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                cx = tx;
                cy = ty;
            }
        }
    };

 
    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (isGameOver) {
                JOptionPane.showMessageDialog(GobangPanel.this, "Please start a new game！");
                return;
            }
            int x = Math.round((e.getX() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            int y = Math.round((e.getY() - OFFSET) * 1.0f / CELL_WIDTH) + 1;
            if (cx >= 1 && cx <= Board.BOARD_SIZE && cy >= 1
                    && cy <= Board.BOARD_SIZE) {
                if (mode == MANUAL) {// human vs human
                    int mods = e.getModifiers();
                    if ((mods & InputEvent.BUTTON1_MASK) != 0)
                        putChess(x, y);
                } else if (mode == HALF) {// human vs computer
                    if (bd.getPlayer() == humanSide) {
                        int mods = e.getModifiers();
                        if ((mods & InputEvent.BUTTON1_MASK) != 0) {
                            if (putChess(x, y)) {
                                if (intel == TREE) {
                                    int[] bestStep = br.findTreeBestStep();
                                    putChess(bestStep[0], bestStep[1]);
                                }

                            }
                        }
                    }
                }
            }
        }
    };

    private boolean putChess(int x, int y) {
        if (bd.moveStone(x, y)) {
            lastStep[0] = x;
            lastStep[1] = y;
            repaint();
            int winSide = bd.judge();
            if (winSide > 0) {
                if (winSide == humanSide) {
                    JOptionPane.showMessageDialog(GobangPanel.this, "White wins！");
                } else if (winSide == computerSide) {
                    JOptionPane.showMessageDialog(GobangPanel.this, "Black wins！");
                } else {
                    JOptionPane.showMessageDialog(GobangPanel.this, "No winners!");
                }
                bd.reset();              
                isGameOver = true;
                repaint();
                return false;
            }

            return true;
        }
        return false;

    }

}
