package com.mycompany.jogo.controller;

import com.mycompany.jogo.App;
import com.mycompany.jogo.DAO.JogadorDAO;
import com.mycompany.jogo.DAO.UpgradeDAO;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.model.Upgrade;
import com.mycompany.jogo.util.Sessao;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class UpgradesController implements Initializable {

    @FXML private Label labelSangueDisp;
    @FXML private Label labelNivel;
    @FXML private VBox  listaTecnica;
    @FXML private VBox  listaVitalidade;
    @FXML private VBox  listaVigor;

    private final UpgradeDAO upgradeDAO = new UpgradeDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        atualizarCabecalho();
        carregarUpgrades();
    }

    private void atualizarCabecalho() {
        Jogador j = jogador();
        labelSangueDisp.setText("🩸 Sangue: " + j.getPontosSangue());
        labelNivel.setText("Nível " + j.getNivel());
    }

    private void carregarUpgrades() {
        try {
            List<Upgrade> todos = upgradeDAO.listarTodos();
            List<Integer> adquiridos = upgradeDAO.listarIdsAdquiridos(jogador().getId());

            listaTecnica.getChildren().clear();
            listaVitalidade.getChildren().clear();
            listaVigor.getChildren().clear();

            for (Upgrade up : todos) {
                VBox card = criarCard(up, adquiridos.contains(up.getId()));
                switch (up.getTipo()) {
                    case "tecnica":
                        listaTecnica.getChildren().add(card);
                        break;
                    case "vitalidade":
                        listaVitalidade.getChildren().add(card);
                        break;
                    case "vigor":
                        listaVigor.getChildren().add(card);
                        break;
                        
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox criarCard(Upgrade up, boolean jaAdquirido) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card-item");

        Label nome = new Label(up.getNome());
        nome.getStyleClass().add("card-item-nome");

        Label desc = new Label(up.getDescricao() + "  (+" + up.getBonusValor() + ")");
        desc.getStyleClass().add("card-item-desc");

        Label custo = new Label("🩸 " + up.getCustoSangue() + " de sangue");
        custo.getStyleClass().add("card-item-custo");

        Button btn = new Button(jaAdquirido ? "✓ Adquirido" : "Comprar");
        btn.getStyleClass().add("btn-acao");
        btn.setDisable(jaAdquirido || jogador().getPontosSangue() < up.getCustoSangue());

        btn.setOnAction(e -> comprarUpgrade(up, btn));

        HBox rodape = new HBox(8);
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        rodape.getChildren().addAll(custo, esp, btn);

        card.getChildren().addAll(nome, desc, rodape);
        return card;
    }

    private void comprarUpgrade(Upgrade up, Button btn) {
        Jogador j = jogador();
        if (!j.gastarSangue(up.getCustoSangue())) return;

        switch (up.getTipo()) {
            case "vitalidade":
                j.aumentarVitalidade(up.getBonusValor());
                break;
            case "vigor":
                j.aumentarVigor(up.getBonusValor());
                break;
            case "tecnica": 
                j.aumentarTecnica(up.getBonusValor());
                break;
        }

        try {
            upgradeDAO.adquirir(j.getId(), up.getId());
            jogadorDAO.salvarAtributos(j);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btn.setText("✓ Adquirido");
        btn.setDisable(true);
        atualizarCabecalho();
        // Atualizar botões de outros cards (saldo mudou)
        carregarUpgrades();
    }

    @FXML private void onIrLoja() throws IOException{
        App.setRoot("Gameplay");
    }
    @FXML private void onContinuar() throws IOException {
        App.setRoot("Gameplay");
    }

    private Jogador jogador() { 
        return Sessao.getInstance().getJogadorAtual(); 
    }
}
