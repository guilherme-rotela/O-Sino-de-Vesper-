package com.mycompany.jogo.controller;

import com.mycompany.jogo.App;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class CreditosController implements Initializable {

    @FXML private Label labelDupla;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Altere aqui com os nomes da dupla
        labelDupla.setText("Aluno 1  &  Aluno 2");
    }

    @FXML
    private void onVoltar() throws IOException {
        App.setRoot("menu");
    }
}
