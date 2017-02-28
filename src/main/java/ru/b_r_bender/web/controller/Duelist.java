package ru.b_r_bender.web.controller;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

/**
 * @author Homenko created on 28.02.2017.
 */
public class Duelist implements Runnable {

    private static final String DUEL_PAGE_URI = "http://elem.mobi/duel/";

    private boolean duelEnable;
    private WebDriver duelistDriver;

    private By coolDownMessageLocator = By.cssSelector(".txt.cntr.small");
    private By coolDownMessageHeaderLocator = By.cssSelector(".fttl");
    private By duelToBattleButtonLocator = By.cssSelector("a[href='/duel/tobattle/']");
    private By nextBattleButtonLocator = By.cssSelector("a[href='/duel/']");

    public Duelist(boolean duelEnable) {
        this.duelEnable = duelEnable;
        this.duelistDriver = SeleniumUtils.getNewDriverInstance();
    }

    public void run() {
        while (true) {
            if (duelEnable) {
                killEmAll();
            } else {
                try {
                    Thread.sleep(Utils.calculateElementCoolDownTime(duelistDriver, coolDownMessageLocator));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkDuelAvailable();
            }
        }
    }

    private void checkDuelAvailable() {

    }

    private void killEmAll() {

    }
}
