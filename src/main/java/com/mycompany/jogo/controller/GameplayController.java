package com.mycompany.jogo.controller;


import com.mycompany.jogo.App;
import com.mycompany.jogo.DAO.JogadorDAO;
import com.mycompany.jogo.model.Inimigo;
import com.mycompany.jogo.model.Jogador;
import com.mycompany.jogo.util.Sessao;
import com.mycompany.jogo.util.SpriteAnimacao;
import java.io.IOException;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GameplayController implements Initializable {

    //FXML 
    @FXML private Canvas      canvasArena;
    @FXML private ProgressBar barraVida, barraVigor;
    @FXML private Label       labelVida, labelVigor, labelSangue, labelXp;
    @FXML private Label       labelFase, labelInimigos;
    @FXML private VBox        painelMensagem;
    @FXML private Label       labelMensagem, labelSubMensagem;
    @FXML private Button      btnAcaoMensagem;
    @FXML private Pane        spritePane;

    // Sprite do jogador 
    private ImageView jogadorSprite;

    private Image sheetAndando;
    private Image sheetAndandoEsquerda;
    private Image sheetAtaque;
    private Image sheetAtaqueEsquerda;

    private SpriteAnimacao animAndando;
    private SpriteAnimacao animAndandoEsquerda;
    private SpriteAnimacao animAtaque;
    private SpriteAnimacao animAtaqueEsquerda;

    private Image imgJogador;
    private Image imgJogadorFrente;
    private Image imgJogadorTras;
    private Image imgJogadorEsquerda;

    private String direcaoJogador = "frente";

    private static final double JOG_SPRITE_W = 96;
    private static final double JOG_SPRITE_H = 96;

    //Sprites dos inimigos 
    private static class DadosAnimacaoInimigo {
        // Cervo
        SpriteAnimacao cervoAndarDir;
        SpriteAnimacao cervoAndarEsq;
        SpriteAnimacao cervoAndarFrente;
        SpriteAnimacao cervoAndarTras;
        SpriteAnimacao cervoAtaqueDir;
        SpriteAnimacao cervoAtaqueEsq;

        // Lobisomem (lob)
        SpriteAnimacao lobAndarDir;
        SpriteAnimacao lobAndarEsq;
        SpriteAnimacao lobAndarFrente;
        SpriteAnimacao lobAndarTras;
        SpriteAnimacao lobAtaqueDir;
        SpriteAnimacao lobAtaqueEsq;

        String direcao          = "frente";
        String direcaoAnterior  = "frente";
        boolean atacando        = false;
        boolean atacandoAnterior = false;
    }

    private final Map<Inimigo, ImageView>             inimigoSprites   = new LinkedHashMap<>();
    private final Map<Inimigo, DadosAnimacaoInimigo>  inimigoAnimacoes = new LinkedHashMap<>();

    // Spritesheets dos inimigos
    private Image sheetCervoAndarDir;
    private Image sheetCervoAndarEsq;
    private Image sheetCervoAndarFrente;
    private Image sheetCervoAndarTras;
    private Image sheetCervoAtaqueDir;
    private Image sheetCervoAtaqueEsq;

    private Image sheetLobAndarDir;
    private Image sheetLobAndarEsq;
    private Image sheetLobAndarFrente;
    private Image sheetLobAndarTras;
    private Image sheetLobAtaqueDir;
    private Image sheetLobAtaqueEsq;

    private static final double CERVO_SPRITE_W = 140;
    private static final double CERVO_SPRITE_H = 140;
    private static final double LOB_SPRITE_W   = 110;
    private static final double LOB_SPRITE_H   = 110;

    //Estado do jogador
    private double jogX, jogY;
    private static final double JOG_SIZE  = 20;
    private static final double JOG_SPEED = 3.0;

    private boolean esquivando    = false;
    private long    esquivaInicio = 0;
    private static final long ESQUIVA_DURACAO = 400_000_000L;

    private boolean atacando     = false;
    private long    ataqueInicio = 0;
    private static final long ATAQUE_DURACAO = 300_000_000L;
    
    //Recuperação de vigor
    private long ultimaRecuperacaoVigor = 0;
    private static final long INTERVALO_RECUPERACAO_VIGOR = 500_000_000L;

    //Teclas
    private final Set<KeyCode> teclasPressionadas = new HashSet<>();

    //Inimigos 
    private final List<Inimigo> inimigos = new ArrayList<>();
    private static final double INIM_SIZE         = 18;
    private static final int    INIMIGOS_POR_FASE = 5;


    private static final double RAIO_ATAQUE_INIMIGO = INIM_SIZE + JOG_SIZE + 10;

    //Game loop 
    private AnimationTimer gameLoop;
    private boolean emJogo = false;

    //Fases
    private static final String[] NOMES_FASES = {
        "Fase 1 — Vila de Vesper",
        "Fase 2 — Beco das Sombras",
        "Fase 3 — Doca dos Pescadores",
        "Fase 4 — Floresta Maldita",
        "Fase 5 — Subterrâneo da Igreja",
        "Fase 6 — Ante-câmara da Catedral"
    };

    private final JogadorDAO jogadorDAO = new JogadorDAO();
    private boolean inicializado = false;

    //initialize 
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        carregarImagens();

        jogadorSprite = criarSprite(imgJogador, JOG_SPRITE_W, JOG_SPRITE_H);
        if (spritePane != null) spritePane.getChildren().add(jogadorSprite);

        Platform.runLater(() -> {
            configurarRedimensionamento();
            configurarTeclado();
            tentarIniciar();
        });
    }

    //Inicialização 
    private void tentarIniciar() {
        double W = canvasArena.getWidth();
        double H = canvasArena.getHeight();

        if (W > 0 && H > 0) {
            if (!inicializado) {
                inicializado = true;
                posicionarJogador();
                gerarInimigos();
                atualizarHUD();
                atualizarLabelFase();
                iniciarGameLoop();
                canvasArena.requestFocus();
            }
        
        }
    }

    //Carregamento de imagens 
    private void carregarImagens() {
        // Jogador
        imgJogador         = carregarImagem("jogador.png");
        imgJogadorFrente   = carregarImagem("frente.png");
        imgJogadorTras     = carregarImagem("tras.png");
        imgJogadorEsquerda = carregarImagem("esquerda.png");

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


        sheetCervoAndarDir    = carregarImagem("cervoAndarDireita.png");
        sheetCervoAndarEsq    = carregarImagem("cervoAndarEsquerda.png");
        sheetCervoAndarFrente = carregarImagem("cervoAndarFrente.png");
        sheetCervoAndarTras   = carregarImagem("cervoAndarTras.png");
        sheetCervoAtaqueDir   = carregarImagem("cervoAtaqueDireita.png");
        sheetCervoAtaqueEsq   = carregarImagem("cervoAtaqueEsquerda.png");

        sheetLobAndarDir    = carregarImagem("lobisomenAndandoDireita.png");
        sheetLobAndarEsq    = carregarImagem("lobisomenAndandoEsquerda.png");
        sheetLobAndarFrente = carregarImagem("lobAndarFrente.png");
        sheetLobAndarTras   = carregarImagem("lobAndarTras.png");
        sheetLobAtaqueDir   = carregarImagem("lobAtaqueDireita.png");
        sheetLobAtaqueEsq   = carregarImagem("lobAtaqueEsquerda.png");
    }

    private Image carregarImagem(String nome) {
        try {
            URL url = getClass().getResource("/com/mycompany/jogo/" + nome);
            if (url != null) return new Image(url.toExternalForm());
        } catch (Exception e) {
            System.err.println("[GameplayController] Imagem não encontrada: " + nome);
        }
        return null;
    }

    //Sprites
    private ImageView criarSprite(Image img, double w, double h) {
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

    //Posição jogador
    private void posicionarJogador() {
        double W = canvasArena.getWidth();
        double H = canvasArena.getHeight();
        if (W == 0 || H == 0) return;
        jogX = W / 2;
        jogY = H / 2;
    }

    //Teclado 
    private void configurarTeclado() {
        if (canvasArena == null) return;
        if (canvasArena.getScene() != null) {
            registrarTeclas(canvasArena.getScene());
        } else {
            canvasArena.sceneProperty().addListener((obs, antigo, novo) -> {
                if (novo != null) registrarTeclas(novo);
            });
        }
        canvasArena.setFocusTraversable(true);
        canvasArena.requestFocus();
    }

    private void registrarTeclas(javafx.scene.Scene scene) {
        scene.setOnKeyPressed(e  -> teclasPressionadas.add(e.getCode()));
        scene.setOnKeyReleased(e -> teclasPressionadas.remove(e.getCode()));
        canvasArena.requestFocus();
    }

    //Geração de inimigos
    private void gerarInimigos() {
        for (ImageView iv : inimigoSprites.values()) spritePane.getChildren().remove(iv);
        inimigoSprites.clear();
        inimigoAnimacoes.clear();
        inimigos.clear();

        int fase   = Sessao.getInstance().getFaseAtual();
        Random rng = new Random();
        double W   = canvasArena.getWidth();
        double H   = canvasArena.getHeight();

        for (int i = 0; i < INIMIGOS_POR_FASE; i++) {
            Inimigo.Tipo tipo = (fase >= 3 && rng.nextBoolean())
                ? Inimigo.Tipo.CERVO : Inimigo.Tipo.LOBISOMEM;
            Inimigo ini = new Inimigo(tipo, fase);

            double x, y;
            do {
                x = 60 + rng.nextDouble() * (W - 120);
                y = 60 + rng.nextDouble() * (H - 120);
            } while (distancia(x, y, jogX, jogY) < 150);

            ini.setX(x);
            ini.setY(y);
            inimigos.add(ini);

            // Sprite inicial (primeiro frame de "andar frente")
            double sw = (tipo == Inimigo.Tipo.LOBISOMEM) ? LOB_SPRITE_W : CERVO_SPRITE_W;
            double sh = (tipo == Inimigo.Tipo.LOBISOMEM) ? LOB_SPRITE_H : CERVO_SPRITE_H;
            Image imgInicial = obterImagemInicialInimigo(tipo);
            ImageView iv = criarSprite(imgInicial, sw, sh);
            posicionarSprite(iv, x, y, sw, sh);
            spritePane.getChildren().add(iv);
            inimigoSprites.put(ini, iv);

            DadosAnimacaoInimigo dad = criarDadosAnimacao(tipo);
            inimigoAnimacoes.put(ini, dad);
        }
        emJogo = true;
    }


    private Image obterImagemInicialInimigo(Inimigo.Tipo tipo) {
        return tipo == Inimigo.Tipo.LOBISOMEM
                ? sheetLobAndarFrente
                : sheetCervoAndarFrente;
    }


    private DadosAnimacaoInimigo criarDadosAnimacao(Inimigo.Tipo tipo) {
        DadosAnimacaoInimigo dad = new DadosAnimacaoInimigo();

        if (tipo == Inimigo.Tipo.CERVO) {
            
            if (sheetCervoAndarDir    != null) dad.cervoAndarDir    = new SpriteAnimacao(sheetCervoAndarDir,    6,  8);
            if (sheetCervoAndarEsq    != null) dad.cervoAndarEsq    = new SpriteAnimacao(sheetCervoAndarEsq,    6,  8);
            if (sheetCervoAndarFrente != null) dad.cervoAndarFrente = new SpriteAnimacao(sheetCervoAndarFrente, 6,  8);
            if (sheetCervoAndarTras   != null) dad.cervoAndarTras   = new SpriteAnimacao(sheetCervoAndarTras,   6,  8);
            if (sheetCervoAtaqueDir   != null) {
                dad.cervoAtaqueDir = new SpriteAnimacao(sheetCervoAtaqueDir,  15, 12);
                dad.cervoAtaqueDir.setLoop(false);
            }
            if (sheetCervoAtaqueEsq   != null) {
                dad.cervoAtaqueEsq = new SpriteAnimacao(sheetCervoAtaqueEsq, 15, 12);
                dad.cervoAtaqueEsq.setLoop(false);
            }
        } else {
            if (sheetLobAndarDir    != null) dad.lobAndarDir    = new SpriteAnimacao(sheetLobAndarDir,    4,  8);
            if (sheetLobAndarEsq    != null) dad.lobAndarEsq    = new SpriteAnimacao(sheetLobAndarEsq,    4,  8);
            if (sheetLobAndarFrente != null) dad.lobAndarFrente = new SpriteAnimacao(sheetLobAndarFrente, 4,  8);
            if (sheetLobAndarTras   != null) dad.lobAndarTras   = new SpriteAnimacao(sheetLobAndarTras,   4,  8);
            if (sheetLobAtaqueDir   != null) {
                dad.lobAtaqueDir = new SpriteAnimacao(sheetLobAtaqueDir,   5, 10);
                dad.lobAtaqueDir.setLoop(false);
            }
            if (sheetLobAtaqueEsq   != null) {
                dad.lobAtaqueEsq = new SpriteAnimacao(sheetLobAtaqueEsq,   5, 10);
                dad.lobAtaqueEsq.setLoop(false);
            }
        }
        return dad;
    }

    //Game loop
    private void iniciarGameLoop() {
        if (gameLoop != null) gameLoop.stop();
        gameLoop = new AnimationTimer() {
            @Override public void handle(long agora) {
                if (!emJogo) return;
                processarEntrada(agora);
                moverInimigos();
                verificarAtaque(agora);
                verificarColisoes();
                renderizar(agora);
                atualizarSprites(agora);
                atualizarHUD();
                verificarCondicoes();
            }
        };
        gameLoop.start();
    }

    // ── Lógica ───────────────────────────────────────────────────────────────
    private void processarEntrada(long agora) {
        // Esquiva (K)
        if (teclasPressionadas.contains(KeyCode.K) && !esquivando) {
            Jogador j = jogador();
            if (j.getVigorAtual() >= 20) {
                esquivando    = true;
                esquivaInicio = agora;
                j.gastarVigor(20);
            }
        }
        if (esquivando && agora - esquivaInicio > ESQUIVA_DURACAO) esquivando = false;

        // Ataque (J)
        if (teclasPressionadas.contains(KeyCode.J) && !atacando) {
            atacando     = true;
            ataqueInicio = agora;
        }
        if (atacando && agora - ataqueInicio > ATAQUE_DURACAO) atacando = false;

       // Inventário (I)
        if (teclasPressionadas.contains(KeyCode.I)) {
            teclasPressionadas.remove(KeyCode.I);
            if (gameLoop != null) gameLoop.stop();
            emJogo = false;
            Platform.runLater(this::onAbrirInventario);
        }

        // Movimento WASD
        double speed = esquivando ? JOG_SPEED * 2.5 : JOG_SPEED;
        double nx = jogX, ny = jogY;
        if (teclasPressionadas.contains(KeyCode.W)) ny -= speed;
        if (teclasPressionadas.contains(KeyCode.S)) ny += speed;
        if (teclasPressionadas.contains(KeyCode.A)) nx -= speed;
        if (teclasPressionadas.contains(KeyCode.D)) nx += speed;

        nx = Math.max(JOG_SIZE, Math.min(canvasArena.getWidth()  - JOG_SIZE, nx));
        ny = Math.max(JOG_SIZE, Math.min(canvasArena.getHeight() - JOG_SIZE, ny));
        jogX = nx;
        jogY = ny;

        if (agora - ultimaRecuperacaoVigor >= INTERVALO_RECUPERACAO_VIGOR) {
            jogador().recuperarVigor(1);
            ultimaRecuperacaoVigor = agora;
        }
    }

    private void moverInimigos() {
        for (Inimigo ini : inimigos) {
            if (!ini.estaVivo()) continue;
            double dx   = jogX - ini.getX();
            double dy   = jogY - ini.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > RAIO_ATAQUE_INIMIGO) {
               
                ini.setX(ini.getX() + (dx / dist) * ini.getVelocidade());
                ini.setY(ini.getY() + (dy / dist) * ini.getVelocidade());
                atualizarDirecaoInimigo(ini, dx, dy);

                DadosAnimacaoInimigo dad = inimigoAnimacoes.get(ini);
                if (dad != null) dad.atacando = false;
            } else {
                
                DadosAnimacaoInimigo dad = inimigoAnimacoes.get(ini);
                if (dad != null) {
                    dad.atacando = true;
                    atualizarDirecaoInimigo(ini, dx, dy);
                }
            }
        }
    }

    private void atualizarDirecaoInimigo(Inimigo ini, double dx, double dy) {
        DadosAnimacaoInimigo dad = inimigoAnimacoes.get(ini);
        if (dad == null) return;

        if (Math.abs(dx) >= Math.abs(dy)) {
            dad.direcao = dx > 0 ? "direita" : "esquerda";
        } else {
            dad.direcao = dy > 0 ? "frente" : "tras";
        }
    }

    private final Set<Inimigo> inimigosAtingidosNoAtaque = new HashSet<>();
    private boolean ataqueAnterior = false;

    private void verificarAtaque(long agora) {
        // Detecta início de um novo ataque para limpar o set
        if (atacando && !ataqueAnterior) {
            inimigosAtingidosNoAtaque.clear();
        }
        ataqueAnterior = atacando;

        if (!atacando) return;

        double alcance = JOG_SIZE + 30;
        for (Inimigo ini : inimigos) {
            if (!ini.estaVivo()) continue;
            if (inimigosAtingidosNoAtaque.contains(ini)) continue; // já levou dano neste ataque
            if (distancia(jogX, jogY, ini.getX(), ini.getY()) <= alcance) {
                ini.receberDano(jogador().getDano());
                inimigosAtingidosNoAtaque.add(ini);
                if (!ini.estaVivo()) {
                    Sessao.getInstance().registrarInimigomorto(
                        ini.getRecompensaSangue(), ini.getRecompensaXp()
                    );
                }
            }
        }
    }

    private long ultimoDanoRecebido = 0;
    private static final long INTERVALO_DANO = 800_000_000L;

    private void verificarColisoes() {
        if (esquivando) return;
        long agora = System.nanoTime();
        if (agora - ultimoDanoRecebido < INTERVALO_DANO) return;
        for (Inimigo ini : inimigos) {
            if (!ini.estaVivo()) continue;
            if (distancia(jogX, jogY, ini.getX(), ini.getY()) < JOG_SIZE + INIM_SIZE) {
                jogador().receberDano(ini.getDano());
                ultimoDanoRecebido = agora;
                break;
            }
        }
    }

    //Redimensionamento
    private void configurarRedimensionamento() {
        if (canvasArena == null || spritePane == null) return;
        if (canvasArena.getParent() instanceof StackPane) {
            bindAoStackPane((StackPane) canvasArena.getParent());
        } else {
            canvasArena.parentProperty().addListener((obs, antigo, novo) -> {
                if (novo instanceof StackPane) bindAoStackPane((StackPane) novo);
            });
        }
    }

    private void bindAoStackPane(StackPane sp) {
        // Canvas precisa ser redimensionado manualmente
        sp.widthProperty().addListener((obs, old, novo) -> {
            canvasArena.setWidth(novo.doubleValue());
            spritePane.setPrefWidth(novo.doubleValue());
            if (!inicializado) tentarIniciar();
        });
        sp.heightProperty().addListener((obs, old, novo) -> {
            canvasArena.setHeight(novo.doubleValue());
            spritePane.setPrefHeight(novo.doubleValue());
            if (!inicializado) tentarIniciar();
        });

        // Aplicar tamanho atual se já existir
        if (sp.getWidth() > 0)  {
            canvasArena.setWidth(sp.getWidth());
            spritePane.setPrefWidth(sp.getWidth());
        }
        if (sp.getHeight() > 0) {
            canvasArena.setHeight(sp.getHeight());
            spritePane.setPrefHeight(sp.getHeight());
        }
    }

    private void renderizar(long agora) {
        GraphicsContext gc = canvasArena.getGraphicsContext2D();
        double W = canvasArena.getWidth(), H = canvasArena.getHeight();

        // Fundo
        gc.setFill(Color.web("#0d0808"));
        gc.fillRect(0, 0, W, H);

        // Grade
        gc.setStroke(Color.web("#1a1010", 0.4));
        gc.setLineWidth(0.5);
        for (double x = 0; x < W; x += 40) gc.strokeLine(x, 0, x, H);
        for (double y = 0; y < H; y += 40) gc.strokeLine(0, y, W, y);

        // Barra de vida e nome dos inimigos
        for (Inimigo ini : inimigos) {
            if (!ini.estaVivo()) continue;
            double bw = 40;
            gc.setFill(Color.web("#1a0505"));
            gc.fillRect(ini.getX() - bw / 2, ini.getY() - INIM_SIZE - 12, bw, 5);
            gc.setFill(Color.web("#8b0000"));
            gc.fillRect(ini.getX() - bw / 2, ini.getY() - INIM_SIZE - 12,
                        bw * ini.getPorcentagemVida(), 5);
            gc.setFill(Color.web("#c8a96e", 0.7));
            gc.fillText(ini.getNome(), ini.getX() - 40, ini.getY() - INIM_SIZE - 16);
        }

        
    }

    //Atualização de sprites
    private void atualizarSprites(long agora) {
        atualizarSpriteJogador(agora);
        atualizarSpritesInimigos(agora);
    }

    private void atualizarSpriteJogador(long agora) {
        boolean movendo = teclasPressionadas.contains(KeyCode.W)
                       || teclasPressionadas.contains(KeyCode.S)
                       || teclasPressionadas.contains(KeyCode.A)
                       || teclasPressionadas.contains(KeyCode.D);

        if (teclasPressionadas.contains(KeyCode.W)) direcaoJogador = "tras";
        if (teclasPressionadas.contains(KeyCode.S)) direcaoJogador = "frente";
        if (teclasPressionadas.contains(KeyCode.D)) direcaoJogador = "direita";
        if (teclasPressionadas.contains(KeyCode.A)) direcaoJogador = "esquerda";

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

    private void atualizarSpritesInimigos(long agora) {
        for (Inimigo ini : inimigos) {
            ImageView iv = inimigoSprites.get(ini);
            if (iv == null) continue;

            if (!ini.estaVivo()) {
                iv.setVisible(false);
                continue;
            }
            iv.setVisible(true);
            double sw = (ini.getTipo() == Inimigo.Tipo.LOBISOMEM) ? LOB_SPRITE_W : CERVO_SPRITE_W;
            double sh = (ini.getTipo() == Inimigo.Tipo.LOBISOMEM) ? LOB_SPRITE_H : CERVO_SPRITE_H;
            posicionarSprite(iv, ini.getX(), ini.getY(), sw, sh);

            DadosAnimacaoInimigo dad = inimigoAnimacoes.get(ini);
            if (dad == null) continue;

            if (ini.getTipo() == Inimigo.Tipo.CERVO) {
                animarCervo(dad, iv, agora);
            } else {
                animarLob(dad, iv, agora);
            }
        }
    }

    private void animarCervo(DadosAnimacaoInimigo dad, ImageView iv, long agora) {
        boolean direcaoMudou = !dad.direcao.equals(dad.direcaoAnterior);
        boolean estadoMudou  = dad.atacando != dad.atacandoAnterior;

       
        if (direcaoMudou || estadoMudou) {
            resetarAndarCervo(dad);
            resetarAtaqueCervo(dad);
        }

        if (dad.atacando) {
            boolean usarEsq = "esquerda".equals(dad.direcao) || "tras".equals(dad.direcao);
            if (usarEsq) {
                if (dad.cervoAtaqueEsq != null) {
                    if (dad.cervoAtaqueEsq.isFinalizado()) dad.cervoAtaqueEsq.reiniciar();
                    dad.cervoAtaqueEsq.atualizar(agora, iv);
                }
            } else {
                if (dad.cervoAtaqueDir != null) {
                    if (dad.cervoAtaqueDir.isFinalizado()) dad.cervoAtaqueDir.reiniciar();
                    dad.cervoAtaqueDir.atualizar(agora, iv);
                }
            }
        } else {
            switch (dad.direcao) {
                case "direita": {
                    if (dad.cervoAndarDir    != null) dad.cervoAndarDir.atualizar(agora, iv); 
                }
                break;
                case "esquerda": { 
                    if (dad.cervoAndarEsq    != null) dad.cervoAndarEsq.atualizar(agora, iv); 
                }
                break;
                case "tras": {
                    if (dad.cervoAndarTras   != null) dad.cervoAndarTras.atualizar(agora, iv); 
                }
                break;
                default: { 
                    if (dad.cervoAndarFrente != null) dad.cervoAndarFrente.atualizar(agora, iv); 
                }
                break;
            }
        }

        dad.direcaoAnterior  = dad.direcao;
        dad.atacandoAnterior = dad.atacando;
    }

    private void animarLob(DadosAnimacaoInimigo dad, ImageView iv, long agora) {
        boolean direcaoMudou = !dad.direcao.equals(dad.direcaoAnterior);
        boolean estadoMudou  = dad.atacando != dad.atacandoAnterior;

        if (direcaoMudou || estadoMudou) {
            resetarAndarLob(dad);
            resetarAtaqueLob(dad);
        }

        if (dad.atacando) {
            boolean usarEsq = "esquerda".equals(dad.direcao) || "tras".equals(dad.direcao);
            if (usarEsq) {
                if (dad.lobAtaqueEsq != null) {
                    if (dad.lobAtaqueEsq.isFinalizado()) dad.lobAtaqueEsq.reiniciar();
                    dad.lobAtaqueEsq.atualizar(agora, iv);
                }
            } else {
                if (dad.lobAtaqueDir != null) {
                    if (dad.lobAtaqueDir.isFinalizado()) dad.lobAtaqueDir.reiniciar();
                    dad.lobAtaqueDir.atualizar(agora, iv);
                }
            }
        } else {
            switch (dad.direcao) {
                case "direita": { 
                    if (dad.lobAndarDir    != null) dad.lobAndarDir.atualizar(agora, iv); 
                }
                break;
                case "esquerda": {
                    if (dad.lobAndarEsq    != null) dad.lobAndarEsq.atualizar(agora, iv); 
                }
                break;
                case "tras": { 
                    if (dad.lobAndarTras   != null) dad.lobAndarTras.atualizar(agora, iv); 
                }
                break;
                default: { 
                    if (dad.lobAndarFrente != null) dad.lobAndarFrente.atualizar(agora, iv); 
                }
                break;
            }
        }

        dad.direcaoAnterior  = dad.direcao;
        dad.atacandoAnterior = dad.atacando;
    }

    private void resetarAtaqueCervo(DadosAnimacaoInimigo dad) {
        if (dad.cervoAtaqueDir != null) dad.cervoAtaqueDir.reiniciar();
        if (dad.cervoAtaqueEsq != null) dad.cervoAtaqueEsq.reiniciar();
    }

    private void resetarAndarCervo(DadosAnimacaoInimigo dad) {
        if (dad.cervoAndarDir    != null) dad.cervoAndarDir.reiniciar();
        if (dad.cervoAndarEsq    != null) dad.cervoAndarEsq.reiniciar();
        if (dad.cervoAndarFrente != null) dad.cervoAndarFrente.reiniciar();
        if (dad.cervoAndarTras   != null) dad.cervoAndarTras.reiniciar();
    }

    private void resetarAtaqueLob(DadosAnimacaoInimigo dad) {
        if (dad.lobAtaqueDir != null) dad.lobAtaqueDir.reiniciar();
        if (dad.lobAtaqueEsq != null) dad.lobAtaqueEsq.reiniciar();
    }

    private void resetarAndarLob(DadosAnimacaoInimigo dad) {
        if (dad.lobAndarDir    != null) dad.lobAndarDir.reiniciar();
        if (dad.lobAndarEsq    != null) dad.lobAndarEsq.reiniciar();
        if (dad.lobAndarFrente != null) dad.lobAndarFrente.reiniciar();
        if (dad.lobAndarTras   != null) dad.lobAndarTras.reiniciar();
    }

    //Condições de vitória/derrota
    private void verificarCondicoes() {
        Jogador j = jogador();
        if (!j.estaVivo()) {
            emJogo = false;
            gameLoop.stop();
            mostrarMensagem("VOCÊ MORREU", "Vesper reclama mais uma alma...", "Tentar Novamente", false);
            salvarPartida(false);
            return;
        }
        boolean todosVencidos = inimigos.stream().noneMatch(Inimigo::estaVivo);
        if (todosVencidos) {
            emJogo = false;
            gameLoop.stop();
            int fase = Sessao.getInstance().getFaseAtual();
            if (fase >= 6)
                mostrarMensagem("FASE CONCLUÍDA!", "A catedral aguarda. Prepare-se para o Sacerdote.", "Enfrentar o Boss", true);
            else
                mostrarMensagem("FASE " + fase + " CONCLUÍDA!",
                    "+" + Sessao.getInstance().getSangueGanhoNaRun() + " de sangue ganhos nesta run.",
                    "Próxima Fase", true);
        }
    }

    private boolean proximaEhBoss = false;

    private void mostrarMensagem(String titulo, String sub, String btnTexto, boolean ehVitoria) {
        proximaEhBoss = ehVitoria && Sessao.getInstance().getFaseAtual() >= 6;
        labelMensagem.setText(titulo);
        labelSubMensagem.setText(sub);
        btnAcaoMensagem.setText(btnTexto);
        painelMensagem.setVisible(true);
    }

    @FXML private void onAcaoMensagem() throws IOException {
        painelMensagem.setVisible(false);
        if (gameLoop != null) gameLoop.stop();
        emJogo = false;

        if (!jogador().estaVivo()) {
            jogador().restaurarVida();
            App.setRoot("Gameplay");   
        } else if (proximaEhBoss) {
            App.setRoot("Boss");
        } else {
            Sessao.getInstance().avancarFase();
            App.setRoot("Loja");
        }
    }
    

    @FXML private void onIrMenu() throws IOException {
        if (gameLoop != null) gameLoop.stop();
        salvarPartida(false);
        App.setRoot("Menu");
    }

    @FXML private void onAbrirInventario() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/jogo/Inventario.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/com/mycompany/jogo/style.css");
            if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

            Stage invStage = new Stage();
            invStage.setTitle("Inventário — " + jogador().getNome());
            invStage.setScene(scene);
            invStage.setResizable(false);
            invStage.initModality(Modality.APPLICATION_MODAL);
            invStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jogador().estaVivo()) {
            emJogo = true;
            if (gameLoop != null) gameLoop.start();
            canvasArena.requestFocus();
        }
    }

    //HUD 
    private void atualizarHUD() {
        Jogador j = jogador();
        barraVida.setProgress((double) j.getVidaAtual() / j.getVidaMaxima());
        barraVigor.setProgress((double) j.getVigorAtual() / j.getVigorMaximo());
        labelVida.setText(j.getVidaAtual() + "/" + j.getVidaMaxima());
        labelVigor.setText(j.getVigorAtual() + "/" + j.getVigorMaximo());
        labelSangue.setText("🩸 " + j.getPontosSangue());
        labelXp.setText("✦ XP: " + j.getTotalXp());
        long vivos = inimigos.stream().filter(Inimigo::estaVivo).count();
        labelInimigos.setText("Inimigos: " + vivos + "/" + INIMIGOS_POR_FASE);
    }

    private void atualizarLabelFase() {
        int f = Sessao.getInstance().getFaseAtual();
        labelFase.setText(f <= NOMES_FASES.length ? NOMES_FASES[f - 1] : "Fase " + f);
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

    private double distancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
}