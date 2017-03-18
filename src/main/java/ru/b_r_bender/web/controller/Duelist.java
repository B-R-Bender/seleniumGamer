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

    private static final By COOL_DOWN_MESSAGE_LOCATOR = By.cssSelector(".txt.cntr.small");
    private static final By HERO_STATS_LOCATOR = By.cssSelector(".c_da");
    private static final By OPPONENT_STATS_LOCATOR = By.cssSelector(".c_da.mt5.mr5");
    private static final By DUEL_TO_BATTLE_BUTTON_LOCATOR = By.cssSelector("a[href='/duel/tobattle/']");
    private static final By NEXT_BATTLE_BUTTON_LOCATOR = By.cssSelector("a[href='/duel/']");
    private static final By HERO_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/duel/'][class='card']");
    private static final By OPPONENT_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/duel/'][class='card chide66']");
    private static final By DAMAGE_MULTIPLIER_BUTTON_LOCATOR = By.cssSelector(".small.mb5");
    private static final By BATTLE_END_GOT_MORE_BUTTON_LOCATOR = By.cssSelector("a[class*='btn'][href='/duel/']");
    private static final By BATTLE_END_NO_MORE_BUTTON_LOCATOR = By.cssSelector("a[class*='btn'][href*='/duel/restore_duels']");

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
        WebElement coolDownElement = SeleniumUtils.getWebElement(duelistDriver, COOL_DOWN_MESSAGE_LOCATOR);
        long coolDownTime = Utils.calculateElementCoolDownTime(DUEL_PAGE_URI, coolDownElement);
        try {
            LOG.info(Utils.getMessage("duelist.info.duel.rest", Utils.millisecondsToTimeString(coolDownTime)));
            Thread.sleep(coolDownTime);
            LOG.info(Utils.getMessage("duelist.info.duel.rested"));
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        SeleniumUtils.refresh(duelistDriver);
        duelAvailable = !isCoolDownActive();
    }

    private boolean isCoolDownActive() {
        return SeleniumUtils.getWebElement(duelistDriver, COOL_DOWN_MESSAGE_LOCATOR) != null;
    }

    private boolean isNexFreeDuelAvailable() {
        return SeleniumUtils.getWebElement(duelistDriver, BATTLE_END_GOT_MORE_BUTTON_LOCATOR) != null;
    }

    private void killEmAll() {
        int opponentSkipCount = 0;
        int opponentsBoundary = Utils.skippedOpponentsBoundary();
        LOG.info(Utils.getMessage("duelist.info.attemptToFindOpponent", opponentsBoundary));
        while (opponentRequirementsDoesNotMatch(opponentSkipCount++, opponentsBoundary)) {
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

    private boolean opponentRequirementsDoesNotMatch(int opponentSkipCount, int opponentsBoundary) {
        if (opponentSkipCount == opponentsBoundary) {
            LOG.info(Utils.getMessage("duelist.info.comparingOpponent.NoMoreTries", opponentsBoundary));
            return false;
        }
        Integer heroStrength = SeleniumUtils.getIntValueFromElement(duelistDriver, HERO_STATS_LOCATOR);
        Integer opponentStrength = SeleniumUtils.getIntValueFromElement(duelistDriver, OPPONENT_STATS_LOCATOR);
        boolean result = opponentStrength < calculatedMinOpponentStrength(heroStrength)
                            || opponentStrength > calculatedMaxOpponentStrength(heroStrength);
        LOG.info(Utils.getMessage("duelist.info.comparingOpponent",
                opponentSkipCount,
                heroStrength,
                opponentStrength,
                result ? "Opponent is to strong" : "Duelist has found \"our guy\""));
        return result;
    }

    private int calculatedMaxOpponentStrength(final int heroStrength) {
        return heroStrength + new Long(Math.round(heroStrength * 0.08)).intValue();
    }

    private int calculatedMinOpponentStrength(final int heroStrength) {
//        return heroStrength - new Long(Math.round(heroStrength * 0.05)).intValue();
        return heroStrength + new Long(Math.round(heroStrength * 0.01)).intValue();
    }

    private void findNewOpponent() {
        LOG.info(Utils.getMessage("duelist.info.attemptToFindOpponent.getNext"));
        SeleniumUtils.getWebElement(duelistDriver, NEXT_BATTLE_BUTTON_LOCATOR).click();
    }

    private void startBattle() {
        LOG.info(Utils.getMessage("duelist.info.duel.start"));
        SeleniumUtils.getWebElement(duelistDriver, DUEL_TO_BATTLE_BUTTON_LOCATOR).click();
    }

    private void getNextFreeDuel() {
        LOG.info(Utils.getMessage("duelist.info.duel.next"));
        SeleniumUtils.getWebElement(duelistDriver, BATTLE_END_GOT_MORE_BUTTON_LOCATOR).click();
    }

    private void defeatThatGuy() {
        while (!isNexFreeDuelAvailable() && !isCoolDownActive()) {
            List<WebElement> opponentCards = SeleniumUtils.getWebElements(duelistDriver, OPPONENT_CARDS_BUTTON_LOCATOR);
            List<WebElement> damageMultipliers = SeleniumUtils.getWebElements(duelistDriver, DAMAGE_MULTIPLIER_BUTTON_LOCATOR);
            List<WebElement> heroCards = SeleniumUtils.getWebElements(duelistDriver, HERO_CARDS_BUTTON_LOCATOR);
            List<DuelAttackOption> attackOptions = new ArrayList<>(3);
            for (int i = 0; i < 3; i++) {
                int opponentStrength = Integer.parseInt(opponentCards.get(i).getText());
                double damageMultiplier = Utils.parseMultiplierValue(damageMultipliers.get(i).getText());
                int heroStrength = Integer.parseInt(heroCards.get(i).getText());
                DuelAttackOption attackOption =
                        new DuelAttackOption(heroCards.get(i), opponentStrength, damageMultiplier, heroStrength);
                attackOptions.add(attackOption);
                LOG.info(Utils.getMessage("duelist.info.duel.step.attackOption", i + 1, attackOption));
            }
            Collections.sort(attackOptions);
            DuelAttackOption bestDuelAttackOption = attackOptions.get(2);
            LOG.info(Utils.getMessage("duelist.info.duel.step.attack", bestDuelAttackOption));
            try {
                Thread.sleep(Utils.getSuperShortDelay());
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            bestDuelAttackOption.attack();
        }
    }

}
