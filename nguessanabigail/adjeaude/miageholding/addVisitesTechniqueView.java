package nguessanabigail.adjeaude.miageholding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
public class addVisitesTechniqueView implements Initializable {
    @FXML private ComboBox<Vehicule> vehiculeComboBox;
    @FXML private DatePicker dateEntreePicker;
    @FXML private DatePicker dateSortiePicker;
    @FXML private TextField motifField;
    @FXML private TextArea observationArea;
    @FXML private TextField coutField;
    @FXML private TextField lieuField;
    @FXML private ComboBox<String> resultatComboBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ObservableList<Vehicule> vehiculesDisponibles = FXCollections.observableArrayList();
    private VisitesTechniques visiteEnCours = null;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeVehiculesComboBox();
        initializeResultatComboBox();
        configureDatePickers();
        configureNumericFields();

        // Pré-remplir le motif avec "Visite technique"
        motifField.setText("Visite technique");
        motifField.setEditable(false);
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
                    "ORDER BY v.MARQUE, v.MODELE, v.IMMATRICULATION";

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
            vehiculeComboBox.setConverter(new StringConverter<Vehicule>() {
                @Override
                public String toString(Vehicule vehicule) {
                    if (vehicule == null) return "";
                    return vehicule.getMarque() + " " + vehicule.getModele() + " - " + vehicule.getImmatriculation();
                }

                @Override
                public Vehicule fromString(String string) {
                    return vehiculesDisponibles.stream()
                            .filter(vehicule -> {
                                String display = vehicule.getMarque() + " " + vehicule.getModele() + " - " + vehicule.getImmatriculation();
                                return display.equals(string);
                            })
                            .findFirst()
                            .orElse(null);
                }
            });

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les véhicules", e.getMessage());
        }
    }

    private void initializeResultatComboBox() {
        resultatComboBox.setItems(FXCollections.observableArrayList(
                "En attente", "CONFORME", "NON CONFORME"
        ));
        resultatComboBox.getSelectionModel().selectFirst();
    }

    private void configureDatePickers() {
        // La date de visite ne peut pas être dans le futur
        dateEntreePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // La date d'expiration doit être après la date de visite
        dateEntreePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && dateSortiePicker.getValue() != null) {
                if (dateSortiePicker.getValue().isBefore(newDate)) {
                    dateSortiePicker.setValue(newDate.plusYears(1));
                }
            }
        });

        dateSortiePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                LocalDate dateEntree = dateEntreePicker.getValue();
                if (dateEntree != null && item.isBefore(dateEntree)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        // Par défaut, la date d'expiration est un an après la date de visite
        dateEntreePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && dateSortiePicker.getValue() == null) {
                dateSortiePicker.setValue(newDate.plusYears(1));
            }
        });
    }

    private void configureNumericFields() {
        coutField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                coutField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void saveVisiteTechnique(ActionEvent event) {
        if (!validateFields()) {
            return;
        }

        try {
            Connection conn = DataBase.getInstance().getConnection();

            // Stocker le résultat dans l'observation
            String observation = observationArea.getText();
            String resultat = resultatComboBox.getValue();
            if (!resultat.equals("En attente")) {
                observation = "Résultat: " + resultat + "\n" + observation;
            }

            if (visiteEnCours == null) {
                insertNewVisite(conn, observation);
            } else {
                updateExistingVisite(conn, observation);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

            closeForm(event);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Erreur lors de l'enregistrement", e.getMessage());
        }
    }

    private void insertNewVisite(Connection conn, String observation) throws SQLException {
        int visiteId = generateVisiteId(conn);

        String query = "INSERT INTO entretien (ID_ENTRETIEN, ID_VEHICULE, DATE_ENTREE_ENTR, " +
                "DATE_SORTIE_ENTR, MOTIF_ENTR, OBSERVATION, COUT_ENTR, LIEU_ENTR) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, visiteId);
        pstmt.setInt(2, vehiculeComboBox.getValue().getId());
        pstmt.setTimestamp(3, Timestamp.valueOf(dateEntreePicker.getValue().atStartOfDay()));
        pstmt.setTimestamp(4, dateSortiePicker.getValue() != null ?
                Timestamp.valueOf(dateSortiePicker.getValue().atTime(23, 59, 59)) : null);
        pstmt.setString(5, motifField.getText());
        pstmt.setString(6, observation);
        pstmt.setInt(7, Integer.parseInt(coutField.getText()));
        pstmt.setString(8, lieuField.getText());

        pstmt.executeUpdate();

        // Si le véhicule n'est pas conforme, le mettre en entretien
        if (resultatComboBox.getValue().equals("NON CONFORME")) {
            updateVehiculeEtat(conn, vehiculeComboBox.getValue().getId(), "En entretien");
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Visite technique ajoutée", "ID de la visite: " + visiteId);
    }

    private void updateExistingVisite(Connection conn, String observation) throws SQLException {
        String query = "UPDATE entretien SET ID_VEHICULE = ?, DATE_ENTREE_ENTR = ?, " +
                "DATE_SORTIE_ENTR = ?, MOTIF_ENTR = ?, OBSERVATION = ?, " +
                "COUT_ENTR = ?, LIEU_ENTR = ? WHERE ID_ENTRETIEN = ?";

        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, vehiculeComboBox.getValue().getId());
        pstmt.setTimestamp(2, Timestamp.valueOf(dateEntreePicker.getValue().atStartOfDay()));
        pstmt.setTimestamp(3, dateSortiePicker.getValue() != null ?
                Timestamp.valueOf(dateSortiePicker.getValue().atTime(23, 59, 59)) : null);
        pstmt.setString(4, motifField.getText());
        pstmt.setString(5, observation);
        pstmt.setInt(6, Integer.parseInt(coutField.getText()));
        pstmt.setString(7, lieuField.getText());
        pstmt.setInt(8, visiteEnCours.getId());

        pstmt.executeUpdate();

        // Gérer l'état du véhicule en fonction du résultat
        if (resultatComboBox.getValue().equals("NON CONFORME")) {
            updateVehiculeEtat(conn, vehiculeComboBox.getValue().getId(), "En entretien");
        } else if (resultatComboBox.getValue().equals("CONFORME")) {
            updateVehiculeEtat(conn, vehiculeComboBox.getValue().getId(), "Disponible");
        }

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Visite technique modifiée", "");
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

        if (vehiculeComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un véhicule.\n");
        }

        if (dateEntreePicker.getValue() == null) {
            errors.append("- La date de visite est obligatoire.\n");
        }

        if (lieuField.getText().trim().isEmpty()) {
            errors.append("- Le centre de visite est obligatoire.\n");
        }

        if (coutField.getText().trim().isEmpty()) {
            errors.append("- Le coût est obligatoire.\n");
        }

        if (resultatComboBox.getValue() == null) {
            errors.append("- Le résultat est obligatoire.\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                    "Veuillez corriger les erreurs suivantes:", errors.toString());
            return false;
        }

        return true;
    }

    private int generateVisiteId(Connection conn) throws SQLException {
        String query = "SELECT MAX(ID_ENTRETIEN) as MAX_ID FROM entretien";
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

    public void prepareEditForm(VisitesTechniques visite) {
        visiteEnCours = visite;

        // Remplir les champs avec les données de la visite
        dateEntreePicker.setValue(visite.getDateEntree().toLocalDate());
        dateSortiePicker.setValue(visite.getDateSortie() != null ? visite.getDateSortie().toLocalDate() : null);
        lieuField.setText(visite.getLieu());
        coutField.setText(String.valueOf(visite.getCout()));

        // Extraire le résultat de l'observation si présent
        String observation = visite.getObservation();
        if (observation != null && observation.startsWith("Résultat: ")) {
            String[] parts = observation.split("\n", 2);
            String resultat = parts[0].replace("Résultat: ", "");
            resultatComboBox.setValue(resultat);
            if (parts.length > 1) {
                observationArea.setText(parts[1]);
            }
        } else {
            resultatComboBox.setValue("En attente");
            observationArea.setText(observation);
        }

        // Sélectionner le véhicule
        for (Vehicule v : vehiculesDisponibles) {
            if (v.getId() == visite.getVehiculeId()) {
                vehiculeComboBox.setValue(v);
                break;
            }
        }

        // Changer le texte du bouton
        saveButton.setText("Mettre à jour");
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}
