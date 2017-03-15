package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.model.pages.MainPage;
import ru.b_r_bender.web.utils.SeleniumUtils;
import ru.b_r_bender.web.utils.Utils;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * @author BRBender created on 07.03.2017.
 */
public class RewardCollector implements Runnable {
    private static final Logger LOG = Logger.getLogger(RewardCollector.class);

    public static final String COLLECTOR_PAGE_URI = "http://elem.mobi/daily/";
    public static volatile boolean allTasksCompleted;
    public static volatile boolean dailyRewardClaimed;

    private static final By MAIN_PAGE_DAILY_REWARD_LOCATOR = By.cssSelector("a[href*='/dailyreward/take/']");
    private static final By DAILY_TASKS_REWARDS_LOCATOR = By.cssSelector("a[href*='/daily/reward/']");

    private WebDriver collectorDriver;

    public RewardCollector(WebDriver webDriver) {
        this.collectorDriver = SeleniumUtils.cloneDriverInstance(webDriver, COLLECTOR_PAGE_URI);
        LOG.info(Utils.getMessage("rewardCollector.info.created"));
    }

    @Override
    public void run() {
        LOG.info(Utils.getMessage("rewardCollector.info.thread.start"));
        while (true) {
            collectDailyReward();
            sleepTillTasksHarvestTime();
            collectDailyTasksRewards();
            sleepTillNextDay();
        }
    }

    private void collectDailyReward() {
        boolean dailyRewardAvailable = false;
        collectorDriver.get(MainPage.MAIN_PAGE_URI);
        WebElement dailyRewardElement;
        while ((dailyRewardElement = SeleniumUtils.getWebElement(collectorDriver, MAIN_PAGE_DAILY_REWARD_LOCATOR)) != null) {
            dailyRewardAvailable = true;
            LOG.info(Utils.getMessage("rewardCollector.info.daily.rewardAvailable"));
            dailyRewardElement.click();
            LOG.info(Utils.getMessage("rewardCollector.info.daily.get"));
        }
        LOG.info(dailyRewardAvailable
                ? Utils.getMessage("rewardCollector.info.daily.got")
                : Utils.getMessage("rewardCollector.info.daily.rewardDoNotAvailable"));
        collectorDriver.get(COLLECTOR_PAGE_URI);
    }

    private void collectDailyTasksRewards() {
        LOG.info(Utils.getMessage("rewardCollector.info.tasks.get"));
        int totalRewardsCollected = 0;
        List<WebElement> rewardElements = SeleniumUtils.getWebElements(collectorDriver, DAILY_TASKS_REWARDS_LOCATOR);
        for (int i = 0; i < rewardElements.size(); i++) {
            SeleniumUtils.getWebElement(collectorDriver, DAILY_TASKS_REWARDS_LOCATOR).click();
            totalRewardsCollected++;
        }
        LOG.info(Utils.getMessage("rewardCollector.info.tasks.got", totalRewardsCollected));
    }

    private void sleepTillTasksHarvestTime() {
        Calendar fiveMinutesToTwelve = SeleniumUtils.getServerTime(collectorDriver);
        fiveMinutesToTwelve.add(Calendar.DAY_OF_MONTH, 1);
        fiveMinutesToTwelve.set(Calendar.HOUR_OF_DAY, 0);
        fiveMinutesToTwelve.set(Calendar.MINUTE, -5);
        fiveMinutesToTwelve.set(Calendar.SECOND, 0);
        fiveMinutesToTwelve.set(Calendar.MILLISECOND, 0);
        Calendar now = SeleniumUtils.getServerTime(collectorDriver);
        long millisTillFiveToTwelve = fiveMinutesToTwelve.getTimeInMillis() - now.getTimeInMillis();
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.tasks", millisTillFiveToTwelve));
        try {
            Thread.sleep(millisTillFiveToTwelve);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.awake"));
    }

    private void sleepTillNextDay() {
        Calendar onePastZero = SeleniumUtils.getServerTime(collectorDriver);
        onePastZero.add(Calendar.DAY_OF_MONTH, 1);
        onePastZero.set(Calendar.HOUR_OF_DAY, 0);
        onePastZero.set(Calendar.MINUTE, 0);
        onePastZero.set(Calendar.SECOND, 30);
        onePastZero.set(Calendar.MILLISECOND, 0);
        Calendar now = SeleniumUtils.getServerTime(collectorDriver);
        long millisTillOnePastZero = onePastZero.getTimeInMillis() - now.getTimeInMillis();
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.daily", millisTillOnePastZero));
        try {
            Thread.sleep(millisTillOnePastZero);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.awake"));
    }

}