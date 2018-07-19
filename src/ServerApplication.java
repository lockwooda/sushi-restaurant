import common.DataPersistence;
import common.StockManagement;
import server.Server;
import server.ServerInterface;
import server.ServerWindow;

public class ServerApplication {

    public Server initialise() {
        StockManagement smStock = new StockManagement();
        Server sNewServer = new Server(smStock);

        return sNewServer;
    }

    public void launchGUI(ServerInterface server) {
        ServerWindow gui = new ServerWindow(server);
    }

    public static void main(String[] args) {
        ServerApplication sa = new ServerApplication();
        Server s = sa.initialise();
        sa.launchGUI(s);
    }
}
