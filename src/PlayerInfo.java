import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 2L;
    private int playerId;
    private boolean isQuit;
    private Position currentPosition;
    private ClientInterface player;
    private int treasureCollected;
    private List<PlayerInfo> rivals;

    public PlayerInfo(int playerId, int mazeSize, Map<Position, Integer> treasureInfo,
                      ClientInterface clientInterface, Collection<PlayerInfo> rivals) {
        this.playerId = playerId;
        this.isQuit = false;
        Position position =
                new Position(mazeSize);
        while (treasureInfo.containsKey(position) || rivals.stream().map(PlayerInfo::getCurrentPosition).collect(Collectors.toSet()).contains(position)) {
            position = new Position(mazeSize);
        }
        this.currentPosition = position;
        this.player = clientInterface;
        this.rivals = new ArrayList<>(rivals);
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public boolean isQuit() {
        return isQuit;
    }

    public ClientInterface getPlayer() {
        return player;
    }

    public int getPlayerId() {
        return playerId;
    }

    public List<PlayerInfo> getRivals() {
        return rivals;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getTreasureCollected() {
        return treasureCollected;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void setPlayer(ClientInterface player) {
        this.player = player;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setRivals(List<PlayerInfo> rivals) {
        this.rivals = rivals;
    }

    public void setTreasureCollected(int treasureCollected) {
        this.treasureCollected = treasureCollected;
    }

    public void setIsQuit(boolean isQuit) {
        this.isQuit = isQuit;
    }

    public void move(String direction,GameInfo gameInfo) {
        Map<Position, Integer> treasureInfo = gameInfo.getTreasureInfo();
        if (currentPosition.moveNext(direction, gameInfo.getMazeSize(), rivals.stream().filter(rivalInfo->!rivalInfo.isQuit()).map(PlayerInfo::getCurrentPosition).collect(Collectors.toSet()))) {
            if (treasureInfo.containsKey(currentPosition)) {
                if (treasureInfo.containsKey(currentPosition)) {
                    treasureCollected += treasureInfo.get(currentPosition);
                    treasureInfo.remove(currentPosition);
                    if (treasureInfo.size() == 0) {
                        gameInfo.setGameState(GameInfo.gameOver);
                    }
                }
            }
        }
    }
}
