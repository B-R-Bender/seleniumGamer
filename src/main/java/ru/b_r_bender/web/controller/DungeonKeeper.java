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

import java.util.List;

/**
 * @author BRBender on 20.03.17.
 */
public class DungeonKeeper implements Runnable {

    private static final Logger LOG = Logger.getLogger(DungeonKeeper.class);

    public static final String DUNGEON_PAGE_URI = "http://elem.mobi/dungeon/";

    private static final By DUNGEONS_ENTRANCE_LOCATOR = By.cssSelector("a[href*='/dungeon/']");
    private static final By HERO_CARDS_BUTTON_LOCATOR = By.cssSelector("a[href*='/dungeon/'][class='card']");
    private static final By DUNGEON_EXIT_LOCATOR = By.cssSelector("a[href='/dungeon/'][class*='btn w180px']");
    private static final By DUNGEON_COOL_DOWN_LOCATOR = By.cssSelector(".bl.w120px.mt7.small.c_lblue2");

    private WebDriver keeperDriver;

    public DungeonKeeper(WebDriver webDriver) {
        keeperDriver = SeleniumUtils.cloneDriverInstance(webDriver, DUNGEON_PAGE_URI);
        MainPage.addDriver(keeperDriver);
        LOG.info(Utils.getMessage("dungeonKeeper.info.created"));
    }

    @Override
    public void run() {
        try {
            LOG.info(Utils.getMessage("dungeonKeeper.info.thread.start"));
            while (true) {
                downToTheDungeon();
                breatheSomeFreshAir();
            }
        } catch (WebDriverException e) {
            LOG.error("Trying to restart thread because there was an error in WebDriver: ", e);
            keeperDriver.close();
            MainPage.resurrectMe(DungeonKeeper.class);
        } catch (Exception e) {
            String screenName = SeleniumUtils.takeErrorScreenShot(keeperDriver);
            LOG.error("Screen shot taken and saved in " + screenName + " for error:\n" + e.getMessage(), e);
        } finally {
            keeperDriver.close();
        }
    }

    private void downToTheDungeon() {
        LOG.info(Utils.getMessage("dungeonKeeper.info.downToTheDungeon"));
        WebElement entranceElement;
        while ((entranceElement = SeleniumUtils.getWebElement(keeperDriver, DUNGEONS_ENTRANCE_LOCATOR)) != null) {
            LOG.info(Utils.getMessage("dungeonKeeper.info.dungeon.enter"));
            entranceElement.click();
            defeatDungeonCreature();
        }

    }

    private void defeatDungeonCreature() {
        LOG.info(Utils.getMessage("dungeonKeeper.info.dungeon.start"));
        WebElement exitElement;
        while ((exitElement = SeleniumUtils.getWebElement(keeperDriver, DUNGEON_EXIT_LOCATOR)) == null) {
            List<WebElement> heroCards = SeleniumUtils.getWebElements(keeperDriver, HERO_CARDS_BUTTON_LOCATOR);
            AttackOption bestAttackOption = heroCards.stream().map(webElement -> new AttackOption(webElement, 1000, 1, Integer.parseInt(webElement.getText())))
                    .reduce((attackOption1, attackOption2) -> attackOption1.getAttackStrength() > attackOption2.getAttackStrength() ? attackOption1 : attackOption2)
                    .orElse(null);
            if (bestAttackOption != null) {
                LOG.info(Utils.getMessage("dungeonKeeper.info.dungeon.attack", bestAttackOption));
                bestAttackOption.attack();
            }
        }
        LOG.info(Utils.getMessage("dungeonKeeper.info.dungeon.creatureDead"));
        exitElement.click();
    }

    private void breatheSomeFreshAir() {
        try {
            List<WebElement> coolDownElements = SeleniumUtils.getWebElements(keeperDriver, DUNGEON_COOL_DOWN_LOCATOR);
            Long dungeonCoolDownTime = coolDownElements.stream()
                    .reduce((one, other) -> {
                        long coolDownTimeOne = Utils.calculateElementCoolDownTime(DUNGEON_PAGE_URI, one);
                        long coolDownTimeOther = Utils.calculateElementCoolDownTime(DUNGEON_PAGE_URI, other);
                        return coolDownTimeOne < coolDownTimeOther ? one : other;
                    })
                    .map(webElement -> Utils.calculateElementCoolDownTime(DUNGEON_PAGE_URI, webElement))
                    .orElse(0L);
            LOG.info(Utils.getMessage("dungeonKeeper.info.goingUpFromTheDungeon", Utils.millisecondsToTimeString(dungeonCoolDownTime)));
            Thread.sleep(dungeonCoolDownTime);
            SeleniumUtils.refresh(keeperDriver);
            LOG.info(Utils.getMessage("dungeonKeeper.info.relaxed"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
