import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerRole extends Super implements ClientInterface {
    private ServerInterface serverRole;
    private StartGameController startGameController;
    private transient Stage stage;
    private int playerID;
    private Timer pingTimer = new Timer();

    public PlayerRole(Stage stage) throws IOException {
        this.stage = stage;
        UnicastRemoteObject.exportObject(this, randomPort());
    }

    public ServerInterface getServerRole() {
        return serverRole;
    }

    public int getPlayerID() {
        return playerID;
    }

    @Override
    public String getPlayerIDAsString() {
        return "player-" + playerID;
    }

    @Override
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public void setServerRole(ServerRole serverRole) {
        this.serverRole = serverRole;
    }

    public void bindPrimaryServerWithPlayer() {
        ServerInterface server = serverRole;
        this.setRole(primaryServer);
        try {
            this.setPlayerInfo(server.recordSelfAsFirstPlayer(this));
        } catch (RemoteException e) {
            LogHelper.logWarnning("never reach this line(PlayerRole-bindPrimaryServerWithPlayer)");
        }
    }

    public void initializedUI() {
        this.stage.setOnCloseRequest(windowEvent -> System.exit(0));
        FXMLLoader fxmlLoader;
        try {
            fxmlLoader = new FXMLLoader(getClass().getResource("start_game_layout.fxml"));
            Parent root = fxmlLoader.load();
            this.stage.setTitle("Join A Game");
            this.stage.setScene(new Scene(root, 300, 275));
            this.startGameController = fxmlLoader.getController();
            this.startGameController.setPlayer(this);
            if (getRole() == null || !getRole().equals(primaryServer)) {
                stage.show();
            }
        } catch (IOException e) {
            LogHelper.logWarnning("error during start_game_layout initialization");
        }
    }

    @Override
    public void showStage() {
        Platform.runLater(() -> {
            try {
                startGameController.showGameUI(stage, new ExchangeData(getGameInfo(), getPlayerInfo()));
            } catch (IOException e) {
                LogHelper.logWarnning("error during game_layout initialization");
            }
        });
    }


    private int randomPort() {
        int max = 9999;
        int min = 9100;
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    @Override
    public String toString() {
        return getPlayerIDAsString() + " : " + getRole();
    }

    @Override
    public void rivalNotification(ExchangeData exchangeData) throws RemoteException {
        setGameInfo(exchangeData.getGameInfo());
        startGameController.drawRival(exchangeData);
    }

    @Override
    public void startMazeGame(ExchangeData exchangeData) throws RemoteException {
        startGameController.startMazeGame(exchangeData);
    }

    @Override
    public void terminateMazeGame() throws RemoteException {
        LogHelper.logPlayer(this,this.getPlayerIDAsString()+" ends the game");
        if (getRole().equals(Super.backupServer)) {
            pingTimer.cancel();
            pingTimer = new Timer();
        }
        startGameController.terminateGame();
    }

    @Override
    public void setPlayerRole(String role) throws RemoteException {
        this.setRole(role);
    }

    @Override
    public void setGameInfo(GameInfo gameInfo) {
        super.setGameInfo(gameInfo);
    }

    @Override
    public ExchangeData becomeServer() throws RemoteException {
        ExchangeData exchangeData;
        pingTimer.cancel();
        if (getRole().equals(Super.primaryServer)) {
            LogHelper.logPrimary(this.getPlayerIDAsString() + " is already the primary server");
            return new ExchangeData(this.getGameInfo(), this.getPlayerInfo());
        } else {
            /** if it is the backup server, it already has the latest information**/
            exchangeData = new ExchangeData(getGameInfo(), getPlayerInfo());
        }
        setRole(Super.primaryServer);
        getGameInfo().setServerID(this.getPlayerIDAsString());
        LogHelper.logPrimary(this.getPlayerIDAsString() + " becomes primary server");
        Platform.runLater(() -> stage.setTitle("MAZE GAME: " + this));
        ServerRole serverRole = new ServerRole(exchangeData);
        this.serverRole = serverRole;
        this.startGameController.getGameController().setRemoteServer(serverRole);
        serverRole.getGameInfo().setServer((ServerInterface) UnicastRemoteObject.exportObject(serverRole, 0));
        LogHelper.logPlayer(this, getPlayerIDAsString() + " pick up a new backup server");
        //      SetTheServerObject to him is its not set yet or just return the Exchnage data
        do {
            ClientInterface nextBackup = null;
            try {
                nextBackup = getGameInfo().tryToGetNextBackupServer();
                nextBackup.becomeBackUpServer(exchangeData);
                getGameInfo().setBackID(nextBackup.getPlayerIDAsString());
                LogHelper.logBackup(nextBackup.getPlayerIDAsString() + " becomes new backup server");
                this.serverRole.pingBackupServer();
                return new ExchangeData(getGameInfo(), getPlayerInfo());
            } catch (RemoteException ex) {
                LogHelper.logPlayer(this, "player-" + getGameInfo().returnIdByRemoteObject(nextBackup) + " crashed too");
            }
        }
        while (getGameInfo().queueSize() != 0);
        if (getGameInfo().queueSize() == 0) {
            LogHelper.logWarnning("*\n*\n*\n*\n*\n*\n*\n*" +
                    "this line(PlayerRole-becomeServer()) should not be reached.\n " +
                    "BackupServer queue is empty!!" +
                    "*\n*\n*\n*\n*\n*\n*\n*");
        }
        return new ExchangeData(getGameInfo(), getPlayerInfo());
    }


    @Override
    public void synchronizeGameState(ExchangeData exchangeData) throws RemoteException {
        //TODO: 2. Implement the self updtae of gmae state
        //UpdateRole();
        setGameInfo(exchangeData.getGameInfo());
        LogHelper.logBackup("backup server["+getGameInfo().backupID()+"] synchronized with the primary server["+getGameInfo().serverID()+"]\n" +
                "current game information:\n" + getGameInfo());
        //Start Ping
    }

    @Override
    public void becomeBackUpServer(ExchangeData exchangeData) throws RemoteException {
        setPlayerRole(Super.backupServer);
        Platform.runLater(() -> stage.setTitle("Maze Game: " + this));
        LogHelper.logBackup(getPlayerIDAsString() + " becomes " + getRole());
        setGameInfo(exchangeData.getGameInfo());
        getGameInfo().setBackID(this.getPlayerIDAsString());
        startGameController.getGameController().setRemoteServer(exchangeData.getGameInfo().getServer());
        this.serverRole = exchangeData.getGameInfo().getServer();
        LogHelper.logBackup("backup server["+this.getPlayerIDAsString()+"]synchronized with the primary server["+getGameInfo().serverID()+"]\n" +
                "current game information:" + getGameInfo());
        //Start Ping
        pingPrimaryServer();

    }

    @Override
    public void pingPrimaryServer() {
        GameInfo gameInfo = getGameInfo();
        PlayerInfo playerInfo = getPlayerInfo();
        PlayerRole playerRole = this;
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LogHelper.logBackup("backup server["+gameInfo.backupID()+"] ping ----> primary server["+gameInfo.serverID()+"]");
                if (!playerRole.ping(getGameInfo().getServer())) {
                    LogHelper.logBackup("primary server["+gameInfo.serverID()+"] crashed");
                    LogHelper.logBackup(playerRole.getPlayerIDAsString() + " will become primary server");
                    do {
                        try {
                            playerRole.becomeServer();
                            pingTimer.cancel();
                            break;
                        } catch (RemoteException ignore) {
                        }
                    }
                    while (gameInfo.queueSize() != 0);
                }
            }
        }, 1000 * 10, 1000 * 30);
    }
}
