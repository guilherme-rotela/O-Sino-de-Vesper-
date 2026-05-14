module com.mycompany.jogo {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.jogo to javafx.fxml;
    exports com.mycompany.jogo;
}
