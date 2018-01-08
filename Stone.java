public class Stone implements Comparable<Stone>{
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    public static final int BORDER = -1;
    public static final int EMPTY = 0;
    public int x,y;
    public int sum; // total score;
    public int side; // black or white
    public int offense; // score of offense
    public int defense; // score of defense
    private StringBuilder eval;

    public Stone(int x, int y){
        this.x = x;
        this.y = y;
        eval = new StringBuilder();
    }

    public int getSum(){
        return sum;
    }

    public void setSum(int sum){
        this.sum = sum;
    }

    public int getSide(){
        return side;
    }

    public void setSide(int side){
        this.side = side;
    }

    public String getEval(){
        return eval.toString();
    }

    public StringBuilder append(String details){
        return this.eval.append(details);
    }

    //clear data
    public void reset(){
        clearEval();
        sum = 0;
        side = EMPTY;
    }

    public void clearEval(){
        eval = new StringBuilder();
    }

    public boolean isEmpty(){
        if (side == EMPTY)
            return true;
        else
            return false;
    }

    public int getOffense(){
        return offense;
    }

    public void setOffense(int offense){
        this.offense = offense;
    }

    public int getDefense(){
        return defense;
    }

    public void setDefense(int defense){
        this.defense = defense;
    }

    //override compareTo method
    public int compareTo(Stone p){
        if (p == null)
            return 0;
        int value1 = sum;
        int value2 = p.getSum();
        if (value1 == value2)
            return 0;
        else if(value1 < value2)
            return 1;
        else
            return -1;
    }


}
