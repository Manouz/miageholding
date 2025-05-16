package nguessanabigail.adjeaude.miageholding;
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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.scene.control.DateCell;
import java.time.LocalDate;


public class addVehiculeFromController implements Initializable {

    @FXML private TextField immatriculationField;
    @FXML private TextField marqueField;
    @FXML private TextField modeleField;
    @FXML private TextField numeroChassiField;
    @FXML private ComboBox<String> etatComboBox;
    @FXML private TextField placesField;
    @FXML private ComboBox<String> energieComboBox;
    @FXML private DatePicker acquisitionDatePicker;
    @FXML private DatePicker amortissementDatePicker;
    @FXML private DatePicker miseEnServiceDatePicker;
    @FXML private TextField puissanceField;
    @FXML private TextField couleurField;
    @FXML private TextField prixField;
    @FXML private DatePicker dateEtatPicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    // Map pour stocker les ID d'état avec leur libellé
    private Map<String, Integer> etatVoitureMap = new HashMap<>();

    // Variable pour stocker l'ID du véhicule en mode édition
    private Integer currentVehicleId = null;

    // Callback pour rafraîchir la table après sauvegarde
    private Runnable onSaveCallback;

    // URL de la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/miageholding";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeComboBoxes();
        setupDateValidation();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void initializeComboBoxes() {
        System.out.println("Initialisation des ComboBox...");

        // Initialiser la ComboBox des états de voiture depuis la base de données
        loadEtatVoitureFromDatabase();

        // Initialiser la ComboBox des types d'énergie
        ObservableList<String> energieTypes = FXCollections.observableArrayList(
                "Essence", "Diesel", "Électrique", "Hybride", "GPL", "GNV");
        energieComboBox.setItems(energieTypes);
    }

    private void loadEtatVoitureFromDatabase() {
        System.out.println("Chargement des états de voiture depuis la base de données");

        ObservableList<String> etats = FXCollections.observableArrayList();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            DataBase database = DataBase.getInstance();
            conn = database.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT ID_ETAT_VOITURE, LIB_ETAT_VOITURE FROM etat_voiture");

            while (rs.next()) {
                int id = rs.getInt("ID_ETAT_VOITURE");
                String libelle = rs.getString("LIB_ETAT_VOITURE");
                etats.add(libelle);
                etatVoitureMap.put(libelle, id);
                System.out.println("État ajouté: " + id + " - " + libelle);
            }

            etatComboBox.setItems(etats);
            System.out.println("Nombre d'états chargés: " + etats.size());

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des états: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les états de voiture.", e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement des états: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void saveVehicle(ActionEvent event) {
        // Vérifier que tous les champs obligatoires sont remplis
        if (!validateFields()) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Choisir entre INSERT ou UPDATE selon si on est en mode édition
            if (currentVehicleId == null) {
                insertVehicle(conn);
            } else {
                updateVehicle(conn);
            }

            // Appeler le callback pour rafraîchir la table
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            // Fermer la fenêtre du formulaire
            closeForm(event);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Erreur lors de l'enregistrement du véhicule", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Veuillez vérifier les champs numériques.",
                    "Assurez-vous que les champs Nombre de places, Puissance et Prix contiennent des valeurs numériques valides.");
        }
    }

    private void insertVehicle(Connection conn) throws SQLException {
        // Générer un ID unique pour le véhicule
        int vehiculeId = generateVehiculeId(conn);

        // Préparer la requête SQL pour insérer le véhicule
        String query = "INSERT INTO vehicules (ID_VEHICULE, ID_ETAT_VOITURE, NUMERO_CHASSI, " +
                "IMMATRICULATION, MARQUE, MODELE, NB_PLACES, ENERGIE, DATE_ACQUISITION, " +
                "DATE_AMMORTISSEMENT, DATE_MISE_EN_SERVICE, PUISSANCE, COULEUR, PRIX_VEHICULE, DATE_ETAT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(query);

        // Récupérer l'ID de l'état sélectionné
        String selectedEtat = etatComboBox.getValue();
        int etatId = etatVoitureMap.get(selectedEtat);

        // Définir les paramètres de la requête
        pstmt.setInt(1, vehiculeId);
        pstmt.setInt(2, etatId);
        pstmt.setString(3, numeroChassiField.getText());
        pstmt.setString(4, immatriculationField.getText());
        pstmt.setString(5, marqueField.getText());
        pstmt.setString(6, modeleField.getText());
        pstmt.setInt(7, Integer.parseInt(placesField.getText()));
        pstmt.setString(8, energieComboBox.getValue());
        pstmt.setDate(9, java.sql.Date.valueOf(acquisitionDatePicker.getValue()));
        pstmt.setDate(10, java.sql.Date.valueOf(amortissementDatePicker.getValue()));
        pstmt.setDate(11, java.sql.Date.valueOf(miseEnServiceDatePicker.getValue()));
        pstmt.setInt(12, Integer.parseInt(puissanceField.getText()));
        pstmt.setString(13, couleurField.getText());
        pstmt.setInt(14, Integer.parseInt(prixField.getText()));
        pstmt.setDate(15, java.sql.Date.valueOf(dateEtatPicker.getValue()));

        // Exécuter la requête
        int rowsAffected = pstmt.executeUpdate();

        if (rowsAffected > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Véhicule ajouté avec succès.", "ID du véhicule: " + vehiculeId);
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Échec de l'ajout du véhicule.", "Aucune ligne n'a été affectée dans la base de données.");
        }
    }

    private void updateVehicle(Connection conn) throws SQLException {
        // Préparer la requête SQL pour mettre à jour le véhicule
        String query = "UPDATE vehicules SET ID_ETAT_VOITURE = ?, NUMERO_CHASSI = ?, " +
                "IMMATRICULATION = ?, MARQUE = ?, MODELE = ?, NB_PLACES = ?, ENERGIE = ?, " +
                "DATE_ACQUISITION = ?, DATE_AMMORTISSEMENT = ?, DATE_MISE_EN_SERVICE = ?, " +
                "PUISSANCE = ?, COULEUR = ?, PRIX_VEHICULE = ?, DATE_ETAT = ? " +
                "WHERE ID_VEHICULE = ?";

        PreparedStatement pstmt = conn.prepareStatement(query);

        // Récupérer l'ID de l'état sélectionné
        String selectedEtat = etatComboBox.getValue();
        int etatId = etatVoitureMap.get(selectedEtat);

        // Définir les paramètres de la requête
        pstmt.setInt(1, etatId);
        pstmt.setString(2, numeroChassiField.getText());
        pstmt.setString(3, immatriculationField.getText());
        pstmt.setString(4, marqueField.getText());
        pstmt.setString(5, modeleField.getText());
        pstmt.setInt(6, Integer.parseInt(placesField.getText()));
        pstmt.setString(7, energieComboBox.getValue());
        pstmt.setDate(8, java.sql.Date.valueOf(acquisitionDatePicker.getValue()));
        pstmt.setDate(9, java.sql.Date.valueOf(amortissementDatePicker.getValue()));
        pstmt.setDate(10, java.sql.Date.valueOf(miseEnServiceDatePicker.getValue()));
        pstmt.setInt(11, Integer.parseInt(puissanceField.getText()));
        pstmt.setString(12, couleurField.getText());
        pstmt.setInt(13, Integer.parseInt(prixField.getText()));
        pstmt.setDate(14, java.sql.Date.valueOf(dateEtatPicker.getValue()));
        pstmt.setInt(15, currentVehicleId);

        // Exécuter la requête
        int rowsAffected = pstmt.executeUpdate();

        if (rowsAffected > 0) {
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Véhicule mis à jour avec succès.", "");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Échec de la mise à jour du véhicule.", "Aucune ligne n'a été affectée dans la base de données.");
        }
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (immatriculationField.getText().isEmpty()) {
            errors.append("- L'immatriculation est obligatoire.");
        }

        if (marqueField.getText().isEmpty()) {
            errors.append("- La marque est obligatoire.");
        }

        if (modeleField.getText().isEmpty()) {
            errors.append("- Le modèle est obligatoire.");
        }

        if (numeroChassiField.getText().isEmpty()) {
            errors.append("- Le numéro de châssis est obligatoire.");
        }

        if (etatComboBox.getValue() == null) {
            errors.append("- L'état du véhicule est obligatoire.");
        }

        if (placesField.getText().isEmpty()) {
            errors.append("- Le nombre de places est obligatoire.");
        } else {
            try {
                Integer.parseInt(placesField.getText());
            } catch (NumberFormatException e) {
                errors.append("- Le nombre de places doit être un nombre entier.");
            }
        }

        if (energieComboBox.getValue() == null) {
            errors.append("- Le type d'énergie est obligatoire.");
        }

        if (acquisitionDatePicker.getValue() == null) {
            errors.append("- La date d'acquisition est obligatoire.");
        }

        if (amortissementDatePicker.getValue() == null) {
            errors.append("- La date d'amortissement est obligatoire.");
        }

        if (miseEnServiceDatePicker.getValue() == null) {
            errors.append("- La date de mise en service est obligatoire.");
        }

        if (puissanceField.getText().isEmpty()) {
            errors.append("- La puissance est obligatoire.\n");
        } else {
            try {
                Integer.parseInt(puissanceField.getText());
            } catch (NumberFormatException e) {
                errors.append("- La puissance doit être un nombre entier.");
            }
        }

        if (couleurField.getText().isEmpty()) {
            errors.append("- La couleur est obligatoire.");
        }

        if (prixField.getText().isEmpty()) {
            errors.append("- Le prix est obligatoire.");
        } else {
            try {
                Integer.parseInt(prixField.getText());
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre entier.");
            }
        }

        if (dateEtatPicker.getValue() == null) {
            errors.append("- La date d'état est obligatoire.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez corriger les erreurs suivantes :", errors.toString());
            return false;
        }

        return true;
    }

    private int generateVehiculeId(Connection conn) throws SQLException {
        int maxId = 0;

        String query = "SELECT MAX(ID_VEHICULE) as MAX_ID FROM vehicules";
        PreparedStatement pstmt = conn.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            maxId = rs.getInt("MAX_ID");
        }

        return maxId + 1;
    }

    @FXML
    public void closeForm(ActionEvent event) {
        // Obtenir la scène actuelle et fermer la fenêtre
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

    public void prepareEditForm(Vehicule vehicule) {
        // Stocker l'ID pour la mise à jour
        currentVehicleId = vehicule.getId();

        // Remplir les champs avec les données du véhicule
        immatriculationField.setText(vehicule.getImmatriculation());
        marqueField.setText(vehicule.getMarque());
        modeleField.setText(vehicule.getModele());
        numeroChassiField.setText(vehicule.getNumeroChassis());
        placesField.setText(String.valueOf(vehicule.getNbPlaces()));
        puissanceField.setText(String.valueOf(vehicule.getPuissance()));
        couleurField.setText(vehicule.getCouleur());
        prixField.setText(String.valueOf(vehicule.getPrix()));

        // Sélectionner les valeurs dans les ComboBox
        etatComboBox.getSelectionModel().select(vehicule.getEtat());
        energieComboBox.getSelectionModel().select(vehicule.getEnergie());

        // Définir les dates
        // Définir les dates (gérer les dates nulles)
        if (vehicule.getDateAcquisition() != null) {
            acquisitionDatePicker.setValue(vehicule.getDateAcquisition());
        }
        if (vehicule.getDateAmortissement() != null) {
            amortissementDatePicker.setValue(vehicule.getDateAmortissement());
        }
        if (vehicule.getDateMiseEnService() != null) {
            miseEnServiceDatePicker.setValue(vehicule.getDateMiseEnService());
        }
        if (vehicule.getDateEtat() != null) {
            dateEtatPicker.setValue(vehicule.getDateEtat());
        }
        // Changer le texte du bouton
        saveButton.setText("Mettre à jour");
    }
    private void setupDateValidation() {
        ChangeListener<LocalDate> listener = (obs, oldVal, newVal) -> validateDates();

        acquisitionDatePicker.valueProperty().addListener(listener);
        amortissementDatePicker.valueProperty().addListener(listener);
        miseEnServiceDatePicker.valueProperty().addListener(listener);
    }

    private void validateDates() {
        LocalDate acquisition = acquisitionDatePicker.getValue();
        LocalDate amortissement = amortissementDatePicker.getValue();
        LocalDate miseEnService = miseEnServiceDatePicker.getValue();

        boolean valid = true;
        String message = "";

        if (acquisition != null && amortissement != null && !acquisition.isBefore(amortissement)) {
            valid = false;
            message = "La date d'acquisition doit être antérieure à la date d'amortissement.";
        } else if (acquisition != null && miseEnService != null && !miseEnService.isAfter(acquisition)) {
            valid = false;
            message = "La date de mise en service doit être postérieure à la date d'acquisition.";
        }

        saveButton.setDisable(!valid);
        errorLabel.setText(message);
    }


}
