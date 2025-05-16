package nguessanabigail.adjeaude.miageholding.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class adminlogistiqueController implements Initializable {
    @FXML
    private Label welcomeLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private AnchorPane body;
    @FXML
    private Pane sidebar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            // Charger la vue administrative par défaut
            loadAccueilView(null);
        } catch (Exception e) {
            e.printStackTrace();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Erreur d'initialisation: " + e.getMessage());
            }
        }
    }

    private void loadView(String fxmlFile) {
        try {
            // 1. Vérification du chemin du fichier
            URL fxmlUrl = getClass().getResource(fxmlFile);
            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable: " + fxmlFile);
            }

            // 2. Chargement du fichier FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            // 3. Vérification que 'body' est bien initialisé
            if (body == null) {
                throw new IllegalStateException("La variable 'body' n'a pas été injectée par FXML");
            }

            // 4. Mise à jour de l'interface
            body.getChildren().clear();
            body.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
            welcomeLabel.setText("Erreur de chargement: " + e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            welcomeLabel.setText("Erreur d'initialisation: " + e.getMessage());
        }
    }



    @FXML
    private void loadMissionsView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/missionsView.fxml");
    }
    @FXML
    private void loadAccueilView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/administratif.fxml");
    }
    @FXML
    private void loadAdministratifView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/accueilView.fxml");
    }

    @FXML
    private void loadStatView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/statView.fxml");
    }
    @FXML
    private void loadSTechniqueView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/sTechniqueView.fxml");
    }
    @FXML
    private void loadAffectationsView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/affectationsView.fxml");
    }
    @FXML
    private void loadFinancementView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/financementView.fxml");
    }
    @FXML
    private void loadInventaireView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/inventaireView.fxml");
    }
    @FXML
    private void loadSocietaireView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/societaireView.fxml");
    }
    @FXML
    private void loadParametresView(ActionEvent event) {
        loadView("/src/main/resources/nguessanabigail/adjeaude/miageholding/parametresView.fxml");
    }
    @FXML
    public void logoutButtonOnAction(ActionEvent event) {
        try {
            // Récupérer la fenêtre actuelle
            Stage dashboardStage = (Stage) logoutButton.getScene().getWindow();

            // Charger la vue de connexion
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent loginRoot = loader.load();

            // Créer une nouvelle scène avec les mêmes dimensions
            Scene loginScene = new Scene(loginRoot, 900, 600);

            // Configurer la nouvelle scène dans la fenêtre actuelle
            dashboardStage.setScene(loginScene);
            dashboardStage.setTitle("MIAGE HOLDING - Connexion");
            dashboardStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher un message d'erreur à l'utilisateur
            welcomeLabel.setText("Erreur lors de la déconnexion");
        }
    }
}
