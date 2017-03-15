package ru.b_r_bender.web.controller.utils;

/**
 * @author BRBender created on 14.03.2017.
 */
public class CardsUtils {

    public static int getStrengthAtSpecifiedLevel(int cardLevel) {
        int resultStrength = 0;

        //MYTODO [Homenko] верные значения для уровней
        if (cardLevel % 5 == 0) {
            switch (cardLevel) {
                case 5:
                    break;
                case 10:
                    break;
                case 15:
                    break;
                case 20:
                    break;
                case 25:
                    break;
                case 30:
                    break;
                case 35:
                    break;
                case 40:
                case 45:
                case 50:
                case 55:
                    resultStrength = 160;
                    break;
                case 60:
                case 65:
                    resultStrength = 250;
                    break;
                case 70:
                    resultStrength = 350;
                    break;
            }
            return resultStrength;
        }

        if (cardLevel > 0 && cardLevel < 20) {
            resultStrength = 10;
        } else if (cardLevel > 20 && cardLevel < 40) {
            resultStrength = 20;
        } else if (cardLevel > 40 && cardLevel < 60) {
            resultStrength = 40;
        } else if (cardLevel > 60) {
            resultStrength = 50;
        }

        return resultStrength;
    }

}
