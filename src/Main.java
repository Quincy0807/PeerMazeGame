import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static int mazeSize;
    private static int treasureNumber;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Peer peer = new Peer(primaryStage,mazeSize,treasureNumber);
    }


    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("incorrect argument number");
        } else {
            mazeSize = Integer.parseInt(args[0]);
            treasureNumber = Integer.parseInt(args[1]);
            System.out.println("mazeSize: "+mazeSize+" treasureNumber: "+treasureNumber);
            launch(args);
        }
    }
}
