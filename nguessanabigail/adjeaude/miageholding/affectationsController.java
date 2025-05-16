package nguessanabigail.adjeaude.miageholding;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
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

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class affectationsController implements Initializable{
    @FXML
    private TableView<Affectation> affectationsTable;
    @FXML
    private TableColumn<Affectation, Integer> idCol;
    @FXML
    private TableColumn<Affectation, String> vehiculeCol;
    @FXML
    private TableColumn<Affectation, String> beneficiaireCol;
    @FXML
    private TableColumn<Affectation, String> typeCol;
    @FXML
    private TableColumn<Affectation, LocalDateTime> dateAffectationCol;
    @FXML
    private TableColumn<Affectation, LocalDateTime> dateRetourPrevueCol;
    @FXML
    private TableColumn<Affectation, LocalDateTime> dateRetourReelleCol;
    @FXML
    private TableColumn<Affectation, String> statutCol;
    @FXML
    private TableColumn<Affectation, String> actionsCol;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private ComboBox<String> filterStatutComboBox;
    @FXML
    private Button btnNouvelleAffectation;
    @FXML
    private Button btnExportPDF;
    @FXML
    private Button btnStats;
    @FXML
    private Label affectationsEnCoursCount;

    private ObservableList<Affectation> affectationsData = FXCollections.observableArrayList();
    private FilteredList<Affectation> filteredData;
    private SortedList<Affectation> sortedData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeTable();
        setupFilters();
        loadAffectationsFromDatabase();
        updateCounts();
    }

    private void initializeTable() {
        // Configuration des colonnes

        vehiculeCol.setCellValueFactory(new PropertyValueFactory<>("vehiculeInfo"));

        // Colonne bénéficiaire (personnel ou mission)
        beneficiaireCol.setCellValueFactory(cellData -> {
            Affectation affectation = cellData.getValue();
            if ("Permanente".equals(affectation.getTypeAffectation())) {
                return affectation.personnelInfoProperty();
            } else {
                return affectation.missionInfoProperty();
            }
        });

        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeAffectation"));
        dateAffectationCol.setCellValueFactory(new PropertyValueFactory<>("dateAffectation"));
        dateRetourPrevueCol.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        dateRetourReelleCol.setCellValueFactory(new PropertyValueFactory<>("dateRetourReelle"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formatage des dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        dateAffectationCol.setCellFactory(column -> createDateCell(formatter));
        dateRetourPrevueCol.setCellFactory(column -> createDateCell(formatter));
        dateRetourReelleCol.setCellFactory(column -> createDateCell(formatter));

        // Formatage du statut avec couleurs
        statutCol.setCellFactory(column -> new TableCell<Affectation, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    switch (statut) {
                        case "En cours":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "Terminée":
                            setStyle("-fx-text-fill: #3498db;");
                            break;
                        case "Annulée":
                            setStyle("-fx-text-fill: #e74c3c;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Colonne actions avec boutons
        actionsCol.setCellFactory(param -> new TableCell<Affectation, String>() {
            private final Button editBtn = new Button("Modifier");
            private final Button returnBtn = new Button("Retour");
            private final Button cancelBtn = new Button("Annuler");

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                returnBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Affectation affectation = getTableView().getItems().get(getIndex());
                    editAffectation(affectation);
                });

                returnBtn.setOnAction(event -> {
                    Affectation affectation = getTableView().getItems().get(getIndex());
                    handleRetourVehicule(affectation);
                });

                cancelBtn.setOnAction(event -> {
                    Affectation affectation = getTableView().getItems().get(getIndex());
                    cancelAffectation(affectation);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Affectation affectation = getTableView().getItems().get(getIndex());
                    HBox buttons = new HBox(5);

                    if ("En cours".equals(affectation.getStatut())) {
                        buttons.getChildren().addAll(editBtn, returnBtn, cancelBtn);
                    } else {
                        buttons.getChildren().add(editBtn);
                    }

                    setGraphic(buttons);
                }
            }
        });
    }

    private TableCell<Affectation, LocalDateTime> createDateCell(DateTimeFormatter formatter) {
        return new TableCell<Affectation, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        };
    }

    private void setupFilters() {
        // ComboBox pour filtrer par type
        filterTypeComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "Permanente", "Mission"
        ));
        filterTypeComboBox.getSelectionModel().selectFirst();

        // ComboBox pour filtrer par statut
        filterStatutComboBox.setItems(FXCollections.observableArrayList(
                "Tous", "En cours", "Terminée", "Annulée"
        ));
        filterStatutComboBox.getSelectionModel().selectFirst();

        // Créer FilteredList
        filteredData = new FilteredList<>(affectationsData, p -> true);

        // Listener pour le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> updateFilter());

        // Listeners pour les ComboBox
        filterTypeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());
        filterStatutComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateFilter());

        // Créer SortedList
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(affectationsTable.comparatorProperty());

        // Assigner à la table
        affectationsTable.setItems(sortedData);
    }

    private void updateFilter() {
        filteredData.setPredicate(affectation -> {
            // Filtre de recherche
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!affectation.getVehiculeInfo().toLowerCase().contains(lowerCaseFilter) &&
                        !affectation.getPersonnelInfo().toLowerCase().contains(lowerCaseFilter) &&
                        !affectation.getMissionInfo().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }

            // Filtre par type
            String selectedType = filterTypeComboBox.getValue();
            if (selectedType != null && !selectedType.equals("Tous")) {
                if (!affectation.getTypeAffectation().equals(selectedType)) {
                    return false;
                }
            }

            // Filtre par statut
            String selectedStatut = filterStatutComboBox.getValue();
            if (selectedStatut != null && !selectedStatut.equals("Tous")) {
                if (!affectation.getStatut().equals(selectedStatut)) {
                    return false;
                }
            }

            return true;
        });
        updateCounts();
    }

    private void loadAffectationsFromDatabase() {
        affectationsData.clear();

        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query =
                    "SELECT a.*, " +
                            "v.MARQUE, v.MODELE, v.IMMATRICULATION, " +
                            "p.NOM_PERSONNEL, p.PRENOM_PERSONNEL, " +
                            "m.LIB_MISSION " +
                            "FROM affectations a " +
                            "JOIN vehicules v ON a.ID_VEHICULE = v.ID_VEHICULE " +
                            "LEFT JOIN personnel p ON a.ID_PERSONNEL = p.ID_PERSONNEL " +
                            "LEFT JOIN mission m ON a.ID_MISSION = m.ID_MISSION " +
                            "ORDER BY a.DATE_AFFECTATION DESC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Affectation affectation = new Affectation(
                        rs.getInt("ID_AFFECTATION"),
                        rs.getInt("ID_VEHICULE"),
                        rs.getObject("ID_PERSONNEL", Integer.class),
                        rs.getObject("ID_MISSION", Integer.class),
                        rs.getString("TYPE_AFFECTATION"),
                        rs.getTimestamp("DATE_AFFECTATION").toLocalDateTime(),
                        rs.getTimestamp("DATE_RETOUR_PREVUE") != null ?
                                rs.getTimestamp("DATE_RETOUR_PREVUE").toLocalDateTime() : null,
                        rs.getTimestamp("DATE_RETOUR_REELLE") != null ?
                                rs.getTimestamp("DATE_RETOUR_REELLE").toLocalDateTime() : null,
                        rs.getString("STATUT"),
                        rs.getString("COMMENTAIRE"),
                        rs.getString("AGENT_LOGISTIQUE")
                );

                // Informations du véhicule
                String vehiculeInfo = rs.getString("MARQUE") + " " +
                        rs.getString("MODELE") + " - " +
                        rs.getString("IMMATRICULATION");
                affectation.setVehiculeInfo(vehiculeInfo);

                // Informations du personnel ou de la mission
                if (affectation.getTypeAffectation().equals("Permanente")) {
                    String personnelInfo = rs.getString("NOM_PERSONNEL") + " " +
                            rs.getString("PRENOM_PERSONNEL");
                    affectation.setPersonnelInfo(personnelInfo);
                } else {
                    affectation.setMissionInfo(rs.getString("LIB_MISSION"));
                }

                affectationsData.add(affectation);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors du chargement des affectations", e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateCounts() {
        long countEnCours = affectationsData.stream()
                .filter(a -> "En cours".equals(a.getStatut()))
                .count();
        affectationsEnCoursCount.setText(String.valueOf(countEnCours));
    }

    @FXML
    public void showAddAffectationForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addAffectationForm.fxml"));
            Parent root = loader.load();

            addAffectationsController controller = loader.getController();
            controller.setOnSaveCallback(() -> {
                loadAffectationsFromDatabase();
                updateCounts();
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle affectation");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'affectation", e.getMessage());
        }
    }

    private void editAffectation(Affectation affectation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addAffectationForm.fxml"));
            Parent root = loader.load();

            addAffectationsController controller = loader.getController();
            controller.prepareEditForm(affectation);
            controller.setOnSaveCallback(() -> {
                loadAffectationsFromDatabase();
                updateCounts();
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier affectation");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger le formulaire de modification", e.getMessage());
        }
    }

    private void handleRetourVehicule(Affectation affectation) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Retour de véhicule");
        dialog.setHeaderText("Enregistrer le retour du véhicule");
        dialog.setContentText("Commentaire (optionnel):");

        dialog.showAndWait().ifPresent(commentaire -> {
            try {
                Connection conn = DataBase.getInstance().getConnection();
                conn.setAutoCommit(false);

                // Mettre à jour l'affectation
                String updateAffectation =
                        "UPDATE affectations SET " +
                                "DATE_RETOUR_REELLE = ?, " +
                                "STATUT = 'Terminée', " +
                                "COMMENTAIRE = CONCAT(IFNULL(COMMENTAIRE, ''), '\n', ?) " +
                                "WHERE ID_AFFECTATION = ?";

                PreparedStatement pstmt1 = conn.prepareStatement(updateAffectation);
                pstmt1.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt1.setString(2, "Retour: " + commentaire);
                pstmt1.setInt(3, affectation.getId());
                pstmt1.executeUpdate();

                // Mettre à jour l'état du véhicule
                String updateVehicule =
                        "UPDATE vehicules SET " +
                                "ID_ETAT_VOITURE = (SELECT ID_ETAT_VOITURE FROM etat_voiture WHERE LIB_ETAT_VOITURE = 'Disponible'), " +
                                "DATE_ETAT = ? " +
                                "WHERE ID_VEHICULE = ?";

                PreparedStatement pstmt2 = conn.prepareStatement(updateVehicule);
                pstmt2.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt2.setInt(2, affectation.getVehiculeId());
                pstmt2.executeUpdate();

                conn.commit();
                conn.setAutoCommit(true);

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Retour enregistré", "Le véhicule est maintenant disponible.");
                loadAffectationsFromDatabase();
                updateCounts();

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Erreur lors de l'enregistrement du retour", e.getMessage());
            }
        });
    }

    private void cancelAffectation(Affectation affectation) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation d'annulation");
        confirmDialog.setHeaderText("Annuler l'affectation");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir annuler cette affectation ?");

        ButtonType buttonTypeOui = new ButtonType("Oui");
        ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        confirmDialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeOui) {
                try {
                    Connection conn = DataBase.getInstance().getConnection();
                    conn.setAutoCommit(false);

                    // Mettre à jour l'affectation
                    String updateAffectation =
                            "UPDATE affectations SET " +
                                    "STATUT = 'Annulée', " +
                                    "DATE_RETOUR_REELLE = ? " +
                                    "WHERE ID_AFFECTATION = ?";

                    PreparedStatement pstmt1 = conn.prepareStatement(updateAffectation);
                    pstmt1.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt1.setInt(2, affectation.getId());
                    pstmt1.executeUpdate();

                    // Mettre à jour l'état du véhicule
                    String updateVehicule =
                            "UPDATE vehicules SET " +
                                    "ID_ETAT_VOITURE = (SELECT ID_ETAT_VOITURE FROM etat_voiture WHERE LIB_ETAT_VOITURE = 'Disponible'), " +
                                    "DATE_ETAT = ? " +
                                    "WHERE ID_VEHICULE = ?";

                    PreparedStatement pstmt2 = conn.prepareStatement(updateVehicule);
                    pstmt2.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    pstmt2.setInt(2, affectation.getVehiculeId());
                    pstmt2.executeUpdate();

                    conn.commit();
                    conn.setAutoCommit(true);

                    showAlert(Alert.AlertType.INFORMATION, "Succès",
                            "Affectation annulée", "Le véhicule est maintenant disponible.");
                    loadAffectationsFromDatabase();
                    updateCounts();

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Erreur lors de l'annulation", e.getMessage());
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
    public void showStatistics() {
        try {
            int totalAffectations = affectationsData.size();
            long enCours = affectationsData.stream().filter(a -> "En cours".equals(a.getStatut())).count();
            long terminees = affectationsData.stream().filter(a -> "Terminée".equals(a.getStatut())).count();
            long annulees = affectationsData.stream().filter(a -> "Annulée".equals(a.getStatut())).count();
            long permanentes = affectationsData.stream().filter(a -> "Permanente".equals(a.getTypeAffectation())).count();
            long missions = affectationsData.stream().filter(a -> "Mission".equals(a.getTypeAffectation())).count();

            Alert statsDialog = new Alert(Alert.AlertType.INFORMATION);
            statsDialog.setTitle("Statistiques des affectations");
            statsDialog.setHeaderText("Résumé des affectations");

            String content = String.format(
                    "Total affectations: %d\n\n" +
                            "Par statut:\n" +
                            "- En cours: %d (%.1f%%)\n" +
                            "- Terminées: %d (%.1f%%)\n" +
                            "- Annulées: %d (%.1f%%)\n\n" +
                            "Par type:\n" +
                            "- Permanentes: %d (%.1f%%)\n" +
                            "- Missions: %d (%.1f%%)",
                    totalAffectations,
                    enCours, (enCours * 100.0) / totalAffectations,
                    terminees, (terminees * 100.0) / totalAffectations,
                    annulees, (annulees * 100.0) / totalAffectations,
                    permanentes, (permanentes * 100.0) / totalAffectations,
                    missions, (missions * 100.0) / totalAffectations
            );

            statsDialog.setContentText(content);
            statsDialog.getDialogPane().setPrefWidth(500);
            statsDialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'afficher les statistiques", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

}