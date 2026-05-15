module com.mycompany.jogo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.jogo to javafx.fxml;
    exports com.mycompany.jogo;
}
