package com.mycompany.jogo.util;

import com.mycompany.jogo.model.Jogador;
import java.util.HashSet;
import java.util.Set;



public class Sessao {

    private static Sessao instance;

    private Jogador jogadorAtual;
    private int faseAtual = 1;
    private int inimigosMotosNaRun = 0;
    private int sangueGanhoNaRun = 0;
    private int xpGanhoNaRun = 0;

    private Sessao() {}

    public static Sessao getInstance() {
        if (instance == null) {
            instance = new Sessao();
        }
        return instance;
    }

    public void iniciarNovaRun() {
        this.faseAtual = 1;
        this.inimigosMotosNaRun = 0;
        this.sangueGanhoNaRun = 0;
        this.xpGanhoNaRun = 0;
        if (jogadorAtual != null) {
            jogadorAtual.resetarParaRun();
        }
    }

    public void registrarInimigomorto(int sangue, int xp) {
        inimigosMotosNaRun++;
        sangueGanhoNaRun += sangue;
        xpGanhoNaRun += xp;
        if (jogadorAtual != null) {
            jogadorAtual.adicionarSangue(sangue);
            jogadorAtual.adicionarXp(xp);
        }
    }
    
    private final Set<Integer> itensCompradosNaRun = new HashSet<>();

    public boolean jaComprou(int itemId) {
        return itensCompradosNaRun.contains(itemId);
    }

    public void registrarCompra(int itemId) {
        itensCompradosNaRun.add(itemId);
    }

    public void avancarFase() {
        faseAtual++;
    }

    public Jogador getJogadorAtual() {
        return jogadorAtual;
    }

    public void setJogadorAtual(Jogador jogadorAtual) {
        this.jogadorAtual = jogadorAtual;
    }

    public int getFaseAtual() {
        return faseAtual;
    }

    public void setFaseAtual(int faseAtual) {
        this.faseAtual = faseAtual;
    }

    public int getInimigosMotosNaRun() {
        return inimigosMotosNaRun;
    }

    public void setInimigosMotosNaRun(int inimigosMotosNaRun) {
        this.inimigosMotosNaRun = inimigosMotosNaRun;
    }

    public int getSangueGanhoNaRun() {
        return sangueGanhoNaRun;
    }

    public void setSangueGanhoNaRun(int sangueGanhoNaRun) {
        this.sangueGanhoNaRun = sangueGanhoNaRun;
    }

    public int getXpGanhoNaRun() {
        return xpGanhoNaRun;
    }

    public void setXpGanhoNaRun(int xpGanhoNaRun) {
        this.xpGanhoNaRun = xpGanhoNaRun;
    }

    
}
