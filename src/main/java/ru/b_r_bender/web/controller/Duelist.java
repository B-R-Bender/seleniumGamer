package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.DuelAttackOption;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.*;

/**
 * @author BRBender created on 28.02.2017.
 */
public class Duelist implements Runnable {
    private static final Logger LOG = Logger.getLogger(Duelist.class);

    public static final String DUEL_PAGE_URI = "http://elem.mobi/duel/";

    private static By coolDownMessageLocator = By.cssSelector(".txt.cntr.small");
    private static By heroStatsLocator = By.cssSelector(".c_da");
    private static By opponentStatsLocator = By.cssSelector(".c_da.mt5.mr5");
    private static By duelToBattleButtonLocator = By.cssSelector("a[href='/duel/tobattle/']");
    private static By nextBattleButtonLocator = By.cssSelector("a[href='/duel/']");
    private static By heroCardsButtonLocator = By.cssSelector("a[href*='/duel/'][class='card']");
    private static By opponentCardsButtonLocator = By.cssSelector("a[href*='/duel/'][class='card chide66']");
    private static By damageMultiplierButtonLocator = By.cssSelector(".small.mb5");
    private static By battleEndGotMoreButtonLocator = By.cssSelector("a[class*='btn'][href='/duel/']");
    private static By battleEndNoMoreButtonLocator = By.cssSelector("a[class*='btn'][href*='/duel/restore_duels']");

    private boolean duelAvailable;
    private WebDriver duelistDriver;

    public Duelist(WebDriver webDriver, boolean duelAvailable) {
        this.duelAvailable = duelAvailable;
        this.duelistDriver = SeleniumUtils.cloneDriverInstance(webDriver, DUEL_PAGE_URI);
        LOG.info(Utils.getMessage("duelist.info.created"));
    }

    public void run() {
        LOG.info(Utils.getMessage("duelist.info.thread.start"));
        while (true) {
            if (duelAvailable) {
                killEmAll();
                if (isNexFreeDuelAvailable()) {
                    getNextFreeDuel();
                }
                duelAvailable = !isCoolDownActive();
            } else {
                rest();
            }
        }
    }

    private void rest() {
//        long coolDownTime = Utils.calculateElementCoolDownTime(DUEL_PAGE_URI);
        long coolDownTime = Utils.getShortDelay();
        try {
            LOG.info(Utils.getFormattedMessage("duelist.info.duel.rest", coolDownTime));
            Thread.sleep(coolDownTime);
            LOG.info(Utils.getFormattedMessage("duelist.info.duel.rested", coolDownTime));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        SeleniumUtils.refresh(duelistDriver);
        duelAvailable = !isCoolDownActive();
    }

    public boolean isCoolDownActive() {
        return SeleniumUtils.getWebElement(duelistDriver, coolDownMessageLocator) != null;
    }

    public boolean isNexFreeDuelAvailable() {
        return SeleniumUtils.getWebElement(duelistDriver, battleEndGotMoreButtonLocator) != null;
    }

    public void killEmAll() {
        int opponentSkipCount = 0;
        int opponentsBoundary = Utils.skippedOpponentsBoundary();
        LOG.info(Utils.getFormattedMessage("duelist.info.attemptToFindOpponent", opponentsBoundary));
        while (opponentIsToStrong(opponentSkipCount++, opponentsBoundary)) {
            try {
                Thread.sleep(Utils.getSuperShortDelay());
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            findNewOpponent();
        }
        startBattle();
        defeatThatGuy();
    }

    private boolean opponentIsToStrong(int opponentSkipCount, int opponentsBoundary) {
        if (opponentSkipCount == opponentsBoundary) {
            LOG.info(Utils.getFormattedMessage("duelist.info.comparingOpponent.NoMoreTries", opponentsBoundary));
            return false;
        }
        Integer heroStrength = SeleniumUtils.getIntValueFromElement(duelistDriver, heroStatsLocator);
        Integer opponentStrength = SeleniumUtils.getIntValueFromElement(duelistDriver, opponentStatsLocator);
        boolean result = calculateHeroCap(heroStrength) < opponentStrength;
        LOG.info(Utils.getFormattedMessage("duelist.info.comparingOpponent",
                opponentSkipCount,
                heroStrength,
                opponentStrength,
                result ? "Opponent is to strong" : "Duelist has found \"our guy\""));
        return result;
    }

    private Integer calculateHeroCap(final Integer heroStrength) {
        Integer cap = heroStrength;
        return cap += new Long(Math.round(cap * 0.07)).intValue();
    }

    private void findNewOpponent() {
        LOG.info(Utils.getMessage("duelist.info.attemptToFindOpponent.getNext"));
        SeleniumUtils.getWebElement(duelistDriver, nextBattleButtonLocator).click();
    }

    private void startBattle() {
        LOG.info(Utils.getMessage("duelist.info.duel.start"));
        SeleniumUtils.getWebElement(duelistDriver, duelToBattleButtonLocator).click();
    }

    private void getNextFreeDuel() {
        LOG.info(Utils.getMessage("duelist.info.duel.next"));
        SeleniumUtils.getWebElement(duelistDriver, battleEndGotMoreButtonLocator).click();
    }

    private void defeatThatGuy() {
        while (!isNexFreeDuelAvailable() && !isCoolDownActive()) {
            List<WebElement> opponentCards = SeleniumUtils.getWebElements(duelistDriver, opponentCardsButtonLocator);
            List<WebElement> damageMultipliers = SeleniumUtils.getWebElements(duelistDriver, damageMultiplierButtonLocator);
            List<WebElement> heroCards = SeleniumUtils.getWebElements(duelistDriver, heroCardsButtonLocator);
            List<DuelAttackOption> attackOptions = new ArrayList<>(3);
            for (int i = 0; i < 3; i++) {
                int opponentStrength = Integer.parseInt(opponentCards.get(i).getText());
                double damageMultiplier = Utils.parseMultiplierValue(damageMultipliers.get(i).getText());
                int heroStrength = Integer.parseInt(heroCards.get(i).getText());
                DuelAttackOption attackOption =
                        new DuelAttackOption(heroCards.get(i), opponentStrength, damageMultiplier, heroStrength);
                attackOptions.add(attackOption);
                LOG.info(Utils.getFormattedMessage("duelist.info.duel.step.attackOption", i, attackOption));
            }
            Collections.sort(attackOptions);
            DuelAttackOption bestDuelAttackOption = attackOptions.get(2);
            LOG.info(Utils.getFormattedMessage("duelist.info.duel.step.attack", bestDuelAttackOption));
            try {
                Thread.sleep(Utils.getSuperShortDelay());
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            bestDuelAttackOption.attack();
        }
    }

}
