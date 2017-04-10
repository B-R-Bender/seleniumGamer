package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import ru.b_r_bender.web.controller.utils.ShopperStrategy;
import ru.b_r_bender.web.model.pages.MainPage;
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

    private static final int RARE_CARD_VALUE = 500;
    private static final int LEGEND_CARD_VALUE = 20;
    private static final int MYTH_CARD_VALUE = 120;

    private WebDriver shopperDriver;
    private int heroEnergy;
    private int heroSilver;
    private int heroGold;

    public Shopper(WebDriver webDriver) {
        shopperDriver = SeleniumUtils.cloneDriverInstance(webDriver, MARKET_PAGE_URI);
        MainPage.addDriver(shopperDriver);
        updateTreasury();
        LOG.info(Utils.getMessage("shopper.info.created"));
    }

    private void updateTreasury() {
        heroEnergy = SeleniumUtils.getIntValueFromElement(shopperDriver, ENERGY_LOCATOR);
        heroSilver = SeleniumUtils.getIntValueFromElement(shopperDriver, SILVER_LOCATOR);
        heroGold = SeleniumUtils.getIntValueFromElement(shopperDriver, GOLD_LOCATOR);
        LOG.info(Utils.getMessage("shopper.info.shop.heroTreasures", heroEnergy, heroSilver, heroGold));
    }

    @Override
    public void run() {
        try {
            LOG.info(Utils.getMessage("shopper.info.thread.start"));
            while (true) {
                letsGoShopping(howMuchCardsCanIBuy(heroSilver, RARE_CARD_VALUE));
                rest();
                updateTreasury();
            }
        } catch (WebDriverException e) {
            LOG.error("Trying to restart thread because there was an error in WebDriver: ", e);
            shopperDriver.close();
            MainPage.resurrectMe(Shopper.class);
        } catch (Exception e) {
            String screenName = SeleniumUtils.takeErrorScreenShot(shopperDriver);
            LOG.error("Screen shot taken and saved in " + screenName + " for error:\n" + e.getMessage(), e);
        } finally {
            shopperDriver.close();
        }
    }

    private int howMuchCardsCanIBuy(int moneyAmount, int cardValue) {
        return cardValue > 0 ? ShopperStrategy.hundredsOfThousandsSavingShopper(moneyAmount) / cardValue : 0;
    }

    private void letsGoShopping(int possibleAmountOfCardsToBy) {
        LOG.info(Utils.getMessage("shopper.info.shop.cardsToBy", possibleAmountOfCardsToBy, possibleAmountOfCardsToBy * 500));
        try {
            for (; possibleAmountOfCardsToBy > 0; possibleAmountOfCardsToBy--) {
                Thread.sleep(Utils.getShortDelay());
                SeleniumUtils.getWebElement(shopperDriver, BY_CARD_FOR_SILVER_BUTTON_LOCATOR).click();
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void rest() {
        try {
            long coolDownTime = Utils.calculateElementCoolDownTime(MARKET_PAGE_URI);
            String timeString = Utils.millisecondsToTimeString(coolDownTime);
            LOG.info(Utils.getMessage("shopper.info.shop.notEnoughMoney", timeString));
            Thread.sleep(coolDownTime);
            SeleniumUtils.refresh(shopperDriver);
            LOG.info(Utils.getMessage("shopper.info.shop.gotMoreMoney"));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
