package nguessanabigail.adjeaude.miageholding;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class assuranceController implements Initializable {

        @FXML private TableView<Assurance> assurancesTable;
        @FXML private TableColumn<Assurance, Integer> numCarteCol;
        @FXML private TableColumn<Assurance, String> vehiculeCol;
        @FXML private TableColumn<Assurance, String> dateDebutCol;
        @FXML private TableColumn<Assurance, String> dateFinCol;
        @FXML private TableColumn<Assurance, String> agenceCol;
        @FXML private TableColumn<Assurance, Integer> coutCol;
        @FXML private TableColumn<Assurance, String> statutCol;
        @FXML private TableColumn<Assurance, String> actionsCol;

        @FXML private TextField searchField;
        @FXML private ComboBox<String> filterComboBox;
        @FXML private Button btnAjouterAssurance;
        @FXML private Button btnExportPDF;
        @FXML private Button btnNotifier;
        @FXML private Label expiringAssurancesCount;

        private ObservableList<Assurance> assurancesData = FXCollections.observableArrayList();

        @Override
        public void initialize(URL url, ResourceBundle rb) {
            // Configuration des colonnes
            numCarteCol.setCellValueFactory(new PropertyValueFactory<>("numCarteAssurance"));
            vehiculeCol.setCellValueFactory(new PropertyValueFactory<>("vehiculeInfo"));
            dateDebutCol.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
            dateFinCol.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
            agenceCol.setCellValueFactory(new PropertyValueFactory<>("agence"));
            coutCol.setCellValueFactory(new PropertyValueFactory<>("cout"));
            statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

            // Configuration des filtres
            filterComboBox.setItems(FXCollections.observableArrayList(
                    "Toutes", "En cours", "Expirées", "Expire bientôt"
            ));
            filterComboBox.getSelectionModel().selectFirst();

            // Chargement des données (à implémenter)
            loadAssurancesFromDatabase();
        }

        private void loadAssurancesFromDatabase() {
            // Ici, vous chargeriez les données depuis la base de données
            // Pour l'instant, on peut simplement initialiser la table
            assurancesTable.setItems(assurancesData);
        }

        @FXML
        public void showAddAssuranceForm() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addAssuranceForm.fxml"));
                Parent root = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Nouvelle assurance");
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'ouvrir le formulaire d'ajout d'assurance", e.getMessage());
            }
        }

        @FXML
        public void exportToPDF() {
            showAlert(Alert.AlertType.INFORMATION, "Export PDF",
                    "Fonction en développement", "L'export en PDF sera disponible prochainement.");
        }

        @FXML
        public void notifierRenouvellements() {
            showAlert(Alert.AlertType.INFORMATION, "Notification",
                    "Fonction en développement", "La notification des renouvellements sera disponible prochainement.");
        }

        private void showAlert(Alert.AlertType type, String title, String header, String content) {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        }


}
