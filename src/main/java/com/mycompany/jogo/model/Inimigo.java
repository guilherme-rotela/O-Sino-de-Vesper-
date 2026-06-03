package com.mycompany.jogo.model;
 
public class Inimigo {
 
    public enum Tipo {
        PESCADOR_FERA,
        LOBISOMEM,
        BOSS_CATEDRAL
    }
 
    private String nome;
    private Tipo tipo;
    private int vidaMaxima;
    private int vidaAtual;
    private int dano;
    private int velocidade;
    private int recompensaSangue;
    private int recompensaXp;
 
    // Posição no mapa (usada pelo GameplayController)
    private double x;
    private double y;
 
    // Estado
    private boolean vivo = true;
 
    public Inimigo(Tipo tipo, int faseAtual) {
        this.tipo = tipo;
        configurar(tipo, faseAtual);
    }
 
    private void configurar(Tipo tipo, int fase) {
        // Escala de dificuldade: cada fase aumenta 20% dos atributos
        double escala = 1.0 + (fase - 1) * 0.2;
 
        switch (tipo) {
            case PESCADOR_FERA:
                this.nome             = "Pescador das Trevas";
                this.vidaMaxima       = (int)(60  * escala);
                this.dano             = (int)(8   * escala);
                this.velocidade       = (int)(2   * escala);
                this.recompensaSangue = (int)(30  * escala);
                this.recompensaXp     = (int)(20  * escala);
            
            case LOBISOMEM:
                this.nome             = "Lobisomem de Vesper";
                this.vidaMaxima       = (int)(100 * escala);
                this.dano             = (int)(15  * escala);
                this.velocidade       = (int)(3   * escala);
                this.recompensaSangue = (int)(60  * escala);
                this.recompensaXp     = (int)(40  * escala);
            
            case BOSS_CATEDRAL:
                this.nome             = "O Sacerdote do Sino";
                this.vidaMaxima       = (int)(500 * escala);
                this.dano             = (int)(30  * escala);
                this.velocidade       = 2;
                this.recompensaSangue = (int)(500 * escala);
                this.recompensaXp     = (int)(300 * escala);
            
        }
        this.vidaAtual = this.vidaMaxima;
    }
 
    public void receberDano(int quantidade) {
        vidaAtual = Math.max(0, vidaAtual - quantidade);
        if (vidaAtual == 0) vivo = false;
    }
 
    public boolean estaVivo() { return vivo; }
 
    public double getPorcentagemVida() {
        return (double) vidaAtual / vidaMaxima;
    }
 
    // Getters
    public String getNome()          { return nome; }
    public Tipo getTipo()            { return tipo; }
    public int getVidaMaxima()       { return vidaMaxima; }
    public int getVidaAtual()        { return vidaAtual; }
    public int getDano()             { return dano; }
    public int getVelocidade()       { return velocidade; }
    public int getRecompensaSangue() { return recompensaSangue; }
    public int getRecompensaXp()     { return recompensaXp; }
    public double getX()             { return x; }
    public double getY()             { return y; }
    public void setX(double x)       { this.x = x; }
    public void setY(double y)       { this.y = y; }
}
