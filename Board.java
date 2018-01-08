import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Board {
    // weighting table
    public static enum Level {

        LONG_5("Long 5", 0, new String[]{"11111", "22222"}, 100000), LIVE_4("Alive 4", 1, new String[]{"011110", "022220"}, 10000),
        GO_4("Go 4", 2, new String[]{"011112|0101110|0110110", "022221|0202220|0220220"}, 500), DEAD_4("Dead 4", 3, new String[]{"211112", "122221"}, -5),
        LIVE_3("Alive 3", 4, new String[]{"01110|010110", "02220|020220"}, 200), SLEEP_3("Sleep 3", 5, new String[]{"001112|010112|011012|10011|10101|2011102",
                "002221|020221|022021|20022|20202|1022201"}, 50), DEAD_3("Dead 3", 6, new String[]{"21112", "12221"}, -5),
        LIVE_2("Alive 2", 7, new String[]{"00110|01010|010010", "00220|02020|020020"}, 5), SLEEP_2("Sleep 2", 8, new String[]{"000112|001012|010012|10001|2010102|2011002",
                "000221|002021|020021|20002|1020201|1022001"}, 3), DEAD_2("Dead 2", 9, new String[]{"2112", "1221"}, -5), NULL("null", 10, new String[]{"", ""}, 0);

        private String name;
        private int label;
        private String[] regex;
        int score;

        private Level(String name, int label, String[] regex, int score) {
            this.name = name;
            this.label = label;
            this.regex = regex;
            this.score = score;
        }

        public String toString() {
            return this.name;
        }

        private static enum Direction {
            VERTICAL, HORIZONTAL, LEFTRIGHT, RIGHTLEFT;
        }

    }

    public static final int BOARD_SIZE = 15; // board size
    public static final int BOARD = BOARD_SIZE + 2;
    public static final int CENTER = BOARD_SIZE / 2 + 1; // center of board
    private int minx, maxx, miny, maxy; // 当前棋局下所有棋子的最小x，最大x，最小y，最大y，用于缩小搜索落子点的范围
    private int currentPlayer = 0; // current player
    private Stack<Point> history; // record board history
    private Stone[][] board;
    private Stone[] sorted; // record weighting

    public Board() {
        board = new Stone[BOARD][BOARD];
        for (int i = 0; i < BOARD; i++)
            for (int j = 0; j < BOARD; j++) {
                board[i][j] = new Stone(i, j);
                if (i == 0 || i == BOARD - 1 || j == 0 || j == BOARD - 1)
                    board[i][j].setSide(Stone.BORDER);
            }
        history = new Stack<Point>();
    }

    public Board(Board b) {
        Stone[][] bBoard = b.getBoard();
        Stone[] bSorted = b.getSorted();
        board = new Stone[BOARD][BOARD];
        for (int i = 0; i < BOARD; i++)
            for (int j = 0; j < BOARD; j++) {
                board[i][j] = new Stone(i, j);
                board[i][j].sum = bBoard[i][j].sum;
                board[i][j].side = bBoard[i][j].side;
            }
        sorted = new Stone[bSorted.length];
        for (int i = 0; i < sorted.length; i++) {
            sorted[i] = new Stone(bSorted[i].x, bSorted[i].y);
            sorted[i].sum = bSorted[i].sum;
            sorted[i].side = bSorted[i].side;
        }
        currentPlayer = b.getPlayer();
        minx = b.minx;
        maxx = b.maxx;
        miny = b.miny;
        maxy = b.maxy;
        history = new Stack<Point>();
    }

    public void start() {
        currentPlayer = Stone.BLACK; // Black first
        moveStone(CENTER, CENTER); // The first move is generally on center
    }

    public void reset() {
        for (int i = 1; i < BOARD - 1; i++)
            for (int j = 1; j < BOARD - 1; j++) {
                board[i][j].reset();
            }
        history.clear();
    }

    // undo
    public Point undo() {
        if (!history.isEmpty()) {
            Point p1 = history.pop();
            Point p2 = history.pop();
            board[p1.x][p1.y].setSide(Stone.EMPTY);
            board[p2.x][p2.y].setSide(Stone.EMPTY);
            return history.peek();
        }
        return null;
    }

    public Stone[][] getBoard() {
        return board;
    }

    public Stone[] getSorted() {
        return sorted;
    }

    public int getPlayer() {
        return currentPlayer;
    }

    public int[][] getHistory() {
        int length = history.size();
        int[][] pos = new int[length][2];
        for (int i = 0; i < length; i++) {
            //for (int j = 0; j < 2; j++){
            Point p = history.get(i);
            pos[i][0] = (int) p.getX();
            pos[i][1] = (int) p.getY();
        }
        return pos;
    }

    public boolean moveStone(int x, int y) {
        if (board[x][y].isEmpty()) {
            minx = Math.min(minx, x);
            maxx = Math.max(maxx, x);
            miny = Math.min(miny, y);
            maxy = Math.max(maxy, y);
            board[x][y].setSide(currentPlayer);
            history.push(new Point(x, y));
            nextPlayer(); // next player to move stone
            sorted = getSortedStone(currentPlayer);
            return true;
        }
        return false;
    }

    public void nextPlayer() {
        currentPlayer = 3 - currentPlayer;
    }

    public int situation(int x, int y, int dx, int dy, int side) {
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            x += dx;
            y += dy;
            if (x < 1 || x > BOARD_SIZE || y < 1 || y > BOARD_SIZE) {
                break;
            }
            if (board[x][y].getSide() == side) {
                sum++;
            } else {
                break;
            }
        }
        return sum;
    }

    public int judge() {
        if (!history.isEmpty()) {
            int side;
            if (history.size() % 2 == 1) {
                side = Stone.BLACK;
            } else {
                side = Stone.WHITE;
            }
            Point lastMove = history.peek();
            int x = (int) lastMove.getX();
            int y = (int) lastMove.getY();
            // if one of the four situation appears, game is over
            if (situation(x, y, 1, 0, side) + situation(x, y, -1, 0, side) >= 4) {
                return side;
            }
            if (situation(x, y, 0, 1, side) + situation(x, y, 0, -1, side) >= 4) {
                return side;
            }
            if (situation(x, y, 1, 1, side) + situation(x, y, -1, -1, side) >= 4) {
                return side;
            }
            if (situation(x, y, 1, -1, side) + situation(x, y, -1, 1, side) >= 4) {
                return side;
            }
        }
        // game will continue:
        for (int i = 0; i < BOARD_SIZE; ++i)
            for (int j = 0; j < BOARD_SIZE; ++j) {
                if (board[i][j].isEmpty())
                    return 0;
            }

        return 3; // No winners
    }

    // When taking player's turn, sorting the weight from high to low
    public Stone[] getSortedStone(int player) {
        int px = Math.max(minx - 5, 1);
        int py = Math.max(miny - 5, 1);
        int qx = Math.min(maxx + 5, Board.BOARD - 1);
        int qy = Math.min(maxy + 5, Board.BOARD - 1);
        Stone[] temp = new Stone[(qx - px + 1) * (qy - py + 1)];
        int count = 0;
        for (int x = px; x <= qx; x++) {
            for (int y = py; y <= qy; y++) {
                temp[count] = new Stone(x, y);
                if (board[x][y].isEmpty()) {
                    board[x][y].clearEval();
                    int offense = getScore(x, y, player) + 1; // score of offense
                    int defense = getScore(x, y, 3 - player); // score of defense
                    board[x][y].offense = temp[count].offense = offense;
                    board[x][y].defense = temp[count].defense = defense;
                    board[x][y].sum = temp[count].sum = offense + defense;
                }
                count++;
            }
        }
        Arrays.sort(temp);
        return temp;
    }

    public int getScore(int x, int y, int side) {
        Level l1 = getLevel(x, y, Level.Direction.HORIZONTAL, side);
        Level l2 = getLevel(x, y, Level.Direction.VERTICAL, side);
        Level l3 = getLevel(x, y, Level.Direction.LEFTRIGHT, side);
        Level l4 = getLevel(x, y, Level.Direction.RIGHTLEFT, side);
        return levelScore(l1, l2, l3, l4);
    }

    // get the current board situation after moving a stone
    public Level getLevel(int x, int y, Level.Direction direction, int side) {
        String regex, left = "", right = "";
        if (direction == Level.Direction.HORIZONTAL) {
            left = getHalfRegex(x, y, -1, 0, side);
            right = getHalfRegex(x, y, 1, 0, side);
        } else if (direction == Level.Direction.VERTICAL) {
            left = getHalfRegex(x, y, 0, -1, side);
            right = getHalfRegex(x, y, 0, 1, side);
        } else if (direction == Level.Direction.RIGHTLEFT) {
            left = getHalfRegex(x, y, -1, -1, side);
            right = getHalfRegex(x, y, 1, 1, side);
        } else if (direction == Level.Direction.LEFTRIGHT) {
            left = getHalfRegex(x, y, -1, 1, side);
            right = getHalfRegex(x, y, 1, -1, side);
        }
        regex = left + side + right;
        String reverseRegex = new StringBuilder(regex).reverse().toString();
        for (Level level : Level.values()) {
            Pattern pattern = Pattern.compile(level.regex[side - 1]);
            Matcher matcher = pattern.matcher(regex);
            boolean regex1 = matcher.find();
            matcher = pattern.matcher(reverseRegex);
            boolean regex2 = matcher.find();
            if (regex1 || regex2)
                return level;
        }
        return Level.NULL;
    }

    public String getHalfRegex(int x, int y, int dx, int dy, int side) {
        String regex = "";
        boolean label = false;
        if (dx < 0 || (dx == 0 && dy == -1))
            label = true;
        for (int i = 0; i < 5; ++i) {
            x += dx;
            y += dy;
            if (x < 1 || x > BOARD_SIZE || y < 1 || y > BOARD_SIZE) {
                break;
            }
            if (label)
                regex = board[x][y].getSide() + regex;
            else
                regex = regex + board[x][y].getSide();
        }
        return regex;
    }

    public int levelScore(Level l1, Level l2, Level l3, Level l4) {
        int size = Level.values().length;
        int[] levelIndex = new int[size];
        for (int i = 0; i < size; i++)
            levelIndex[i] = 0;
        levelIndex[l1.label]++;
        levelIndex[l2.label]++;
        levelIndex[l3.label]++;
        levelIndex[l4.label]++;
        int score = 0;
        if (levelIndex[Level.GO_4.label] >= 2
                || levelIndex[Level.GO_4.label] >= 1
                && levelIndex[Level.LIVE_3.label] >= 1)// 双活4，冲4活三
            score = 10000;
        else if (levelIndex[Level.LIVE_3.label] >= 2)// 双活3
            score = 5000;
        else if (levelIndex[Level.SLEEP_3.label] >= 1
                && levelIndex[Level.LIVE_3.label] >= 1)// 活3眠3
            score = 1000;
        else if (levelIndex[Level.LIVE_2.label] >= 2)// 双活2
            score = 100;
        else if (levelIndex[Level.SLEEP_2.label] >= 1
                && levelIndex[Level.LIVE_2.label] >= 1)// 活2眠2
            score = 10;

        score = Math.max(
                score,
                Math.max(Math.max(l1.score, l2.score),
                        Math.max(l3.score, l4.score)));
        return score;
    }


}
