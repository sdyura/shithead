package net.yura.shithead.uicomponents;

/**
 * some examples taken from https://easings.net/
 *
 * input @param x is time, from 0 to 1
 * output @return distance, from 0 to 1
 */
public class Easings {

    public static double easeOutSine(double x) {
        return Math.sin((x * Math.PI) / 2);
    }

    public static double easeOutCubic(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    public static double easeOutQuint(double x) {
        return 1 - Math.pow(1 - x, 5);
    }

    public static double easeOutCirc(double x) {
        return Math.sqrt(1 - Math.pow(x - 1, 2));
    }
}
