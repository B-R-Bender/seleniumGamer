package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.PlayCard;
import ru.b_r_bender.web.model.entities.Reward;
import ru.b_r_bender.web.model.pages.MainPage;
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
        managerDriver = SeleniumUtils.cloneDriverInstance(webDriver, PLAY_DECK_PAGE_URI);
        MainPage.addDriver(managerDriver);
        playDeck = updateDeck(PLAY_DECK_PAGE_URI);
        weakDeck = updateDeck(WEAK_DECK_PAGE_URI);
        upgradeCounts = Reward.UPGRADE_5_PLAY_CARDS.getRewardProgress(webDriver);
        LOG.info(Utils.getMessage("deckManager.info.created"));
    }

    private List<PlayCard> updateDeck(String deckURI) {
        List<PlayCard> result = new ArrayList<>();

        String deckStartMessage = deckURI.equals(PLAY_DECK_PAGE_URI)
                ? Utils.getMessage("deckManager.info.deck.updatePlayDeck")
                : Utils.getMessage("deckManager.info.deck.checkWeakDeck");
        LOG.info(deckStartMessage);

        try {
            if (!managerDriver.getCurrentUrl().equals(deckURI)) {
                managerDriver.get(deckURI);
            }

            List<WebElement> cardElements = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR);
            for (int i = 0; i < (deckURI.equals(PLAY_DECK_PAGE_URI) ? cardElements.size() : 5); i++) {
                WebElement cardElement = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR).get(i);
                cardElement.click();
                result.add(new PlayCard(managerDriver));
                Thread.sleep(Utils.getThreeSecondsDelay());
                managerDriver.get(deckURI);
                Thread.sleep(Utils.getThreeSecondsDelay());
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
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
        try {
            LOG.info(Utils.getMessage("deckManager.info.thread.start"));
            while (true) {
                upgradeDeck();
                rest();
            }
        } catch (WebDriverException e) {
            LOG.error("Trying to restart thread because there was an error in WebDriver: ", e);
            MainPage.resurrectMe(DeckManager.class, managerDriver);
        } catch (Exception e) {
            String screenName = SeleniumUtils.takeErrorScreenShot(managerDriver);
            LOG.error("Screen shot taken and saved in " + screenName + " for error:\n" + e.getMessage(), e);
        } finally {
            SeleniumUtils.driverDismiss(managerDriver);
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
        if (upgradeCounts >= 5) {
            LOG.info(Utils.getMessage("deckManager.info.deck.upgrade.upgradeLimitReached", upgradeCounts));
            return;
        }

        if (card.getLevelProgress() == 100d) {
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

                Calendar now = SeleniumUtils.getServerTime(managerDriver);
                now.add(Calendar.MILLISECOND, (int) coolDownTime);
                final int DAY_OF_WEEK = now.get(Calendar.DAY_OF_WEEK);

                if (DAY_OF_WEEK == Calendar.FRIDAY || DAY_OF_WEEK == Calendar.SATURDAY) {
                    coolDownTime += DAY_OF_WEEK == Calendar.FRIDAY ? 86_400_000 * 2 : 86_400_000;
                    String day = DAY_OF_WEEK == Calendar.FRIDAY
                            ? Utils.getMessage("common.friday")
                            : Utils.getMessage("common.saturday");
                    LOG.info(Utils.getMessage("deckManager.info.deck.manager.weekendSleep", day));
                }

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
