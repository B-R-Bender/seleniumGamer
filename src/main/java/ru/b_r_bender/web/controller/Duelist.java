package ru.b_r_bender.web.controller;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.List;

/**
 * @author BRBender created on 28.02.2017.
 */
public class Duelist implements Runnable {

    private static final String DUEL_PAGE_URI = "http://elem.mobi/duel/";

    private static By coolDownMessageLocator = By.cssSelector(".txt.cntr.small");
    private static By heroStatsLocator = By.cssSelector(".c_da");
    private static By opponentStatsLocator = By.cssSelector(".c_da.mt5.mr5");
    private static By duelToBattleButtonLocator = By.cssSelector("a[href='/duel/tobattle/']");
    private static By nextBattleButtonLocator = By.cssSelector("a[href='/duel/']");
    private static By cardsButtonLocator = By.cssSelector("a[href*='/duel/'][class='card']");
    private static By battleEndLocator = By.cssSelector(".mr.wing");

    private boolean duelAvailable;
    private WebDriver duelistDriver;

    public Duelist(WebDriver webDriver, boolean duelAvailable) {
        this.duelAvailable = duelAvailable;
        this.duelistDriver = SeleniumUtils.cloneDriverInstance(webDriver, DUEL_PAGE_URI);
    }

    public void run() {
        while (true) {
            if (duelAvailable) {
                killEmAll();
            } else {
                try {
                    long coolDownTime = Utils.calculateElementCoolDownTime(duelistDriver, coolDownMessageLocator);
                    coolDownTime += Utils.getLongDelay();
                    Thread.sleep(coolDownTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkDuelAvailable();
            }
        }
    }

    public void checkDuelAvailable() {
        duelAvailable = SeleniumUtils.getWebElement(duelistDriver, coolDownMessageLocator) == null;
    }

    public void killEmAll() {
        while (opponentIsToStrong()) {
            findNewOpponent();
        }
        SeleniumUtils.getWebElement(duelistDriver, duelToBattleButtonLocator).click();
        defeatThatGuy();
    }

    private boolean opponentIsToStrong() {
        Integer heroStats = SeleniumUtils.getIntValueFromElement(duelistDriver, heroStatsLocator);
        Integer opponentStats = SeleniumUtils.getIntValueFromElement(duelistDriver, opponentStatsLocator);
        return calculateHeroCap(heroStats) < opponentStats;
    }

    private Integer calculateHeroCap(final Integer heroStats) {
        Integer cap = heroStats;
        return cap += new Long(Math.round(cap*0.07)).intValue();
    }

    private void findNewOpponent() {
        SeleniumUtils.getWebElement(duelistDriver, nextBattleButtonLocator).click();
    }

    private void defeatThatGuy() {
        while (SeleniumUtils.getWebElement(duelistDriver, battleEndLocator) == null) {
            WebElement attack = calculateNexAttack();
            attack.click();
        }
    }

    //TODO: implement fight here
    private WebElement calculateNexAttack() {
        List<WebElement> webElements = SeleniumUtils.getWebElements(duelistDriver, cardsButtonLocator);
        return null;
    }


}
