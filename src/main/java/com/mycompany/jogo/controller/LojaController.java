package com.mycompany.jogo.controller;

import com.mycompany.jogo.App;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.DAO.ItemDAO;
import com.mycompany.jogo.DAO.JogadorDAO;
import com.mycompany.jogo.model.Item;
import com.mycompany.jogo.util.Sessao;
import com.mycompany.jogo.util.SceneManager;
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

public class LojaController implements Initializable {

    @FXML private Label labelSangue;
    @FXML private Label labelFeedback;
    @FXML private VBox  listaArmas;
    @FXML private VBox  listaArmaduras;
    @FXML private VBox  listaPocoes;

    private final ItemDAO    itemDAO    = new ItemDAO();
    private final JogadorDAO jogadorDAO = new JogadorDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        atualizarSangue();
        carregarItens();
    }

    private void atualizarSangue() {
        labelSangue.setText("🩸 Sangue: " + jogador().getPontosSangue());
    }

    private void carregarItens() {
    try {
        List<Item> itens = itemDAO.listarLoja();
        listaArmas.getChildren().clear();
        listaArmaduras.getChildren().clear();
        listaPocoes.getChildren().clear();

        for (Item item : itens) {
            VBox card = criarCard(item);
            switch (item.getTipo()) {
                case ARMA: 
                    listaArmas.getChildren().add(card);
                    break;
                case ARMADURA:
                    listaArmaduras.getChildren().add(card);
                    break;
                case POCAO: 
                    listaPocoes.getChildren().add(card);
                    break;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private VBox criarCard(Item item) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card-item");

        Label nome = new Label(item.getNome());
        nome.getStyleClass().add("card-item-nome");

        Label desc = new Label(item.getDescricao());
        desc.getStyleClass().add("card-item-desc");

        String bonusStr = item.getBonusAtributo() != null
            ? "  +" + item.getBonusValor() + " " + item.getBonusAtributo() : "";
        Label bonus = new Label(bonusStr);
        bonus.getStyleClass().add("card-item-desc");

        Label custo = new Label("🩸 " + item.getCustoSangue());
        custo.getStyleClass().add("card-item-custo");

        boolean semSaldo = jogador().getPontosSangue() < item.getCustoSangue();
        Button btn = new Button("Comprar");
        btn.getStyleClass().add("btn-acao");
        btn.setDisable(semSaldo);
        btn.setOnAction(e -> comprar(item, btn));

        HBox rodape = new HBox(8);
        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        rodape.getChildren().addAll(custo, esp, btn);

        card.getChildren().addAll(nome, desc, bonus, rodape);
        return card;
    }

    private void comprar(Item item, Button btn) {
        Jogador j = jogador();
        if (!j.gastarSangue(item.getCustoSangue())) {
            mostrarFeedback("Sangue insuficiente!");
            return;
        }

        // Aplicar efeito imediato de poções
        if (item.getTipo() == Item.Tipo.POCAO && item.getBonusAtributo() != null) {
            switch (item.getBonusAtributo()) {
                case "vitalidade":
                    j.curar(item.getBonusValor());
                    break;
                case "vigor": 
                    j.recuperarVigor(item.getBonusValor());
                    break;
            }
        }

        try {
            itemDAO.comprar(j.getId(), item.getId());
            jogadorDAO.salvarAtributos(j);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mostrarFeedback("✓ " + item.getNome() + " adquirido!");
        atualizarSangue();
        carregarItens();
    }

    private void mostrarFeedback(String msg) {
        labelFeedback.setText(msg);
        labelFeedback.setVisible(true);
    }

    @FXML private void onIrUpgrades() throws IOException { 
        App.setRoot("Upgrades");
    }
    @FXML private void onContinuar() throws IOException  {
        App.setRoot("Gameplay");
    }

    private Jogador jogador() {
        return Sessao.getInstance().getJogadorAtual(); 
    }
}
