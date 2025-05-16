package nguessanabigail.adjeaude.miageholding;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class reparationsController implements Initializable {

    @FXML private TableView<Reparation> visitesTable;
    @FXML private TableColumn<Reparation, Integer> idCol;
    @FXML private TableColumn<Reparation, String> vehiculeCol;
    @FXML private TableColumn<Reparation, LocalDateTime> dateVisiteCol;
    @FXML private TableColumn<Reparation, LocalDateTime> dateExpirationCol;
    @FXML private TableColumn<Reparation, String> centreCol;
    @FXML private TableColumn<Reparation, Integer> coutCol;
    @FXML private TableColumn<Reparation, String> resultatCol;
    @FXML private TableColumn<Reparation, String> actionsCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button btnAjouterVisite;
    @FXML private Button btnExportPDF;
    @FXML private Button btnNotifier;
    @FXML private Label expiringVisitesCount;

    private ObservableList<Reparation> reparationsData = FXCollections.observableArrayList();
    private FilteredList<Reparation> filteredData;
    private SortedList<Reparation> sortedData;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //initializeTable();
        //setupFilters();
        loadReparationsFromDatabase();
        //FromDatabase();
        //updateExpiringCount();
    }
    public void showAddReparations(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addReparationsController.fxml"));
            Parent root = loader.load();

            addVisitesTechniqueView controller = loader.getController();
            controller.setOnSaveCallback(() -> {
                loadReparationsFromDatabase();
                //updateExpiringCount();
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle réparation ");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    private void loadReparationsFromDatabase() {
      //  visitesData.clear();

        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "SELECT e.*, v.MARQUE, v.MODELE, v.IMMATRICULATION " +
                    "FROM entretien e " +
                    "JOIN vehicules v ON e.ID_VEHICULE = v.ID_VEHICULE " +
                    "WHERE e.MOTIF_ENTR LIKE '%visite technique%' " +
                    "ORDER BY e.DATE_ENTREE_ENTR DESC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String vehiculeInfo = rs.getString("MARQUE") + " " +
                        rs.getString("MODELE") + " - " +
                        rs.getString("IMMATRICULATION");

                VisitesTechniques visite = new VisitesTechniques(
                        rs.getInt("ID_ENTRETIEN"),
                        rs.getInt("ID_VEHICULE"),
                        rs.getTimestamp("DATE_ENTREE_ENTR").toLocalDateTime(),
                        rs.getTimestamp("DATE_SORTIE_ENTR") != null ?
                                rs.getTimestamp("DATE_SORTIE_ENTR").toLocalDateTime() : null,
                        rs.getString("MOTIF_ENTR"),
                        rs.getString("OBSERVATION"),
                        rs.getInt("COUT_ENTR"),
                        rs.getString("LIEU_ENTR"),
                        vehiculeInfo
                );
               // visitesData.add(visite);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void exportToPDF (ActionEvent event){
    }

    public void statistique (ActionEvent event){

    }
    private void showAlert (Alert.AlertType type, String title, String header, String content){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}