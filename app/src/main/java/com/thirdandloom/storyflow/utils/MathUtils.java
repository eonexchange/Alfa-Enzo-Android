package com.thirdandloom.storyflow.utils;

import android.graphics.Point;

public class MathUtils extends BaseUtils {

    /** Euclidean algorithm
     *
     *  @param x first number
     *  @param y second number
     *  @return greatest common divisior (GCD)
     */
    public static int calculateGreatestCommonDivisior(int x, int y) {
        while (y != 0 ) {
            int temp = x%y;
            x = y;
            y = temp;
        }
        return x;
    }

    public static float calculateMinScaleRatio(int realWidth, int realHeight, int boxWidth, int boxHeight) {
        float scaleHeight = (float) boxWidth / realWidth;
        float scaleWidth = (float) boxHeight / realHeight;
        return Math.min(scaleHeight, scaleWidth);
    }

    public static float calculateMaxScaleRatio(int realWidth, int realHeight, int boxWidth, int boxHeight) {
        float scaleHeight = (float) boxWidth / realWidth;
        float scaleWidth = (float) boxHeight / realHeight;
        return Math.max(scaleHeight, scaleWidth);
    }

    public static float calculateMaxScaleRatio(int realWidth, int realHeight, int boxWidth) {
        return calculateMaxScaleRatio(realWidth, realHeight, boxWidth, 0);
    }

    /** Calculate intersection point Y
     *
     * @param start Point(X0, Y0) *X0 - start X; *Y0 - start Y: start straight line point
     * @param end Point(X1, Y1) *X1 - end X; *Y1 - end Y: end straight line point
     *            Also consider in this way:
     *            X0 - is 2d variable end value
     *            X1 - is 2d variable start value
     *            Y0 - is 1st variable start value
     *            Y1 - is 1st variable end value
     *            For example if you have: 1st: [96..100] and 2d: [300..0]
     *              point will looks like:
     *                      start Point:(0, 96)
     *                        end Point:(300, 100)
     *
     * @param X intersection point X
     *          X must be in 2d value interval, otherwise return point Y is invalid
     * @return intersection point Y
     *              it shows how is 1st parameter is changing, when 2d parameter was changed
     */
    public static float getPointY(Point start, Point end, float X) {
        return (((X - start.x)*(end.y-start.y))/(end.x-start.x))+start.y;
    }

    /** Calculate intersection point X
     *
     * @param start Point(X0, Y0) *X0 - start X; *Y0 - start Y: start straight line point
     * @param end Point(X1, Y1) *X1 - end X; *Y1 - end Y: end straight line point
     *            Also consider in this way:
     *            X0 - is 2d variable end value
     *            X1 - is 2d variable start value
     *            Y0 - is 1st variable start value
     *            Y1 - is 1st variable end value
     *            For example if you have: 1st: [96..100] and 2d: [300..0]
     *              point will looks like:
     *                      start Point:(0, 96)
     *                        end Point:(300, 100)
     *
     * @param Y intersection point Y
     *          Y must be in 1st value interval, otherwise return point X is invalid
     * @return intersection point X
     *              it shows how is 2d parameter is changing, when 1st parameter was changed
     */
    public static float getPointX(Point start, Point end, float Y) {
        return (((Y-start.y)*(end.x-start.x))/(end.y-start.y))+start.x;
    }

    /**
     *
     * @param value1 first value
     * @param value2 second value
     * @param checkingValue value for check
     * @return return is value for check inside value1..value2
     */

    public static boolean isValuesBetween(int value1, int value2, int checkingValue) {
        if (value1 > value2) {
            return checkingValue <= value1 && checkingValue >= value2;
        } else {
            return checkingValue >= value1 && checkingValue <= value2;
        }
    }
}
