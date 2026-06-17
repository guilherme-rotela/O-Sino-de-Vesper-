package com.mycompany.jogo.model;

public class Ranking {

    private String nomeJogador;
    private int nivel;
    private int totalXp;
    private int pontosSangue;
    private int totalPartidas;
    private int totalInimigos;
    private int vitorias;

    public Ranking(String nomeJogador, int nivel, int totalXp, int pontosSangue, int totalPartidas, int totalInimigos, int vitorias) {
        this.nomeJogador = nomeJogador;
        this.nivel = nivel;
        this.totalXp = totalXp;
        this.pontosSangue = pontosSangue;
        this.totalPartidas = totalPartidas;
        this.totalInimigos = totalInimigos;
        this.vitorias = vitorias;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public void setNomeJogador(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public int getPontosSangue() {
        return pontosSangue;
    }

    public void setPontosSangue(int pontosSangue) {
        this.pontosSangue = pontosSangue;
    }

    public int getTotalPartidas() {
        return totalPartidas;
    }

    public void setTotalPartidas(int totalPartidas) {
        this.totalPartidas = totalPartidas;
    }

    public int getTotalInimigos() {
        return totalInimigos;
    }

    public void setTotalInimigos(int totalInimigos) {
        this.totalInimigos = totalInimigos;
    }

    public int getVitorias() {
        return vitorias;
    }

    public void setVitorias(int vitorias) {
        this.vitorias = vitorias;
    }

    
}
