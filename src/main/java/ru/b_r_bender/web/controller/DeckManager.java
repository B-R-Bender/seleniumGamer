package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.PlayCard;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author BRBender created on 03.03.2017.
 */
public class DeckManager implements Runnable {

    private static final Logger LOG = Logger.getLogger(DeckManager.class);

    public static final String DECK_PAGE_URI = "http://elem.mobi/deck/";

    private static final By PLAY_DECK_CARDS_LOCATOR = By.cssSelector("a[class='card'][href*='/card/']");

    private WebDriver managerDriver;
    private List<PlayCard> playDeck;
    private List<PlayCard> weakDeck;

    {
        playDeck = new ArrayList<>(9);
        weakDeck = new ArrayList<>(50);
    }

    public DeckManager(WebDriver webDriver) {
        LOG.info(Utils.getMessage("deckManager.info.created"));
        managerDriver = SeleniumUtils.cloneDriverInstance(webDriver, DECK_PAGE_URI);
        updatePlayDeck();
        updateWeakDeck();
    }

    private void updatePlayDeck() {
        List<WebElement> cardElements = SeleniumUtils.getWebElements(managerDriver, PLAY_DECK_CARDS_LOCATOR);
        for (WebElement cardElement : cardElements) {
            cardElement.click();
            playDeck.add(new PlayCard(managerDriver));
            managerDriver.navigate().back();
        }
    }

    private void updateWeakDeck() {

    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("deckManager.info.thread.start"));
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
