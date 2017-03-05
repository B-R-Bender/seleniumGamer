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
        updateWeakDeck();
    }

    private void updatePlayDeck() {
        for (int i = 0; i < 9; i++) {
            WebElement cardElement = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR).get(i);
            cardElement.click();
            playDeck.add(new PlayCard(managerDriver));
            managerDriver.navigate().back();
        }
        Collections.sort(playDeck);
    }

    private void updateWeakDeck() {
        managerDriver.get(WEAK_DECK_PAGE_URI);
        List<WebElement> cardElement = SeleniumUtils.getWebElements(managerDriver, DECK_CARDS_LOCATOR);
        weakCardsAvailable = cardElement.size() != 0;
        managerDriver.navigate().back();
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("deckManager.info.thread.start"));
        while (true) {
            if (weakCardsAvailable) {
                LOG.info("Досутпны слабые карты, будем пытаться апгрейдить колоду");
                upgradeDeck();
            } else {
                rest();
            }
        }
    }

    private void upgradeDeck() {
        for (int i = 0; i < 9; i++) {
            PlayCard card = playDeck.get(i);
            LOG.info("Берем карту №" + i + " -> " + card);
            managerDriver.get(card.getCardUrl());
            if (card.getLevelProgress() == 100d) {
                LOG.info("Запускаем апгрейд");
                card.upgrade(managerDriver);
            }
            while (card.isAbsorptionAvailable(managerDriver)) {
                LOG.info("Для карты доступна прочкачка слабыми картами - запускаем процесс");
                card.absorbWeakCards(managerDriver);
                LOG.info("Прокачали карту - " + card);
                if (card.getLevelProgress() == 100d) {
                    LOG.info("Уровень заполнен на 100%, пробуем поднять уровень");
                    card.upgrade(managerDriver);
                    LOG.info("Карта после попытки прокачки - " + card);
                }
            }
        }
        weakCardsAvailable = false;
    }

    private void rest() {
        try {
            long coolDownTime = Utils.calculateElementCoolDownTime(PLAY_DECK_PAGE_URI, null);
            LOG.info("Один проход менеджера колоды завершен ложимся спать на " + coolDownTime + " мс.");
//            LOG.info(Utils.getMessage("shopper.info.shop.notEnoughMoney", coolDownTime));
            Thread.sleep(coolDownTime);
            LOG.info("Отоспался, сейчас обновим колоды и вперед");
            updatePlayDeck();
            updateWeakDeck();
//            LOG.info(Utils.getMessage("shopper.info.shop.gotMoreMoney"));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
