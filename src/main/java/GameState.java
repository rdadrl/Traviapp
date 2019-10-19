import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

public class GameState {
    private static GameState ourInstance = new GameState();

    public static GameState getInstance() {
        return ourInstance;
    }

    private GameState() {
    }

    public static final String APP_NAME = "Traviapp";
    public static final String VERSION  = "1.0a";

    public static final String SERVER_URL = "https://ts4.travian.com.tr/";
    public static final HttpClient HTTP_CLIENT = HttpClients.createDefault();


    public static String currentPlayerName = null;
}