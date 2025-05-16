package nguessanabigail.adjeaude.miageholding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
public class suivieTechniqueController implements Initializable {
    @FXML
    private Pane bodySuivieTechnique;
    @FXML
    private Label welcomeLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        showReparationsView();
    }
    @FXML
    private void showReparationsView() {
        loadView("/nguessanabigail/adjeaude/miageholding/reparationsView.fxml");
    }
    @FXML
    private void showVisiteTechniqueView() {
        loadView("/nguessanabigail/adjeaude/miageholding/visiteTechniqueView.fxml");
    }
    @FXML
    private void showAssuranceView() {
        loadView("/nguessanabigail/adjeaude/miageholding/assuranceView.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            System.out.println("Tentative de chargement de: " + fxmlFile);

            // 1. Vérification du chemin du fichier
            URL fxmlUrl = getClass().getResource(fxmlFile);
            if (fxmlUrl == null) {
                System.err.println("ERREUR: Fichier FXML introuvable: " + fxmlFile);
                throw new IOException("Fichier FXML introuvable: " + fxmlFile);
            }
            System.out.println("Fichier FXML trouvé à: " + fxmlUrl);

            // 2. Création d'un nouveau FXMLLoader à chaque fois
            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            // 3. Charger une nouvelle instance du contrôleur à chaque fois
            loader.setControllerFactory(type -> {
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    System.err.println("Erreur lors de la création d'une instance du contrôleur: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            });

            // 4. Chargement du fichier FXML
            Parent view = loader.load();
            System.out.println("Fichier FXML chargé avec succès");

            // 5. Vérification que 'bodySuivieTechnique' est bien initialisé
            if (bodySuivieTechnique == null) {
                System.err.println("ERREUR: bodySuivieTechnique est null");
                throw new IllegalStateException("La variable 'bodySuivieTechnique' n'a pas été injectée par FXML");
            }

            // 6. Mise à jour de l'interface
            bodySuivieTechnique.getChildren().clear();
            bodySuivieTechnique.getChildren().add(view);

            // Ajuster la taille de la vue pour qu'elle remplisse le conteneur parent
            view.prefWidth(bodySuivieTechnique.getWidth());
            view.prefHeight(bodySuivieTechnique.getHeight());

            System.out.println("Vue mise à jour avec succès");

        } catch (IOException e) {
            System.err.println("Erreur d'E/S lors du chargement de la vue: " + e.getMessage());
            e.printStackTrace();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Erreur de chargement: " + e.getMessage());
            }
        } catch (IllegalStateException e) {
            System.err.println("Erreur d'état lors du chargement de la vue: " + e.getMessage());
            e.printStackTrace();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Erreur d'initialisation: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Erreur générale lors du chargement de la vue: " + e.getMessage());
            e.printStackTrace();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Erreur inattendue: " + e.getMessage());
            }
        }
    }

}
