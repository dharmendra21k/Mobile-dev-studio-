package com.mobiledev.androidstudio.terminal;

/**
 * Custom Motion Event class for terminal
 */
public class MotionEvent {
    private float x;
    private float y;
    
    public MotionEvent(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public long getEventTime() {
        return System.currentTimeMillis();
    }
}