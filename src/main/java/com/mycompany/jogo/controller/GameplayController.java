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
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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
    
    private Image imgJogador;
    private Image imgPescador;
    private Image imgLobisomem;
    
    private static final double JOG_SPRITE_W  = 40;
    private static final double JOG_SPRITE_H  = 40;
    private static final double INIM_SPRITE_W = 36;
    private static final double INIM_SPRITE_H = 36;
    
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
        posicionarJogador();
        configurarTeclado();
        gerarInimigos();
        atualizarHUD();
        iniciarGameLoop();
        
    }
    
    private void carregarImagens() {
        imgJogador   = carregarImagem("jogador.png");
        imgPescador  = carregarImagem("pescador.png");
        imgLobisomem = carregarImagem("lobisomem.png");
    }
    
    private Image carregarImagem(String nome) {
        try {
            URL url = getClass().getResource("/com/sinodevesper/images/" + nome);
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
        jogX = canvasArena.getWidth() / 2;
        jogY = canvasArena.getHeight() / 2;
        jogadorSprite = criarSprite(imgJogador, JOG_SPRITE_W, JOG_SPRITE_H);
        posicionarSprite(jogadorSprite, jogX, jogY, JOG_SPRITE_W, JOG_SPRITE_H);
        spritePane.getChildren().add(jogadorSprite);  // spritePane precisa existir no FXML
    }

    private void configurarTeclado() {
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
        jogadorSprite.setOpacity(esquivando ? 0.5 : 1.0);
        posicionarSprite(jogadorSprite, jogX, jogY, JOG_SPRITE_W, JOG_SPRITE_H);

        for (Inimigo ini : inimigos) {
            ImageView iv = inimigoSprites.get(ini);
            if (iv == null) continue;
            if (!ini.estaVivo()) {
                iv.setVisible(false);
            } else {
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

    @FXML private void onAcaoMensagem() {
        painelMensagem.setVisible(false);
        if (!jogador().estaVivo())        SceneManager.navigateTo("Upgrades.fxml");
        else if (proximaEhBoss)           SceneManager.navigateTo("Boss.fxml");
        else { Sessao.getInstance().avancarFase(); SceneManager.navigateTo("Loja.fxml"); }
    }
    
    @FXML private void onIrMenu() {
        gameLoop.stop(); salvarPartida(false);
        SceneManager.navigateTo("MenuPrincipal.fxml");
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