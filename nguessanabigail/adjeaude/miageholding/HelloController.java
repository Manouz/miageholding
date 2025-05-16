package nguessanabigail.adjeaude.miageholding;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;

import java.net.URL;

public class HelloController implements Initializable {
    @FXML
    private Label slogan;
    @FXML
    private Label smslogin;
    @FXML
    private ImageView logogauche;
    @FXML
    private ImageView logodroite;
    @FXML
    private TextField emailtextfield;
    @FXML
    private PasswordField passfield;
    @FXML
    private Button loginButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            File brandingFile = new File("src/main/resources/images/logodroite.png");
            Image brandingImage = new Image(brandingFile.toURI().toString());
            logodroite.setImage(brandingImage);

            File logoFile = new File("src/main/resources/images/logogauche.png");
            Image logoImage = new Image(logoFile.toURI().toString());
            logogauche.setImage(logoImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void loginButtonOnAction(ActionEvent event) {
        if (emailtextfield.getText().isBlank() == false && passfield.getText().isBlank() == false) {
            validatelogin();
        } else {
            smslogin.setText("Veuillez entrer votre email et votre mot de passe");
        }
    }

    public boolean validatelogin() {
        // Utiliser l'instance unique via getInstance() au lieu du constructeur
        DataBase connexionNow = DataBase.getInstance();
        Connection connexionDB = null;

        try {
            connexionDB = connexionNow.getConnection();
        } catch (Exception e) {
            Platform.runLater(() ->
                    smslogin.setText("Erreur de connexion à la base de données: " + e.getMessage())
            );
            return false;
        }

        if (connexionDB == null) {
            Platform.runLater(() ->
                    smslogin.setText("Erreur de connexion à la base de données")
            );
            return false;
        }

        String email = emailtextfield.getText();
        String password = passfield.getText();

        try {
            String verifyLogin = "SELECT COUNT(1) FROM personnel WHERE EMAIL_PERSONNEL = ? AND MDP_PERSONNEL = ?";
            PreparedStatement preparedStatement = connexionDB.prepareStatement(verifyLogin);
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            ResultSet queryResult = preparedStatement.executeQuery();

            if (queryResult.next() && queryResult.getInt(1) == 1) {
                System.out.println("Connexion réussie, tentative de redirection...");
                Platform.runLater(() -> {
                    try {
                        smslogin.setText("Connexion réussie");
                        openDashboard();
                    } catch (IOException e) {
                        System.err.println("Erreur lors de la redirection: " + e.getMessage());
                        e.printStackTrace();
                        smslogin.setText("Erreur lors de l'ouverture du tableau de bord");
                    }
                });
                return true;
            } else {
                Platform.runLater(() ->
                        smslogin.setText("Email ou mot de passe incorrect")
                );
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                    smslogin.setText("Erreur: " + e.getMessage())
            );
            return false;
        }
        finally {
            try {
                if (connexionDB != null) {
                    // On ne ferme pas la connexion ici car c'est une instance Singleton
                    // connexionNow.closeConnection();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openDashboard() throws IOException {
        try {
            System.out.println("Début de openDashboard()");

            // 1. Vérifier que le fichier FXML existe
            URL fxmlUrl = getClass().getResource("/nguessanabigail/adjeaude/miageholding/adminlogistique.fxml");
            if (fxmlUrl == null) {
                throw new IOException("Fichier FXML introuvable à l'emplacement spécifié");
            }
            System.out.println("FXML trouvé à: " + fxmlUrl);

            // 2. Chargement du FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent dashboardRoot = loader.load();
            System.out.println("FXML chargé avec succès");

            // 3. Récupération de la scène actuelle
            Stage loginStage = (Stage) loginButton.getScene().getWindow();

            // 4. Création de la nouvelle scène
            Scene dashboardScene = new Scene(dashboardRoot, 900, 600);
            System.out.println("Nouvelle scène créée");

            // 5. Application de la nouvelle scène
            loginStage.setScene(dashboardScene);
            loginStage.setTitle("MIAGE HOLDING - Tableau de bord");
            loginStage.centerOnScreen();
            System.out.println("Redirection effectuée avec succès");

        } catch (IOException e) {
            System.err.println("Échec critique dans openDashboard(): " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}