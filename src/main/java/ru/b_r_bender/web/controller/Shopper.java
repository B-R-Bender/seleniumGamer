package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

/**
 * @author BRBender on 01.03.17.
 */
public class Shopper implements Runnable {
    private static final Logger LOG = Logger.getLogger(Shopper.class);

    public static final String MARKET_PAGE_URI = "http://elem.mobi/shop/";

    private static final By ENERGY_LOCATOR = By.cssSelector(".c_energy");
    private static final By SILVER_LOCATOR = By.cssSelector(".c_silver");
    private static final By GOLD_LOCATOR = By.cssSelector(".c_gold");
    private static final By BY_CARD_FOR_SILVER_BUTTON_LOCATOR = By.cssSelector("a[href*='/shop/cards/buy/1100/']");

    private WebDriver shopperDriver;
    private int heroEnergy;
    private int heroSilver;
    private int heroGold;

    public Shopper(WebDriver webDriver) {
        LOG.info(Utils.getMessage("shopper.info.created"));
        shopperDriver = SeleniumUtils.cloneDriverInstance(webDriver, MARKET_PAGE_URI);
        updateTreasury();
    }

    private void updateTreasury() {
        heroEnergy = SeleniumUtils.getIntValueFromElement(shopperDriver, ENERGY_LOCATOR);
        heroSilver = SeleniumUtils.getIntValueFromElement(shopperDriver, SILVER_LOCATOR);
        heroGold = SeleniumUtils.getIntValueFromElement(shopperDriver, GOLD_LOCATOR);
        LOG.info(Utils.getMessage("shopper.info.shop.heroTreasures", heroEnergy, heroSilver, heroGold));
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("shopper.info.thread.start"));
        int possibleAmountOfCardsToBy = heroSilver / 500;
        while (true) {
            if (possibleAmountOfCardsToBy > 0) {
                letsGoShopping(possibleAmountOfCardsToBy);
            } else {
                rest();
            }
            updateTreasury();
            possibleAmountOfCardsToBy = heroSilver / 500;
        }
    }

    private void letsGoShopping(int possibleAmountOfCardsToBy) {
        LOG.info(Utils.getMessage("shopper.info.shop.cardsToBy", possibleAmountOfCardsToBy, possibleAmountOfCardsToBy * 500));
        try {
            for (; possibleAmountOfCardsToBy > 0; possibleAmountOfCardsToBy--) {
                Thread.sleep(Utils.getSuperShortDelay());
                SeleniumUtils.getWebElement(shopperDriver, BY_CARD_FOR_SILVER_BUTTON_LOCATOR).click();
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void rest() {
        try {
            long coolDownTime = Utils.calculateElementCoolDownTime(MARKET_PAGE_URI, null);
            LOG.info(Utils.getMessage("shopper.info.shop.notEnoughMoney", coolDownTime));
            Thread.sleep(coolDownTime);
            SeleniumUtils.refresh(shopperDriver);
            LOG.info(Utils.getMessage("shopper.info.shop.gotMoreMoney"));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
