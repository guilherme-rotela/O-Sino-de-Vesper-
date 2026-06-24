package com.mycompany.jogo.DAO;

import com.mycompany.jogo.model.Ranking;
import com.mycompany.jogo.util.Conexao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RankingDAO {

    public List<Ranking> listar() throws SQLException {
        List<Ranking> lista = new ArrayList<>();
        String sql = "SELECT * FROM ranking ORDER BY total_xp DESC LIMIT 20";
        try (Statement st = Conexao.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Ranking(
                    rs.getString("nome"),
                    rs.getInt("nivel"),
                    rs.getInt("total_xp"),
                    rs.getInt("pontos_sangue"),
                    rs.getInt("total_partidas"),
                    rs.getInt("total_inimigos_mortos"),
                    rs.getInt("vitorias")
                ));
            }
        }
        return lista;
    }

    public String exportarCSV(List<Ranking> ranking) {
        StringBuilder sb = new StringBuilder();
        sb.append("Posição,Nome,Nível,XP Total,Pontos de Sangue,Partidas,Inimigos Mortos,Vitórias\n");
        int pos = 1;
        for (Ranking e : ranking) {
            sb.append(pos++).append(",")
              .append(e.getNomeJogador()).append(",")
              .append(e.getNivel()).append(",")
              .append(e.getTotalXp()).append(",")
              .append(e.getPontosSangue()).append(",")
              .append(e.getTotalPartidas()).append(",")
              .append(e.getTotalInimigos()).append(",")
              .append(e.getVitorias()).append("\n");
        }
        return sb.toString();
    }
}
