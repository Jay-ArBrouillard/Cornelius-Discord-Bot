package utils;

import java.util.concurrent.ThreadLocalRandom;

public class CorneliusUtils {
    public static final String[] QUIT = {"q", "Q", "quit", "QUIT"};
    
    public static boolean isNumeric(final String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException nfe) {
        }
        return false;
    }

    public static double normalize(double value, double tMin, double tMax, double min, double max) {
        return ((value - tMin) / (tMax - tMin)) * (min - max) + min;
    }

    public static double randomNumber01() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static int randomIntBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}