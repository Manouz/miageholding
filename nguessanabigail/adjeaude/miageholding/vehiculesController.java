package nguessanabigail.adjeaude.miageholding;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class vehiculesController implements Initializable {

    // Champs pour le formulaire d'ajout de véhicule
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

    // Bouton d'ajout de véhicule dans la vue vehiculesView
    @FXML private Button btnAjouterVehicule;

    // Map pour stocker les ID d'état avec leur libellé
    private Map<String, Integer> etatVoitureMap = new HashMap<>();

    // URL de la base de données
    private static final String DB_URL = "jdbc:mysql://localhost:3306/miageholding";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Références FXML pour la table
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, String> marqueCol;
    @FXML private TableColumn<Vehicule, String> modeleCol;
    @FXML private TableColumn<Vehicule, String> immatriculationCol;
    @FXML private TableColumn<Vehicule, String> energieCol;
    @FXML private TableColumn<Vehicule, Integer> placesCol;
    @FXML private TableColumn<Vehicule, String> etatCol;
    @FXML private TableColumn<Vehicule, String> actionsCol;

    // ObservableList pour stocker les données
    private ObservableList<Vehicule> vehiculesData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Vérifier si on est dans la vue de la table des véhicules
        if (vehiculesTable != null && marqueCol != null) {
            initializeVehiculesTable();
        }

        // Vérifier si on est dans le formulaire d'ajout
       // if (etatComboBox != null && energieComboBox != null) {
           // initializeComboBoxes();
       // }
    }

    private void initializeVehiculesTable() {
        // Configuration des colonnes - vérifier que chaque colonne existe
        if (marqueCol != null) {
            marqueCol.setCellValueFactory(new PropertyValueFactory<>("marque"));
        }
        if (modeleCol != null) {
            modeleCol.setCellValueFactory(new PropertyValueFactory<>("modele"));
        }
        if (immatriculationCol != null) {
            immatriculationCol.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        }
        if (energieCol != null) {
            energieCol.setCellValueFactory(new PropertyValueFactory<>("energie"));
        }
        if (placesCol != null) {
            placesCol.setCellValueFactory(new PropertyValueFactory<>("nbPlaces"));
        }
        if (etatCol != null) {
            etatCol.setCellValueFactory(new PropertyValueFactory<>("etat"));
        }

        // Colonne actions avec boutons
        if (actionsCol != null) {
            actionsCol.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn = new Button("Modifier");
                private final Button deleteBtn = new Button("Supprimer");

                {
                    editBtn.setOnAction(event -> {
                        Vehicule vehicule = getTableView().getItems().get(getIndex());
                        editVehicule(vehicule);
                    });

                    deleteBtn.setOnAction(event -> {
                        Vehicule vehicule = getTableView().getItems().get(getIndex());
                        deleteVehicule(vehicule);
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

        // Chargement des données
        loadVehiculesFromDatabase();
    }

    private void loadVehiculesFromDatabase() {
        System.out.println("Chargement des véhicules depuis la base de données");

        vehiculesData.clear(); // Clear existing data first

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            DataBase database = DataBase.getInstance();
            conn = database.getConnection();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT v.*, e.LIB_ETAT_VOITURE FROM vehicules v JOIN etat_voiture e ON v.ID_ETAT_VOITURE = e.ID_ETAT_VOITURE");

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
                vehiculesData.add(vehicule);
                System.out.println("Véhicule ajouté: " + vehicule.getMarque() + " " + vehicule.getModele());
            }

            if (vehiculesTable != null) {
                vehiculesTable.setItems(vehiculesData);
                System.out.println("Nombre de véhicules chargés: " + vehiculesData.size());
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des véhicules: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du chargement des véhicules", e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement des véhicules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* Initialise les ComboBox avec les valeurs appropriées*/
    private void initializeComboBoxes() {
        System.out.println("Initialisation des ComboBox...");

        // Initialiser la ComboBox des états de voiture depuis la base de données
        loadEtatVoitureFromDatabase();

        // Initialiser la ComboBox des types d'énergie
        ObservableList<String> energieTypes = FXCollections.observableArrayList(
                "Essence", "Diesel", "Électrique", "Hybride", "GPL", "GNV");
        energieComboBox.setItems(energieTypes);
    }

    /**
     * Charge les états de voiture depuis la base de données
     */
    private void loadEtatVoitureFromDatabase() {
        System.out.println("Chargement des véhicules depuis la base de données");

        vehiculesData.clear(); // Clear existing data first

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            DataBase database = DataBase.getInstance();
            conn = database.getConnection();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT v.*, e.LIB_ETAT_VOITURE FROM vehicules v JOIN etat_voiture e ON v.ID_ETAT_VOITURE = e.ID_ETAT_VOITURE");

            while (rs.next()) {
                // Gérer les dates qui peuvent être nulles
                Date dateAcquisition = rs.getDate("DATE_ACQUISITION");
                LocalDate localDateAcquisition = (dateAcquisition != null) ? dateAcquisition.toLocalDate() : null;

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

                // Définir les autres propriétés qui peuvent exister
                vehicule.setNumeroChassis(rs.getString("NUMERO_CHASSI"));

                Date dateAmortissement = rs.getDate("DATE_AMMORTISSEMENT");
                if (dateAmortissement != null) {
                    vehicule.setDateAmortissement(dateAmortissement.toLocalDate());
                }

                Date dateMiseEnService = rs.getDate("DATE_MISE_EN_SERVICE");
                if (dateMiseEnService != null) {
                    vehicule.setDateMiseEnService(dateMiseEnService.toLocalDate());
                }

                Date dateEtat = rs.getDate("DATE_ETAT");
                if (dateEtat != null) {
                    vehicule.setDateEtat(dateEtat.toLocalDate());
                }

                vehiculesData.add(vehicule);
                System.out.println("Véhicule ajouté: " + vehicule.getMarque() + " " + vehicule.getModele());
            }

            if (vehiculesTable != null) {
                vehiculesTable.setItems(vehiculesData);
                System.out.println("Nombre de véhicules chargés: " + vehiculesData.size());
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL lors du chargement des états: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les états de voiture.", e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement des états: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode appelée lorsque l'utilisateur clique sur "Ajouter un véhicule" dans la vue vehiculesView
     */
    @FXML
    public void showAddVehicleForm(ActionEvent event) {
        try {
            System.out.println("Tentative d'ouverture du formulaire d'ajout de véhicule...");

            // Corriger le chemin du fichier FXML
            String fxmlFile = "/nguessanabigail/adjeaude/miageholding/addVehicleForm.fxml";
            URL fxmlUrl = getClass().getResource(fxmlFile);

            if (fxmlUrl == null) {
                System.err.println("Fichier FXML introuvable à: " + fxmlFile);
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Fichier FXML introuvable", fxmlFile);
                return;
            }

            System.out.println("Fichier FXML trouvé à: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            // Spécifier explicitement la factory pour le contrôleur
            loader.setControllerFactory(type -> {
                if (type == addVehiculeFromController.class) {
                    return new addVehiculeFromController();
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Impossible de créer le contrôleur: " + type, e);
                }
            });

            Parent root = loader.<Parent>load();

            // Créer une nouvelle fenêtre modale
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau véhicule");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Configurer le callback pour rafraîchir la table
           addVehiculeFromController formController = loader.getController();
            formController.setOnSaveCallback(() -> loadVehiculesFromDatabase());

            // Afficher la fenêtre
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur IO: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire d'ajout de véhicule", e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur générale: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur inattendue s'est produite", e.getMessage());
        }
    }

    /**
     * Méthode appelée lorsque l'utilisateur clique sur "Enregistrer" dans le formulaire d'ajout de véhicule
     */
    @FXML
    public void saveVehicle(ActionEvent event) {
        // Vérifier que tous les champs obligatoires sont remplis
        if (!validateFields()) {
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
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

                // Fermer la fenêtre du formulaire
                closeForm(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Échec de l'ajout du véhicule.", "Aucune ligne n'a été affectée dans la base de données.");
            }

        } catch (NumberFormatException | SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Veuillez vérifier les champs numériques.", e.getMessage());
        }
    }

    /**
     * Valide les champs du formulaire
     * @return true si tous les champs obligatoires sont valides, false sinon
     */
    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();

        if (immatriculationField.getText().isEmpty()) {
            errors.append("- L'immatriculation est obligatoire.\n");
        }

        if (marqueField.getText().isEmpty()) {
            errors.append("- La marque est obligatoire.\n");
        }

        if (modeleField.getText().isEmpty()) {
            errors.append("- Le modèle est obligatoire.\n");
        }

        if (numeroChassiField.getText().isEmpty()) {
            errors.append("- Le numéro de châssis est obligatoire.\n");
        }

        if (etatComboBox.getValue() == null) {
            errors.append("- L'état du véhicule est obligatoire.\n");
        }

        if (placesField.getText().isEmpty()) {
            errors.append("- Le nombre de places est obligatoire.\n");
        } else {
            try {
                Integer.parseInt(placesField.getText());
            } catch (NumberFormatException e) {
                errors.append("- Le nombre de places doit être un nombre entier.\n");
            }
        }

        if (energieComboBox.getValue() == null) {
            errors.append("- Le type d'énergie est obligatoire.\n");
        }

        if (acquisitionDatePicker.getValue() == null) {
            errors.append("- La date d'acquisition est obligatoire.\n");
        }

        if (amortissementDatePicker.getValue() == null) {
            errors.append("- La date d'amortissement est obligatoire.\n");
        }

        if (miseEnServiceDatePicker.getValue() == null) {
            errors.append("- La date de mise en service est obligatoire.\n");
        }

        if (puissanceField.getText().isEmpty()) {
            errors.append("- La puissance est obligatoire.\n");
        } else {
            try {
                Integer.parseInt(puissanceField.getText());
            } catch (NumberFormatException e) {
                errors.append("- La puissance doit être un nombre entier.\n");
            }
        }

        if (couleurField.getText().isEmpty()) {
            errors.append("- La couleur est obligatoire.\n");
        }

        if (prixField.getText().isEmpty()) {
            errors.append("- Le prix est obligatoire.\n");
        } else {
            try {
                Integer.parseInt(prixField.getText());
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre entier.\n");
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

    /**
     * Génère un ID unique pour un nouveau véhicule
     * @param conn la connexion à la base de données
     * @return un ID unique pour le véhicule
     * @throws SQLException si une erreur survient lors de l'accès à la base de données
     */
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

    /**
     * Méthode appelée lorsque l'utilisateur clique sur "Annuler" dans le formulaire d'ajout de véhicule
     */
    @FXML
    public void closeForm(ActionEvent event) {
        // Obtenir la scène actuelle et fermer la fenêtre
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Affiche une boîte de dialogue d'alerte
     * @param type le type d'alerte
     * @param title le titre de l'alerte
     * @param header le texte d'en-tête de l'alerte
     * @param content le contenu de l'alerte
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void editVehicule(Vehicule vehicule) {
        try {
            // Charger le formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/nguessanabigail/adjeaude/miageholding/addVehicleForm.fxml"));
            loader.setControllerFactory(type -> {
                if (type == addVehiculeFromController.class) {
                    return new addVehiculeFromController();
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Impossible de créer le contrôleur: " + type, e);
                }
            });

            Parent root = loader.load();

            // Récupérer le contrôleur
            addVehiculeFromController formController = loader.getController();

            // Configurer le formulaire en mode édition
            formController.prepareEditForm(vehicule);

            // Configurer le callback pour rafraîchir la table
            formController.setOnSaveCallback(() -> loadVehiculesFromDatabase());

            // Créer et afficher la fenêtre
            Stage editStage = new Stage();
            editStage.setTitle("Modifier Véhicule - " + vehicule.getImmatriculation());
            editStage.setScene(new Scene(root));
            editStage.initModality(Modality.APPLICATION_MODAL);
            editStage.showAndWait();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger le formulaire de modification", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur inattendue est survenue", e.getMessage());
        }
    }

//    public void prepareEditForm(Vehicule vehicule) {
//        // Remplir les champs avec les données du véhicule
//        immatriculationField.setText(vehicule.getImmatriculation());
//        marqueField.setText(vehicule.getMarque());
//        modeleField.setText(vehicule.getModele());
//        numeroChassiField.setText(vehicule.getNumeroChassis());
//        placesField.setText(String.valueOf(vehicule.getNbPlaces()));
//        puissanceField.setText(String.valueOf(vehicule.getPuissance()));
//        couleurField.setText(vehicule.getCouleur());
//        prixField.setText(String.valueOf(vehicule.getPrix()));
//
//        // Sélectionner les valeurs dans les ComboBox
//        etatComboBox.getSelectionModel().select(vehicule.getEtat());
//        energieComboBox.getSelectionModel().select(vehicule.getEnergie());
//
//        // Définir les dates
//        acquisitionDatePicker.setValue(vehicule.getDateAcquisition());
//        amortissementDatePicker.setValue(vehicule.getDateAmortissement());
//        miseEnServiceDatePicker.setValue(vehicule.getDateMiseEnService());
//        dateEtatPicker.setValue(vehicule.getDateEtat());
//
//        // Stocker l'ID pour la mise à jour
//        Object currentVehicleId = vehicule.getId();
//
//        // Changer le texte du bouton
//        saveButton.setText("Mettre à jour");
//    }

    @FXML
    private void deleteVehicule(Vehicule vehicule) {
        // Demander confirmation avant de supprimer
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Supprimer le véhicule");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer le véhicule " +
                vehicule.getMarque() + " " + vehicule.getModele() +
                " (" + vehicule.getImmatriculation() + ") ?");

        ButtonType buttonTypeOui = new ButtonType("Oui");
        ButtonType buttonTypeNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(buttonTypeOui, buttonTypeNon);

        confirmDialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeOui) {
                // Supprimer le véhicule de la base de données
                try {
                    Connection conn = DataBase.getInstance().getConnection();
                    String query = "DELETE FROM vehicules WHERE ID_VEHICULE = ?";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, vehicule.getId());

                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Succès",
                                "Véhicule supprimé", "Le véhicule a été supprimé avec succès.");

                        // Rafraîchir la table
                        loadVehiculesFromDatabase();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Erreur",
                                "Échec de la suppression", "Le véhicule n'a pas pu être supprimé.");
                    }

                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                            "Impossible de supprimer le véhicule", e.getMessage());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Une erreur inattendue s'est produite", e.getMessage());
                }
            }
        });
    }
}
