import TravianBindings.Village;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

public class Controller {
    private static final GameState GAME_STATE = GameState.getInstance();
    private TravianParser session = null;

    private ObservableList<Village> villagesOList = null;

    @FXML
    private Label heroLabel;

    @FXML
    private ImageView heroImage;

    @FXML
    private TableView<Village> villageList;
    @FXML
    private TableColumn<Village, String> villageNameColumn;
    @FXML
    private TableColumn<Village, Integer> lumberColumn;
    @FXML
    private TableColumn<Village, Integer> clayColumn;
    @FXML
    private TableColumn<Village, Integer> ironColumn;
    @FXML
    private TableColumn<Village, Integer> cropColumn;

    @FXML
    private Label villageNameLabel;

    @FXML
    protected void initialize() {
        session = new TravianParser(GAME_STATE.HTTP_CLIENT);

        heroImage.setImage(session.getHeroImage());
        heroLabel.setText(GAME_STATE.currentPlayerName);

        villageNameColumn.setCellValueFactory(new PropertyValueFactory<Village, String>("name"));
        lumberColumn.setCellValueFactory(new PropertyValueFactory<Village, Integer>("lumber"));
        clayColumn.setCellValueFactory(new PropertyValueFactory<Village, Integer>("clay"));
        ironColumn.setCellValueFactory(new PropertyValueFactory<Village, Integer>("iron"));
        cropColumn.setCellValueFactory(new PropertyValueFactory<Village, Integer>("crop"));

        updateVillagesTable();

        Village activeVillage = session.getVillages().get(0);
        villageNameLabel.setText(activeVillage.getName() + " (" + activeVillage.getX() + ", " + activeVillage.getY() + ")");
    }

    private void updateVillagesTable () {
        villageList.getItems().setAll(session.getVillages());
    }
}
