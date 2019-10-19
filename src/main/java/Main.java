import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    private static final GameState GAME_STATE = GameState.getInstance();
    private static Scene mainScene;
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("./fxml/MainMenu.fxml"));
        primaryStage.setTitle(GAME_STATE.APP_NAME + " " + GAME_STATE.VERSION);

        mainScene = new Scene(root, 300, 275);

        ((Button) mainScene.lookup("#loginButton")).setOnAction(event -> {
            String username = ((TextField) mainScene.lookup("#usernameField")).getText();
            String password = ((PasswordField) mainScene.lookup("#passwordField")).getText();

            ((Label) mainScene.lookup("#errorLabel")).setText("");

            if (attemptLogIn(username, password)) {
                System.out.println("Succesfully logged in as " + username);
                GAME_STATE.currentPlayerName = username;

                Scene gameScene = null;
                try {
                    Parent gameRoot = FXMLLoader.load(getClass().getResource("./fxml/MainView.fxml"));
                    gameScene = new Scene(gameRoot);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }

                primaryStage.setTitle(GAME_STATE.APP_NAME + " " + GAME_STATE.VERSION + " - " + GAME_STATE.currentPlayerName);
                primaryStage.setScene(gameScene);
            }
            else {
                ((Label) mainScene.lookup("#errorLabel")).setText("Username & Password does not match.");
            }
        });

        primaryStage.setScene(mainScene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    //https://stackoverflow.com/questions/3324717/sending-http-post-request-in-java
    public static boolean attemptLogIn(String username, String password) {
        boolean attempt = false;

        HttpPost httppost = new HttpPost(GAME_STATE.SERVER_URL + "login.php");

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        params.add(new BasicNameValuePair("name", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("lowRes", "1"));

        UrlEncodedFormEntity formEntity = null;
        try {
            formEntity = new UrlEncodedFormEntity(params, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        httppost.setEntity(formEntity);

        //Execute and get the response.
        HttpResponse response = null;
        try {
            response = GAME_STATE.HTTP_CLIENT.execute(httppost);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try {
                InputStream instream = entity.getContent();
                Document doc = Jsoup.parse(EntityUtils.toString(entity, "UTF-8"));
                if (doc.select("li.villageResources").size() > 0) attempt = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return attempt;
    }
}
