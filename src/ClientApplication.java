import client.Client;
import client.ClientInterface;
import client.ClientWindow;

public class ClientApplication {
    public Client initialise() {
        Client cNewClient = new Client();
        return cNewClient;
    }

    public void launchGUI(ClientInterface client) {
        ClientWindow gui = new ClientWindow(client);
    }

    public static void main(String[] args) {
        ClientApplication ca = new ClientApplication();
        ca.launchGUI(ca.initialise());
    }
}
