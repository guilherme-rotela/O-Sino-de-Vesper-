package com.mycompany.jogo.controller;

import com.mycompany.jogo.DAO.JogadorDAO;
import com.mycompany.jogo.model.Inimigo;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.util.SceneManager;
import com.mycompany.jogo.util.Sessao;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class BossController implements Initializable {

    @FXML private Canvas      canvasBoss;
    @FXML private Pane        spritePane;
    @FXML private ProgressBar barraBoss, barraVida, barraVigor;
    @FXML private Label       labelVidaBoss, labelVida, labelVigor, labelSangue;
    @FXML private VBox        painelResultado;
    @FXML private Label       labelResultado, labelResultadoDesc;
    @FXML private Button      btnResultado;

    private ImageView jogadorSprite;
    private ImageView bossSprite;
    private ImageView projetilSprite;

    private Image imgJogador;
    private Image imgBoss;
    private Image imgProjetil;

    private static final double JOG_SPRITE_W  = 40;
    private static final double JOG_SPRITE_H  = 40;
    private static final double BOSS_SPRITE_W = 72;
    private static final double BOSS_SPRITE_H = 72;
    private static final double PROJ_SPRITE_W = 24;
    private static final double PROJ_SPRITE_H = 24;

    private double jogX, jogY;
    private static final double JOG_SIZE  = 20;
    private static final double JOG_SPEED = 3.0;
    private boolean esquivando = false;
    private long    esquivaInicio = 0;
    private static final long ESQUIVA_DUR = 400_000_000L;

    private boolean atacando = false;
    private long    ataqueInicio = 0;
    private static final long ATAQUE_DUR = 300_000_000L;

    private final Set<KeyCode> teclas = new HashSet<>();

    private Inimigo boss;
    private double bossX, bossY;
    private static final double BOSS_SIZE = 36;

    private double projX = -100, projY = -100;
    private double projDX, projDY;
    private boolean projAtivo = false;
    private long    ultimoProjetil = 0;
    private static final long INTERVALO_PROJ = 3_000_000_000L;

    private boolean fase2 = false;
    private AnimationTimer loop;
    private boolean emBatalha = true;

    private final JogadorDAO jogadorDAO = new JogadorDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        boss = new Inimigo(Inimigo.Tipo.BOSS_CATEDRAL, Sessao.getInstance().getFaseAtual());
        jogX  = 100;
        jogY  = canvasBoss.getHeight() / 2;
        bossX = canvasBoss.getWidth() - 100;
        bossY = canvasBoss.getHeight() / 2;

        carregarImagens();
        criarSprites();
        configurarTeclado();
        iniciarLoop();
    }

    private void carregarImagens() {
        imgJogador  = carregarImagem("jogador.png");
        imgBoss     = carregarImagem("boss.png");
        imgProjetil = carregarImagem("projetil.png");
    }

    private Image carregarImagem(String nome) {
        try {
            URL url = getClass().getResource("/com/sinodevesper/images/" + nome);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception e) {
            System.err.println("[BossController] Imagem não encontrada: " + nome);
        }
        return null;
    }

    private ImageView criarImageView(Image img, double w, double h) {
        ImageView iv = new ImageView();
        if (img != null) iv.setImage(img);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    private void criarSprites() {
        jogadorSprite  = criarImageView(imgJogador,  JOG_SPRITE_W,  JOG_SPRITE_H);
        bossSprite     = criarImageView(imgBoss,     BOSS_SPRITE_W, BOSS_SPRITE_H);
        projetilSprite = criarImageView(imgProjetil, PROJ_SPRITE_W, PROJ_SPRITE_H);
        projetilSprite.setVisible(false);

        posicionarSprite(jogadorSprite, jogX,  jogY,  JOG_SPRITE_W,  JOG_SPRITE_H);
        posicionarSprite(bossSprite,    bossX, bossY, BOSS_SPRITE_W, BOSS_SPRITE_H);

        spritePane.getChildren().addAll(jogadorSprite, bossSprite, projetilSprite);
    }

    private void posicionarSprite(ImageView iv, double cx, double cy, double w, double h) {
        iv.setLayoutX(cx - w / 2.0);
        iv.setLayoutY(cy - h / 2.0);
    }

    private void configurarTeclado() {
        canvasBoss.sceneProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                novo.setOnKeyPressed(e  -> teclas.add(e.getCode()));
                novo.setOnKeyReleased(e -> teclas.remove(e.getCode()));
                canvasBoss.requestFocus();
            }
        });
        canvasBoss.setFocusTraversable(true);
    }

    private void iniciarLoop() {
        loop = new AnimationTimer() {
            @Override public void handle(long agora) {
                if (!emBatalha) return;
                processarEntrada(agora);
                moverBoss();
                moverProjetil(agora);
                verificarAtaqueJogador(agora);
                verificarColisoes(agora);
                renderizar(agora);
                atualizarSprites();
                atualizarHUD();
                verificarCondicoes();
            }
        };
        loop.start();
    }

    private void processarEntrada(long agora) {
        if (teclas.contains(KeyCode.K) && !esquivando) {
            Jogador j = jogador();
            if (j.getVigorAtual() >= 20) {
                esquivando = true; esquivaInicio = agora;
                j.gastarVigor(20);
            }
        }
        if (esquivando && agora - esquivaInicio > ESQUIVA_DUR) esquivando = false;

        if (teclas.contains(KeyCode.J) && !atacando) { atacando = true; ataqueInicio = agora; }
        if (atacando && agora - ataqueInicio > ATAQUE_DUR) atacando = false;

        double speed = esquivando ? JOG_SPEED * 2.5 : JOG_SPEED;
        double nx = jogX, ny = jogY;
        if (teclas.contains(KeyCode.W)) ny -= speed;
        if (teclas.contains(KeyCode.S)) ny += speed;
        if (teclas.contains(KeyCode.A)) nx -= speed;
        if (teclas.contains(KeyCode.D)) nx += speed;

        nx = Math.max(JOG_SIZE, Math.min(canvasBoss.getWidth()  - JOG_SIZE, nx));
        ny = Math.max(JOG_SIZE, Math.min(canvasBoss.getHeight() - JOG_SIZE, ny));
        jogX = nx; jogY = ny;
        jogador().recuperarVigor(1);
    }

    private void moverBoss() {
        double vel = fase2 ? boss.getVelocidade() * 1.8 : boss.getVelocidade();
        double dx = jogX - bossX, dy = jogY - bossY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) { bossX += (dx / dist) * vel; bossY += (dy / dist) * vel; }
        if (!fase2 && boss.getPorcentagemVida() < 0.5) fase2 = true;
    }

    private void moverProjetil(long agora) {
        long intervalo = fase2 ? 2_000_000_000L : INTERVALO_PROJ;
        if (!projAtivo && agora - ultimoProjetil > intervalo) {
            projX = bossX; projY = bossY;
            double dx = jogX - bossX, dy = jogY - bossY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) { projDX = (dx / dist) * 4; projDY = (dy / dist) * 4; }
            projAtivo = true; ultimoProjetil = agora;
        }
        if (projAtivo) {
            projX += projDX; projY += projDY;
            if (projX < 0 || projX > canvasBoss.getWidth() ||
                projY < 0 || projY > canvasBoss.getHeight())
                projAtivo = false;
        }
    }

    private void verificarAtaqueJogador(long agora) {
        if (!atacando) return;
        double dist = Math.sqrt(Math.pow(bossX - jogX, 2) + Math.pow(bossY - jogY, 2));
        if (dist <= JOG_SIZE + 40) {
            boss.receberDano(jogador().getDano());
            if (!boss.estaVivo()) { emBatalha = false; loop.stop(); onVitoria(); }
        }
    }

    private long ultimoDanoRecebido = 0;
    private static final long INTERVALO_DANO = 800_000_000L;

    private void verificarColisoes(long agora) {
        if (esquivando) return;
        if (agora - ultimoDanoRecebido < INTERVALO_DANO) return;

        double dist = Math.sqrt(Math.pow(bossX - jogX, 2) + Math.pow(bossY - jogY, 2));
        if (dist < JOG_SIZE + BOSS_SIZE) {
            jogador().receberDano(boss.getDano()); ultimoDanoRecebido = agora;
        }

        if (projAtivo) {
            double dp = Math.sqrt(Math.pow(projX - jogX, 2) + Math.pow(projY - jogY, 2));
            if (dp < JOG_SIZE + 8) {
                jogador().receberDano(boss.getDano() / 2);
                projAtivo = false; ultimoDanoRecebido = agora;
            }
        }

        if (!jogador().estaVivo()) { emBatalha = false; loop.stop(); onDerrota(); }
    }

    private void renderizar(long agora) {
        GraphicsContext gc = canvasBoss.getGraphicsContext2D();
        double W = canvasBoss.getWidth(), H = canvasBoss.getHeight();

        gc.setFill(fase2 ? Color.web("#0a0005") : Color.web("#0d0808"));
        gc.fillRect(0, 0, W, H);

        if (fase2 && (agora / 200_000_000L) % 3 == 0) {
            gc.setStroke(Color.web("#660033", 0.15));
            gc.setLineWidth(1);
            for (int i = 0; i < 5; i++) {
                double rx = Math.random() * W;
                gc.strokeLine(rx, 0, rx + (Math.random() * 40 - 20), H);
            }
        }

        gc.setStroke(Color.web("#1a0010", 0.3));
        gc.setLineWidth(0.5);
        for (double x = 0; x < W; x += 60) gc.strokeLine(x, 0, x, H);
        for (double y = 0; y < H; y += 60) gc.strokeLine(0, y, W, y);

        gc.setFill(Color.web("#c8a96e"));
        gc.fillText("O Sacerdote do Sino" + (fase2 ? " ☠" : ""),
                    bossX - 70, bossY - BOSS_SPRITE_H / 2 - 10);

        gc.setStroke(Color.web("#cc0000", 0.3));
        gc.setLineWidth(3);
        gc.strokeOval(bossX - BOSS_SIZE - 6, bossY - BOSS_SIZE - 6,
                      (BOSS_SIZE + 6) * 2, (BOSS_SIZE + 6) * 2);

        if (atacando) {
            gc.setStroke(Color.web("#c8a96e", 0.3));
            gc.setLineWidth(1.5);
            gc.strokeOval(jogX - (JOG_SIZE + 40), jogY - (JOG_SIZE + 40),
                          (JOG_SIZE + 40) * 2, (JOG_SIZE + 40) * 2);
        }

        if (esquivando) {
            gc.setStroke(Color.web("#ffffff", 0.3));
            gc.setLineWidth(2);
            gc.strokeOval(jogX - JOG_SIZE - 5, jogY - JOG_SIZE - 5,
                          (JOG_SIZE + 5) * 2, (JOG_SIZE + 5) * 2);
        }
    }

    private void atualizarSprites() {
        jogadorSprite.setOpacity(esquivando ? 0.5 : 1.0);
        posicionarSprite(jogadorSprite, jogX, jogY, JOG_SPRITE_W, JOG_SPRITE_H);

        double bossOpacidade = fase2 ? (System.nanoTime() / 150_000_000L % 2 == 0 ? 0.7 : 1.0) : 1.0;
        bossSprite.setOpacity(bossOpacidade);
        posicionarSprite(bossSprite, bossX, bossY, BOSS_SPRITE_W, BOSS_SPRITE_H);

        projetilSprite.setVisible(projAtivo);
        if (projAtivo) posicionarSprite(projetilSprite, projX, projY, PROJ_SPRITE_W, PROJ_SPRITE_H);
    }

    private void atualizarHUD() {
        Jogador j = jogador();
        barraBoss.setProgress(boss.getPorcentagemVida());
        labelVidaBoss.setText(boss.getVidaAtual() + "/" + boss.getVidaMaxima());
        barraVida.setProgress((double) j.getVidaAtual() / j.getVidaMaxima());
        barraVigor.setProgress((double) j.getVigorAtual() / j.getVigorMaximo());
        labelVida.setText("❤ " + j.getVidaAtual() + "/" + j.getVidaMaxima());
        labelVigor.setText("◈ " + j.getVigorAtual() + "/" + j.getVigorMaximo());
        labelSangue.setText("🩸 " + j.getPontosSangue());
    }

    private void verificarCondicoes() {
        if (!jogador().estaVivo()) { emBatalha = false; loop.stop(); onDerrota(); }
    }

    private void onVitoria() {
        Sessao gs = Sessao.getInstance();
        gs.registrarInimigomorto(boss.getRecompensaSangue(), boss.getRecompensaXp());
        salvarPartida(true);
        labelResultado.setText("✝  SINO SILENCIADO  ✝");
        labelResultadoDesc.setText("O Sacerdote caiu. Vesper respira novamente.\n\n"
            + "+" + boss.getRecompensaSangue() + " de sangue  ·  +" + boss.getRecompensaXp() + " XP");
        btnResultado.setText("Ver Ranking");
        painelResultado.setVisible(true);
    }

    private void onDerrota() {
        salvarPartida(false);
        labelResultado.setText("VOCÊ MORREU");
        labelResultadoDesc.setText("O Sacerdote do Sino reclama mais uma alma.\nTente novamente.");
        btnResultado.setText("Tentar Novamente");
        painelResultado.setVisible(true);
    }

    @FXML private void onContinuarAposResultado() {
        if (jogador().estaVivo()) SceneManager.navigateTo("Ranking.fxml");
        else { 
            Sessao.getInstance().iniciarNovaRun(); 
            SceneManager.navigateTo("Upgrades.fxml"); 
        }
    }

    private void salvarPartida(boolean vitoria) {
        try {
            Sessao gs = Sessao.getInstance();
            jogadorDAO.salvarAtributos(jogador());
            jogadorDAO.salvarPartida(jogador().getId(), gs.getFaseAtual(),
                gs.getInimigosMotosNaRun(), gs.getSangueGanhoNaRun(), gs.getXpGanhoNaRun(), vitoria);
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    private Jogador jogador() {
        return Sessao.getInstance().getJogadorAtual(); 
    }
}