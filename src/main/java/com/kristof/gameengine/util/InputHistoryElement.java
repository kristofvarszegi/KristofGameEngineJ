package com.kristof.gameengine.util;

public class InputHistoryElement {
    public static final String KEY_FWD = "key_fwd";
    public static final String KEY_BP = "key_bp";
    public static final String KEY_LEFT = "key_left";
    public static final String KEY_RIGHT = "key_right";
    public static final String KEY_SPACE = "key_space";
    public static final String MOUSE_LEFT = "mouse_left";
    public static final String MOUSE_RIGHT = "mouse_right";
    public static final String START = "start";
    
    private final long timeStamp;
    private final String eventId;

    public InputHistoryElement(String eventId, long timeStamp) {
        this.eventId = eventId;
        this.timeStamp = timeStamp;
    }

    public String getEventId() {
        return eventId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
