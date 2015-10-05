import java.io.Serializable;
import java.util.Collection;
import java.util.Random;

public class Position implements Serializable {
    private static final long serialVersionUID = 1L;
    private int x;
    private int y;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public int hashCode() {
        return x * 13 + y * 17;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Position) {
            return ((Position) obj).x == this.x && ((Position) obj).y == this.y;
        } else {
            return false;
        }
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(int mazeSize) {
        Random random = new Random();
        this.x = random.nextInt(mazeSize);
        this.y = random.nextInt(mazeSize);
    }
    public boolean moveNext(String direction,int mazeSize,Collection<Position> rivalPositions){
       boolean result=false;
        switch (direction){
            case "N":
                if (y-1>=0 && !rivalPositions.contains(new Position(x,y-1)) ){
                    y-=1;
                    result=true;
                }
                break;
            case "E":
                if (x-1>=0 && !rivalPositions.contains(new Position(x-1,y))){
                    x-=1;
                    result=true;
                }
                break;
            case "W":
                if (x + 1 < mazeSize && !rivalPositions.contains(new Position(x+1,y))) {
                    x+=1;
                    result=true;
                }
                break;
            case "S":
                if (y+1<mazeSize && !rivalPositions.contains(new Position(x,y+1))){
                    y+=1;
                    result=true;
                }
                break;
        }
        return result;
    }
}
