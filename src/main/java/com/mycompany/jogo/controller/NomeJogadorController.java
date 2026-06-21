package com.mycompany.jogo.controller;

import com.mycompany.jogo.App;
import com.mycompany.jogo.DAO.JogadorDAO;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.util.Sessao;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class NomeJogadorController {

    @FXML private TextField campoNome;
    @FXML private Label     labelErro;

    private final JogadorDAO jogadorDAO = new JogadorDAO();

    @FXML
    private void onConfirmar() {
        String nome = campoNome.getText().trim();

        if (nome.isEmpty()) {
            mostrarErro("Informe um nome para continuar.");
            return;
        }
        if (nome.length() < 2) {
            mostrarErro("Nome muito curto. Use ao menos 2 caracteres.");
            return;
        }

        try {
            Jogador jogador = jogadorDAO.buscarPorNome(nome);
            if (jogador == null) {
                jogador = jogadorDAO.criar(nome);
            }
            Sessao.getInstance().setJogadorAtual(jogador);
            Sessao.getInstance().iniciarNovaRun();
            App.setRoot("Gameplay");
        } catch (Exception e) {
            mostrarErro("Erro ao conectar ao banco. Verifique o PostgreSQL.");
            e.printStackTrace();
        }
    }

    @FXML
    private void onVoltar() throws IOException {
        App.setRoot("Menu");
    }

    private void mostrarErro(String msg) {
        labelErro.setText(msg);
        labelErro.setVisible(true);
    }
}
