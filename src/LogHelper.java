import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogHelper {
    static String[] colors = new String[]{"pink", "grey", "red", "green", "gold", "brown",
            "violet", "black"};

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS :: ");

    static void logPlayer(PlayerRole player, String str) {
        if (player.getRole().equals(Super.backupServer)) {
            logBackup(str);
        } else {
            if (player.getRole().equals(Super.primaryServer)) {
                logPrimary(str);
            } else {
                logNormalPlayer(player.getPlayerID(), str);
            }

        }
    }

    public static void logPrimary(String str) {
        System.out.println("PrimaryServer:  \n");
        sendToSocket(0, str, "magenta");
    }

    public static void logBackup(String str) {
        System.out.println("BackUpServer:   \n");
        sendToSocket(1, str, "blue");
    }

    public static void logWarnning(String str) {
        System.out.println("NormalPlayer:   \n");
        sendToSocket(2, str, "dark");
    }

    private static void logNormalPlayer(int id, String str) {
        System.out.println("NormalPlayer:   \n");
        sendToSocket(2, str, colors[id % colors.length]);

    }

    private static void sendToSocket(int type, String str, String color) {
        try {
            Socket so = new Socket();
            so.connect(new InetSocketAddress("127.0.0.1", 8081));
            ObjectOutput objectOutput = new ObjectOutputStream(so.getOutputStream());
            objectOutput.writeObject(new TextInfo(type, color, LocalDateTime.now().format(dateTimeFormatter) + str + "\n"));
            objectOutput.close();
            so.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

