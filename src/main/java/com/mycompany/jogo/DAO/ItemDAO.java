
package com.mycompany.jogo.DAO;

import com.mycompany.jogo.model.Item;
import com.mycompany.jogo.util.Conexao;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lucas
 */
public class ItemDAO {
    public List<Item> listarLoja() throws SQLException {
        List<Item> lista = new ArrayList<>();
        String sql = "SELECT * FROM itens_loja ORDER BY tipo, custo_sangue";
        try (Statement st = Conexao.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Item(
                    rs.getInt("id"), rs.getString("nome"),
                    rs.getString("descricao"),
                    Item.Tipo.valueOf(rs.getString("tipo").toUpperCase()),
                    rs.getInt("custo_sangue"),
                    rs.getString("bonus_atributo"),
                    rs.getInt("bonus_valor")
                ));
            }
        }
        return lista;
    }

    public List<Item> listarInventario(int jogadorId) throws SQLException {
        List<Item> lista = new ArrayList<>();
        String sql = "SELECT il.*, inv.quantidade FROM inventario inv " +
                     "JOIN itens_loja il ON inv.item_id = il.id " +
                     "WHERE inv.jogador_id = ?";
        try (PreparedStatement ps = Conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jogadorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Item item = new Item(
                    rs.getInt("id"), rs.getString("nome"),
                    rs.getString("descricao"),
                    Item.Tipo.valueOf(rs.getString("tipo").toUpperCase()),
                    rs.getInt("custo_sangue"),
                    rs.getString("bonus_atributo"),
                    rs.getInt("bonus_valor")
                );
                item.setQuantidade(rs.getInt("quantidade"));
                lista.add(item);
            }
        }
        return lista;
    }

    public void comprar(int jogadorId, int itemId) throws SQLException {
        String sql = "INSERT INTO inventario (jogador_id, item_id) VALUES (?,?) " +
                     "ON CONFLICT DO NOTHING";
        // Verifica se já tem
        String sqlCheck = "SELECT id, quantidade FROM inventario WHERE jogador_id=? AND item_id=?";
        try (PreparedStatement ps = Conexao.getConnection().prepareStatement(sqlCheck)) {
            ps.setInt(1, jogadorId);
            ps.setInt(2, itemId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Incrementa quantidade
                String upd = "UPDATE inventario SET quantidade = quantidade + 1 WHERE jogador_id=? AND item_id=?";
                try (PreparedStatement upPs = Conexao.getConnection().prepareStatement(upd)) {
                    upPs.setInt(1, jogadorId);
                    upPs.setInt(2, itemId);
                    upPs.executeUpdate();
                }
            } else {
                try (PreparedStatement ins = Conexao.getConnection().prepareStatement(sql)) {
                    ins.setInt(1, jogadorId);
                    ins.setInt(2, itemId);
                    ins.executeUpdate();
                }
            }
        }
    }
}
