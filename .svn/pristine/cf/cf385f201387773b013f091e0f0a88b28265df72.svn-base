import java.io.Serializable;

public class ExchangeData implements Serializable {
    private static final long serialVersionUID = 4L;
    private GameInfo gameInfo;
    private PlayerInfo playerInfo;

    public ExchangeData() {
    }

    public ExchangeData(int mazeSize, int treasureNumber) {
        gameInfo = new GameInfo(mazeSize, treasureNumber);
    }

    public ExchangeData(GameInfo gameInfo, PlayerInfo playerInfo) {
        this.gameInfo = gameInfo;
        this.playerInfo = playerInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public PlayerInfo getPlayerInfo() {
        return playerInfo;
    }
}