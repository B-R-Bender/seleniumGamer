package ru.b_r_bender.web.controller.utils;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.DuelAttackOption;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.*;

/**
 * @author BRBender on 17.03.17.
 */
public class Gladiator implements Runnable {

    private static final Logger LOG = Logger.getLogger(Gladiator.class);

    public static final String ARENA_PAGE_URI = "http://elem.mobi/arena/";

    private static final By ARENA_CHECK_IN_BUTTON_LOCATOR = By.xpath("//a[@href='/arena/join/']");
    private static final By ARENA_REFRESH_BUTTON_LOCATOR = By.xpath("//a[@href='/arena/']");
    private static final By ARENA_TASKS_LOCATOR = By.xpath("//div[@class='c_99 cntr small']");
    private static final By ARENA_BATTLE_COUNTDOWN_LOCATOR = By.xpath("//div[@class='c_fe']");

    private static final By HERO_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/arena/'][class='card']");
    private static final By OPPONENT_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/arena/'][class='card chide66']");
    private static final By DAMAGE_MULTIPLIER_BUTTON_LOCATOR = By.cssSelector(".small.mb5");

    private static final By ARENA_WIN_LOCATOR = By.cssSelector(".c_win.medium");
    private static final By ARENA_LOSE_LOCATOR = By.cssSelector(".c_lose.medium");

    private WebDriver gladiatorDriver;
    private boolean arenaIsOpen;

    public Gladiator(WebDriver gladiatorDriver) {
        this.gladiatorDriver = SeleniumUtils.cloneDriverInstance(gladiatorDriver, ARENA_PAGE_URI);
    }

    @Override
    public void run() {
        while (true) {
            forTheEmperor();
            sharpenWeapons();
        }
    }

    private void forTheEmperor() {
        int arenaWins = 0;
        int arenaParticipate = 0;
        WebElement arenaTasksElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_TASKS_LOCATOR);
        while ((arenaWins < 5 || arenaParticipate++ < 10) && arenaTasksElement != null) {
            enterArena();
            if (fight()) {
                arenaWins++;
            }
            SeleniumUtils.getWebElement(gladiatorDriver, ARENA_REFRESH_BUTTON_LOCATOR).click();
        }
    }

    private void enterArena() {
        try {
            SeleniumUtils.getWebElement(gladiatorDriver, ARENA_CHECK_IN_BUTTON_LOCATOR).click();
            Integer countdown = SeleniumUtils.getIntValueFromElement(gladiatorDriver, ARENA_BATTLE_COUNTDOWN_LOCATOR) * 1_000;
            Thread.sleep(countdown != null ? countdown : 30_000);
            SeleniumUtils.getWebElement(gladiatorDriver, ARENA_REFRESH_BUTTON_LOCATOR).click();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private boolean fight() {
        WebElement winElement;
        WebElement loseElement;
        while ((winElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_WIN_LOCATOR)) == null
                && (loseElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_LOSE_LOCATOR))== null) {
            try {
                Thread.sleep(Utils.getTenSecondsDelay());
                SeleniumUtils.refresh(gladiatorDriver);
                if ((winElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_WIN_LOCATOR)) != null
                        || SeleniumUtils.getWebElement(gladiatorDriver, ARENA_LOSE_LOCATOR) != null) {
                    break;
                }
                List<WebElement> opponentCards = SeleniumUtils.getWebElements(gladiatorDriver, OPPONENT_CARDS_BUTTON_LOCATOR);
                List<WebElement> damageMultipliers = SeleniumUtils.getWebElements(gladiatorDriver, DAMAGE_MULTIPLIER_BUTTON_LOCATOR);
                List<WebElement> heroCards = SeleniumUtils.getWebElements(gladiatorDriver, HERO_CARDS_BUTTON_LOCATOR);
                List<DuelAttackOption> attackOptions = new ArrayList<>(3);
                for (int i = 0; i < 3; i++) {
                    int opponentStrength = Integer.parseInt(opponentCards.get(i).getText());
                    double damageMultiplier = Utils.parseMultiplierValue(damageMultipliers.get(i).getText());
                    int heroStrength = Integer.parseInt(heroCards.get(i).getText());
                    DuelAttackOption attackOption =
                            new DuelAttackOption(heroCards.get(i), opponentStrength, damageMultiplier, heroStrength);
                    attackOptions.add(attackOption);
                }
                attackOptions.sort(Comparator.comparingInt(DuelAttackOption::getAttackStrength));
                DuelAttackOption bestDuelAttackOption = attackOptions.get(2);
                bestDuelAttackOption.attack();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return winElement != null;
    }

    private void sharpenWeapons() {
        try {
            Calendar tomorrow = SeleniumUtils.getTomorrow(gladiatorDriver);
            tomorrow.set(Calendar.MINUTE, 15);
            Calendar now = SeleniumUtils.getServerTime(gladiatorDriver);

            long millisTillTomorrowArena = tomorrow.getTimeInMillis() - now.getTimeInMillis();
            String timeString = Utils.millisecondsToTimeString(millisTillTomorrowArena);
            LOG.info("sharping weapon" + timeString);
            Thread.sleep(millisTillTomorrowArena);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
