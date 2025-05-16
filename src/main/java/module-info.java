module nguessanabigail.adjeaude.miageholding {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;
    requires mysql.connector.j;


    opens nguessanabigail.adjeaude.miageholding to javafx.fxml;
    exports nguessanabigail.adjeaude.miageholding;
    exports nguessanabigail.adjeaude.miageholding.Controller;
    opens nguessanabigail.adjeaude.miageholding.Controller to javafx.fxml;
}