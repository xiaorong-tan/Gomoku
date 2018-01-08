public class Robot {
    private Board bd;
    private int INFINITY = 1000000;
    private int movex, movey;
    private int level;// depth
    private int node;

    public Robot(Board bd, int level, int node) {
        this.bd = bd;
        this.level = level;
        this.node = node;
    }


    
    public int[] findBestStep() {
        alpha_beta(0, bd, -INFINITY, INFINITY);
        int[] result = { movex, movey };
        return result;
    }

    // alpha-beta algorithm
    public int alpha_beta(int depth, Board board, int alpha, int beta) {
        if (depth == level || board.judge() != 0) {
            Stone[] sorted = board.getSorted();
            Stone move = board.getBoard()[sorted[0].x][sorted[0].y];
            return move.getSum();
        }
        // put a stone based on the high score
        Board temp = new Board(board);
        Stone[] sorted = temp.getSorted();
        int score;
        for (int i = 0; i < node; i++) {
            int x = sorted[i].x;
            int y = sorted[i].y;

            // put a stone on x, y
            if (!temp.moveStone(x, y))
                continue;
            if (sorted[i].getOffense() >= Board.Level.LIVE_4.score) {
                
                score = INFINITY + 1;
            } else if (sorted[i].getDefense() >= Board.Level.LIVE_4.score) {
                
                score = INFINITY;
            } else {
                score = alpha_beta(depth + 1, temp, alpha, beta);
            }
            temp = new Board(board);// undo current move

            if (depth % 2 == 0) {// MAX
                if (score > alpha) {
                    alpha = score;
                    if (depth == 0) {
                        movex = x;
                        movey = y;
                    }
                }
                if (alpha >= beta) {
                    score = alpha;                    
                    return score;
                }
            } else {// MIN
                if (score < beta) {
                    beta = score;
                }
                if (alpha >= beta) {
                    score = beta;                    
                    return score;
                }
            }

        }
        return depth % 2 == 0 ? alpha : beta;
    }

}
