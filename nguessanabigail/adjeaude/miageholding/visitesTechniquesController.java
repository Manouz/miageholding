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

public class visitesTechniquesController implements Initializable {
    @FXML private TableView<VisitesTechniques> visitesTable;
    @FXML private TableColumn<VisitesTechniques, Integer> idCol;
    @FXML private TableColumn<VisitesTechniques, String> vehiculeCol;
    @FXML private TableColumn<VisitesTechniques, LocalDateTime> dateVisiteCol;
    @FXML private TableColumn<VisitesTechniques, LocalDateTime> dateExpirationCol;
    @FXML private TableColumn<VisitesTechniques, String> centreCol;
    @FXML private TableColumn<VisitesTechniques, Integer> coutCol;
    @FXML private TableColumn<VisitesTechniques, String> resultatCol;
    @FXML private TableColumn<VisitesTechniques, String> actionsCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button btnAjouterVisite;
    @FXML private Button btnExportPDF;
    @FXML private Button btnNotifier;
    @FXML private Label expiringVisitesCount;

    private ObservableList<VisitesTechniques> visitesData = FXCollections.observableArrayList();
    private FilteredList<VisitesTechniques> filteredData;
    private SortedList<VisitesTechniques> sortedData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeTable();
        setupFilters();
        loadVisitesFromDatabase();
        updateExpiringCount();
    }

    private void initializeTable() {
        // Configuration des colonnes
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehiculeCol.setCellValueFactory(new PropertyValueFactory<>("vehiculeInfo"));
        dateVisiteCol.setCellValueFactory(new PropertyValueFactory<>("dateEntree"));
        dateExpirationCol.setCellValueFactory(new PropertyValueFactory<>("dateSortie"));
        centreCol.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        coutCol.setCellValueFactory(new PropertyValueFactory<>("cout"));
        resultatCol.setCellValueFactory(cellData -> {
            // Pour le résultat, on peut utiliser l'observation pour stocker CONFORME/NON CONFORME
            String observation = cellData.getValue().getObservation();
            if (observation != null && observation.contains("CONFORME")) {
                return new SimpleStringProperty("CONFORME");
            } else if (observation != null && observation.contains("NON CONFORME")) {
                return new SimpleStringProperty("NON CONFORME");
            } else {
                return new SimpleStringProperty("En attente");
            }
        });

        // Formatage des dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateVisiteCol.setCellFactory(column -> new TableCell<VisitesTechniques, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        dateExpirationCol.setCellFactory(column -> new TableCell<VisitesTechniques, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Formatage des coûts
        coutCol.setCellFactory(column -> new TableCell<VisitesTechniques, Integer>() {
            @Override
            protected void updateItem(Integer cout, boolean empty) {
                super.updateItem(cout, empty);
                if (empty || cout == null) {
                    setText(null);
                } else {
                    setText(String.format("%,d FCFA", cout));
                }
            }
        });

        // Colonne actions avec boutons
        actionsCol.setCellFactory(param -> new TableCell<VisitesTechniques, String>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    VisitesTechniques visite = getTableView().getItems().get(getIndex());
                    editVisite(visite);
                });

                deleteBtn.setOnAction(event -> {
                    VisitesTechniques visite = getTableView().getItems().get(getIndex());
                    deleteVisite(visite);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupFilters() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "Toutes", "En attente", "Conformes", "Non conformes", "Expirent bientôt"
        ));
        filterComboBox.getSelectionModel().selectFirst();

        filteredData = new FilteredList<>(visitesData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilter();
        });

        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilter();
        });

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(visitesTable.comparatorProperty());
        visitesTable.setItems(sortedData);
    }

    private void updateFilter() {
        filteredData.setPredicate(visite -> {
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!visite.getVehiculeInfo().toLowerCase().contains(lowerCaseFilter) &&
                        !visite.getLieu().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }

            String selectedFilter = filterComboBox.getValue();
            if (selectedFilter != null && !selectedFilter.equals("Toutes")) {
                switch (selectedFilter) {
                    case "En attente":
                        return visite.getObservation() == null || visite.getObservation().isEmpty();
                    case "Conformes":
                        return visite.getObservation() != null && visite.getObservation().contains("CONFORME")
                                && !visite.getObservation().contains("NON");
                    case "Non conformes":
                        return visite.getObservation() != null && visite.getObservation().contains("NON CONFORME");
                    case "Expirent bientôt":
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime expiration = visite.getDateSortie();
                        return expiration != null && expiration.isAfter(now) &&
                                expiration.isBefore(now.plusDays(30));
                }
            }

            return true;
        });
    }

    private void loadVisitesFromDatabase() {
        visitesData.clear();

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
                visitesData.add(visite);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors du chargement des visites techniques", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateExpiringCount() {
        long count = visitesData.stream()
                .filter(visite -> {
                    LocalDateTime expiration = visite.getDateSortie();
                    LocalDateTime now = LocalDateTime.now();
                    return expiration != null && expiration.isAfter(now) &&
                            expiration.isBefore(now.plusDays(30));
                })
                .count();

        expiringVisitesCount.setText(String.valueOf(count));
    }

    @FXML
    public void showAddVisiteTechnique() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addVisitesTechniques.fxml"));
            Parent root = loader.load();

            addVisitesTechniqueView controller = loader.getController();
            controller.setOnSaveCallback(() -> {
                loadVisitesFromDatabase();
                updateExpiringCount();
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle visite technique");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    private void editVisite(VisitesTechniques visite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addVisitesTechniques.fxml"));
            Parent root = loader.load();

            addVisitesTechniqueView controller = loader.getController();
            controller.prepareEditForm(visite);
            controller.setOnSaveCallback(() -> {
                loadVisitesFromDatabase();
                updateExpiringCount();
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier visite technique");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger le formulaire de modification", e.getMessage());
        }
    }

    private void deleteVisite(VisitesTechniques visite) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Supprimer la visite technique");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer cette visite technique ?");

        ButtonType buttonTypeOui = new ButtonType("Oui");
        ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        confirmDialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeOui) {
                try {
                    Connection conn = DataBase.getInstance().getConnection();

                    // Remettre le véhicule en état disponible si nécessaire
                    String updateVehicule = "UPDATE vehicules SET ID_ETAT_VOITURE = " +
                            "(SELECT ID_ETAT_VOITURE FROM etat_voiture WHERE LIB_ETAT_VOITURE = 'Disponible') " +
                            "WHERE ID_VEHICULE = ? AND ID_ETAT_VOITURE = " +
                            "(SELECT ID_ETAT_VOITURE FROM etat_voiture WHERE LIB_ETAT_VOITURE = 'En entretien')";
                    PreparedStatement pstmt1 = conn.prepareStatement(updateVehicule);
                    pstmt1.setInt(1, visite.getVehiculeId());
                    pstmt1.executeUpdate();

                    // Supprimer la visite
                    String deleteQuery = "DELETE FROM entretien WHERE ID_ENTRETIEN = ?";
                    PreparedStatement pstmt2 = conn.prepareStatement(deleteQuery);
                    pstmt2.setInt(1, visite.getId());
                    int rowsAffected = pstmt2.executeUpdate();

                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès",
                                "Visite supprimée", "La visite technique a été supprimée avec succès.");
                        loadVisitesFromDatabase();
                        updateExpiringCount();
                    }

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                            "Impossible de supprimer la visite", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void exportToPDF() {
        showAlert(Alert.AlertType.INFORMATION, "Export PDF",
                "Fonction en développement", "L'export en PDF sera disponible prochainement.");
    }

    @FXML
    public void notifierVisites() {
        showAlert(Alert.AlertType.INFORMATION, "Notification",
                "Fonction en développement", "Les notifications seront disponibles prochainement.");
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}
