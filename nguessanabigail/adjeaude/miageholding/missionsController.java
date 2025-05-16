package nguessanabigail.adjeaude.miageholding;

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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class missionsController implements Initializable {
    @FXML private TableView<Mission> missionsTable;
    //@FXML private TableColumn<Mission, Integer> idCol;
    @FXML private TableColumn<Mission, String> libelleCol;
    @FXML private TableColumn<Mission, String> vehiculeCol;
    @FXML private TableColumn<Mission, LocalDateTime> dateDebutCol;
    @FXML private TableColumn<Mission, LocalDateTime> dateFinCol;
    @FXML private TableColumn<Mission, Integer> coutMissionCol;
    @FXML private TableColumn<Mission, Integer> coutCarburantCol;
    @FXML private TableColumn<Mission, String> actionsCol;
    @FXML private TableColumn<Mission, String>StatutCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button btnAjouterMission;
    @FXML private Button btnExportPDF;
    @FXML private Button btnStats;
    @FXML private Label welcomeLabel;

    // ObservableList pour stocker les données des missions
    private ObservableList<Mission> missionsData = FXCollections.observableArrayList();
    private FilteredList<Mission> filteredData;
    private SortedList<Mission> sortedData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (missionsTable != null) {
            initializeMissionTable();
            setupFilters();
            loadMissionsFromDatabase();
        }
    }

    private void initializeMissionTable() {
        // Configuration des colonnes
       // idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        libelleCol.setCellValueFactory(new PropertyValueFactory<>("libelle"));
        vehiculeCol.setCellValueFactory(new PropertyValueFactory<>("vehiculeInfo"));
        dateDebutCol.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinCol.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        coutMissionCol.setCellValueFactory(new PropertyValueFactory<>("coutMission"));
        coutCarburantCol.setCellValueFactory(new PropertyValueFactory<>("coutCarburant"));

        // Ajout de la colonne statut
        TableColumn<Mission, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
        missionsTable.getColumns().add(7, statutCol);

        // Formatage des dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        dateDebutCol.setCellFactory(column -> new TableCell<Mission, LocalDateTime>() {
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

        dateFinCol.setCellFactory(column -> new TableCell<Mission, LocalDateTime>() {
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
        coutMissionCol.setCellFactory(column -> new TableCell<Mission, Integer>() {
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

        coutCarburantCol.setCellFactory(column -> new TableCell<Mission, Integer>() {
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
        actionsCol.setCellFactory(param -> new TableCell<Mission, String>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            //private final Button detailsBtn = new Button("Détails");

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
               // detailsBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

                editBtn.setOnAction(event -> {
                    Mission mission = getTableView().getItems().get(getIndex());
                    editMission(mission);
                });

                deleteBtn.setOnAction(event -> {
                    Mission mission = getTableView().getItems().get(getIndex());
                    deleteMission(mission);
                });

//                detailsBtn.setOnAction(event -> {
//                    Mission mission = getTableView().getItems().get(getIndex());
//                    showMissionDetails(mission);
//                });
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
        // Configuration des filtres
        filterComboBox.setItems(FXCollections.observableArrayList(
                "Toutes", "En cours", "Planifiées", "Terminées"
        ));
        filterComboBox.getSelectionModel().selectFirst();

        // Créer FilteredList
        filteredData = new FilteredList<>(missionsData, p -> true);

        // Listener pour le champ de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateFilter();
        });

        // Listener pour le filtre ComboBox
        filterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateFilter();
        });

        // Créer SortedList
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(missionsTable.comparatorProperty());

        // Assigner à la table
        missionsTable.setItems(sortedData);
    }

    private void updateFilter() {
        filteredData.setPredicate(mission -> {
            // Filtre de recherche
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!mission.getLibelle().toLowerCase().contains(lowerCaseFilter) &&
                        //!mission.getVehiculeInfo().toLowerCase().contains(lowerCaseFilter) &&
                        !mission.getCircuit().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }

            // Filtre par statut
            String selectedFilter = filterComboBox.getValue();
            if (selectedFilter != null && !selectedFilter.equals("Toutes")) {
                //String statut = mission.getStatut();
                //return statut.equals(selectedFilter);
            }

            return true;
        });
    }

    private void loadMissionsFromDatabase() {
        missionsData.clear();

        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "SELECT m.*, v.MARQUE, v.MODELE, v.IMMATRICULATION " +
                    "FROM mission m " +
                    "JOIN vehicules v ON m.ID_VEHICULE = v.ID_VEHICULE " +
                    "ORDER BY m.DATE_DEBUT_MISSION DESC";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String vehiculeInfo = rs.getString("MARQUE") + " " +
                        rs.getString("MODELE") + " - " +
                        rs.getString("IMMATRICULATION");

                Mission mission = new Mission(
                        rs.getInt("ID_MISSION"),
                        rs.getInt("ID_VEHICULE"),
                        rs.getString("LIB_MISSION"),
                        rs.getDate("DATE_DEBUT_MISSION").toLocalDate().atStartOfDay(),
                        rs.getDate("DATE_FIN_MISSION").toLocalDate().atStartOfDay(),
                        rs.getInt("COUT_MISSION"),
                        rs.getInt("COUT_CARBURANT"),
                        rs.getString("OBSERVATION_MISSION"),
                        rs.getString("CIRCUIT_MISSION"),
                        vehiculeInfo
                );
                missionsData.add(mission);
            }

            // Afficher un message de succès
            if (welcomeLabel != null) {
                welcomeLabel.setText(missionsData.size() + " missions chargées");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL",
                    "Erreur lors du chargement des missions", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur générale lors du chargement", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void showAddMissionForm() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addMissions.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            addMissionController controller = loader.getController();
            controller.setOnSaveCallback(() -> loadMissionsFromDatabase());

            Stage stage = new Stage();
            stage.setTitle("Nouvelle mission");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout de mission", e.getMessage());
        }
    }

    private void editMission(Mission mission) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addMissions.fxml"));
            Parent root = loader.load();

            // Configurer le contrôleur
            addMissionController controller = loader.getController();
            controller.prepareEditForm(mission);
            controller.setOnSaveCallback(() -> loadMissionsFromDatabase());

            Stage stage = new Stage();
            stage.setTitle("Modifier mission - " + mission.getLibelle());
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger le formulaire de modification", e.getMessage());
        }
    }

    private void deleteMission(Mission mission) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Supprimer la mission");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer la mission \"" +
                mission.getLibelle() + "\" ?");

        ButtonType buttonTypeOui = new ButtonType("Oui");
        ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        confirmDialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeOui) {
                try {
                    Connection conn = DataBase.getInstance().getConnection();

                    // D'abord supprimer les participations
                    String deleteParticipations = "DELETE FROM participer WHERE ID_MISSION = ?";
                    PreparedStatement pstmt1 = conn.prepareStatement(deleteParticipations);
                    pstmt1.setInt(1, mission.getId());
                    pstmt1.executeUpdate();

                    // Remettre le véhicule en état disponible
                    String updateVehicule = "UPDATE vehicules SET ID_ETAT_VOITURE = " +
                            "(SELECT ID_ETAT_VOITURE FROM etat_voiture WHERE LIB_ETAT_VOITURE = 'Disponible') " +
                            "WHERE ID_VEHICULE = ?";
                    PreparedStatement pstmt2 = conn.prepareStatement(updateVehicule);
                    pstmt2.setInt(1, mission.getVehiculeId());
                    pstmt2.executeUpdate();

                    // Ensuite supprimer la mission
                    String deleteMission = "DELETE FROM mission WHERE ID_MISSION = ?";
                    PreparedStatement pstmt3 = conn.prepareStatement(deleteMission);
                    pstmt3.setInt(1, mission.getId());
                    int rowsAffected = pstmt3.executeUpdate();

                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès",
                                "Mission supprimée", "La mission a été supprimée avec succès.");
                        loadMissionsFromDatabase();
                    }

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                            "Impossible de supprimer la mission", e.getMessage());
                }
            }
        });
    }

    private void showMissionDetails(Mission mission) {
        try {
            String participants = getParticipants(mission.getId());

            Alert detailsDialog = new Alert(Alert.AlertType.INFORMATION);
            detailsDialog.setTitle("Détails de la mission");
            detailsDialog.setHeaderText(mission.getLibelle());

            String content = String.format(
                    "ID: %d\n" +
                            "Véhicule: %s\n" +
                            "Circuit: %s\n" +
                            "Date début: %s\n" +
                            "Date fin: %s\n" +
                            "Coût mission: %,d FCFA\n" +
                            "Coût carburant: %,d FCFA\n" +
                            "Coût total: %,d FCFA\n" +
                            "Statut: %s\n" +
                            "Observations: %s\n" +
                            "Participants: %s",
                    mission.getId(),
                    mission.getVehiculeInfo(),
                    mission.getCircuit(),
                    mission.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    mission.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    mission.getCoutMission(),
                    mission.getCoutCarburant(),
                    mission.getCoutMission() + mission.getCoutCarburant(),
                   // mission.getStatut(),
                    mission.getObservation().isEmpty() ? "(Aucune)" : mission.getObservation(),
                    participants.isEmpty() ? "(Aucun)" : participants
            );

            detailsDialog.setContentText(content);
            detailsDialog.getDialogPane().setPrefWidth(600);
            detailsDialog.showAndWait();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'afficher les détails", e.getMessage());
        }
    }

    private String getParticipants(int missionId) {
        StringBuilder participants = new StringBuilder();
        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "SELECT p.NOM_PERSONNEL, p.PRENOM_PERSONNEL " +
                    "FROM personnel p " +
                    "JOIN participer pt ON p.ID_PERSONNEL = pt.ID_PERSONNEL " +
                    "WHERE pt.ID_MISSION = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, missionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (participants.length() > 0) {
                    participants.append(", ");
                }
                participants.append(rs.getString("PRENOM_PERSONNEL"))
                        .append(" ")
                        .append(rs.getString("NOM_PERSONNEL"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants.toString();
    }

    @FXML
    public void exportToPDF() {
        showAlert(Alert.AlertType.INFORMATION, "Export PDF",
                "Fonction en développement", "L'export en PDF sera disponible prochainement.");
    }

    @FXML
    public void showStatistics() {
        try {
            // Calculer quelques statistiques basiques
            int totalMissions = missionsData.size();
            int missionsEnCours = 0;
            int missionsPlanifiees = 0;
            int missionsTerminees = 0;
            double coutTotalMissions = 0;
            double coutTotalCarburant = 0;

            for (Mission m : missionsData) {
                String statut = m.getStatut();
                if (statut.equals("En cours")) missionsEnCours++;
                else if (statut.equals("Planifiées")) missionsPlanifiees++;
                else if (statut.equals("Terminées")) missionsTerminees++;

                coutTotalMissions += m.getCoutMission();
                coutTotalCarburant += m.getCoutCarburant();
            }

            Alert statsDialog = new Alert(Alert.AlertType.INFORMATION);
            statsDialog.setTitle("Statistiques des missions");
            statsDialog.setHeaderText("Résumé des missions");

            String content = String.format(
                    "Total missions: %d\n" +
                            "Missions en cours: %d\n" +
                            "Missions planifiées: %d\n" +
                            "Missions terminées: %d\n\n" +
                            "Coût total des missions: %,.0f FCFA\n" +
                            "Coût total carburant: %,.0f FCFA\n" +
                            "Coût total global: %,.0f FCFA\n\n" +
                            "Coût moyen par mission: %,.0f FCFA",
                    totalMissions,
                    missionsEnCours,
                    missionsPlanifiees,
                    missionsTerminees,
                    coutTotalMissions,
                    coutTotalCarburant,
                    coutTotalMissions + coutTotalCarburant,
                    totalMissions > 0 ? (coutTotalMissions + coutTotalCarburant) / totalMissions : 0
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
