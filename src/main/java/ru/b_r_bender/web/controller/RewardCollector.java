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
            LOG.info("Ежедневная награда доступна");
            dailyRewardElement.click();
            LOG.info("Попытался забрать");
        }
        LOG.info(dailyRewardAvailable ? "Забрал" : "Награды небыло");
        collectorDriver.get(COLLECTOR_PAGE_URI);
    }

    private void collectDailyTasksRewards() {
        LOG.info("Попытаемся собрать награды за задания");
        int totalRewardsCollected = 0;
        List<WebElement> rewardElements = SeleniumUtils.getWebElements(collectorDriver, DAILY_TASKS_REWARDS_LOCATOR);
        for (int i = 0; i < rewardElements.size(); i++) {
            SeleniumUtils.getWebElement(collectorDriver, DAILY_TASKS_REWARDS_LOCATOR).click();
            totalRewardsCollected++;
        }
        LOG.info("Забрал " + totalRewardsCollected + "шт. наград");
    }

    private void sleepTillTasksHarvestTime() {
        Calendar halfPastEleven = Calendar.getInstance();
        halfPastEleven.add(Calendar.DAY_OF_MONTH, 1);
        halfPastEleven.set(Calendar.HOUR_OF_DAY, 0);
        halfPastEleven.set(Calendar.MINUTE, -5);
        halfPastEleven.set(Calendar.SECOND, 0);
        halfPastEleven.set(Calendar.MILLISECOND, 0);
        Calendar now = Calendar.getInstance();
        long millisTillHalfPastEleven = halfPastEleven.getTimeInMillis() - now.getTimeInMillis();
        LOG.info("Спим до без пяти минут двенадцать " + millisTillHalfPastEleven + " мс");
        try {
            Thread.sleep(millisTillHalfPastEleven);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("Проснулся соберем награды за задания");
    }

    private void sleepTillNextDay() {
        Calendar oneMinuteOfTomorrow = Calendar.getInstance();
        oneMinuteOfTomorrow.add(Calendar.DAY_OF_MONTH, 1);
        oneMinuteOfTomorrow.set(Calendar.HOUR_OF_DAY, 0);
        oneMinuteOfTomorrow.set(Calendar.MINUTE, 1);
        oneMinuteOfTomorrow.set(Calendar.SECOND, 0);
        oneMinuteOfTomorrow.set(Calendar.MILLISECOND, 0);
        Calendar now = Calendar.getInstance();
        long millisTillOneMinuteOfTomorrow = oneMinuteOfTomorrow.getTimeInMillis() - now.getTimeInMillis();
        LOG.info("Спим до одной минуты первого завтрашнего дня " + millisTillOneMinuteOfTomorrow + " мс");
        try {
            Thread.sleep(millisTillOneMinuteOfTomorrow);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("Проснулся соберем ежедневную награду");
    }

}
