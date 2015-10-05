import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends BaseInterface, Serializable {
    long serialVersionUID = 5L;

    void setPlayerID(int playerID) throws RemoteException;

    String getPlayerIDAsString() throws RemoteException;

    void rivalNotification(ExchangeData exchangeData) throws RemoteException;

    void startMazeGame(ExchangeData exchangeData) throws RemoteException;

    void terminateMazeGame() throws RemoteException;

    void setPlayerRole(String role) throws RemoteException;

    void setGameInfo(GameInfo gameInfo) throws RemoteException;

    void showStage() throws RemoteException;

    void becomeBackUpServer(ExchangeData exchangeData) throws RemoteException;

    ExchangeData becomeServer() throws RemoteException;

    void synchronizeGameState(ExchangeData exchangeData) throws RemoteException;

    void pingPrimaryServer() throws RemoteException;

    String getRole() throws RemoteException;
}
