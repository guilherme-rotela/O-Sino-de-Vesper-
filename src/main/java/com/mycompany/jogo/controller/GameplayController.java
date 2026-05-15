package com.mycompany.jogo.controller;

import com.mycompany.jogo.*;
import java.io.IOException;
import javafx.fxml.FXML;

public class GameplayController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}