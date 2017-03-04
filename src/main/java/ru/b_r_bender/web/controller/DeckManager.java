package ru.b_r_bender.web.controller;

import ru.b_r_bender.web.model.entities.PlayCard;

import java.util.List;

/**
 * @author BRBender created on 03.03.2017.
 */
public class DeckManager implements Runnable {

    List<PlayCard> playDeck;
    List<PlayCard> weakDeck;

    @Override
    public void run() {
        while (true) {
            if (hasMoreAvailableWeakCards()) {

            } else {
                rest();
            }
        }
    }

    private boolean hasMoreAvailableWeakCards() {
        return false;
    }

    private void rest() {

    }
}
