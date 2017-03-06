package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.PlayCard;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author BRBender created on 03.03.2017.
 */
public class DeckManager implements Runnable {

    private static final Logger LOG = Logger.getLogger(DeckManager.class);

    public static final String PLAY_DECK_PAGE_URI = "http://elem.mobi/deck/";
    public static final String WEAK_DECK_PAGE_URI = "http://elem.mobi/weakcards/";

    private static final By DECK_CARDS_LOCATOR = By.cssSelector("a[class='card'][href*='/card/']");

    private WebDriver managerDriver;
    private List<PlayCard> playDeck;
    private List<PlayCard> weakDeck;
    private boolean weakCardsAvailable;

    {
        playDeck = new ArrayList<>(9);
        weakDeck = new ArrayList<>(50);
    }

    public DeckManager(WebDriver webDriver) {
        LOG.info(Utils.getMessage("deckManager.info.created"));
        managerDriver = SeleniumUtils.cloneDriverInstance(webDriver, PLAY_DECK_PAGE_URI);
        updatePlayDeck();
        isSomeWeakCardsAvailable();
    }

    private void updatePlayDeck() {
        LOG.info(Utils.getMessage("deckManager.info.deck.updatePlayDeck"));
        if (!managerDriver.getCurrentUrl().equals(PLAY_DECK_PAGE_URI)) {
            managerDriver.get(PLAY_DECK_PAGE_URI);
        }
        for (int i = 0; i < 9; i++) {
            WebElement cardElement = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR).get(i);
            cardElement.click();
            playDeck.add(new PlayCard(managerDriver));
            managerDriver.navigate().back();
        }
        LOG.info(Utils.getMessage("deckManager.info.deck.heroPlayDeck", playDeck));
        Collections.sort(playDeck);
    }

    private void isSomeWeakCardsAvailable() {
        LOG.info(Utils.getMessage("deckManager.info.deck.checkWeakDeck"));
        managerDriver.get(WEAK_DECK_PAGE_URI);
        List<WebElement> cardElement = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR);
        weakCardsAvailable = cardElement.size() != 0;
        managerDriver.navigate().back();
        LOG.info(Utils.getMessage("deckManager.info.deck.weakCardsAvailable", weakCardsAvailable));
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("deckManager.info.thread.start"));
        while (true) {
            if (weakCardsAvailable) {
                upgradeDeck();
            } else {
                rest();
            }
        }
    }

    private void upgradeDeck() {
        LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.start"));
        for (int i = 0; i < 9; i++) {
            PlayCard card = playDeck.get(i);
            LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.currentCard", i, card));
            managerDriver.get(card.getCardUrl());
            if (card.getLevelProgress() == 100d) {
                LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.cardLevelProgressIsFull"));
                boolean upgradeResult = card.upgrade(managerDriver);
                String logMessage = upgradeResult
                        ? "deckManager.info.deck.upgrade.success"
                        : "deckManager.info.deck.upgrade.failure";
                LOG.info(Utils.getMessage(logMessage, card));
            }
            while (card.isAbsorptionAvailable(managerDriver)) {
                LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.weakCardsAvailable"));
                card.absorbWeakCards(managerDriver);
                LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.cardAfterUpgrade", card));
                if (card.getLevelProgress() == 100d) {
                    LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.cardLevelProgressIsFull"));
                    boolean upgradeResult = card.upgrade(managerDriver);
                    String logMessage = upgradeResult
                            ? "deckManager.info.deck.upgrade.success"
                            : "deckManager.info.deck.upgrade.failure";
                    LOG.info(Utils.getMessage(logMessage, card));
                }
            }
        }
        weakCardsAvailable = false;
    }

    private void rest() {
        try {
            long coolDownTime = Utils.calculateElementCoolDownTime(PLAY_DECK_PAGE_URI, null);
            LOG.info(Utils.getMessage("deckManager.info.deck.manager.sleep", coolDownTime));
            Thread.sleep(coolDownTime);
            LOG.info(Utils.getMessage("deckManager.info.deck.manager.wake"));
            updatePlayDeck();
            isSomeWeakCardsAvailable();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
