package com.mycompany.jogo.model;

public class Upgrade {

    private int id;
    private String nome;
    private String descricao;
    private String tipo; // vitalidade, vigor, tecnica
    private int custoSangue;
    private int bonusValor;

    public Upgrade(int id, String nome, String descricao,
                   String tipo, int custoSangue, int bonusValor) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.custoSangue = custoSangue;
        this.bonusValor = bonusValor;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCustoSangue() {
        return custoSangue;
    }

    public void setCustoSangue(int custoSangue) {
        this.custoSangue = custoSangue;
    }

    public int getBonusValor() {
        return bonusValor;
    }

    public void setBonusValor(int bonusValor) {
        this.bonusValor = bonusValor;
    }

    
}