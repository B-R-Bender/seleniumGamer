package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.PlayCard;
import ru.b_r_bender.web.model.entities.Reward;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
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
    private int upgradeCounts;
    private Calendar fiveCardsUpgradedTime;

    {
        playDeck = new ArrayList<>(9);
        weakDeck = new ArrayList<>(15);
    }

    public DeckManager(WebDriver webDriver) {
        LOG.info(Utils.getMessage("deckManager.info.created"));
        managerDriver = SeleniumUtils.cloneDriverInstance(webDriver, PLAY_DECK_PAGE_URI);
        playDeck = updateDeck(PLAY_DECK_PAGE_URI);
        weakDeck = updateDeck(WEAK_DECK_PAGE_URI);
        upgradeCounts = Reward.UPGRADE_5_PLAY_CARDS.getRewardProgress(webDriver);
    }

    private List<PlayCard> updateDeck(String deckURI) {
        List<PlayCard> result = new ArrayList<>();

        String deckStartMessage = deckURI.equals(PLAY_DECK_PAGE_URI)
                ? Utils.getMessage("deckManager.info.deck.updatePlayDeck")
                : Utils.getMessage("deckManager.info.deck.checkWeakDeck");
        LOG.info(deckStartMessage);

        if (!managerDriver.getCurrentUrl().equals(deckURI)) {
            managerDriver.get(deckURI);
        }

        List<WebElement> cardElements = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR);
        for (int i = 0; i < cardElements.size(); i++) {
            WebElement cardElement = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR).get(i);
            cardElement.click();
            result.add(new PlayCard(managerDriver));
            managerDriver.navigate().back();
        }
        Collections.sort(result);

        String deckEndMessage = deckURI.equals(PLAY_DECK_PAGE_URI)
                ? Utils.getMessage("deckManager.info.deck.heroPlayDeck", result)
                : Utils.getMessage("deckManager.info.deck.heroWeakDeck", result);
        LOG.info(deckEndMessage);
        return result;
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("deckManager.info.thread.start"));
        while (true) {
            if (upgradeCounts < 5) {
                upgradeDeck();
            } else {
                rest();
            }
        }
    }

    private void upgradeDeck() {
        LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.start"));
        for (int i = 0; i < 6; i++) {
            PlayCard card = playDeck.get(i);
            LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.currentCard", i + 1, card));
            managerDriver.get(card.getCardUrl());
            upgradeCard(card);
            while (card.isAbsorptionAvailable(managerDriver)) {
                LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.weakCardsAvailable"));
                card.absorbWeakCards(managerDriver);
                LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.cardAfterUpgrade", card));
                upgradeCard(card);
            }
        }
    }

    private void upgradeCard(PlayCard card) {
        if (card.getLevelProgress() == 100d) {
            if (upgradeCounts >= 5) {
                LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.upgradeLimitReached", upgradeCounts));
                return;
            }
            LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.cardLevelProgressIsFull"));
            int upgradeResult = card.upgrade(managerDriver);
            this.upgradeCounts += upgradeResult;
            String logMessage = upgradeResult > 0
                    ? "deckManager.info.deck.upgrade.success"
                    : "deckManager.info.deck.upgrade.failure";
            LOG.info(Utils.getMessage(logMessage, card));
        }
    }

    private void rest() {
        try {
            if (upgradeCounts >= 5) {
                Calendar tomorrow = SeleniumUtils.getTomorrow(managerDriver);
                Calendar now = SeleniumUtils.getServerTime(managerDriver);
                long coolDownTillTomorrow = tomorrow.getTimeInMillis() - now.getTimeInMillis() + Utils.getMediumDelay();
                String timeString = Utils.millisecondsToTimeString(coolDownTillTomorrow);

                LOG.info(Utils.getMessage("deckManager.info.deck.manager.sleepTillTomorrow", upgradeCounts, timeString));
                Thread.sleep(coolDownTillTomorrow);
                upgradeCounts = 0;
                LOG.info(Utils.getMessage("deckManager.info.deck.manager.wake"));
            } else {
                long coolDownTime = Utils.calculateElementCoolDownTime(PLAY_DECK_PAGE_URI);
                String timeString = Utils.millisecondsToTimeString(coolDownTime);

                LOG.info(Utils.getMessage("deckManager.info.deck.manager.sleep", timeString));
                Thread.sleep(coolDownTime);
                LOG.info(Utils.getMessage("deckManager.info.deck.manager.wake"));
            }
            playDeck = updateDeck(PLAY_DECK_PAGE_URI);
            weakDeck = updateDeck(WEAK_DECK_PAGE_URI);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
