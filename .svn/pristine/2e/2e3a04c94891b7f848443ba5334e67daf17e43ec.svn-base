import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ServerInterface extends BaseInterface,Serializable {
    long serialVersionUID = 6L;

    ExchangeData joinGame(ClientInterface clientInterface) throws RemoteException;

    ExchangeData move(int id, String direction) throws RemoteException;

    boolean quitGame(int id) throws RemoteException;

    PlayerInfo recordSelfAsFirstPlayer(PlayerRole playerRole) throws RemoteException;

    void pingBackupServer() throws RemoteException;
}
