package com.mycompany.jogo.DAO;

import com.mycompany.jogo.util.Conexao;
import com.mycompany.jogo.model.Upgrade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UpgradeDAO {

    public List<Upgrade> listarTodos() throws SQLException {
        List<Upgrade> lista = new ArrayList<>();
        String sql = "SELECT * FROM upgrades ORDER BY tipo, custo_sangue";
        try (Statement st = Conexao.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Upgrade(
                    rs.getInt("id"), rs.getString("nome"),
                    rs.getString("descricao"), rs.getString("tipo"),
                    rs.getInt("custo_sangue"), rs.getInt("bonus_valor")
                ));
            }
        }
        return lista;
    }

    public List<Integer> listarIdsAdquiridos(int jogadorId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT upgrade_id FROM jogador_upgrades WHERE jogador_id = ?";
        try (PreparedStatement ps = Conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jogadorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("upgrade_id"));
        }
        return ids;
    }

    public void adquirir(int jogadorId, int upgradeId) throws SQLException {
        String sql = "INSERT INTO jogador_upgrades (jogador_id, upgrade_id) VALUES (?,?) " +
                     "ON CONFLICT (jogador_id, upgrade_id) DO UPDATE SET quantidade = jogador_upgrades.quantidade + 1";
        try (PreparedStatement ps = Conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jogadorId);
            ps.setInt(2, upgradeId);
            ps.executeUpdate();
        }
    }
}
