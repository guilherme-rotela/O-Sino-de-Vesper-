package com.mycompany.jogo.controller;

import com.mycompany.jogo.*;
import java.io.IOException;
import javafx.fxml.FXML;

public class MenuController {

    @FXML
    private void gameplay() throws IOException {
        App.setRoot("NomeJogador");
    }   
    @FXML
    private void ranking() throws IOException {
        App.setRoot("Ranking");
    }  
    @FXML
    private void creditos() throws IOException {
        App.setRoot("Creditos");
    }  
    @FXML
    private void encerrar() throws IOException {
        System.exit(0);
    }  
}
