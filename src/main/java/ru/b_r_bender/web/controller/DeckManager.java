package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.model.entities.PlayCard;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BRBender created on 03.03.2017.
 */
public class DeckManager implements Runnable {

    private static final Logger LOG = Logger.getLogger(DeckManager.class);

    public static final String DECK_PAGE_URI = "http://elem.mobi/deck/";

    private WebDriver shopperDriver;
    private List<PlayCard> playDeck;
    private List<PlayCard> weakDeck;

    public DeckManager(WebDriver shopperDriver) {
        this.shopperDriver = shopperDriver;
        playDeck = new ArrayList<>(9);
        weakDeck = new ArrayList<>(50);
    }

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
