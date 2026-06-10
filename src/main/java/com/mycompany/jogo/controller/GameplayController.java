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
import java.util.*;

public class GameplayController implements Initializable {

    //FXML
    @FXML private Canvas    canvasArena;
    @FXML private ProgressBar barraVida, barraVigor;
    @FXML private Label     labelVida, labelVigor, labelSangue, labelXp;
    @FXML private Label     labelFase, labelInimigos;
    @FXML private VBox      painelMensagem;
    @FXML private Label     labelMensagem, labelSubMensagem;
    @FXML private Button    btnAcaoMensagem;

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
        posicionarJogador();
        configurarTeclado();
        gerarInimigos();
    }

    private void posicionarJogador() {
        jogX = canvasArena.getWidth() / 2;
        jogY = canvasArena.getHeight() / 2;
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
        }
        emJogo = true;
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
    
    @FXML
    private void onAbrirInventario() {
        SceneManager.openNewWindow("Inventario.fxml", "Inventário — " + jogador().getNome());
    }
    
    private Jogador jogador() {
        return Sessao.getInstance().getJogadorAtual();
    }
    
    private double distancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

}