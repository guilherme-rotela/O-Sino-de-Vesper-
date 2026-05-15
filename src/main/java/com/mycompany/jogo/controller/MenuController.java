package com.mycompany.jogo.controller;

import com.mycompany.jogo.*;
import java.io.IOException;
import javafx.fxml.FXML;

public class MenuController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }   
}
