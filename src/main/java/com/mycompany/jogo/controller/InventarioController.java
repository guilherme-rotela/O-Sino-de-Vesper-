
package com.mycompany.jogo.controller;
import com.mycompany.jogo.DAO.ItemDAO;
import com.mycompany.jogo.model.Item;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.util.Sessao;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
public class InventarioController implements Initializable {
    @FXML private Label            labelJogador;
    @FXML private ListView<String> listaArmas;
    @FXML private ListView<String> listaArmaduras;
    @FXML private ListView<String> listaPocoes;
    private final ItemDAO itemDAO = new ItemDAO();
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Jogador j = Sessao.getInstance().getJogadorAtual();
        if (j == null) return;
        labelJogador.setText(j.getNome() + "  ·  Nível " + j.getNivel()
            + "  ·  🩸 " + j.getPontosSangue());
        try {
            List<Item> inventario = itemDAO.listarInventario(j.getId());
            for (Item item : inventario) {
                String linha = item.getNome() + "  (x" + item.getQuantidade() + ")";
                switch (item.getTipo()) {
                    case ARMA: 
                        listaArmas.getItems().add(linha);
                    break;
                    case ARMADURA: 
                        listaArmaduras.getItems().add(linha);
                    break;
                    case POCAO: 
                        listaPocoes.getItems().add(linha);
                    break;
                }
            }
            if (listaArmas.getItems().isEmpty())     listaArmas.getItems().add("— Nenhuma arma —");
            if (listaArmaduras.getItems().isEmpty()) listaArmaduras.getItems().add("— Nenhuma armadura —");
            if (listaPocoes.getItems().isEmpty())    listaPocoes.getItems().add("— Nenhuma poção —");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onFechar() {
        Stage stage = (Stage) labelJogador.getScene().getWindow();
        stage.close();
    }
}