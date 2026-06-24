package com.mycompany.jogo.model;

public class Item {

    public enum Tipo { ARMA, ARMADURA, POCAO }

    private int id;
    private String nome;
    private String descricao;
    private Tipo tipo;
    private int custoSangue;
    private String bonusAtributo;
    private int bonusValor;
    private int quantidade;

    public Item(int id, String nome, String descricao, Tipo tipo,
                int custoSangue, String bonusAtributo, int bonusValor) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.custoSangue = custoSangue;
        this.bonusAtributo = bonusAtributo;
        this.bonusValor = bonusValor;
        this.quantidade = 1;
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
    public Tipo getTipo() {
        return tipo;
    }
    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
    public int getCustoSangue() {
        return custoSangue;
    }
    public void setCustoSangue(int custoSangue) {
        this.custoSangue = custoSangue;
    }
    public String getBonusAtributo() {
        return bonusAtributo;
    }
    public void setBonusAtributo(String bonusAtributo) {
        this.bonusAtributo = bonusAtributo;
    }
    public int getBonusValor() {
        return bonusValor;
    }
    public void setBonusValor(int bonusValor) {
        this.bonusValor = bonusValor;
    }
    public int getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

}