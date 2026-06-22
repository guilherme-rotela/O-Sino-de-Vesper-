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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class GameplayController implements Initializable {

    //FXML
    @FXML private Canvas    canvasArena;
    @FXML private ProgressBar barraVida, barraVigor;
    @FXML private Label     labelVida, labelVigor, labelSangue, labelXp;
    @FXML private Label     labelFase, labelInimigos;
    @FXML private VBox      painelMensagem;
    @FXML private Label     labelMensagem, labelSubMensagem;
    @FXML private Button    btnAcaoMensagem;
    @FXML private Pane spritePane;

    private ImageView jogadorSprite;
    private final Map<Inimigo, ImageView> inimigoSprites = new LinkedHashMap<>();
    
    
    private Image sheetAndando;
    private Image sheetAndandoEsquerda;
    private Image sheetAtaque;
    private Image sheetAtaqueEsquerda;

    
    private SpriteAnimacao animAndando;
    private SpriteAnimacao animAndandoEsquerda;
    private SpriteAnimacao animAtaque;
    private SpriteAnimacao animAtaqueEsquerda;
    
    private Image imgJogador;
    private Image imgPescador;
    private Image imgLobisomem;
    private Image imgJogadorFrente;
    private Image imgJogadorTras;
    private Image imgJogadorEsquerda;
    
    private String direcao = "frente";
    
    private static final double JOG_SPRITE_W  = 64;
    private static final double JOG_SPRITE_H  = 64;
    private static final double INIM_SPRITE_W = 48;
    private static final double INIM_SPRITE_H = 48;
    
    //Estado do jogador na tela
    private double jogX, jogY;
    private static final double JOG_SIZE  = 20;
    private static final double JOG_SPEED = 3.0;
    private boolean esquivando = false;
    private long esquivaInicio = 0;
    private static final long ESQUIVA_DURACAO = 400_000_000L; // 400ms em nanos

    //Ataque
    private boolean atacando = false;
    private long ataqueInicio = 0;
    private static final long ATAQUE_DURACAO = 300_000_000L;

    //Teclas pressionadas
    private final Set<KeyCode> teclasPressionadas = new HashSet<>();

    //Inimigos
    private final List<Inimigo> inimigos = new ArrayList<>();
    private static final double INIM_SIZE = 18;
    private static final int INIMIGOS_POR_FASE = 5;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        carregarImagens();
        configurarTeclado();

        jogadorSprite = criarSprite(imgJogador, JOG_SPRITE_W, JOG_SPRITE_H);
        if (spritePane != null) spritePane.getChildren().add(jogadorSprite);

        // Adia para depois do layout estar pronto
        Platform.runLater(() -> {
            configurarRedimensionamento();

            if (canvasArena.getWidth() > 0) {
                posicionarJogador();
                gerarInimigos();
                atualizarHUD();
                iniciarGameLoop();
            } else {
                canvasArena.widthProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() > 0 && jogX == 0 && jogY == 0) {
                        posicionarJogador();
                        gerarInimigos();
                        atualizarHUD();
                        iniciarGameLoop();
                    }
                });
            }
        });
    }
    private void carregarImagens() {
        imgJogador = carregarImagem("jogador.png");
        imgJogadorFrente = carregarImagem("frente.png");
        imgJogadorTras = carregarImagem("tras.png");
        imgJogadorEsquerda = carregarImagem("esquerda.png");
        
        sheetAndando        = carregarImagem("andando.png");
        sheetAndandoEsquerda = carregarImagem("andando-esquerda.png");
        sheetAtaque         = carregarImagem("ataque.png");
        sheetAtaqueEsquerda = carregarImagem("ataque-esquerda.png");
        
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

        imgPescador = carregarImagem("pescador.png");
        imgLobisomem = carregarImagem("lobisomem.png");
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


    private void posicionarJogador() {
        double W = canvasArena.getWidth();
        double H = canvasArena.getHeight();
        if (W == 0 || H == 0) return; 
        jogX = W / 2;
        jogY = H / 2;
        
    }

    private void configurarTeclado() {
        // Garante que o canvas existe antes de qualquer operação
        if (canvasArena == null) return;

        canvasArena.sceneProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                novo.setOnKeyPressed(e  -> teclasPressionadas.add(e.getCode()));
                novo.setOnKeyReleased(e -> teclasPressionadas.remove(e.getCode()));
                canvasArena.requestFocus();
            }
        });
        canvasArena.setFocusTraversable(true);
    }

    private void gerarInimigos() {
        inimigos.clear();
        Jogador j = Sessao.getInstance().getJogadorAtual();
        int fase   = Sessao.getInstance().getFaseAtual();
        Random rng = new Random();

        for (int i = 0; i < INIMIGOS_POR_FASE; i++) {
            // Fase 3+ inclui lobisomens
            Inimigo.Tipo tipo = (fase >= 3 && rng.nextBoolean())
                ? Inimigo.Tipo.LOBISOMEM : Inimigo.Tipo.PESCADOR_FERA;
            Inimigo ini = new Inimigo(tipo, fase);

            // Posição aleatória longe do jogador
            double x, y;
            do {
                x = 40 + rng.nextDouble() * (canvasArena.getWidth()  - 80);
                y = 40 + rng.nextDouble() * (canvasArena.getHeight() - 80);
            } while (distancia(x, y, jogX, jogY) < 150);
            ini.setX(x); ini.setY(y);
            inimigos.add(ini);
            Image imgIni = tipo == Inimigo.Tipo.LOBISOMEM ? imgLobisomem : imgPescador;
            ImageView iv = criarSprite(imgIni, INIM_SPRITE_W, INIM_SPRITE_H);
            posicionarSprite(iv, x, y, INIM_SPRITE_W, INIM_SPRITE_H);
            spritePane.getChildren().add(iv);
            inimigoSprites.put(ini, iv);
        }
        emJogo = true;
        atualizarLabelFase();
    }
    
    private void iniciarGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override public void handle(long agora) {
                if (!emJogo) return;
                processarEntrada(agora);
                moverInimigos();
                verificarAtaque(agora);
                verificarColisoes();
                renderizar(agora);
                atualizarSprites();
                atualizarHUD();
                verificarCondicoes();
            }
        };
        gameLoop.start();
    }
    
    private void processarEntrada(long agora) {
        // Esquiva (K) — dura 400ms, consome vigor
        if (teclasPressionadas.contains(KeyCode.K) && !esquivando) {
            Jogador j = jogador();
            if (j.getVigorAtual() >= 20) {
                esquivando  = true;
                esquivaInicio = agora;
                j.gastarVigor(20);
            }
        }
        if (esquivando && agora - esquivaInicio > ESQUIVA_DURACAO) {
            esquivando = false;
        }

        // Ataque (J)
        if (teclasPressionadas.contains(KeyCode.J) && !atacando) {
            atacando    = true;
            ataqueInicio = agora;
        }
        if (atacando && agora - ataqueInicio > ATAQUE_DURACAO) {
            atacando = false;
        }

        // Inventário (I)
        if (teclasPressionadas.contains(KeyCode.I)) {
            teclasPressionadas.remove(KeyCode.I);
            onAbrirInventario();
        }

        // Movimento WASD
        double speed = esquivando ? JOG_SPEED * 2.5 : JOG_SPEED;
        double nx = jogX, ny = jogY;
        if (teclasPressionadas.contains(KeyCode.W)) ny -= speed;
        if (teclasPressionadas.contains(KeyCode.S)) ny += speed;
        if (teclasPressionadas.contains(KeyCode.A)) nx -= speed;
        if (teclasPressionadas.contains(KeyCode.D)) nx += speed;

        // Limitar dentro da arena
        nx = Math.max(JOG_SIZE, Math.min(canvasArena.getWidth()  - JOG_SIZE, nx));
        ny = Math.max(JOG_SIZE, Math.min(canvasArena.getHeight() - JOG_SIZE, ny));
        jogX = nx; jogY = ny;

        // Recuperação passiva de vigor
        jogador().recuperarVigor(1);
    }
    private void moverInimigos() {
        for (Inimigo ini : inimigos) {
            if (!ini.estaVivo()) continue;
            double dx = jogX - ini.getX();
            double dy = jogY - ini.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                ini.setX(ini.getX() + (dx / dist) * ini.getVelocidade());
                ini.setY(ini.getY() + (dy / dist) * ini.getVelocidade());
            }
        }
    }
    
    private void verificarAtaque(long agora) {
        if (!atacando) return;
        // Alcance do ataque
        double alcance = JOG_SIZE + 30;
        for (Inimigo ini : inimigos) {
            if (!ini.estaVivo()) continue;
            if (distancia(jogX, jogY, ini.getX(), ini.getY()) <= alcance) {
                ini.receberDano(jogador().getDano());
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
        if (esquivando) {
            return;
        }
            
        long agora = System.nanoTime();
        if (agora - ultimoDanoRecebido < INTERVALO_DANO) {
            return;
        }
            
        for (Inimigo ini : inimigos) {
            if (!ini.estaVivo()) {
                continue;
            }
                
            if (distancia(jogX, jogY, ini.getX(), ini.getY()) < JOG_SIZE + INIM_SIZE) {
                jogador().receberDano(ini.getDano());
                ultimoDanoRecebido = agora;
                break;
            }
        }
    }
    
   private void configurarRedimensionamento() {
        if (canvasArena == null || spritePane == null) return;

        canvasArena.parentProperty().addListener((obs, antigo, novo) -> {
            if (novo instanceof StackPane) {
                StackPane sp = (StackPane) novo; // cast explícito para Java 11
                canvasArena.widthProperty().bind(sp.widthProperty());
                canvasArena.heightProperty().bind(sp.heightProperty());
                spritePane.prefWidthProperty().bind(sp.widthProperty());
                spritePane.prefHeightProperty().bind(sp.heightProperty());

                sp.widthProperty().addListener((o, ov, nv) -> {
                    if (nv.doubleValue() > 0 && jogX == 0 && jogY == 0) {
                        posicionarJogador();
                    }
                });
            }
        });
    }
    
    private void renderizar(long agora) {
        GraphicsContext gc = canvasArena.getGraphicsContext2D();
        double W = canvasArena.getWidth(), H = canvasArena.getHeight();

        gc.setFill(Color.web("#0d0808"));
        gc.fillRect(0, 0, W, H);

        gc.setStroke(Color.web("#1a1010", 0.4));
        gc.setLineWidth(0.5);
        for (double x = 0; x < W; x += 40) gc.strokeLine(x, 0, x, H);
        for (double y = 0; y < H; y += 40) gc.strokeLine(0, y, W, y);

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

        if (atacando) {
            gc.setStroke(Color.web("#c8a96e", 0.35));
            gc.setLineWidth(1.5);
            gc.strokeOval(jogX - (JOG_SIZE + 30), jogY - (JOG_SIZE + 30),
                          (JOG_SIZE + 30) * 2, (JOG_SIZE + 30) * 2);
        }

        if (esquivando) {
            gc.setStroke(Color.web("#ffffff", 0.3));
            gc.setLineWidth(2);
            gc.strokeOval(jogX - JOG_SIZE - 5, jogY - JOG_SIZE - 5,
                          (JOG_SIZE + 5) * 2, (JOG_SIZE + 5) * 2);
        }
    }
    

    private void atualizarSprites() {
    boolean movendo = teclasPressionadas.contains(KeyCode.W)
                   || teclasPressionadas.contains(KeyCode.S)
                   || teclasPressionadas.contains(KeyCode.A)
                   || teclasPressionadas.contains(KeyCode.D);

    
    if (teclasPressionadas.contains(KeyCode.W)) direcao = "tras";
    if (teclasPressionadas.contains(KeyCode.S)) direcao = "frente";
    if (teclasPressionadas.contains(KeyCode.D)) direcao = "direita";
    if (teclasPressionadas.contains(KeyCode.A)) direcao = "esquerda";

    long agora = System.nanoTime();

    if (atacando) {
        // Ataque para esquerda ou direita
        if ("esquerda".equals(direcao) && animAtaqueEsquerda != null) {
            if (animAtaqueEsquerda.isFinalizado()) animAtaqueEsquerda.reiniciar();
            animAtaqueEsquerda.atualizar(agora, jogadorSprite);
        } else if (animAtaque != null) {
            if (animAtaque.isFinalizado()) animAtaque.reiniciar();
            animAtaque.atualizar(agora, jogadorSprite);
        }

    } else if (movendo) {
        // Andar para esquerda ou direita
        if ("esquerda".equals(direcao) && animAndandoEsquerda != null) {
            animAndando.reiniciar(); // reseta o outro lado
            animAndandoEsquerda.atualizar(agora, jogadorSprite);
        } else if ("direita".equals(direcao) && animAndando != null) {
            animAndandoEsquerda.reiniciar();
            animAndando.atualizar(agora, jogadorSprite);
        } else {
            Image imgEstatica;
            if ("tras".equals(direcao))    imgEstatica = imgJogadorTras;
            else                            imgEstatica = imgJogadorFrente;
            if (imgEstatica != null) jogadorSprite.setImage(imgEstatica);
        }

    } else {
        // Parado — sprite estático da direção atual
        Image imgEstatica;
        if ("tras".equals(direcao))         imgEstatica = imgJogadorTras;
        else if ("Frente".equals(direcao)) imgEstatica = imgJogadorFrente;
        else if ("esquerda".equals(direcao)) imgEstatica = imgJogadorEsquerda;
        else                                 imgEstatica = imgJogador;

        if (imgEstatica != null) jogadorSprite.setImage(imgEstatica);

        // Reseta animações de andar
        if (animAndando != null)         animAndando.reiniciar();
        if (animAndandoEsquerda != null) animAndandoEsquerda.reiniciar();
    }

    jogadorSprite.setOpacity(esquivando ? 0.5 : 1.0);
    posicionarSprite(jogadorSprite, jogX, jogY, JOG_SPRITE_W, JOG_SPRITE_H);

    // Inimigos (sem alteração)
    for (Inimigo ini : inimigos) {
        ImageView iv = inimigoSprites.get(ini);
        if (iv == null) continue;
        if (!ini.estaVivo()) iv.setVisible(false);
        else {
            iv.setVisible(true);
            posicionarSprite(iv, ini.getX(), ini.getY(), INIM_SPRITE_W, INIM_SPRITE_H);
        }
    }
}
    private void verificarCondicoes() {
        Jogador j = jogador();
        if (!j.estaVivo()) {
            emJogo = false; gameLoop.stop();
            mostrarMensagem("VOCÊ MORREU", "Vesper reclama mais uma alma...", "Tentar Novamente", false);
            salvarPartida(false);
            return;
        }
        boolean todosVencidos = inimigos.stream().noneMatch(Inimigo::estaVivo);
        if (todosVencidos) {
            emJogo = false; gameLoop.stop();
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
        if (!jogador().estaVivo())        App.setRoot("Upgrade");
        else if (proximaEhBoss)           App.setRoot("Boss");
        else { 
            Sessao.getInstance().avancarFase(); 
            App.setRoot("Loja ");
        }
    }
    
    @FXML private void onIrMenu() throws IOException {
        gameLoop.stop(); salvarPartida(false);
        App.setRoot("Menu");
    }
    
    @FXML
    private void onAbrirInventario() {
        SceneManager.openNewWindow("Inventario.fxml", "Inventário — " + jogador().getNome());
    }
    
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