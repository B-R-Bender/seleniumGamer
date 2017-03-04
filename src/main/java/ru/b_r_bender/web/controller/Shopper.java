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

    private static By energyLocator = By.cssSelector(".c_energy");
    private static By silverLocator = By.cssSelector(".c_silver");
    private static By goldLocator = By.cssSelector(".c_gold");
    private static By byCardForSilverButtonLocator = By.cssSelector("a[href*='/shop/cards/buy/1100/']");

    private WebDriver shopperDriver;
    private int heroEnergy;
    private int heroSilver;
    private int heroGold;

    public Shopper(WebDriver webDriver) {
        shopperDriver = SeleniumUtils.cloneDriverInstance(webDriver, MARKET_PAGE_URI);
        updateTreasury();
        LOG.info(Utils.getMessage("shopper.info.created"));
    }

    private void updateTreasury() {
        heroEnergy = SeleniumUtils.getIntValueFromElement(shopperDriver, energyLocator);
        heroSilver = SeleniumUtils.getIntValueFromElement(shopperDriver, silverLocator);
        heroGold = SeleniumUtils.getIntValueFromElement(shopperDriver, goldLocator);
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("shopper.info.thread.start"));
        int possibleAmountOfCardsToBy = heroSilver / 500;
        while (true) {
            LOG.info(Utils.getMessage("shopper.info.shop.heroTreasures", heroEnergy, heroSilver, heroGold));
            if (possibleAmountOfCardsToBy > 0) {
                LOG.info(Utils.getMessage("shopper.info.shop.cardsToBy", possibleAmountOfCardsToBy, possibleAmountOfCardsToBy * 500));
                letsGoShopping(possibleAmountOfCardsToBy);
            } else {
                rest();
            }
            updateTreasury();
            possibleAmountOfCardsToBy = heroSilver / 500;
        }
    }

    public void letsGoShopping(int possibleAmountOfCardsToBy) {
        try {
            for (; possibleAmountOfCardsToBy > 0; possibleAmountOfCardsToBy--) {
                Thread.sleep(Utils.getSuperShortDelay());
                SeleniumUtils.getWebElement(shopperDriver, byCardForSilverButtonLocator).click();
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
            LOG.info(Utils.getMessage("shopper.info.shop.gotMoreMoney"));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
