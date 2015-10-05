import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

class TextInfo implements Serializable {
    public String color;
    public String text;

    public TextInfo(int code, String color, String text) {
        this.code = code;
        this.color = color;
        this.text = text;
    }

    public int code;

    public TextInfo(String text) {
        this.text = text;
        this.color = "black";
    }

}

public class LogWindow extends Application {

    TextFlow primaryTextFlow = new TextFlow();
    TextFlow backupTextFlow = new TextFlow();
    TextFlow normalTextFlow = new TextFlow();
    Map<String, Color> hashMap = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    private void iniHashMap() {
        hashMap.put("black", Color.BLACK);
        hashMap.put("blue", Color.BLUE);
        hashMap.put("red", Color.RED);
        hashMap.put("green", Color.GREEN);
        hashMap.put("gold", Color.GOLD);
        hashMap.put("brown", Color.BROWN);
        hashMap.put("violet", Color.VIOLET);
        hashMap.put("pink", Color.PINK);
        hashMap.put("magenta", Color.MAGENTA);
        hashMap.put("grey", Color.GREY);
        hashMap.put("dark", Color.DARKKHAKI);

    }

    private void startThread() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8081);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread() {
                @Override
                public void run() {
                    try {
                        ObjectInput scanner = new ObjectInputStream(socket.getInputStream());
                        TextInfo textInfo = (TextInfo) scanner.readObject();
                        Text text = new Text("--------------------\n" + textInfo.text + "--------------------\n");
                        text.setFill(hashMap.get(textInfo.color));
                        Platform.runLater(() -> {
                            if (textInfo.code == 0) {
                                primaryTextFlow.getChildren().add(text);
                            } else {
                                if (textInfo.code == 1) {
                                    backupTextFlow.getChildren().add(text);

                                } else {
                                    normalTextFlow.getChildren().add(text);
                                }
                            }

                        });

                        scanner.close();
                        socket.close();
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }.start();


        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        iniHashMap();
        Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
        GridPane gridPane = new GridPane();
        gridPane.getColumnConstraints().add(0, new ColumnConstraints(rectangle2D.getWidth() / 3, rectangle2D.getWidth() / 3, rectangle2D.getWidth() / 3));
        gridPane.getColumnConstraints().add(1, new ColumnConstraints(rectangle2D.getWidth() / 3, rectangle2D.getWidth() / 3, rectangle2D.getWidth() / 3));
        gridPane.getColumnConstraints().add(2, new ColumnConstraints(rectangle2D.getWidth() / 3, rectangle2D.getWidth() / 3, rectangle2D.getWidth() / 3));
        gridPane.getRowConstraints().add(0, new RowConstraints(rectangle2D.getHeight() * 0.9, rectangle2D.getHeight() * 0.9, rectangle2D.getHeight() * 0.9));
        gridPane.add(new ScrollPane(primaryTextFlow), 0, 0);
        gridPane.add(new ScrollPane(backupTextFlow), 1, 0);
        gridPane.add(new ScrollPane(normalTextFlow), 2, 0);

        StackPane stackPane = new StackPane(gridPane);
        new Thread(() -> {
            try {
                startThread();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        Scene scene = new Scene(stackPane);
        primaryStage.setScene(scene);
        primaryStage.setWidth(rectangle2D.getWidth());
        primaryStage.setHeight(rectangle2D.getHeight());
        primaryStage.show();
    }


}
