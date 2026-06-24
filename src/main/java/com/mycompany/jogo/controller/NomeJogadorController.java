package com.mycompany.jogo.controller;

import com.mycompany.jogo.App;
import com.mycompany.jogo.DAO.JogadorDAO;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.util.Sessao;

import java.io.IOException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class NomeJogadorController {

    @FXML private TextField campoNome;
    @FXML private Label     labelErro;
    @FXML private Button    btnConfirmar;
    @FXML private Button    btnVoltar;

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

        Task<Jogador> task = new Task<>() {
            @Override
            protected Jogador call() throws Exception {
                Jogador jogador = jogadorDAO.buscarPorNome(nome);
                if (jogador == null) {
                    jogador = jogadorDAO.criar(nome);
                }
                return jogador;
            }
        };

        task.setOnRunning(e -> {
            labelErro.setVisible(false);
            setControlesHabilitados(false);
        });

        task.setOnSucceeded(e -> {
            Jogador jogador = task.getValue();
            Sessao.getInstance().setJogadorAtual(jogador);
            Sessao.getInstance().iniciarNovaRun();
            setControlesHabilitados(true);
            try {
                App.setRoot("Gameplay");
            } catch (Exception ex) {
                mostrarErro("Erro ao abrir o jogo.");
                ex.printStackTrace();
            }
        });

        task.setOnFailed(e -> {
            setControlesHabilitados(true);
            mostrarErro("Erro ao conectar ao banco. Verifique se o PostgreSQL está rodando. "+e);
            System.out.println(e);
            
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "conexao-banco");
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void onVoltar() throws IOException {
        App.setRoot("Menu");
    }

    private void mostrarErro(String msg) {
        labelErro.setText(msg);
        labelErro.setVisible(true);
    }

    private void setControlesHabilitados(boolean habilitados) {
        campoNome.setDisable(!habilitados);
        if (btnConfirmar != null) btnConfirmar.setDisable(!habilitados);
        if (btnVoltar != null) btnVoltar.setDisable(!habilitados);
    }
}
