import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class GameInfo implements Serializable {
    private static final long serialVersionUID = 3L;
    public static final String gameOver = "OVER";
    public static final String gameStart = "START";
    public static final String gameWaiting = "WAITING";
    private String gameState;
    private int globalId;
    private int mazeSize;
    private int treasureNumber;
    private ServerInterface server;
    private String serverID;
    private String backID;
    private LinkedList<ClientInterface> backupServerQueue = new LinkedList<>();
    private Map<Integer, PlayerInfo> playerInfos = new HashMap<>();
    private Map<Position, Integer> treasureInfo = new HashMap<>();

    public GameInfo(int mazeSize, int treasureNumber) {
        this.mazeSize = mazeSize;
        this.treasureNumber = treasureNumber;
        this.globalId = 1;
    }

    public void reset() {
        gameState = gameOver;
        globalId = 1;
        backupServerQueue.clear();
        playerInfos.clear();
        treasureInfo.clear();
        generateTreasures();
    }

    public String getGameState() {
        return gameState;
    }

    public synchronized int getGlobalId() {
        int currentId = globalId;
        globalId++;
        return currentId;
    }

    public void generateTreasures() {
        for (int i = 0; i < treasureNumber; i++) {
            Position position = new Position(mazeSize);
            if (treasureInfo.containsKey(position)) {
                treasureInfo.put(position, treasureInfo.get(position) + 1);
            } else {
                treasureInfo.put(position, 1);
            }
        }

    }

    public int getMazeSize() {
        return mazeSize;
    }

    public int getTreasureNumber() {
        return treasureNumber;
    }

    public LinkedList<ClientInterface> getBackupServerQueue() {
        return backupServerQueue;
    }

    public Map<Integer, PlayerInfo> getPlayerInfos() {
        return playerInfos;
    }

    public Map<Position, Integer> getTreasureInfo() {
        return treasureInfo;
    }

    public synchronized void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String backupServerQueueToString() {
        return backupServerQueue.stream().
                map(player -> {
                    try {
                        return player
                                .getPlayerIDAsString();
                    } catch (RemoteException e) {
                        return " ";
                    }
                }).
                collect(Collectors.joining("->"));
    }

    @Override
    public String toString() {
        return "gameState: " + gameState + "\n" +
                "globalId: " + globalId + "\n" +
                "mazeSize: " + mazeSize + "\n" +
                "treasureNumber: " + treasureNumber + "\n" +
                "backupServer Queue: " + backupServerQueueToString() + "\n" +
                "current treasure information:" +
                treasureInfo.entrySet().parallelStream().
                        map(entry -> entry.getKey() + "[" + entry.getValue() + "]").
                        collect(Collectors.joining(",")) + "\n" +
                "players information:" +
                playerInfos.entrySet().parallelStream().map(info -> {
                    try {
                        return info.getValue().getPlayer().getPlayerIDAsString() + ": position->" + info.getValue().getCurrentPosition()
                                + " #treasure collected->" + info.getValue().getTreasureCollected();
                    } catch (RemoteException e) {
                        return "player-" + info.getKey() + "[crashed]: position->" + info.getValue().getCurrentPosition()
                                + " #treasure collected->" + info.getValue().getTreasureCollected();
                    }
                }).collect(Collectors.joining("\n")) + "";

    }

    public ServerInterface getServer() {
        return server;
    }

    public void setServer(ServerInterface server) {
        this.server = server;
    }

    public ClientInterface getCurrentBackupServer() {
        return backupServerQueue.getLast();
    }


    public ClientInterface tryToGetNextBackupServer() {
        backupServerQueue.removeLast();
        return backupServerQueue.getLast();
    }

    public int returnIdByRemoteObject(ClientInterface clientInterface) {
        return playerInfos.values().parallelStream().
                filter(info -> info.getPlayer() == clientInterface).
                map(PlayerInfo::getPlayerId).collect(Collectors.toList()).
                get(0);
    }

    public String backupID() {
        return backID;
    }

    public String serverID() {
        return serverID;
    }

    public void setBackID(String backID) {
        this.backID = backID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public int queueSize() {
        return backupServerQueue.size();
    }
}
