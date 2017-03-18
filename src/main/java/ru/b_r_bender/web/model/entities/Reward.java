package ru.b_r_bender.web.model.entities;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.utils.SeleniumUtils;

/**
 * @author BRBender created on 17.03.2017.
 */
public enum Reward {
    WIN_20_DUELS(By.xpath("//*[text()='Выиграйте 20 дуэлей']/..//div[@class='left pt5']"), 20),
    PARTICIPATE_IN_30_DUELS(By.xpath("//*[text()='Проведите 30 дуэлей']/..//div[@class='left pt5']"), 30),
    GET_3_PLAY_CARDS(By.xpath("//*[text()='Добудьте 3 карты в кампании']/..//div[@class='left pt5']"), 3),
    PARTICIPATE_IN_10_ARENAS(By.xpath("//*[text()='Поучаствуйте в 10 боях на арене']/..//div[@class='left pt5']"), 10),
    WIN_5_ARENAS(By.xpath("//*[text()='Выиграйте 5 арен']/..//div[@class='left pt5']"), 5),
    UPGRADE_5_PLAY_CARDS(By.xpath("//*[text()='Улучшите боевые карты 5 раз']/..//div[@class='left pt5']"), 5);

    private static final String REWARD_PAGE_URI = "http://elem.mobi/daily/";

    By rewardProgressLocator;
    int rewardTarget;
    boolean rewardCollected;

    Reward(By rewardProgressLocator, int rewardTarget) {
        this.rewardProgressLocator = rewardProgressLocator;
        this.rewardTarget = rewardTarget;
    }

    /**
     * Get reward progress in int value.<br>
     * If progress element not available for reward locator consider reward already taken.
     * @param webDriver driver used to get elements
     * @return null if progress not available (reward collected), otherwise int value of current progress
     */
    public int getRewardProgress(WebDriver webDriver) {
        webDriver.get(REWARD_PAGE_URI);
        WebElement rewardProgressElement = SeleniumUtils.getWebElement(webDriver, rewardProgressLocator);
        if (rewardProgressElement == null) {
            webDriver.navigate().back();
            return this.rewardTarget;
        } else {
            String rewardProgressElementText = rewardProgressElement.getText();
            webDriver.navigate().back();
            return parseProgress(rewardProgressElementText);
        }
    }

    public boolean isRewardCollected(WebDriver webDriver) {
        return this.rewardCollected;
    }

    private int parseProgress(String textToParse) {
        String message = textToParse.split("\n")[0];
        if ("Выполнено!".equals(message)) {
            return this.rewardTarget;
        } else {
            int startIndex = message.indexOf(":") + 1;
            int endIndex = message.indexOf("из");
            return Integer.parseInt(message.substring(startIndex, endIndex).trim());
        }
    }
}
