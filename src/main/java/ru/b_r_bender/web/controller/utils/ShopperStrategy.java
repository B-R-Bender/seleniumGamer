package ru.b_r_bender.web.controller.utils;

/**
 * Class will calculate how much money Shopper can spend.
 *
 * @author BRBender created on 07.03.2017.
 */
public class ShopperStrategy {

    private ShopperStrategy() {
    }

    /**
     * This strategy will allowed to spend only modulus from hero money divided by 50_000 and still more 20% of the rest
     * @param heroMoney total hero money value
     * @return how much money Shopper can spend
     */
    public static int hundredsOfThousandsSavingShopper(int heroMoney) {
        heroMoney = heroMoney - heroMoney / 50_000 * 50_000;
        return shopperCalculations(heroMoney, 0.2);
    }

    /**
     * This strategy will save 20% of all Hero money
     * @param heroMoney total hero money value
     * @return how much money Shopper can spend
     */
    public static int lightSavingShopper(int heroMoney) {
        return shopperCalculations(heroMoney, 0.2);
    }

    /**
     * This strategy will save 40% of all Hero money
     * @param heroMoney total hero money value
     * @return how much money Shopper can spend
     */
    public static int tightSavingShopper(int heroMoney) {
        return shopperCalculations(heroMoney, 0.4);
    }

    /**
     * This strategy will save nothing
     * @param heroMoney total hero money value
     * @return how much money Shopper can spend
     */
    public static int spendthriftShopper(int heroMoney) {
        return shopperCalculations(heroMoney, 0);
    }

    private static int shopperCalculations(int heroMoney, double savingValue) {
        return new Long(Math.round(heroMoney * (1 - savingValue))).intValue();
    }
}
