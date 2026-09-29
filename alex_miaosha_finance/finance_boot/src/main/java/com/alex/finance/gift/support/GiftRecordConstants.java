package com.alex.finance.gift.support;

public final class GiftRecordConstants {

    public static final String DIRECTION_GIVE = "GIVE";
    public static final String DIRECTION_RECEIVE = "RECEIVE";
    public static final String DIRECTION_RETURN = "RETURN";

    private GiftRecordConstants() {
    }

    public static boolean isValidDirection(String direction) {
        return DIRECTION_GIVE.equals(direction)
                || DIRECTION_RECEIVE.equals(direction)
                || DIRECTION_RETURN.equals(direction);
    }

    public static String normalizeDirection(String direction) {
        return isValidDirection(direction) ? direction : null;
    }
}
