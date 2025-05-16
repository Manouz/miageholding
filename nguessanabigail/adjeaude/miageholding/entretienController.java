package nguessanabigail.adjeaude.miageholding;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.sql.*;
import java.time.LocalDate;

public class entretienController {
    @FXML private TableView<Entretien> entretienTable;
    @FXML private TableColumn<Entretien, String> vehiculeCol;
    @FXML private TableColumn<Entretien, LocalDate> dateEntreeCol;
    @FXML private TableColumn<Entretien, LocalDate> dateSortieCol;
    @FXML private TableColumn<Entretien, String> motifCol;
    @FXML private TableColumn<Entretien, Number> coutCol;
    @FXML private ComboBox<String> vehiculeCombo;
    @FXML private DatePicker dateEntreePicker;
    @FXML private TextField motifField;
    @FXML private TextField coutField;
    @FXML private TextField lieuField;

    private ObservableList<Entretien> entretienData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configuration des colonnes
//        vehiculeCol.setCellValueFactory(cellData -> cellData.getValue().vehiculeProperty());
//        dateEntreeCol.setCellValueFactory(cellData -> cellData.getValue().dateEntreeProperty());
//        dateSortieCol.setCellValueFactory(cellData -> cellData.getValue().dateSortieProperty());
//        motifCol.setCellValueFactory(cellData -> cellData.getValue().motifProperty());
//        coutCol.setCellValueFactory(cellData -> cellData.getValue().coutProperty());

        // Chargement des donn es
        chargerVehicules();
        chargerEntretiens();
    }

    private void chargerVehicules() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT IMMATRICULATION FROM vehicules")) {

            while (rs.next()) {
                vehiculeCombo.getItems().add(rs.getString("IMMATRICULATION"));
            }
        } catch (SQLException e) {
            showAlert("Erreur", " chec du chargement des v hicules: " + e.getMessage());
        }
    }

    private void chargerEntretiens() {
        entretienData.clear();
        String query = "SELECT e.*, v.IMMATRICULATION FROM entretien e JOIN vehicules v ON e.ID_VEHICULE = v.ID_VEHICULE";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                entretienData.add(new Entretien(
                        rs.getString("IMMATRICULATION"),
                        rs.getDate("DATE_ENTREE_ENTR").toLocalDate(),
                        rs.getDate("DATE_SORTIE_ENTR") != null ? rs.getDate("DATE_SORTIE_ENTR").toLocalDate() : null,
                        rs.getString("MOTIF_ENTR"),
                        rs.getInt("COUT_ENTR")
                ));
            }
            entretienTable.setItems(entretienData);
        } catch (SQLException e) {
            showAlert("Erreur", " chec du chargement des entretiens: " + e.getMessage());
        }
    }

    @FXML
    private void programmerEntretien() {
        if (validateForm()) {
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO entretien (ID_VEHICULE, DATE_ENTREE_ENTR, MOTIF_ENTR, COUT_ENTR, LIEU_ENTR) " +
                                 "VALUES ((SELECT ID_VEHICULE FROM vehicules WHERE IMMATRICULATION = ?), ?, ?, ?, ?)")) {

                pstmt.setString(1, vehiculeCombo.getValue());
                pstmt.setDate(2, Date.valueOf(dateEntreePicker.getValue()));
                pstmt.setString(3, motifField.getText());
                pstmt.setInt(4, Integer.parseInt(coutField.getText()));
                pstmt.setString(5, lieuField.getText());
                pstmt.executeUpdate();

                showAlert("Succ s", "Entretien programm  avec succ s !");
                chargerEntretiens();
                clearForm();
            } catch (SQLException e) {
                showAlert("Erreur", " chec de l'enregistrement: " + e.getMessage());
            }
        }
    }

    @FXML
    private void marquerTermine() {
        Entretien selected = entretienTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "UPDATE entretien SET DATE_SORTIE_ENTR = ? WHERE ID_ENTRETIEN = " +
                                 "(SELECT ID_ENTRETIEN FROM entretien e JOIN vehicules v ON e.ID_VEHICULE = v.ID_VEHICULE " +
                                 "WHERE v.IMMATRICULATION = ? AND e.DATE_ENTREE_ENTR = ?)")) {

                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
                pstmt.setString(2, selected.getVehicule());
//                pstmt.setDate(3, Date.valueOf(selected.getDateEntree()));
                pstmt.executeUpdate();

                showAlert("Succ s", "Entretien marqu  comme termin  !");
                chargerEntretiens();
            } catch (SQLException e) {
                showAlert("Erreur", " chec de la mise   jour: " + e.getMessage());
            }
        } else {
            showAlert("Avertissement", "Veuillez s lectionner un entretien !");
        }
    }

    private boolean validateForm() {
        if (vehiculeCombo.getValue() == null || dateEntreePicker.getValue() == null ||
                motifField.getText().isEmpty() || coutField.getText().isEmpty()) {
            showAlert("Validation", "Tous les champs obligatoires doivent  tre remplis !");
            return false;
        }
        try {
            Integer.parseInt(coutField.getText());
        } catch (NumberFormatException e) {
            showAlert("Validation", "Le co t doit  tre un nombre valide !");
            return false;
        }
        return true;
    }

    private void clearForm() {
        vehiculeCombo.setValue(null);
        dateEntreePicker.setValue(null);
        motifField.clear();
        coutField.clear();
        lieuField.clear();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_parc", "user", "password");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe mod le interne
    public static class Entretien {
        private final StringProperty vehicule;
        private final ObjectProperty<LocalDate> dateEntree;
        private final ObjectProperty<LocalDate> dateSortie;
        private final StringProperty motif;
        private final IntegerProperty cout;

        public Entretien(String vehicule, LocalDate dateEntree, LocalDate dateSortie, String motif, int cout) {
            this.vehicule = new SimpleStringProperty(vehicule);
            this.dateEntree = new SimpleObjectProperty<>(dateEntree);
            this.dateSortie = new SimpleObjectProperty<>(dateSortie);
            this.motif = new SimpleStringProperty(motif);
            this.cout = new SimpleIntegerProperty(cout);
        }

        // Getters et property getters
        public String getVehicule() { return vehicule.get(); }
        public StringProperty vehiculeProperty() { return vehicule; }
        // ... autres getters
    }

}
