package ru.b_r_bender.web.controller;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

/**
 * @author BRBender on 01.03.17.
 */
public class Shopper implements Runnable {

    public static final String MARKET_PAGE_URI = "http://elem.mobi/shop/";

    private static By energyLocator = By.cssSelector(".c_energy");
    private static By silverLocator = By.cssSelector(".c_silver");
    private static By goldLocator = By.cssSelector(".c_gold");
    private static By byCardForSilverButtonLocator = By.cssSelector("a[href*='/shop/cards/buy/1100/']");
    private static By serverTimeClockLocator = By.id("server_time");

    private WebDriver shopperDriver;
    private int heroEnergy;
    private int heroSilver;
    private int heroGold;

    public Shopper(WebDriver webDriver) {
        shopperDriver = SeleniumUtils.cloneDriverInstance(webDriver, MARKET_PAGE_URI);
        updateTreasury();
    }

    //FIXME: i'm broken
    private void updateTreasury() {
        heroEnergy = SeleniumUtils.getIntValueFromElement(shopperDriver, energyLocator);
        heroSilver = SeleniumUtils.getIntValueFromElement(shopperDriver, silverLocator);
        heroGold = SeleniumUtils.getIntValueFromElement(shopperDriver, goldLocator);
    }

    @Override
    public void run() {
        int possibleAmountOfCardsToBy = heroSilver / 500;
        while (true) {
            if (possibleAmountOfCardsToBy-- > 0) {
                SeleniumUtils.getWebElement(shopperDriver, byCardForSilverButtonLocator).click();
            } else {
                try {
                    Thread.sleep(Utils.getMediumDelay());
                } catch (InterruptedException e) {
                    //TODO: логгирование
                    e.printStackTrace();
                }
                updateTreasury();
                possibleAmountOfCardsToBy = heroSilver / 500;
            }
        }
    }

    public void letsGoShopping() {

    }
}
