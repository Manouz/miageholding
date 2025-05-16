package nguessanabigail.adjeaude.miageholding;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import javafx.scene.control.DateCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class addMissionController implements Initializable{

    // Champs du formulaire
    @FXML private TextField libMissionField;
    @FXML private TextArea observationArea;
    @FXML private TextField circuitField;
    @FXML private ComboBox<Vehicule> vehiculeComboBox;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField coutMissionField;
    @FXML private TextField coutCarburantField;
    @FXML private ListView<Personnel> personnelListView;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ObservableList<Vehicule> vehiculesDisponibles = FXCollections.observableArrayList();
    private ObservableList<Personnel> personnelList = FXCollections.observableArrayList();

    // Mission en cours de modification (null si nouvelle mission)
    private Mission missionEnCours = null;

    // Callback pour rafraîchir la table après sauvegarde
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Initialiser les ComboBox et ListView
            initializeVehiculesComboBox();
            initializePersonnelListView();

            // Configurer les DatePickers
            configureDatePickers();

            // Configurer les champs numériques
            configureNumericFields();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Erreur lors de l'initialisation du formulaire", e.getMessage());
            e.printStackTrace();
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void initializeVehiculesComboBox() {
        try {
            vehiculesDisponibles.clear();
            Connection conn = DataBase.getInstance().getConnection();

            String query = "SELECT v.*, e.LIB_ETAT_VOITURE FROM vehicules v " +
                    "JOIN etat_voiture e ON v.ID_ETAT_VOITURE = e.ID_ETAT_VOITURE " +
                    "WHERE e.LIB_ETAT_VOITURE = 'Disponible'";

            // Si on est en mode édition, inclure aussi le véhicule actuel
            if (missionEnCours != null) {
                query = "SELECT v.*, e.LIB_ETAT_VOITURE FROM vehicules v " +
                        "JOIN etat_voiture e ON v.ID_ETAT_VOITURE = e.ID_ETAT_VOITURE " +
                        "WHERE e.LIB_ETAT_VOITURE = 'Disponible' OR v.ID_VEHICULE = " + missionEnCours.getVehiculeId();
            }

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Vehicule vehicule = new Vehicule(
                        rs.getInt("ID_VEHICULE"),
                        rs.getString("MARQUE"),
                        rs.getString("MODELE"),
                        rs.getString("IMMATRICULATION"),
                        rs.getInt("NB_PLACES"),
                        rs.getString("ENERGIE"),
                        rs.getString("LIB_ETAT_VOITURE"),
                        rs.getDate("DATE_ACQUISITION").toLocalDate(),
                        rs.getInt("PUISSANCE"),
                        rs.getString("COULEUR"),
                        rs.getInt("PRIX_VEHICULE")
                );
                vehiculesDisponibles.add(vehicule);
            }

            vehiculeComboBox.setItems(vehiculesDisponibles);

            // Configurer l'affichage du ComboBox
            vehiculeComboBox.setConverter(new StringConverter<Vehicule>() {
                @Override
                public String toString(Vehicule vehicule) {
                    if (vehicule == null) return "";
                    return vehicule.getMarque() + " " + vehicule.getModele() + " - " + vehicule.getImmatriculation();
                }

                @Override
                public Vehicule fromString(String string) {
                    return null;
                }
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les véhicules disponibles", e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializePersonnelListView() {
        try {
            personnelList.clear();
            Connection conn = DataBase.getInstance().getConnection();

            String query = "SELECT p.*, f.LIB_FONCTION, s.LIB_SERVICE FROM personnel p " +
                    "JOIN fonction f ON p.ID_FONCTION = f.ID_FONCTION " +
                    "JOIN service s ON p.ID_SERVICE = s.ID_SERVICE " +
                    "ORDER BY p.NOM_PERSONNEL, p.PRENOM_PERSONNEL";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Personnel personnel = new Personnel(
                        rs.getInt("ID_PERSONNEL"),
                        rs.getString("NOM_PERSONNEL"),
                        rs.getString("PRENOM_PERSONNEL"),
                        rs.getString("CONTACT_PERSONNEL"),
                        rs.getString("EMAIL_PERSONNEL"),
                        rs.getString("LIB_FONCTION"),
                        rs.getString("LIB_SERVICE")
                );
                personnelList.add(personnel);
            }

            personnelListView.setItems(personnelList);
            personnelListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Configurer l'affichage
            personnelListView.setCellFactory(new Callback<ListView<Personnel>, ListCell<Personnel>>() {
                @Override
                public ListCell<Personnel> call(ListView<Personnel> param) {
                    return new ListCell<Personnel>() {
                        @Override
                        protected void updateItem(Personnel item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getPrenom() + " " + item.getNom() + " - " +
                                        item.getFonction() + " (" + item.getService() + ")");
                            }
                        }
                    };
                }
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger le personnel", e.getMessage());
            e.printStackTrace();
        }
    }

    private void configureDatePickers() {
        // Configurer les DatePickers pour accepter seulement les dates futures
        Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isBefore(LocalDate.now()) && missionEnCours == null) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        };

        dateDebutPicker.setDayCellFactory(dayCellFactory);
        dateFinPicker.setDayCellFactory(dayCellFactory);

        // Listener pour valider que la date de fin est après la date de début
        dateDebutPicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && dateFinPicker.getValue() != null) {
                if (dateFinPicker.getValue().isBefore(newDate)) {
                    dateFinPicker.setValue(newDate);
                }
            }
        });

        // Configurer la date de fin pour qu'elle ne puisse pas être avant la date de début
        dateFinPicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                LocalDate dateDebut = dateDebutPicker.getValue();
                if (dateDebut != null && item.isBefore(dateDebut)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });
    }

    private void configureNumericFields() {
        // Configurer les champs numériques pour accepter seulement des nombres
        coutMissionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                coutMissionField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        coutCarburantField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                coutCarburantField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void saveMission(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            Connection conn = DataBase.getInstance().getConnection();

            if (missionEnCours == null) {
                // Nouvelle mission
                insertNewMission(conn);
            } else {
                // Mise à jour d'une mission existante
                updateExistingMission(conn);
            }

            // Appeler le callback pour rafraîchir la table
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            closeForm(event);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Erreur lors de l'enregistrement de la mission", e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertNewMission(Connection conn) throws SQLException {
        int missionId = generateMissionId(conn);

        String query = "INSERT INTO mission (ID_MISSION, ID_VEHICULE, LIB_MISSION, " +
                "DATE_DEBUT_MISSION, DATE_FIN_MISSION, COUT_MISSION, COUT_CARBURANT, " +
                "OBSERVATION_MISSION, CIRCUIT_MISSION) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, missionId);
        pstmt.setInt(2, vehiculeComboBox.getValue().getId());
        pstmt.setString(3, libMissionField.getText());
        pstmt.setTimestamp(4, Timestamp.valueOf(dateDebutPicker.getValue().atStartOfDay()));
        pstmt.setTimestamp(5, Timestamp.valueOf(dateFinPicker.getValue().atTime(23, 59, 59)));
        pstmt.setInt(6, Integer.parseInt(coutMissionField.getText()));
        pstmt.setInt(7, Integer.parseInt(coutCarburantField.getText()));
        pstmt.setString(8, observationArea.getText());
        pstmt.setString(9, circuitField.getText());

        pstmt.executeUpdate();

        // Ajouter les participants
        addParticipants(conn, missionId);

        // Mettre à jour l'état du véhicule
        updateVehiculeEtat(conn, vehiculeComboBox.getValue().getId(), "En mission");

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Mission ajoutée avec succès", "ID de la mission: " + missionId);
    }

    private void updateExistingMission(Connection conn) throws SQLException {
        String query = "UPDATE mission SET ID_VEHICULE = ?, LIB_MISSION = ?, " +
                "DATE_DEBUT_MISSION = ?, DATE_FIN_MISSION = ?, COUT_MISSION = ?, " +
                "COUT_CARBURANT = ?, OBSERVATION_MISSION = ?, CIRCUIT_MISSION = ? " +
                "WHERE ID_MISSION = ?";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, vehiculeComboBox.getValue().getId());
        pstmt.setString(2, libMissionField.getText());
        pstmt.setTimestamp(3, Timestamp.valueOf(dateDebutPicker.getValue().atStartOfDay()));
        pstmt.setTimestamp(4, Timestamp.valueOf(dateFinPicker.getValue().atTime(23, 59, 59)));
        pstmt.setInt(5, Integer.parseInt(coutMissionField.getText()));
        pstmt.setInt(6, Integer.parseInt(coutCarburantField.getText()));
        pstmt.setString(7, observationArea.getText());
        pstmt.setString(8, circuitField.getText());
        pstmt.setInt(9, missionEnCours.getId());

        pstmt.executeUpdate();

        // Supprimer les anciens participants et ajouter les nouveaux
        removeParticipants(conn, missionEnCours.getId());
        addParticipants(conn, missionEnCours.getId());

        // Si le véhicule a changé, mettre à jour les états
        if (vehiculeComboBox.getValue().getId() != missionEnCours.getVehiculeId()) {
            updateVehiculeEtat(conn, missionEnCours.getVehiculeId(), "Disponible");
            updateVehiculeEtat(conn, vehiculeComboBox.getValue().getId(), "En mission");
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Mission modifiée avec succès", "");
    }

    private void addParticipants(Connection conn, int missionId) throws SQLException {
        String query = "INSERT INTO participer (ID_PERSONNEL, ID_MISSION) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(query);

        ObservableList<Personnel> selectedItems = personnelListView.getSelectionModel().getSelectedItems();
        for (Personnel personnel : selectedItems) {
            pstmt.setInt(1, personnel.getId());
            pstmt.setInt(2, missionId);
            pstmt.executeUpdate();
        }
    }

    private void removeParticipants(Connection conn, int missionId) throws SQLException {
        String query = "DELETE FROM participer WHERE ID_MISSION = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, missionId);
        pstmt.executeUpdate();
    }

    private void updateVehiculeEtat(Connection conn, int vehiculeId, String etat) throws SQLException {
        String query = "UPDATE vehicules SET ID_ETAT_VOITURE = " +
                "(SELECT ID_ETAT_VOITURE FROM etat_voiture WHERE LIB_ETAT_VOITURE = ?), " +
                "DATE_ETAT = ? WHERE ID_VEHICULE = ?";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, etat);
        pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        pstmt.setInt(3, vehiculeId);
        pstmt.executeUpdate();
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (libMissionField.getText().trim().isEmpty()) {
            errors.append("- Le libellé de la mission est obligatoire.\n");
        }

        if (vehiculeComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un véhicule.\n");
        }

        if (dateDebutPicker.getValue() == null) {
            errors.append("- La date de début est obligatoire.\n");
        }

        if (dateFinPicker.getValue() == null) {
            errors.append("- La date de fin est obligatoire.\n");
        }

        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null) {
            if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
                errors.append("- La date de fin doit être après la date de début.\n");
            }
        }

        if (coutMissionField.getText().trim().isEmpty()) {
            errors.append("- Le coût de la mission est obligatoire.\n");
        }

        if (coutCarburantField.getText().trim().isEmpty()) {
            errors.append("- Le coût du carburant est obligatoire.\n");
        }

        if (circuitField.getText().trim().isEmpty()) {
            errors.append("- Le circuit de la mission est obligatoire.\n");
        }

        if (personnelListView.getSelectionModel().getSelectedItems().isEmpty()) {
            errors.append("- Veuillez sélectionner au moins un participant.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez corriger les erreurs suivantes:", errors.toString());
            return false;
        }

        return true;
    }

    private int generateMissionId(Connection conn) throws SQLException {
        String query = "SELECT MAX(ID_MISSION) as MAX_ID FROM mission";
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        int maxId = 0;
        if (rs.next()) {
            maxId = rs.getInt("MAX_ID");
        }

        return maxId + 1;
    }

    @FXML
    private void closeForm(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Méthode pour préparer le formulaire en mode édition
    public void prepareEditForm(Mission mission) {
        missionEnCours = mission;

        // Recharger les véhicules pour inclure le véhicule de la mission actuelle
        initializeVehiculesComboBox();

        // Remplir les champs avec les données de la mission
        libMissionField.setText(mission.getLibelle());
        observationArea.setText(mission.getObservation());
        circuitField.setText(mission.getCircuit());
        dateDebutPicker.setValue(mission.getDateDebut().toLocalDate());
        dateFinPicker.setValue(mission.getDateFin().toLocalDate());
        coutMissionField.setText(String.valueOf(mission.getCoutMission()));
        coutCarburantField.setText(String.valueOf(mission.getCoutCarburant()));

        // Sélectionner le véhicule
        for (Vehicule v : vehiculesDisponibles) {
            if (v.getId() == mission.getVehiculeId()) {
                vehiculeComboBox.setValue(v);
                break;
            }
        }

        // Sélectionner les participants
        loadParticipants(mission.getId());

        // Changer le texte du bouton
        saveButton.setText("Mettre à jour");
    }

    private void loadParticipants(int missionId) {
        try {
            Connection conn = DataBase.getInstance().getConnection();
            String query = "SELECT ID_PERSONNEL FROM participer WHERE ID_MISSION = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, missionId);
            ResultSet rs = pstmt.executeQuery();

            List<Integer> participantIds = new ArrayList<>();
            while (rs.next()) {
                participantIds.add(rs.getInt("ID_PERSONNEL"));
            }

            // Sélectionner les participants dans la ListView
            personnelListView.getSelectionModel().clearSelection();
            for (int i = 0; i < personnelList.size(); i++) {
                if (participantIds.contains(personnelList.get(i).getId())) {
                    personnelListView.getSelectionModel().select(i);
                }
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les participants", e.getMessage());
        }
    }
}



