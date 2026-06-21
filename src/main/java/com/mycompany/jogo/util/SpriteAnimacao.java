package com.mycompany.jogo.util;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class SpriteAnimacao {

    private final WritableImage[] frames;
    private int frameAtual = 0;
    private long ultimoTempo = 0;
    private final long intervalNanos; // tempo entre frames em nanosegundos
    private boolean loop = true;
    private boolean finalizado = false;

    
    public SpriteAnimacao(Image spritesheet, int totalFrames, int fps) {
        this.intervalNanos = 1_000_000_000L / fps;
        int frameW = (int) (spritesheet.getWidth()  / totalFrames);
        int frameH = (int)  spritesheet.getHeight();
        PixelReader pr = spritesheet.getPixelReader();

        frames = new WritableImage[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            frames[i] = new WritableImage(pr, i * frameW, 0, frameW, frameH);
        }
    }

    /** Define se a animação repete (true) ou para no último frame (false) */
    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    /** Reinicia a animação do início */
    public void reiniciar() {
        frameAtual = 0;
        ultimoTempo = 0;
        finalizado = false;
    }

    /** Retorna true se uma animação sem loop chegou ao fim */
    public boolean isFinalizado() {
        return finalizado;
    }

    /**
     * Atualiza o frame e aplica no ImageView.
     * Chame isso a cada tick do game loop, passando System.nanoTime().
     */
    public void atualizar(long agora, ImageView iv) {
        if (finalizado) return;

        if (ultimoTempo == 0) ultimoTempo = agora;

        if (agora - ultimoTempo >= intervalNanos) {
            ultimoTempo = agora;
            frameAtual++;

            if (frameAtual >= frames.length) {
                if (loop) {
                    frameAtual = 0;
                } else {
                    frameAtual = frames.length - 1;
                    finalizado = true;
                }
            }
        }

        iv.setImage(frames[frameAtual]);
    }
}