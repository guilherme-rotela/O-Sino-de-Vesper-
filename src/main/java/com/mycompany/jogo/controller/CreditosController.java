package com.mycompany.jogo.controller;

import com.mycompany.jogo.App;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CreditosController {

    @FXML private Label labelDupla;

    
    @FXML
    private void onVoltar() throws IOException {
        App.setRoot("Menu");
    }
}
