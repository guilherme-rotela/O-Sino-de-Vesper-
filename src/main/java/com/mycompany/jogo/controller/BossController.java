package com.mycompany.jogo.controller;

import com.mycompany.jogo.App;
import com.mycompany.jogo.DAO.JogadorDAO;
import com.mycompany.jogo.model.Inimigo;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.util.SceneManager;
import com.mycompany.jogo.util.Sessao;
import com.mycompany.jogo.util.SpriteAnimacao;
import java.io.IOException;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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
import javafx.scene.layout.StackPane;

public class BossController implements Initializable {

    // ── FXML ──────────────────────────────────────────────────────────────────
    @FXML private Canvas      canvasBoss;
    @FXML private Pane        spritePane;
    @FXML private ProgressBar barraBoss, barraVida, barraVigor;
    @FXML private Label       labelVidaBoss, labelVida, labelVigor, labelSangue;
    @FXML private VBox        painelResultado;
    @FXML private Label       labelResultado, labelResultadoDesc;
    @FXML private Button      btnResultado;

    // ── Sprites ───────────────────────────────────────────────────────────────
    private ImageView jogadorSprite;
    private ImageView bossSprite;

    // ── Imagens estáticas do jogador ──────────────────────────────────────────
    private Image imgJogador;
    private Image imgJogadorFrente;
    private Image imgJogadorTras;
    private Image imgJogadorEsquerda;

    // ── Spritesheets do jogador ───────────────────────────────────────────────
    private Image sheetAndando;
    private Image sheetAndandoEsquerda;
    private Image sheetAtaque;
    private Image sheetAtaqueEsquerda;

    // ── Animações do jogador ──────────────────────────────────────────────────
    private SpriteAnimacao animAndando;
    private SpriteAnimacao animAndandoEsquerda;
    private SpriteAnimacao animAtaque;
    private SpriteAnimacao animAtaqueEsquerda;

    // ── Direção do jogador ────────────────────────────────────────────────────
    private String direcaoJogador = "frente";

    // ── Imagen do boss ────────────────────────────────────────────
    private Image imgBoss;

    // ── Tamanhos dos sprites ──────────────────────────────────────────────────
    private static final double JOG_SPRITE_W  = 64;
    private static final double JOG_SPRITE_H  = 64;
    private static final double BOSS_SPRITE_W = 72;
    private static final double BOSS_SPRITE_H = 72;

    // ── Estado do jogador ─────────────────────────────────────────────────────
    private double jogX, jogY;
    private static final double JOG_SIZE  = 20;
    private static final double JOG_SPEED = 3.0;

    private boolean esquivando    = false;
    private long    esquivaInicio = 0;
    private static final long ESQUIVA_DUR = 400_000_000L;

    private boolean atacando     = false;
    private long    ataqueInicio = 0;
    private static final long ATAQUE_DUR = 300_000_000L;

    // ── Teclas ────────────────────────────────────────────────────────────────
    private final Set<KeyCode> teclas = new HashSet<>();

    // ── Boss ──────────────────────────────────────────────────────────────────
    private Inimigo boss;
    private double  bossX, bossY;
    private static final double BOSS_SIZE = 36;


    // ── Game loop ─────────────────────────────────────────────────────────────
    private boolean fase2     = false;
    private AnimationTimer loop;
    private boolean emBatalha = true;

    private boolean inicializado = false;

    private final JogadorDAO jogadorDAO = new JogadorDAO();

    // ── initialize ────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        boss = new Inimigo(Inimigo.Tipo.BOSS_CATEDRAL, Sessao.getInstance().getFaseAtual());

        carregarImagens();

        jogadorSprite  = criarImageView(imgJogador,  JOG_SPRITE_W,  JOG_SPRITE_H);
        bossSprite     = criarImageView(imgBoss,     BOSS_SPRITE_W, BOSS_SPRITE_H);
        
        jogadorSprite  = criarImageView(imgJogador,  JOG_SPRITE_W,  JOG_SPRITE_H);
        bossSprite     = criarImageView(imgBoss,     BOSS_SPRITE_W, BOSS_SPRITE_H);

        // ← adicione estas duas linhas:
        spritePane.getChildren().add(jogadorSprite);
        spritePane.getChildren().add(bossSprite);

        configurarTeclado();

        Platform.runLater(() -> {
            configurarRedimensionamento();   
            tentarIniciar();                 
        });
    }

    private void configurarRedimensionamento() {
        if (canvasBoss == null || spritePane == null) return;
        if (canvasBoss.getParent() instanceof StackPane) {
            bindAoStackPane((StackPane) canvasBoss.getParent());
        } else {
            canvasBoss.parentProperty().addListener((obs, antigo, novo) -> {
                if (novo instanceof StackPane) bindAoStackPane((StackPane) novo);
            });
        }
    }

    private void bindAoStackPane(StackPane sp) {
        canvasBoss.widthProperty().bind(sp.widthProperty());
        canvasBoss.heightProperty().bind(sp.heightProperty());
        spritePane.prefWidthProperty().bind(sp.widthProperty());
        spritePane.prefHeightProperty().bind(sp.heightProperty());

        // Listener para quando o tamanho finalmente chegar
        sp.widthProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 0 && !inicializado) tentarIniciar();
        });
    }

    private void tentarIniciar() {
        double W = canvasBoss.getWidth();
        double H = canvasBoss.getHeight();
        if (W > 0 && H > 0 && !inicializado) {
            inicializado = true;
            posicionarPersonagens(W, H);
            posicionarSprite(jogadorSprite, jogX,  jogY,  JOG_SPRITE_W,  JOG_SPRITE_H);
            posicionarSprite(bossSprite,    bossX, bossY, BOSS_SPRITE_W, BOSS_SPRITE_H);
            iniciarLoop();
            canvasBoss.requestFocus();
        }
    }

    private void posicionarPersonagens(double W, double H) {
        jogX  = 100;
        jogY  = H / 2;
        bossX = W - 100;
        bossY = H / 2;
    }
 

    // ── Carregamento de imagens ───────────────────────────────────────────────
    private void carregarImagens() {
        // Imagens estáticas do jogador
        imgJogador         = carregarImagem("jogador.png");
        imgJogadorFrente   = carregarImagem("frente.png");
        imgJogadorTras     = carregarImagem("tras.png");
        imgJogadorEsquerda = carregarImagem("esquerda.png");

        // Spritesheets do jogador
        sheetAndando         = carregarImagem("andando.png");
        sheetAndandoEsquerda = carregarImagem("andando-esquerda.png");
        sheetAtaque          = carregarImagem("ataque.png");
        sheetAtaqueEsquerda  = carregarImagem("ataque-esquerda.png");

        if (sheetAndando != null)
            animAndando = new SpriteAnimacao(sheetAndando, 8, 10);
        if (sheetAndandoEsquerda != null)
            animAndandoEsquerda = new SpriteAnimacao(sheetAndandoEsquerda, 8, 10);
        if (sheetAtaque != null) {
            animAtaque = new SpriteAnimacao(sheetAtaque, 15, 20);
            animAtaque.setLoop(false);
        }
        if (sheetAtaqueEsquerda != null) {
            animAtaqueEsquerda = new SpriteAnimacao(sheetAtaqueEsquerda, 15, 20);
            animAtaqueEsquerda.setLoop(false);
        }

        // Boss
        imgBoss     = carregarImagem("boss.png");
    }

    private Image carregarImagem(String nome) {
        try {
            URL url = getClass().getResource("/com/mycompany/jogo/" + nome);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception e) {
            System.err.println("[BossController] Imagem não encontrada: " + nome);
        }
        return null;
    }

    // ── Utilitário de sprite ──────────────────────────────────────────────────
    private ImageView criarImageView(Image img, double w, double h) {
        ImageView iv = new ImageView();
        if (img != null) iv.setImage(img);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    private void posicionarSprite(ImageView iv, double cx, double cy, double w, double h) {
        iv.setLayoutX(cx - w / 2.0);
        iv.setLayoutY(cy - h / 2.0);
    }

    // ── Teclado ───────────────────────────────────────────────────────────────
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

    // ── Game loop ─────────────────────────────────────────────────────────────
    private void iniciarLoop() {
        loop = new AnimationTimer() {
            @Override public void handle(long agora) {
                if (!emBatalha) return;
                processarEntrada(agora);
                moverBoss();
                verificarAtaqueJogador(agora);
                verificarColisoes(agora);
                renderizar(agora);
                atualizarSprites(agora);
                atualizarHUD();
                verificarCondicoes();
            }
        };
        loop.start();
    }

    // ── Lógica ───────────────────────────────────────────────────────────────
    private void processarEntrada(long agora) {
        if (teclas.contains(KeyCode.K) && !esquivando) {
            Jogador j = jogador();
            if (j.getVigorAtual() >= 20) {
                esquivando = true;
                esquivaInicio = agora;
                j.gastarVigor(20);
            }
        }
        if (esquivando && agora - esquivaInicio > ESQUIVA_DUR) esquivando = false;

        if (teclas.contains(KeyCode.J) && !atacando) {
            atacando = true;
            ataqueInicio = agora;
        }
        if (atacando && agora - ataqueInicio > ATAQUE_DUR) atacando = false;

        double speed = esquivando ? JOG_SPEED * 2.5 : JOG_SPEED;
        double nx = jogX, ny = jogY;
        if (teclas.contains(KeyCode.W)) ny -= speed;
        if (teclas.contains(KeyCode.S)) ny += speed;
        if (teclas.contains(KeyCode.A)) nx -= speed;
        if (teclas.contains(KeyCode.D)) nx += speed;

        nx = Math.max(JOG_SIZE, Math.min(canvasBoss.getWidth()  - JOG_SIZE, nx));
        ny = Math.max(JOG_SIZE, Math.min(canvasBoss.getHeight() - JOG_SIZE, ny));
        jogX = nx;
        jogY = ny;

        // Atualiza direção com base nas teclas pressionadas
        if (teclas.contains(KeyCode.W)) direcaoJogador = "tras";
        if (teclas.contains(KeyCode.S)) direcaoJogador = "frente";
        if (teclas.contains(KeyCode.D)) direcaoJogador = "direita";
        if (teclas.contains(KeyCode.A)) direcaoJogador = "esquerda";

        jogador().recuperarVigor(1);
    }

    private void moverBoss() {
        double vel = fase2 ? boss.getVelocidade() * 1.8 : boss.getVelocidade();
        double dx = jogX - bossX, dy = jogY - bossY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
            bossX += (dx / dist) * vel;
            bossY += (dy / dist) * vel;
        }
        if (!fase2 && boss.getPorcentagemVida() < 0.5) fase2 = true;
    }

    private void verificarAtaqueJogador(long agora) {
        if (!atacando) return;
        double dist = Math.sqrt(Math.pow(bossX - jogX, 2) + Math.pow(bossY - jogY, 2));
        if (dist <= JOG_SIZE + 40) {
            boss.receberDano(jogador().getDano());
            if (!boss.estaVivo()) {
                emBatalha = false;
                loop.stop();
                onVitoria();
            }
        }
    }

    private long ultimoDanoRecebido = 0;
    private static final long INTERVALO_DANO = 800_000_000L;

    private void verificarColisoes(long agora) {
        if (esquivando) return;
        if (agora - ultimoDanoRecebido < INTERVALO_DANO) return;

        double dist = Math.sqrt(Math.pow(bossX - jogX, 2) + Math.pow(bossY - jogY, 2));
        if (dist < JOG_SIZE + BOSS_SIZE) {
            jogador().receberDano(boss.getDano());
            ultimoDanoRecebido = agora;
        }


        if (!jogador().estaVivo()) {
            emBatalha = false;
            loop.stop();
            onDerrota();
        }
    }

    // ── Renderização (canvas) ─────────────────────────────────────────────────
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

        
    }

    // ── Atualização de sprites ────────────────────────────────────────────────
    private void atualizarSprites(long agora) {
        atualizarSpriteJogador(agora);
        atualizarSpriteBoss();
    }

    /** Espelho exato do atualizarSpriteJogador() do GameplayController */
    private void atualizarSpriteJogador(long agora) {
        boolean movendo = teclas.contains(KeyCode.W)
                       || teclas.contains(KeyCode.S)
                       || teclas.contains(KeyCode.A)
                       || teclas.contains(KeyCode.D);

        if (atacando) {
            if ("esquerda".equals(direcaoJogador) && animAtaqueEsquerda != null) {
                if (animAtaqueEsquerda.isFinalizado()) animAtaqueEsquerda.reiniciar();
                animAtaqueEsquerda.atualizar(agora, jogadorSprite);
            } else if (animAtaque != null) {
                if (animAtaque.isFinalizado()) animAtaque.reiniciar();
                animAtaque.atualizar(agora, jogadorSprite);
            }
        } else if (movendo) {
            if ("esquerda".equals(direcaoJogador) && animAndandoEsquerda != null) {
                if (animAndando != null) animAndando.reiniciar();
                animAndandoEsquerda.atualizar(agora, jogadorSprite);
            } else if ("direita".equals(direcaoJogador) && animAndando != null) {
                if (animAndandoEsquerda != null) animAndandoEsquerda.reiniciar();
                animAndando.atualizar(agora, jogadorSprite);
            } else {
                Image imgEstatica = "tras".equals(direcaoJogador) ? imgJogadorTras : imgJogadorFrente;
                if (imgEstatica != null) jogadorSprite.setImage(imgEstatica);
            }
        } else {
            Image imgEstatica;
            if ("tras".equals(direcaoJogador))          imgEstatica = imgJogadorTras;
            else if ("frente".equals(direcaoJogador))   imgEstatica = imgJogadorFrente;
            else if ("esquerda".equals(direcaoJogador)) imgEstatica = imgJogadorEsquerda;
            else                                         imgEstatica = imgJogador;
            if (imgEstatica != null) jogadorSprite.setImage(imgEstatica);

            if (animAndando != null)         animAndando.reiniciar();
            if (animAndandoEsquerda != null) animAndandoEsquerda.reiniciar();
        }

        jogadorSprite.setOpacity(esquivando ? 0.5 : 1.0);
        posicionarSprite(jogadorSprite, jogX, jogY, JOG_SPRITE_W, JOG_SPRITE_H);
    }

    private void atualizarSpriteBoss() {
        double bossOpacidade = fase2
            ? (System.nanoTime() / 150_000_000L % 2 == 0 ? 0.7 : 1.0)
            : 1.0;
        bossSprite.setOpacity(bossOpacidade);
        posicionarSprite(bossSprite, bossX, bossY, BOSS_SPRITE_W, BOSS_SPRITE_H);
    }


    // ── HUD ───────────────────────────────────────────────────────────────────
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

    // ── Condições ─────────────────────────────────────────────────────────────
    private void verificarCondicoes() {
        if (!jogador().estaVivo()) {
            emBatalha = false;
            loop.stop();
            onDerrota();
        }
    }

    // ── Resultado ─────────────────────────────────────────────────────────────
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

    @FXML private void onContinuarAposResultado() throws IOException {
        if (jogador().estaVivo()) App.setRoot("Ranking");
        else {
            Sessao.getInstance().iniciarNovaRun();
            App.setRoot("Boss");
        }
    }

    // ── Persistência ──────────────────────────────────────────────────────────
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

    // ── Utilitários ───────────────────────────────────────────────────────────
    private Jogador jogador() {
        return Sessao.getInstance().getJogadorAtual();
    }
}