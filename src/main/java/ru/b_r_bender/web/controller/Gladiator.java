package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.entities.AttackOption;
import ru.b_r_bender.web.model.pages.MainPage;
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
    private static final By ARENA_CHECK_IN_REFRESH_BUTTON_LOCATOR = By.xpath("//a[@href='/arena/']");
    private static final By ARENA_TASKS_LOCATOR = By.xpath("//div[@class='c_99 cntr small']");
    private static final By ARENA_BATTLE_COUNTDOWN_LOCATOR = By.xpath("//div[@class='c_fe']");

    private static final By CARDS_REFRESH_COOL_DOWN_LOCATOR = By.cssSelector(".count");
    private static final By CARDS_REFRESH_LOCATOR = By.cssSelector("a[href*='/arena/'][class*='btn blue w100px']");
    private static final By HERO_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/arena/'][class='card']");
    private static final By OPPONENT_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/arena/'][class='card chide66']");
    private static final By DAMAGE_MULTIPLIER_BUTTON_LOCATOR = By.cssSelector(".small.mb5");

    private static final By ARENA_WIN_LOCATOR = By.xpath("//*[text()[contains(.,'Ваша команда победила')]]");
    private static final By ARENA_LOSE_LOCATOR = By.cssSelector(".c_lose.medium");

    private WebDriver gladiatorDriver;
    private int arenaWins;
    private int arenaParticipate;

    public Gladiator(WebDriver webDriver) {
        gladiatorDriver = SeleniumUtils.cloneDriverInstance(webDriver, ARENA_PAGE_URI);
        MainPage.addDriver(gladiatorDriver);
        LOG.info(Utils.getMessage("gladiator.info.created"));
    }

    @Override
    public void run() {
        try {
            LOG.info(Utils.getMessage("gladiator.info.thread.start"));
            while (true) {
                forTheEmperor();
                sharpenWeapons();
            }
        } catch (WebDriverException e) {
            LOG.error("Trying to restart thread because there was an error in WebDriver: ", e);
            MainPage.resurrectMe(Gladiator.class, gladiatorDriver);
        } catch (Exception e) {
            String screenName = SeleniumUtils.takeErrorScreenShot(gladiatorDriver);
            LOG.error("Screen shot taken and saved in " + screenName + " for error:\n" + e.getMessage(), e);
        } finally {
            SeleniumUtils.driverDismiss(gladiatorDriver);
        }
    }

    private void forTheEmperor() {
        LOG.info(Utils.getMessage("gladiator.info.forTheEmperor", Utils.getAppProperty("game.login")));
        arenaWins = 0;
        arenaParticipate = 0;
        WebElement arenaTasksElement;
        while ((arenaWins < 5 || arenaParticipate < 10)
                && (arenaTasksElement  = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_TASKS_LOCATOR))!= null) {
            LOG.info(Utils.getMessage("gladiator.info.starts", ++arenaParticipate));
            enterArena();
            if (fight()) {
                arenaWins++;
            }
            SeleniumUtils.getWebElement(gladiatorDriver, ARENA_CHECK_IN_REFRESH_BUTTON_LOCATOR).click();
        }
        LOG.info(Utils.getMessage("gladiator.info.result", arenaParticipate, arenaWins));
    }

    private void enterArena() {
        try {
            SeleniumUtils.getWebElement(gladiatorDriver, ARENA_CHECK_IN_BUTTON_LOCATOR).click();
            Integer countdown = SeleniumUtils.getIntValueFromElement(gladiatorDriver, ARENA_BATTLE_COUNTDOWN_LOCATOR);
            Thread.sleep((countdown != null ? countdown * 1_000 : 30_000) + 1_000);
            WebElement refreshElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_CHECK_IN_REFRESH_BUTTON_LOCATOR);
            if (refreshElement != null) {
                refreshElement.click();
            } else {
                SeleniumUtils.refresh(gladiatorDriver);
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private boolean fight() {
        WebElement winElement;
        WebElement loseElement;
        LOG.info(Utils.getMessage("gladiator.info.fight.start"));
        while ((winElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_WIN_LOCATOR)) == null
                && (loseElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_LOSE_LOCATOR))== null) {
            try {
                Integer coolDownTimeInSeconds =
                        SeleniumUtils.getIntValueFromElement(gladiatorDriver, CARDS_REFRESH_COOL_DOWN_LOCATOR);
                Thread.sleep(coolDownTimeInSeconds != null ? coolDownTimeInSeconds * 1_000 : 10_000);
                WebElement refreshCardsElement = SeleniumUtils.getWebElement(gladiatorDriver, CARDS_REFRESH_LOCATOR);
                if (refreshCardsElement != null) {
                    refreshCardsElement.click();
                } else {
                    SeleniumUtils.refresh(gladiatorDriver);
                }
                if ((winElement = SeleniumUtils.getWebElement(gladiatorDriver, ARENA_WIN_LOCATOR)) != null
                        || SeleniumUtils.getWebElement(gladiatorDriver, ARENA_LOSE_LOCATOR) != null) {
                    break;
                }
                List<WebElement> opponentCards = SeleniumUtils.getWebElements(gladiatorDriver, OPPONENT_CARDS_BUTTON_LOCATOR);
                List<WebElement> damageMultipliers = SeleniumUtils.getWebElements(gladiatorDriver, DAMAGE_MULTIPLIER_BUTTON_LOCATOR);
                List<WebElement> heroCards = SeleniumUtils.getWebElements(gladiatorDriver, HERO_CARDS_BUTTON_LOCATOR);
                List<AttackOption> attackOptions = new ArrayList<>(3);
                for (int i = 0; i < heroCards.size(); i++) {
                    int opponentStrength = Integer.parseInt(opponentCards.get(i).getText());
                    double damageMultiplier = Utils.parseMultiplierValue(damageMultipliers.get(i).getText());
                    int heroStrength = Integer.parseInt(heroCards.get(i).getText());
                    AttackOption attackOption =
                            new AttackOption(heroCards.get(i), opponentStrength, damageMultiplier, heroStrength);
                    attackOptions.add(attackOption);
                }
                if (attackOptions.size() == 0) {
                    continue;
                }
                attackOptions.sort(arenaWins < 5
                        ? Comparator.comparingInt(AttackOption::getAttackStrength)
                        : Comparator.comparingInt(AttackOption::getAttackStrength).reversed());
                AttackOption bestAttackOption = attackOptions.get(attackOptions.size() - 1);
                bestAttackOption.attack();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        LOG.info(winElement != null
                ? Utils.getMessage("gladiator.info.fight.end.win")
                : Utils.getMessage("gladiator.info.fight.end.lose"));
        return winElement != null;
    }

    private void sharpenWeapons() {
        try {
            Calendar tomorrow = SeleniumUtils.getTomorrow(gladiatorDriver);
            tomorrow.set(Calendar.MINUTE, 15);
            Calendar now = SeleniumUtils.getServerTime(gladiatorDriver);

            long millisTillTomorrowArena = tomorrow.getTimeInMillis() - now.getTimeInMillis();
            String timeString = Utils.millisecondsToTimeString(millisTillTomorrowArena);
            LOG.info(Utils.getMessage("gladiator.info.rest", timeString));
            Thread.sleep(millisTillTomorrowArena);
            SeleniumUtils.refresh(gladiatorDriver);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

}
