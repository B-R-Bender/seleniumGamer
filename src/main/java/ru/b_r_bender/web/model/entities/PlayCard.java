package ru.b_r_bender.web.model.entities;

import java.util.List;

/**
 * @author BRBender created on 02.03.2017.
 */
public class PlayCard {

    private long cardId;
    private int cardStrength;
    private int cardLevel;
    private boolean isWeak;
    private double levelProgress;

    private List<PlayCard> weakCardsDeck;

    public PlayCard(long cardId, int cardStrength, int cardLevel, boolean isWeak, double levelProgress) {
        this.cardId = cardId;
        this.cardStrength = cardStrength;
        this.cardLevel = cardLevel;
        this.isWeak = isWeak;
        this.levelProgress = levelProgress;
    }

    public List<PlayCard> getWeakCardsDeck() {
        return weakCardsDeck;
    }

    public void setWeakCardsDeck(List<PlayCard> weakCardsDeck) {
        this.weakCardsDeck = weakCardsDeck;
    }
}
