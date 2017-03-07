package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

/**
 * @author BRBender created on 07.03.2017.
 */
public class RewardCollector implements Runnable {
    private static final Logger LOG = Logger.getLogger(RewardCollector.class);

    public static final String COLLECTOR_PAGE_URI = "http://elem.mobi/daily/";
    public static volatile boolean allTasksCompleted;
    public static volatile boolean dailyRewardClaimed;

    private static final By EVERYDAY_REWARD_LOCATOR = By.cssSelector("");

    private WebDriver collectorDriver;

    public RewardCollector(WebDriver webDriver) {
        this.collectorDriver = SeleniumUtils.cloneDriverInstance(webDriver, COLLECTOR_PAGE_URI);
        LOG.info(Utils.getMessage("rewardCollector.info.created"));
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("rewardCollector.info.thread.start"));
        while (true) {

        }
    }

    private void getReward() {
        WebElement reward = SeleniumUtils.getWebElement(collectorDriver, EVERYDAY_REWARD_LOCATOR);
        if (reward != null) {

        } else {

        }
    }
}
