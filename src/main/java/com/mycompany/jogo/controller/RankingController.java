package com.mycompany.jogo.controller;

import com.mycompany.jogo.DAO.RankingDAO;
import com.mycompany.jogo.model.Ranking;
import com.mycompany.jogo.util.SceneManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class RankingController implements Initializable {

    @FXML private TableView<Ranking>         tabelaRanking;
    @FXML private TableColumn<Ranking, String>  colPos;
    @FXML private TableColumn<Ranking, String>  colNome;
    @FXML private TableColumn<Ranking, Integer> colNivel;
    @FXML private TableColumn<Ranking, Integer> colXp;
    @FXML private TableColumn<Ranking, Integer> colSangue;
    @FXML private TableColumn<Ranking, Integer> colPartidas;
    @FXML private TableColumn<Ranking, Integer> colInimigos;
    @FXML private TableColumn<Ranking, Integer> colVitorias;
    @FXML private Label labelExportFeedback;

    private final RankingDAO rankingDAO = new RankingDAO();
    private List<Ranking> entradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColunas();
        carregarRanking();
    }

    private void configurarColunas() {
        AtomicInteger pos = new AtomicInteger(1);

        colPos.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(
                tabelaRanking.getItems().indexOf(c.getValue()) + 1
            ))
        );
        colNome.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getNomeJogador()));
        colNivel.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getNivel()).asObject());
        colXp.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getTotalXp()).asObject());
        colSangue.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getPontosSangue()).asObject());
        colPartidas.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getTotalPartidas()).asObject());
        colInimigos.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getTotalInimigos()).asObject());
        colVitorias.setCellValueFactory(c ->
            new SimpleIntegerProperty(c.getValue().getVitorias()).asObject());
    }

    private void carregarRanking() {
        try {
            entradas = rankingDAO.listar();
            tabelaRanking.getItems().setAll(entradas);
        } catch (SQLException e) {
            e.printStackTrace();
            tabelaRanking.setPlaceholder(
                new Label("Erro ao carregar ranking. Verifique o banco de dados.")
            );
        }
    }

    @FXML
    private void onExportar() {
        if (entradas == null || entradas.isEmpty()) {
            labelExportFeedback.setText("Nenhum dado para exportar.");
            labelExportFeedback.setVisible(true);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar Ranking");
        chooser.setInitialFileName("ranking_sino_de_vesper.csv");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV", "*.csv"),
            new FileChooser.ExtensionFilter("TXT", "*.txt")
        );

        File arquivo = chooser.showSaveDialog(tabelaRanking.getScene().getWindow());
        if (arquivo == null) return;

        try (FileWriter fw = new FileWriter(arquivo)) {
            fw.write(rankingDAO.exportarCSV(entradas));
            labelExportFeedback.setText("✓ Exportado: " + arquivo.getName());
            labelExportFeedback.setVisible(true);
        } catch (Exception e) {
            labelExportFeedback.setText("Erro ao exportar arquivo.");
            labelExportFeedback.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML private void onVoltar() { 
        SceneManager.navigateTo("Menu.fxml"); 
    }
}
