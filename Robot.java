public class Robot {
    private Board bd;
    private int INFINITY = 1000000;
    private int movex, movey;
    private int level;// 深度
    private int node;// 每层结点

    public Robot(Board bd, int level, int node) {
        this.bd = bd;
        this.level = level;
        this.node = node;
    }


    // 估值函数+搜索树
    public int[] findTreeBestStep() {
        alpha_beta(0, bd, -INFINITY, INFINITY);
        int[] result = { movex, movey };
        return result;
    }

    // alpha-beta剪枝搜索算法
    public int alpha_beta(int depth, Board board, int alpha, int beta) {
        if (depth == level || board.judge() != 0) {
            Stone[] sorted = board.getSorted();
            Stone move = board.getBoard()[sorted[0].x][sorted[0].y];
            return move.getSum();// 局面估分
        }
        // 对局面下得分最高的几个点进行拓展
        Board temp = new Board(board);
        Stone[] sorted = temp.getSorted();
        int score;
        for (int i = 0; i < node; i++) {
            int x = sorted[i].x;
            int y = sorted[i].y;

            // 走这个走法
            if (!temp.moveStone(x, y))
                continue;
            if (sorted[i].getOffense() >= Board.Level.LIVE_4.score) {
                //System.out.println("我们快要赢啦！");
                score = INFINITY + 1;
            } else if (sorted[i].getDefense() >= Board.Level.LIVE_4.score) {
                //System.out.println("对方快要赢啦！");
                score = INFINITY;
            } else {
                score = alpha_beta(depth + 1, temp, alpha, beta);
            }
            temp = new Board(board);// 撤消这个走法

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
                    // System.out.println(" beta剪枝");
                    return score;
                }
            } else {// MIN
                if (score < beta) {
                    beta = score;
                }
                if (alpha >= beta) {
                    score = beta;
                    // System.out.println(" alpha剪枝");
                    return score;
                }
            }

        }
        return depth % 2 == 0 ? alpha : beta;
    }

}