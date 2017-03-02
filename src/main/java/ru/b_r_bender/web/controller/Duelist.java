package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.DuelAttackOption;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    }

    public void run() {
        while (true) {
            if (duelAvailable) {
                killEmAll();
                if (isNexFreeDuelAvailable()) {
                    SeleniumUtils.getWebElement(duelistDriver, battleEndGotMoreButtonLocator).click();
                }
                duelAvailable = !isCoolDownActive();
            } else {
                try {
                    long coolDownTime = Utils.calculateElementCoolDownTime(DUEL_PAGE_URI);
                    Thread.sleep(coolDownTime);
                } catch (InterruptedException e) {
                    //MYTODO [Homenko] логгирование
                    e.printStackTrace();
                }
                SeleniumUtils.refresh(duelistDriver);
                duelAvailable = !isCoolDownActive();
            }
        }
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
        while (opponentIsToStrong(opponentSkipCount++, opponentsBoundary)) {
            try {
                Thread.sleep(Utils.getShortDelay());
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            findNewOpponent();
        }
        SeleniumUtils.getWebElement(duelistDriver, duelToBattleButtonLocator).click();
        defeatThatGuy();
    }

    private boolean opponentIsToStrong(int opponentSkipCount, int opponentsBoundary) {
        if (opponentSkipCount == opponentsBoundary) {
            return false;
        }
        Integer heroStrength = SeleniumUtils.getIntValueFromElement(duelistDriver, heroStatsLocator);
        Integer opponentStrength = SeleniumUtils.getIntValueFromElement(duelistDriver, opponentStatsLocator);
        LOG.info("Comparing new opponent#" + opponentSkipCount
                            + ". Hero strength: " + heroStrength
                            + ". Opponent strength: " + opponentStrength);
        return calculateHeroCap(heroStrength) < opponentStrength;
    }

    private Integer calculateHeroCap(final Integer heroStrength) {
        Integer cap = heroStrength;
        return cap += new Long(Math.round(cap * 0.07)).intValue();
    }

    private void findNewOpponent() {
        SeleniumUtils.getWebElement(duelistDriver, nextBattleButtonLocator).click();
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
            }
            Collections.sort(attackOptions);
            DuelAttackOption bestDuelAttackOption = attackOptions.get(2);
            try {
                Thread.sleep(Utils.getShortDelay());
            } catch (InterruptedException e) {
                //MYTODO [Homenko] логгирование
                e.printStackTrace();
            }
            bestDuelAttackOption.attack();
        }
    }

}
