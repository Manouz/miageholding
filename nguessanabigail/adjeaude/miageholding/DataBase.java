package nguessanabigail.adjeaude.miageholding;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBase {
    private static DataBase instance;
    private Connection connection;

    // Constructeur privé pour empêcher l'instanciation directe
    private DataBase() {
        // Initialisation de la connexion
        try {
            String url = "jdbc:mysql://localhost:3306/miageholding";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Initialiser la connexion sinon
    public void initialize() throws SQLException {
        if (getConnection().isClosed()){
            DataBase.getInstance();
        }
    }
    // Méthode pour obtenir l'instance unique
    public static synchronized DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    // Optionnel: méthode pour fermer la connexion
    /*public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }*/
}
