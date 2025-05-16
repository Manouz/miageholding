package nguessanabigail.adjeaude.miageholding;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.Callback;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.Optional;

public class addAffectationsController implements Initializable{
    // Composants FXML
    @FXML private ComboBox<Vehicule> vehiculeComboBox;
    @FXML private RadioButton rbPermanente;
    @FXML private RadioButton rbMission;
    @FXML private ComboBox<Personnel> personnelComboBox;
    @FXML private ComboBox<Mission> missionComboBox;
    @FXML private DatePicker dateAffectationPicker;
    @FXML private DatePicker dateRetourPrevuePicker;
    @FXML private TextArea commentaireArea;
    @FXML private TextField agentLogistiqueField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    // Labels pour afficher/masquer selon le type
    @FXML private Label personnelLabel;
    @FXML private Label missionLabel;
    @FXML private Label dateRetourLabel;
    @FXML private VBox personnelContainer;
    @FXML private VBox missionContainer;
    @FXML private VBox dateRetourContainer;

    // Groupe de boutons radio
    private ToggleGroup typeGroup;

    // Listes de données
    private ObservableList<Vehicule> vehiculesDisponibles = FXCollections.observableArrayList();
    private ObservableList<Personnel> personnelList = FXCollections.observableArrayList();
    private ObservableList<Mission> missionsList = FXCollections.observableArrayList();

    // Données pour l'édition
    private Affectation affectationEnCours = null;
    private Runnable onSaveCallback;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            setupRadioButtons();
            initializeComboBoxes();
            configureDatePickers();
            setDefaultValues();

            // Charger les données
            loadVehiculesDisponibles();
            loadPersonnel();
            loadMissions();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation",
                    "Erreur lors de l'initialisation du formulaire", e.getMessage());
        }
    }

    /**
     * Configure le callback à appeler après la sauvegarde
     */
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    /**
     * Configuration des boutons radio
     */
    private void setupRadioButtons() {
        typeGroup = new ToggleGroup();
        rbPermanente.setToggleGroup(typeGroup);
        rbMission.setToggleGroup(typeGroup);
        rbPermanente.setSelected(true);

        // Listener pour changer l'affichage selon le type
        typeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == rbPermanente) {
                showPermanenteFields();
            } else if (newValue == rbMission) {
                showMissionFields();
            }
        });

        // Afficher les champs par défaut
        showPermanenteFields();
    }

    /**
     * Affiche les champs pour une affectation permanente
     */
    private void showPermanenteFields() {
        // Afficher le personnel
        personnelLabel.setVisible(true);
        personnelComboBox.setVisible(true);
        if (personnelContainer != null) personnelContainer.setVisible(true);

        // Masquer la mission et la date de retour
        missionLabel.setVisible(false);
        missionComboBox.setVisible(false);
        if (missionContainer != null) missionContainer.setVisible(false);

        dateRetourLabel.setVisible(false);
        dateRetourPrevuePicker.setVisible(false);
        if (dateRetourContainer != null) dateRetourContainer.setVisible(false);
    }

    /**
     * Affiche les champs pour une affectation mission
     */
    private void showMissionFields() {
        // Masquer le personnel
        personnelLabel.setVisible(false);
        personnelComboBox.setVisible(false);
        if (personnelContainer != null) personnelContainer.setVisible(false);

        // Afficher la mission et la date de retour
        missionLabel.setVisible(true);
        missionComboBox.setVisible(true);
        if (missionContainer != null) missionContainer.setVisible(true);

        dateRetourLabel.setVisible(true);
        dateRetourPrevuePicker.setVisible(true);
        if (dateRetourContainer != null) dateRetourContainer.setVisible(true);
    }

    /**
     * Initialise les ComboBox avec les convertisseurs
     */
    private void initializeComboBoxes() {
        // Convertisseur pour véhicules
        vehiculeComboBox.setConverter(new StringConverter<Vehicule>() {
            @Override
            public String toString(Vehicule vehicule) {
                if (vehicule == null) return "";
                return vehicule.getMarque() + " " + vehicule.getModele() +
                        " - " + vehicule.getImmatriculation();
            }

            @Override
            public Vehicule fromString(String string) {
                return vehiculesDisponibles.stream()
                        .filter(v -> (v.getMarque() + " " + v.getModele() + " - " + v.getImmatriculation()).equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Convertisseur pour personnel
        personnelComboBox.setConverter(new StringConverter<Personnel>() {
            @Override
            public String toString(Personnel personnel) {
                if (personnel == null) return "";
                return personnel.getNom() + " " + personnel.getPrenom() +
                        " (" + personnel.getFonction() + ")";
            }

            @Override
            public Personnel fromString(String string) {
                return null;
            }
        });

        // Convertisseur pour missions
        missionComboBox.setConverter(new StringConverter<Mission>() {
            @Override
            public String toString(Mission mission) {
                if (mission == null) return "";
                return mission.getLibelle();
            }

            @Override
            public Mission fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Configure les DatePickers
     */
    private void configureDatePickers() {
        // Factory pour désactiver les dates passées
        Callback<DatePicker, DateCell> dayCellFactory = dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // En mode création, désactiver les dates passées
                if (!isEditMode && date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        };

        dateAffectationPicker.setDayCellFactory(dayCellFactory);

        // Pour la date de retour, elle doit être après la date d'affectation
        dateRetourPrevuePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                LocalDate dateAffectation = dateAffectationPicker.getValue();
                if (dateAffectation != null && date.isBefore(dateAffectation)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // Listener pour ajuster automatiquement la date de retour selon la mission
        missionComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && rbMission.isSelected()) {
                // Définir automatiquement la date de retour à la fin de la mission
                dateRetourPrevuePicker.setValue(newVal.getDateFin().toLocalDate());
            }
        });

        // Listener pour valider que la date de retour est après la date d'affectation
        dateAffectationPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dateRetourPrevuePicker.getValue() != null) {
                if (dateRetourPrevuePicker.getValue().isBefore(newVal)) {
                    dateRetourPrevuePicker.setValue(newVal.plusDays(1));
                }
            }
        });
    }

    /**
     * Définit les valeurs par défaut
     */
    private void setDefaultValues() {
        // Date d'affectation par défaut : aujourd'hui
        dateAffectationPicker.setValue(LocalDate.now());

        // Agent logistique par défaut (à remplacer par l'utilisateur connecté)
        agentLogistiqueField.setText("Agent actuel");
    }

    /**
     * Charge les véhicules disponibles
     */
    private void loadVehiculesDisponibles() {
        try {
            vehiculesDisponibles.clear();
            Connection conn = DataBase.getInstance().getConnection();

            String query = "SELECT v.*, e.LIB_ETAT_VOITURE FROM vehicules v " +
                    "JOIN etat_voiture e ON v.ID_ETAT_VOITURE = e.ID_ETAT_VOITURE " +
                    "WHERE e.LIB_ETAT_VOITURE = 'Disponible'";

            // En mode édition, inclure aussi le véhicule actuel
            if (isEditMode && affectationEnCours != null) {
                query += " OR v.ID_VEHICULE = " + affectationEnCours.getVehiculeId();
            }

            query += " ORDER BY v.MARQUE, v.MODELE";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Date dateAcquisition = rs.getDate("DATE_ACQUISITION");
                LocalDate localDateAcquisition = dateAcquisition != null ? dateAcquisition.toLocalDate() : null;

                Vehicule vehicule = new Vehicule(
                        rs.getInt("ID_VEHICULE"),
                        rs.getString("MARQUE"),
                        rs.getString("MODELE"),
                        rs.getString("IMMATRICULATION"),
                        rs.getInt("NB_PLACES"),
                        rs.getString("ENERGIE"),
                        rs.getString("LIB_ETAT_VOITURE"),
                        localDateAcquisition,
                        rs.getInt("PUISSANCE"),
                        rs.getString("COULEUR"),
                        rs.getInt("PRIX_VEHICULE")
                );
                vehiculesDisponibles.add(vehicule);
            }

            vehiculeComboBox.setItems(vehiculesDisponibles);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les véhicules disponibles", e.getMessage());
        }
    }

    /**
     * Charge le personnel disponible
     */
    private void loadPersonnel() {
        try {
            personnelList.clear();
            Connection conn = DataBase.getInstance().getConnection();

            // Charger le personnel qui n'a pas déjà une affectation permanente en cours
            String query = "SELECT p.*, f.LIB_FONCTION, s.LIB_SERVICE FROM personnel p " +
                    "JOIN fonction f ON p.ID_FONCTION = f.ID_FONCTION " +
                    "JOIN service s ON p.ID_SERVICE = s.ID_SERVICE " +
                    "WHERE NOT EXISTS (" +
                    "    SELECT 1 FROM affectations a " +
                    "    WHERE a.ID_PERSONNEL = p.ID_PERSONNEL " +
                    "    AND a.TYPE_AFFECTATION = 'Permanente' " +
                    "    AND a.STATUT = 'En cours'" +
                    ")";

            // En mode édition, inclure le personnel actuel
            if (isEditMode && affectationEnCours != null && affectationEnCours.getPersonnelId() != null) {
                query += " OR p.ID_PERSONNEL = " + affectationEnCours.getPersonnelId();
            }

            query += " ORDER BY p.NOM_PERSONNEL, p.PRENOM_PERSONNEL";

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

            personnelComboBox.setItems(personnelList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger le personnel", e.getMessage());
        }
    }

    /**
     * Charge les missions disponibles
     */
    private void loadMissions() {
        try {
            missionsList.clear();
            Connection conn = DataBase.getInstance().getConnection();

            // Charger les missions en cours ou futures
            String query = "SELECT * FROM mission " +
                    "WHERE DATE_FIN_MISSION >= CURDATE() " +
                    "ORDER BY DATE_DEBUT_MISSION";

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Mission mission = new Mission(
                        rs.getInt("ID_MISSION"),
                        rs.getInt("ID_VEHICULE"),
                        rs.getString("LIB_MISSION"),
                        rs.getTimestamp("DATE_DEBUT_MISSION").toLocalDateTime(),
                        rs.getTimestamp("DATE_FIN_MISSION").toLocalDateTime(),
                        rs.getInt("COUT_MISSION"),
                        rs.getInt("COUT_CARBURANT"),
                        rs.getString("OBSERVATION_MISSION"),
                        rs.getString("CIRCUIT_MISSION")
                );
                missionsList.add(mission);
            }

            missionComboBox.setItems(missionsList);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les missions", e.getMessage());
        }
    }

    /**
     * Sauvegarde l'affectation
     */
    @FXML
    private void saveAffectation(ActionEvent event) {
        if (!validateForm()) {
            return;
        }

        try {
            Connection conn = DataBase.getInstance().getConnection();
            conn.setAutoCommit(false);

            try {
                if (isEditMode) {
                    updateAffectation(conn);
                } else {
                    createAffectation(conn);
                }

                conn.commit();

                // Appeler le callback
                if (onSaveCallback != null) {
                    onSaveCallback.run();
                }

                // Fermer la fenêtre
                closeForm(event);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de sauvegarde",
                    "Impossible de sauvegarder l'affectation", e.getMessage());
        }
    }

    /**
     * Crée une nouvelle affectation
     */
    private void createAffectation(Connection conn) throws SQLException {
        String query = "INSERT INTO affectations " +
                "(ID_VEHICULE, ID_PERSONNEL, ID_MISSION, TYPE_AFFECTATION, " +
                "DATE_AFFECTATION, DATE_RETOUR_PREVUE, STATUT, COMMENTAIRE, AGENT_LOGISTIQUE) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'En cours', ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(query);

        pstmt.setInt(1, vehiculeComboBox.getValue().getId());

        if (rbPermanente.isSelected()) {
            pstmt.setInt(2, personnelComboBox.getValue().getId());
            pstmt.setNull(3, Types.INTEGER);
            pstmt.setString(4, "Permanente");
            pstmt.setTimestamp(5, Timestamp.valueOf(dateAffectationPicker.getValue().atStartOfDay()));
            pstmt.setNull(6, Types.TIMESTAMP);
        } else {
            pstmt.setNull(2, Types.INTEGER);
            pstmt.setInt(3, missionComboBox.getValue().getId());
            pstmt.setString(4, "Mission");
            pstmt.setTimestamp(5, Timestamp.valueOf(dateAffectationPicker.getValue().atStartOfDay()));
            pstmt.setTimestamp(6, Timestamp.valueOf(dateRetourPrevuePicker.getValue().atTime(23, 59, 59)));
        }

        pstmt.setString(7, commentaireArea.getText());
        pstmt.setString(8, agentLogistiqueField.getText());

        pstmt.executeUpdate();

        // Mettre à jour l'état du véhicule
        updateVehiculeState(conn, vehiculeComboBox.getValue().getId(), "Affecté");

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Affectation créée", "L'affectation a été créée avec succès.");
    }

    /**
     * Met à jour une affectation existante
     */
    private void updateAffectation(Connection conn) throws SQLException {
        String query = "UPDATE affectations SET " +
                "ID_VEHICULE = ?, ID_PERSONNEL = ?, ID_MISSION = ?, " +
                "TYPE_AFFECTATION = ?, DATE_AFFECTATION = ?, " +
                "DATE_RETOUR_PREVUE = ?, COMMENTAIRE = ?, AGENT_LOGISTIQUE = ? " +
                "WHERE ID_AFFECTATION = ?";

        PreparedStatement pstmt = conn.prepareStatement(query);

        pstmt.setInt(1, vehiculeComboBox.getValue().getId());

        if (rbPermanente.isSelected()) {
            pstmt.setInt(2, personnelComboBox.getValue().getId());
            pstmt.setNull(3, Types.INTEGER);
            pstmt.setString(4, "Permanente");
            pstmt.setNull(6, Types.TIMESTAMP);
        } else {
            pstmt.setNull(2, Types.INTEGER);
            pstmt.setInt(3, missionComboBox.getValue().getId());
            pstmt.setString(4, "Mission");
            pstmt.setTimestamp(6, Timestamp.valueOf(dateRetourPrevuePicker.getValue().atTime(23, 59, 59)));
        }

        pstmt.setTimestamp(5, Timestamp.valueOf(dateAffectationPicker.getValue().atStartOfDay()));
        pstmt.setString(7, commentaireArea.getText());
        pstmt.setString(8, agentLogistiqueField.getText());
        pstmt.setInt(9, affectationEnCours.getId());

        pstmt.executeUpdate();

        // Si le véhicule a changé, mettre à jour les états
        if (vehiculeComboBox.getValue().getId() != affectationEnCours.getVehiculeId()) {
            updateVehiculeState(conn, affectationEnCours.getVehiculeId(), "Disponible");
            updateVehiculeState(conn, vehiculeComboBox.getValue().getId(), "Affecté");
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Affectation modifiée", "L'affectation a été mise à jour avec succès.");
    }

    /**
     * Met à jour l'état d'un véhicule
     */
    private void updateVehiculeState(Connection conn, int vehiculeId, String etat) throws SQLException {
        String query = "UPDATE vehicules SET " +
                "ID_ETAT_VOITURE = (SELECT ID_ETAT_VOITURE FROM etat_voiture WHERE LIB_ETAT_VOITURE = ?), " +
                "DATE_ETAT = ? " +
                "WHERE ID_VEHICULE = ?";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, etat);
        pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        pstmt.setInt(3, vehiculeId);
        pstmt.executeUpdate();
    }

    /**
     * Valide le formulaire
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Véhicule obligatoire
        if (vehiculeComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un véhicule.\n");
        }

        // Validation selon le type
        if (rbPermanente.isSelected()) {
            if (personnelComboBox.getValue() == null) {
                errors.append("- Veuillez sélectionner un membre du personnel.\n");
            }
        } else if (rbMission.isSelected()) {
            if (missionComboBox.getValue() == null) {
                errors.append("- Veuillez sélectionner une mission.\n");
            }
            if (dateRetourPrevuePicker.getValue() == null) {
                errors.append("- La date de retour prévue est obligatoire pour une mission.\n");
            }
        }

        // Date d'affectation obligatoire
        if (dateAffectationPicker.getValue() == null) {
            errors.append("- La date d'affectation est obligatoire.\n");
        }

        // Agent logistique obligatoire
        if (agentLogistiqueField.getText().trim().isEmpty()) {
            errors.append("- L'agent logistique est obligatoire.\n");
        }

        // Validation des dates
        if (dateAffectationPicker.getValue() != null && dateRetourPrevuePicker.getValue() != null) {
            if (dateRetourPrevuePicker.getValue().isBefore(dateAffectationPicker.getValue())) {
                errors.append("- La date de retour doit être après la date d'affectation.\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez corriger les erreurs suivantes:", errors.toString());
            return false;
        }

        return true;
    }

    /**
     * Ferme le formulaire
     */
    @FXML
    private void closeForm(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Prépare le formulaire pour l'édition
     */
    public void prepareEditForm(Affectation affectation) {
        this.affectationEnCours = affectation;
        this.isEditMode = true;

        // Recharger les données
        loadVehiculesDisponibles();
        loadPersonnel();
        loadMissions();

        // Sélectionner le véhicule
        for (Vehicule v : vehiculesDisponibles) {
            if (v.getId() == affectation.getVehiculeId()) {
                vehiculeComboBox.setValue(v);
                break;
            }
        }

        // Type d'affectation et bénéficiaire
        if ("Permanente".equals(affectation.getTypeAffectation())) {
            rbPermanente.setSelected(true);
            if (affectation.getPersonnelId() != null) {
                for (Personnel p : personnelList) {
                    if (p.getId() == affectation.getPersonnelId()) {
                        personnelComboBox.setValue(p);
                        break;
                    }
                }
            }
        } else {
            rbMission.setSelected(true);
            if (affectation.getMissionId() != null) {
                for (Mission m : missionsList) {
                    if (m.getId() == affectation.getMissionId()) {
                        missionComboBox.setValue(m);
                        break;
                    }
                }
            }
            if (affectation.getDateRetourPrevue() != null) {
                dateRetourPrevuePicker.setValue(affectation.getDateRetourPrevue().toLocalDate());
            }
        }

        // Autres champs
        dateAffectationPicker.setValue(affectation.getDateAffectation().toLocalDate());
        commentaireArea.setText(affectation.getCommentaire() != null ? affectation.getCommentaire() : "");
        agentLogistiqueField.setText(affectation.getAgentLogistique() != null ? affectation.getAgentLogistique() : "");

        // Changer le texte du bouton
        saveButton.setText("Mettre à jour");
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
