package com.mycompany.jogo.model;
 
public class Jogador {
 
    private int id;
    private String nome;
 
    // Atributos base (persistidos)
    private int vitalidade;
    private int vigor;
    private int tecnica;
    private int pontosSangue;
    private int totalXp;
    private int nivel;
 
    // Estado dinâmico durante a run (calculado a partir dos atributos)
    private int vidaMaxima;
    private int vidaAtual;
    private int vigorMaximo;
    private int vigorAtual;
    private int dano;
 
    public Jogador(int id, String nome, int vitalidade, int vigor, int tecnica,
                   int pontosSangue, int totalXp, int nivel) {
        this.id = id;
        this.nome = nome;
        this.vitalidade = vitalidade;
        this.vigor = vigor;
        this.tecnica = tecnica;
        this.pontosSangue = pontosSangue;
        this.totalXp = totalXp;
        this.nivel = nivel;
        calcularAtributosDerivados();
    }
 
    /** Recalcula vida/vigor/dano com base nos atributos base */
    public void calcularAtributosDerivados() {
        this.vidaMaxima  = 50 + (vitalidade * 5);
        this.vidaAtual   = vidaMaxima;
        this.vigorMaximo = 50 + (vigor * 3);
        this.vigorAtual  = vigorMaximo;
        this.dano        = 5  + (tecnica * 2);
    }
 
    /** Reseta vida e vigor para o início de uma nova run */
    public void resetarParaRun() {
        calcularAtributosDerivados();
    }
 
    public void receberDano(int quantidade) {
        vidaAtual = Math.max(0, vidaAtual - quantidade);
    }
 
    public void curar(int quantidade) {
        vidaAtual = Math.min(vidaMaxima, vidaAtual + quantidade);
    }
 
    public void gastarVigor(int quantidade) {
        vigorAtual = Math.max(0, vigorAtual - quantidade);
    }
 
    public void recuperarVigor(int quantidade) {
        vigorAtual = Math.min(vigorMaximo, vigorAtual + quantidade);
    }
 
    public boolean estaVivo() {
        return vidaAtual > 0;
    }
 
    public void adicionarSangue(int quantidade) {
        pontosSangue += quantidade;
    }
 
    public boolean gastarSangue(int quantidade) {
        if (pontosSangue >= quantidade) {
            pontosSangue -= quantidade;
            return true;
        }
        return false;
    }
 
    public void adicionarXp(int quantidade) {
        totalXp += quantidade;
        verificarNivel();
    }
 
    private void verificarNivel() {
        int novoNivel = 1 + (totalXp / 500);
        if (novoNivel > nivel) {
            nivel = novoNivel;
        }
    }
 
    public void aumentarVitalidade(int bonus) {
        vitalidade += bonus;
        calcularAtributosDerivados();
    }
 
    public void aumentarVigor(int bonus) {
        vigor += bonus;
        calcularAtributosDerivados();
    }
 
    public void aumentarTecnica(int bonus) {
        tecnica += bonus;
        calcularAtributosDerivados();
    }
    
    public void restaurarVida() {
        this.vidaAtual = this.vidaMaxima;
        this.vigorAtual = this.vigorMaximo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getVitalidade() {
        return vitalidade;
    }

    public void setVitalidade(int vitalidade) {
        this.vitalidade = vitalidade;
    }

    public int getVigor() {
        return vigor;
    }

    public void setVigor(int vigor) {
        this.vigor = vigor;
    }

    public int getTecnica() {
        return tecnica;
    }

    public void setTecnica(int tecnica) {
        this.tecnica = tecnica;
    }

    public int getPontosSangue() {
        return pontosSangue;
    }

    public void setPontosSangue(int pontosSangue) {
        this.pontosSangue = pontosSangue;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public int getVidaMaxima() {
        return vidaMaxima;
    }

    public void setVidaMaxima(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
    }

    public int getVidaAtual() {
        return vidaAtual;
    }

    public void setVidaAtual(int vidaAtual) {
        this.vidaAtual = vidaAtual;
    }

    public int getVigorMaximo() {
        return vigorMaximo;
    }

    public void setVigorMaximo(int vigorMaximo) {
        this.vigorMaximo = vigorMaximo;
    }

    public int getVigorAtual() {
        return vigorAtual;
    }

    public void setVigorAtual(int vigorAtual) {
        this.vigorAtual = vigorAtual;
    }

    public int getDano() {
        return dano;
    }

    public void setDano(int dano) {
        this.dano = dano;
    }
 
    
}
