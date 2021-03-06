package ru.b_r_bender.web.utils;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import ru.b_r_bender.web.controller.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * Utility class containing methods to get random delays, to calculate different values so on.
 *
 * @author BRBender created on 28.02.2017.
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class);

    private Utils() {
    }

    private static Random random = new Random();
    private static ResourceBundle messages = ResourceBundle.getBundle("messages");
    private static Properties appProperties = new Properties();

    static {
        try (InputStream stream = Utils.class.getClassLoader().getResourceAsStream("app.properties");){
            appProperties.load(stream);
        } catch (java.io.IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Method will calculate cool down time for specified <b>pageUri</b> till it will be active again.
     * by default method will return 1+ hours.
     * @return cool down time in milliseconds
     */
    public static long calculateElementCoolDownTime(String pageUri) {
        return calculateElementCoolDownTime(pageUri, null);
    }

    /**
     * Method will calculate cool down time for specified <b>pageUri</b> till it will be active again.
     * by default method will return 1+ hours. You can specified a web element to calculate cool down for.
     * @return cool down time in milliseconds
     */
    public static long calculateElementCoolDownTime(String pageUri, WebElement coolDownElement) {
        switch (pageUri) {
            case Watcher.WATCHER_PAGE_URI:
                return 3_600_000 + getLongDelay();
            case DungeonKeeper.DUNGEON_PAGE_URI:
                return parseCoolDownString(coolDownElement.getText());
            case Duelist.DUEL_PAGE_URI:
                String text = coolDownElement.getText();
                String coolDownText = text.split("\n")[1];
                String coolDownValue = coolDownText.substring(coolDownText.indexOf("через") + 5).trim();

                return parseCoolDownString(coolDownValue);
            case Shopper.MARKET_PAGE_URI:
                return 36_000_000 + getLongDelay();
            case DeckManager.PLAY_DECK_PAGE_URI:
                return 7_700_700 + getLongDelay();
            default:
                return 3_600_000 + getLongDelay();
        }
    }

    private static long parseCoolDownString(String coolDownValue) {
        int hourIndex;
        int minuteIndex;
        int secondsIndex;

        int hourCoolDownValue = 0;
        int minuteCoolDownValue = 0;
        int secondsCoolDownValue = 0;

        hourIndex = coolDownValue.indexOf("ч");
        minuteIndex = coolDownValue.indexOf("м");
        secondsIndex = coolDownValue.indexOf("с");

        if (hourIndex == -1) {
            if (minuteIndex == -1) {
                secondsCoolDownValue = Integer.parseInt(coolDownValue.substring(minuteIndex+1,secondsIndex).trim());
            } else {
                minuteCoolDownValue = Integer.parseInt(coolDownValue.substring(hourIndex + 1, minuteIndex).trim());
                if (secondsIndex != -1) {
                    secondsCoolDownValue = Integer.parseInt(coolDownValue.substring(minuteIndex + 1, secondsIndex).trim());
                }
            }
        } else {
            hourCoolDownValue = Integer.parseInt(coolDownValue.substring(0,hourIndex).trim());
            if (minuteIndex != -1) {
                minuteCoolDownValue = Integer.parseInt(coolDownValue.substring(hourIndex + 1, minuteIndex).trim());
            }
        }
        return hourCoolDownValue * 3_600_000
                + minuteCoolDownValue * 60_000
                + secondsCoolDownValue * 1_000
                + getMediumDelay();
    }

    /**
     * Return 10 seconds delay.
     *
     * @return delay in milliseconds
     */
    public static long getTenSecondsDelay() {
        return 10_000;
    }

    /**
     * Return 3 seconds delay.
     *
     * @return delay in milliseconds
     */
    public static long getThreeSecondsDelay() {
        return 3_000;
    }

    /**
     * Return random super short delay time to imitate human behavior. Delay range 200 - 1000 milliseconds.
     *
     * @return random delay in milliseconds
     */
    public static long getSuperShortDelay() {
        return random.nextInt(800) + 200;
    }

    /**
     * Return random short delay time to imitate human behavior. Delay range 500 - 3000 milliseconds.
     *
     * @return random delay in milliseconds
     */
    public static long getShortDelay() {
        return random.nextInt(2_500) + 500;
    }

    /**
     * Return random medium delay time to imitate human behavior. Delay range 5000 - 25000 milliseconds.
     *
     * @return random delay in milliseconds
     */
    public static long getMediumDelay() {
        return random.nextInt(20_000) + 5_000;
    }

    /**
     * Return random long delay time to imitate human behavior. Delay range 155000 - 425000 milliseconds.
     *
     * @return random delay in milliseconds
     */
    public static long getLongDelay() {
        return random.nextInt(270_000) + 155_000;
    }

    /**
     * Converted specified time in milliseconds to string pattern "%02d ч : %02d мин : %02d сек"
     * @param millis time in milliseconds
     * @return time in formatted string
     */
    public static String millisecondsToTimeString(long millis) {
        long day = (millis / (1000 * 60 * 60 * 24)) % 7;
        long hour = (millis / (1000 * 60 * 60)) % 24;
        long minute = (millis / (1000 * 60)) % 60;
        long second = (millis / 1000) % 60;

        StringBuilder builder = new StringBuilder();
        if (day != 0) {
            builder.append(String.format("%02d д : ", day));
        }
        if (hour != 0 || builder.length() != 0) {
            builder.append(String.format("%02d ч : ", hour));
        }
        if (minute != 0 || builder.length() != 0) {
            builder.append(String.format("%02d мин : ", minute));
        }
        if (second != 0 || builder.length() != 0) {
            builder.append(String.format("%02d сек", second));
        }

        return builder.toString();
    }

    /**
     * Return random integer in range 41-63 to be used as boundary of skipped opponents.
     *
     * @return random integer in range 41-63
     */
    public static int skippedOpponentsBoundary() {
        return random.nextInt(22) + 41;
    }

    /**
     * Calculates result of multiplying round to int
     *
     * @param value      vale to multiply
     * @param multiplier to apply on value
     * @return value * multiplier round to int
     */
    public static int calculateMultiplierResult(int value, double multiplier) {
        return new Long(Math.round(value * multiplier)).intValue();
    }

    public static double parseMultiplierValue(String multiplierText) {
        return Double.parseDouble(multiplierText.substring(2));
    }

    public static String getMessage(String messageKey) {
        return messages.getString(messageKey);
    }

    public static <T extends Collection> String getMessage(String messageKey, T collection) {
        Iterator iterator = collection.iterator();
        String result = "\n" + "---||---|Collection start|---||---" + "\n";
        while (iterator.hasNext()) {
            result += iterator.next().toString();
            result += "\n";
        }
        result += "---||---|Collection end|---||---";
        return getMessage(messageKey, result);
    }

    public static String getMessage(String messageKey, Object... objects) {
        return MessageFormat.format(messages.getString(messageKey), objects);
    }

    public static String getAppProperty(String key) {
        return appProperties.getProperty(key);
    }

    public synchronized static void sendEmail(String subject, String message, String... attachFiles)
                                                                                        throws MessagingException {

        String host = getAppProperty("mail.smtp.host");
        String port = getAppProperty("mail.smtp.port");
        final String userName = getAppProperty("mail.user");
        final String password = getAppProperty("mail.password");
        final String receiverAddress = getAppProperty("mail.receiver.address");
        final String senderAddress = getAppProperty("mail.user.address");

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.user", userName);
        properties.put("mail.password", password);

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(senderAddress));
        InternetAddress[] toAddresses = { new InternetAddress(receiverAddress) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(message, "UTF-8", "plain");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (String filePath : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();

                try {
                    attachPart.attachFile(filePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                multipart.addBodyPart(attachPart);
            }
        }

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        // sends the e-mail
        Transport.send(msg);
    }
}
