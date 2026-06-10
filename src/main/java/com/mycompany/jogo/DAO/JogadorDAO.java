package com.mycompany.jogo.DAO;

import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.util.Conexao;

import java.sql.*;

public class JogadorDAO {

    /** Busca jogador pelo nome. Retorna null se não existir. */
    public Jogador buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT j.id, j.nome, ja.vitalidade, ja.vigor, ja.tecnica, " +
                     "ja.pontos_sangue, ja.total_xp, ja.nivel " +
                     "FROM jogadores j JOIN jogador_atributos ja ON j.id = ja.jogador_id " +
                     "WHERE j.nome = ?";
        try (PreparedStatement ps = Conexao.getConnection().prepareStatement(sql)) {
            ps.setString(1, nome);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Jogador(
                    rs.getInt("id"), rs.getString("nome"),
                    rs.getInt("vitalidade"), rs.getInt("vigor"),
                    rs.getInt("tecnica"), rs.getInt("pontos_sangue"),
                    rs.getInt("total_xp"), rs.getInt("nivel")
                );
            }
        }
        return null;
    }

    /** Cria novo jogador com atributos iniciais. Retorna o jogador criado. */
    public Jogador criar(String nome) throws SQLException {
        Connection conn = Conexao.getConnection();
        conn.setAutoCommit(false);
        try {
            // Insere jogador
            String sqlJ = "INSERT INTO jogadores (nome) VALUES (?) RETURNING id";
            int jogadorId;
            try (PreparedStatement ps = conn.prepareStatement(sqlJ)) {
                ps.setString(1, nome);
                ResultSet rs = ps.executeQuery();
                rs.next();
                jogadorId = rs.getInt("id");
            }
            // Insere atributos iniciais
            String sqlA = "INSERT INTO jogador_atributos (jogador_id) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlA)) {
                ps.setInt(1, jogadorId);
                ps.executeUpdate();
            }
            conn.commit();
            return new Jogador(jogadorId, nome, 10, 10, 10, 0, 0, 1);
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Salva atributos atuais do jogador no banco */
    public void salvarAtributos(Jogador j) throws SQLException {
        String sql = "UPDATE jogador_atributos SET vitalidade=?, vigor=?, tecnica=?, " +
                     "pontos_sangue=?, total_xp=?, nivel=? WHERE jogador_id=?";
        try (PreparedStatement ps = Conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, j.getVitalidade());
            ps.setInt(2, j.getVigor());
            ps.setInt(3, j.getTecnica());
            ps.setInt(4, j.getPontosSangue());
            ps.setInt(5, j.getTotalXp());
            ps.setInt(6, j.getNivel());
            ps.setInt(7, j.getId());
            ps.executeUpdate();
        }
    }

    /** Salva histórico de uma partida */
    public void salvarPartida(int jogadorId, int faseAlcancada, int inimigosMortos,
                               int sangueGanho, int xpGanho, boolean vitoria) throws SQLException {
        String sql = "INSERT INTO partidas (jogador_id, fase_alcancada, inimigos_mortos, " +
                     "sangue_ganho, xp_ganho, vitoria) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = Conexao.getConnection().prepareStatement(sql)) {
            ps.setInt(1, jogadorId);
            ps.setInt(2, faseAlcancada);
            ps.setInt(3, inimigosMortos);
            ps.setInt(4, sangueGanho);
            ps.setInt(5, xpGanho);
            ps.setBoolean(6, vitoria);
            ps.executeUpdate();
        }
    }
}
