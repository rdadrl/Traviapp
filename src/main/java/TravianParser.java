import TravianBindings.Building;
import TravianBindings.NetworkUnavailableException;
import TravianBindings.SessionUnavailableException;
import TravianBindings.Village;
import javafx.scene.image.Image;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TravianParser {
    private static final GameState GAME_STATE = GameState.getInstance();
    private HttpClient httpClient = null;

    //Game Contents
    private Image heroImage = null;
    private ArrayList<Village> villages = null;

    private int maxStorage;
    private int maxWheatStorage;

    private int wood;
    private int brick;
    private int iron;
    private int wheat;

    private int woodProd;
    private int brickProd;
    private int ironProd;
    private int wheatProd;

    private int gold;
    private int silver;

    //Latest updated web documents
    private Document dorf1 = null;

    public TravianParser(HttpClient a) {
        //local http client for multiple travian sessions
        httpClient = a;

        //update web documents
        Document dorf1Temp = fetchDorf1();
        if (dorf1Temp != null) dorf1 = dorf1Temp;

        //Parse web documents
        updateHeroImage(dorf1);
        updateStocks(dorf1);
        updateVillageList(dorf1);
        updateBuildingList(dorf1);
    }

    private Document fetchDocument(String urlSuffix) {
        HttpGet httpGet = new HttpGet(GAME_STATE.SERVER_URL + urlSuffix);
        HttpResponse response = null;
        try {
            response = GAME_STATE.HTTP_CLIENT.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ResponseHandler<String> handler = new BasicResponseHandler();
        try {
            String body = handler.handleResponse(response);
            return Jsoup.parse(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("Fetched '" + GAME_STATE.SERVER_URL + urlSuffix + "' with status code " + statusCode);

        return null;
    }

    private Document fetchDorf1 () {
        Document docTemp = fetchDocument("dorf1.php");

        if (docTemp == null)
            throw new NetworkUnavailableException;
        if (docTemp.getElementsByClass("heroImage").size() < 1)
            throw new SessionUnavailableException;

        return docTemp;
        //if (dorf1temp == null)
    }

    private Document fetchLumberProduction() {
        Document dorf1temp = fetchDocument("production.php?t=1");

        if (dorf1temp == null)
            throw new NetworkUnavailableException;
        if (dorf1temp.getElementsByClass("heroImage").size() < 1)
            throw new SessionUnavailableException;

        return dorf1temp;
        //if (dorf1temp == null)
    }

    private void updateHeroImage(Document samplePage) {
        Element heroImageElement = samplePage.getElementsByClass("heroImage").first();

        String imageURL = heroImageElement.attr("src");

        System.out.println("Hero Image URL: " + imageURL);
        HttpGet request = new HttpGet(GAME_STATE.SERVER_URL + imageURL);

        HttpResponse response = null;
        try {
            response = GAME_STATE.HTTP_CLIENT.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                File file = new File("./AppData/HeroImage.png");
                file.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream("./AppData/HeroImage.png");
                entity.writeTo(fos);
                fos.close();

                file.getParentFile().mkdirs();
                heroImage = new Image(file.toURI().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateStocks(Document samplePage) {
        String regex = "[^0-9]";
        maxStorage = Integer.parseInt(samplePage.getElementById("stockBarWarehouse").text().replaceAll(regex, "").trim());
        wood = Integer.parseInt(samplePage.getElementById("l1").text().replaceAll(regex, "").trim());
        brick = Integer.parseInt(samplePage.getElementById("l2").text().replaceAll(regex, "").trim());
        iron = Integer.parseInt(samplePage.getElementById("l3").text().replaceAll(regex, "").trim());
        maxWheatStorage = Integer.parseInt(samplePage.getElementById("stockBarGranary").text().replaceAll(regex, "").trim());
        wheat = Integer.parseInt(samplePage.getElementById("l4").text().replaceAll(regex, "").trim());
    }

    //TODO: Select current village and seek it's resources one by one
    private void updateVillageList(Document samplePage) {
        villages = new ArrayList<>();

        //a list of <a> that includes all info
        Elements villageList = samplePage.getElementById("sidebarBoxVillagelist").getElementsByClass("innerBox").get(1).getElementsByTag("a");
        String regex = "[^0-9]";

        for (int i = 0; i < villageList.size(); i++) {
            Element currVillageElem = villageList.get(i);
            String name = currVillageElem.getElementsByClass("name").first().text();

            String xCoordString = currVillageElem.getElementsByClass("coordinateX").first().text();
            int xCoord = Integer.parseInt(xCoordString.replaceAll(regex, "").trim());
            if (xCoordString.contains("−")) xCoord = -xCoord;

            String yCoordString = currVillageElem.getElementsByClass("coordinateY").first().text();
            int yCoord = Integer.parseInt(yCoordString.replaceAll(regex, "").trim());
            if (yCoordString.contains("−")) yCoord = -yCoord;

            Village newVillage = new Village(name, xCoord, yCoord);
            newVillage.setLumber(wood);
            newVillage.setClay(brick);
            newVillage.setIron(iron);
            newVillage.setCrop(wheat);

            villages.add(newVillage);

            System.out.println("Village " + i + ": " + name + " at (" + xCoord + ", " + yCoord + ").");
        }
        System.out.println("Populated village list, amount: " + villages.size());
    }

    private void updateBuildingList(Document samplePage) {
        Elements buildingList = samplePage.getElementsByClass("buildingList").first().getElementsByTag("li");
        String regex = "[^0-9]";

        for (int i = 0; i < buildingList.size(); i++) {
            Element currBuilding = buildingList.get(i);

            int level = Integer.parseInt(currBuilding.getElementsByClass("lvl").first().text().replaceAll(regex, "")) - 1;
            currBuilding.getElementsByClass("lvl").first().remove();

            String name = currBuilding.getElementsByClass("name").text();
            int due = Integer.parseInt(currBuilding.getElementsByClass("timer").first().attr("value"));

            Building newBuilding = new Building(name, level);
            newBuilding.setBuilding(true);
            newBuilding.setDueInSec(due);

            System.out.println("A building is upgrading: " + name + " to level " + (level + 1) + " in " + due + " seconds.");
        }

    }
    //Getters
    public Image getHeroImage() { return heroImage; }

    public int getMaxStorage() { return maxStorage; }
    public int getMaxWheatStorage() { return maxWheatStorage; }

    public int getWood() { return wood; }
    public int getBrick() { return brick; }
    public int getIron() { return iron; }
    public int getWheat() { return wheat; }

    public int getWoodProd() { return woodProd; }
    public int getBrickProd() { return brickProd; }
    public int getIronProd() { return ironProd; }
    public int getWheatProd() { return wheatProd; }

    public int getGold() { return gold; }
    public int getSilver() { return silver; }

    public ArrayList<Village> getVillages() { return villages; }
}
