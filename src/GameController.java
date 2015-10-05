import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GameController implements Initializable {
    @FXML
    private GridPane gridPane;
    @FXML
    private StackPane stackPane;
    @FXML
    private TableColumn<PlayerView, String> playerColumn;
    @FXML
    private TableColumn<PlayerView, Integer> numColumn;
    @FXML
    private TableView<PlayerView> infoTable;
    private GridPane mazeGrid;
    private PlayerRole player;
    private ServerInterface remoteServer;
    private Scene startGameScene;
    private EventHandler<KeyEvent> eventHandler;
    private HashMap<Position, Node> treasureImages;
    private Map<Integer, PlayerView> playerViews;

    public void initializeMazeUI(PlayerRole player, Scene startGameScene, ExchangeData exchangeData) {
        this.startGameScene = startGameScene;
        GameInfo gameInfo = exchangeData.getGameInfo();
        PlayerInfo playerInfo = exchangeData.getPlayerInfo();
        this.remoteServer = gameInfo.getServer();
        this.player = player;
        player.setExchangeData(exchangeData);
        for (int i = 0; i < gameInfo.getMazeSize(); i++) {
            mazeGrid.getColumnConstraints().add(new ColumnConstraints(50));
            mazeGrid.getRowConstraints().add(new RowConstraints(50));
        }
        mazeGrid.setGridLinesVisible(true);
        mazeGrid.setMaxWidth(500);
        drawPlayer(gameInfo, playerInfo, "SELF");
        gameInfo.getPlayerInfos().values().stream().filter(rival -> rival.getPlayerId() != playerInfo.getPlayerId()).forEach(rival -> drawPlayer(gameInfo, rival, "RIVAL"));
        drawTreasure(gameInfo.getTreasureInfo());
        gridPane.add(mazeGrid, 1, 0);
    }

    public void drawPlayer(GameInfo gameInfo, PlayerInfo playerInfo, String type) {
        int id = playerInfo.getPlayerId();
        if (playerInfo.isQuit()) {
            if (!playerViews.get(id).isQuit()) {
                playerViews.get(id).setQuit(true);
                mazeGrid.getChildren().remove(playerViews.get(id).getImage());
                getPlayerView(id).setTreasureCollected(playerInfo.getTreasureCollected());
                infoTable.setItems(FXCollections.observableArrayList(playerViews.values()).sorted((o1, o2) -> o2.getTreasureCollected() - o1.getTreasureCollected()));
            }
            return;
        }
        Position position = playerInfo.getCurrentPosition();
        final Node node;
        switch (type) {
            case "RIVAL":
                node = new ImageView(new File("/Users/Quincy/Project/IDEA/PeerMazeGame/src/rival.png").toURI().toString());
                break;
            default:
                node = new ImageView(new File("/Users/Quincy/Project/IDEA/PeerMazeGame/src/self.png").toURI().toString());
                break;
        }
        Platform.runLater(() -> {
            StackPane stackPane = new StackPane();
            stackPane.setPrefWidth(50);
            stackPane.setPrefWidth(50);
            stackPane.getChildren().add(0, node);
            stackPane.getChildren().add(1, getPlayerLabel(id, type));
            if (getPlayerView(id).containsImage()) {
                mazeGrid.getChildren().remove(getPlayerView(id).getImage());
            }
            getPlayerView(id).setImage(stackPane);
            mazeGrid.add(stackPane, position.getX(), position.getY());
            getPlayerView(id).setTreasureCollected(playerInfo.getTreasureCollected());
            infoTable.setItems(FXCollections.observableArrayList(playerViews.values()).sorted((o1, o2) -> o2.getTreasureCollected() - o1.getTreasureCollected()));
            drawTreasure(gameInfo.getTreasureInfo());
        });
    }

    public void drawTreasure(Map<Position, Integer> treasures) {
        if (treasureImages.size() == treasures.size()) {
            return;
        }
        if (treasureImages.size() == 0) {
            treasures.forEach((Position position, Integer integer) -> {
                StackPane stackPane = new StackPane();
                Node node = new ImageView(new File("/Users/Quincy/Project/IDEA/PeerMazeGame/src/treasure.jpeg").toURI().toString());
                stackPane.getChildren().add(0, node);
                stackPane.getChildren().add(1, createLabelWithFading(integer + ""));
                treasureImages.put(position, stackPane);
                mazeGrid.add(stackPane, position.getX(), position.getY());
            });
        } else {
            List<Position> treasureCollected = treasureImages.keySet().stream().filter(position -> !treasures.keySet().contains(position)).collect(Collectors.toList());
            treasureCollected.forEach(position -> {
                mazeGrid.getChildren().remove(treasureImages.get(position));
                treasureImages.remove(position);
            });
        }
        if (treasures.size() == 0) {
            terminateGame();
        }
    }

    public void startMazeGame(ExchangeData exchangeData) {
        player.setExchangeData(exchangeData);
        if (player.getRole().equals(Super.backupServer)) {
            player.pingPrimaryServer();
        }
        Platform.runLater(() -> {
            stackPane.getChildren().remove(0);
            gridPane.setOpacity(1);
            setEventHandler();
        });
    }

    private Label getPlayerLabel(int id, String type) {
        if (getPlayerView(id).containsLabel()) {
            return getPlayerView(id).getLabel();
        } else {
            String text;
            if (type.equals("RIVAL")) {
                text = "player" + id;
            } else {
                text = "YOU";
            }
            Label label = createLabelWithFading(text);
            getPlayerView(id).setLabelWithName(label, text);
            return label;
        }
    }

    private Label createLabelWithFading(String text) {
        Label newLabel = new Label(text);
        newLabel.setTextFill(Color.BLUEVIOLET);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), newLabel);
        fadeTransition.setCycleCount(Timeline.INDEFINITE);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0.2);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
        return newLabel;
    }

    private PlayerView getPlayerView(int id) {
        if (playerViews.containsKey(id)) {
            return playerViews.get(id);
        } else {
            PlayerView rival = new PlayerView();
            playerViews.put(id, rival);
            return rival;
        }
    }

    private void setEventHandler() {
        eventHandler = new KeyboardHandler();
        gridPane.addEventHandler(KeyEvent.KEY_PRESSED, eventHandler);
    }

    public void terminateGame() {
        gridPane.removeEventHandler(KeyEvent.KEY_PRESSED, eventHandler);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("All treasures have been collected");
        alert.setContentText("Please start a new game");
        ObservableList<Node> nodes = alert.getDialogPane().getChildren();
        nodes.remove(1);//remove the Label
        Button button = (Button) ((ButtonBar) nodes.get(1)).getButtons().get(0);
        button.setText("Click!");
        button.setOnAction(actionEvent -> {
            if (player.getRole().equals(Super.primaryServer)) {
                stackPane.setEffect(new GaussianBlur(60));
                player.bindPrimaryServerWithPlayer();
            } else {
                Stage root = ((Stage) stackPane.getScene().getWindow());
                root.setScene(startGameScene);
                root.setWidth(300);
                root.setHeight(275);
            }
        });
        alert.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playerViews = FXCollections.observableHashMap();
        treasureImages = new HashMap<>();
        mazeGrid = new GridPane();
        playerColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        playerColumn.setCellFactory(param -> new TableCell<PlayerView, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                if (item != null) {
                    setText(item);
                }
                setAlignment(Pos.CENTER);
            }
        });
        numColumn.setCellValueFactory(new PropertyValueFactory<>("treasureCollected"));
        numColumn.setCellFactory(param -> new TableCell<PlayerView, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {

                if (item != null) {
                    setText(item.toString());
                }
                setAlignment(Pos.CENTER);
            }
        });
        infoTable.setPlaceholder(new Label(""));
        infoTable.setItems(FXCollections.observableArrayList(playerViews.values()).sorted());
    }

    public void setRemoteServer(ServerInterface remoteServer) {
        this.remoteServer = remoteServer;
    }

    @SuppressWarnings("all")
    private void moveAction(String direction) {
        try {
            LogHelper.logPlayer(player, player.getPlayerIDAsString() + " start to make a movement. Type: " + direction);
            ExchangeData exchangeData = remoteServer.move(player.getPlayerID(), direction);
            GameInfo gameInfo = exchangeData.getGameInfo();
            PlayerInfo playerInfo = exchangeData.getPlayerInfo();
            if (!direction.equals("NoMove")) {
                drawPlayer(gameInfo, playerInfo, "SELF");
            }
            playerInfo.getRivals().forEach(rival -> drawPlayer(gameInfo, rival, "RIVAL"));
            LogHelper.logPlayer(player, player.getPlayerIDAsString() + " moves complete");
        } catch (RemoteException e) {
            LogHelper.logPlayer(player, "primary server[" + player.getGameInfo().serverID() + "] crashed!");
            //TODO: 3. Server Die when Backup is updating
            //Check If I am backup
            if (player.getRole().equals(Super.backupServer)) {
                LogHelper.logPlayer(player, "I am [" + player.getPlayerIDAsString() + "] the current backup server, and will become the primary server");
                try {
                    remoteServer = player.becomeServer().getGameInfo().getServer();
                    ExchangeData exchangeData = remoteServer.move(player.getPlayerID(), direction);
                    GameInfo gameInfo = exchangeData.getGameInfo();
                    PlayerInfo playerInfo = exchangeData.getPlayerInfo();
                    if (!direction.equals("NoMove")) {
                        drawPlayer(gameInfo, playerInfo, "SELF");
                    }
                    playerInfo.getRivals().forEach(rival -> drawPlayer(gameInfo, rival, "RIVAL"));
                } catch (RemoteException ignore) {
                    LogHelper.logWarnning("*\n*\n*\n*\n*\n*\n"
                            + "Under this situation,this line(GameController-KeyboardHandler-handle()) should never be reached");
                }
            }
            //TODO : Case 4: Normal player is updating and Server Dies
            else {
                notifyBackup2PrimaryServer();
                moveAction(direction);
            }
        }
    }

    @SuppressWarnings("all")
    private void notifyBackup2PrimaryServer() {
        ClientInterface currentBackupServer = player.getGameInfo().getCurrentBackupServer();
        ExchangeData exchangeData = null;
        LogHelper.logPlayer(player, player.getPlayerIDAsString() + " now tell current backup server[" + player.getGameInfo().backupID() + "] to become primary server");
        try {
            exchangeData = currentBackupServer.becomeServer();
        } catch (RemoteException e1) {
            LogHelper.logPlayer(player, "current backup server[player-" + player.getGameInfo().returnIdByRemoteObject(currentBackupServer) + "] also crashed\ncontinue to notify next player");
            do {
                ClientInterface nextBackup = null;
                try {
                    nextBackup = player.getGameInfo().tryToGetNextBackupServer();
                    exchangeData = nextBackup.becomeServer();
                    LogHelper.logPlayer(player, nextBackup.getPlayerIDAsString() + " will become the primary server");
                    break;
                } catch (RemoteException ee) {
                    LogHelper.logPlayer(player, "player-" + player.getGameInfo().returnIdByRemoteObject(nextBackup) + " crashed too");
                }
            }
            while (player.getGameInfo().getMazeSize() != 0);
        }
        remoteServer = exchangeData.getGameInfo().getServer();
    }

    class KeyboardHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent keyEvent) {
            String direction = null;
            boolean isQuit = false;
            switch (keyEvent.getCode()) {
                case UP:
                    direction = "N";
                    break;
                case DOWN:
                    direction = "S";
                    break;
                case LEFT:
                    direction = "E";
                    break;
                case RIGHT:
                    direction = "W";
                    break;
                case SPACE:
                    direction = "NoMove";
                    break;
                case ESCAPE:
                    isQuit = true;
                    break;
            }
            if (direction != null) {
                moveAction(direction);
            } else {
                if (isQuit && !player.getRole().equals(Super.primaryServer)) {
                    try {
                        boolean result = remoteServer.quitGame(player.getPlayerID());
                        if (result) {
                            LogHelper.logPlayer(player, player.getPlayerIDAsString() + " quit successfully");
                            System.exit(0);
                        } else {
                            LogHelper.logPlayer(player, player.getPlayerIDAsString() + " failed to quit");
                        }
                    } catch (RemoteException e) {
                        LogHelper.logPlayer(player, "now primary server[" + player.getGameInfo().serverID() + "] crashed!");
                        notifyBackup2PrimaryServer();
                        try {
                            remoteServer.quitGame(player.getPlayerID());
                        } catch (RemoteException ignore) {
                        }
                    }
                } else if (isQuit) {
                    notifyBackup2PrimaryServer();
                    try {
                        boolean result = remoteServer.quitGame(player.getPlayerID());
                        if (result) {
                            LogHelper.logPrimary(player.getPlayerIDAsString() + " quit successfully");
                            System.exit(0);
                        } else {
                            LogHelper.logPrimary(player.getPlayerIDAsString() + " failed to quit");
                        }
                    } catch (RemoteException ignore) {
                    }
                }
            }
        }
    }
}


