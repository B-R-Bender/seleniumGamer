package ru.b_r_bender.web.controller;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
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
    private static final By MARATHON_REWARD_LOCATOR = By.xpath("//a[@href='/notif/OapojAzy/']");
    private static final By MARATHON_END_REWARD_LOCATOR = By.cssSelector(".end");

    private WebDriver collectorDriver;

    public RewardCollector(WebDriver webDriver) {
        this.collectorDriver = SeleniumUtils.cloneDriverInstance(webDriver, COLLECTOR_PAGE_URI);
        LOG.info(Utils.getMessage("rewardCollector.info.created"));
    }

    @Override
    public void run() {
        try {
            LOG.info(Utils.getMessage("rewardCollector.info.thread.start"));
            while (true) {
                collectDailyReward();
                sleepTillTasksHarvestTime();
                collectDailyTasksRewards();
                collectWeeklyReward();
                sleepTillNextDay();
            }
        } catch (WebDriverException e) {
            LOG.error("Trying to restart thread because there was an error in WebDriver: ", e);
            collectorDriver.close();
            MainPage.resurrectMe(RewardCollector.class);
        } catch (Exception e) {
            String screenName = SeleniumUtils.takeErrorScreenShot(collectorDriver);
            LOG.error("Screen shot taken and saved in " + screenName + " for error:\n" + e.getMessage(), e);
        } finally {
            collectorDriver.close();
        }
    }

    private void collectWeeklyReward() {
        int dayOfTheWeek = Calendar.getInstance(TimeZone.getTimeZone("Europe/Moscow")).get(Calendar.DAY_OF_WEEK);
        if (dayOfTheWeek == Calendar.SATURDAY) {
            LOG.info(Utils.getMessage("rewardCollector.info.weekly.saturday"));
            WebElement marathonElement = SeleniumUtils.getWebElement(collectorDriver, MARATHON_REWARD_LOCATOR);
            boolean weeklyRewardAvailable = marathonElement != null;
            if (weeklyRewardAvailable) {
                LOG.info(Utils.getMessage("rewardCollector.info.weekly.available"));
                marathonElement.click();
                if (SeleniumUtils.getWebElement(collectorDriver, MARATHON_END_REWARD_LOCATOR) == null) {
                    SeleniumUtils.refresh(collectorDriver);
                    collectWeeklyReward();
                } else {
                    LOG.info(Utils.getMessage("rewardCollector.info.weekly.got"));
                }
            } else {
                LOG.info(Utils.getMessage("rewardCollector.info.weekly.notAvailable"));
            }
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
        collectorDriver.get(COLLECTOR_PAGE_URI);
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
        Calendar fiveMinutesToTwelve = SeleniumUtils.getTomorrow(collectorDriver);
        fiveMinutesToTwelve.set(Calendar.MINUTE, -5);
        Calendar now = SeleniumUtils.getServerTime(collectorDriver);

        long millisTillFiveToTwelve = fiveMinutesToTwelve.getTimeInMillis() - now.getTimeInMillis();
        String timeString = Utils.millisecondsToTimeString(millisTillFiveToTwelve);
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.tasks", timeString));
        try {
            Thread.sleep(millisTillFiveToTwelve > 0 ? millisTillFiveToTwelve : 0);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.awake"));
    }

    private void sleepTillNextDay() {
        Calendar onePastZero = SeleniumUtils.getTomorrow(collectorDriver);
        onePastZero.set(Calendar.SECOND, 30);
        Calendar now = SeleniumUtils.getServerTime(collectorDriver);

        long millisTillOnePastZero = onePastZero.getTimeInMillis() - now.getTimeInMillis();
        String timeString = Utils.millisecondsToTimeString(millisTillOnePastZero);
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.daily", timeString));
        try {
            Thread.sleep(millisTillOnePastZero > 0 ? millisTillOnePastZero : 0);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info(Utils.getMessage("rewardCollector.info.sleep.awake"));
    }

}