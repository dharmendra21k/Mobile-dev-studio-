package com.mobiledev.androidstudio.terminal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom view for displaying terminal output
 */
public class TerminalView extends View {

    private static final int MAX_LINES = 1000;
    private static final int CHAR_WIDTH = 10;
    private static final int LINE_HEIGHT = 24;
    private static final int PADDING = 8;

    private final Paint textPaint;
    private final List<String> lines;
    private TerminalSession session;
    private int scrollY;

    /**
     * Create a new TerminalView
     *
     * @param context Context
     */
    public TerminalView(Context context) {
        this(context, null);
    }

    /**
     * Create a new TerminalView
     *
     * @param context Context
     * @param attrs Attribute set
     */
    public TerminalView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Create a new TerminalView
     *
     * @param context Context
     * @param attrs Attribute set
     * @param defStyleAttr Default style attribute
     */
    public TerminalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        lines = new ArrayList<>();
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setTextSize(14 * getResources().getDisplayMetrics().density);
    }

    /**
     * Attach a terminal session to this view
     *
     * @param session Terminal session
     */
    public void attachSession(TerminalSession session) {
        this.session = session;
        lines.clear();
        lines.add("");
        invalidate();
    }

    /**
     * Add text to the terminal view
     *
     * @param text Text to add
     */
    public void addText(String text) {
        if (text == null) {
            return;
        }
        
        String[] newLines = text.split("\n", -1);
        
        if (lines.isEmpty()) {
            lines.add("");
        }
        
        // Append the first new line to the last existing line
        int lastLineIndex = lines.size() - 1;
        String lastLine = lines.get(lastLineIndex);
        lines.set(lastLineIndex, lastLine + newLines[0]);
        
        // Add the rest of the new lines
        for (int i = 1; i < newLines.length; i++) {
            lines.add(newLines[i]);
        }
        
        // Limit the number of lines
        while (lines.size() > MAX_LINES) {
            lines.remove(0);
        }
        
        invalidate();
    }

    /**
     * Get the number of visible lines
     *
     * @return Number of visible lines
     */
    private int getVisibleLines() {
        return (getHeight() - 2 * PADDING) / LINE_HEIGHT;
    }

    /**
     * Get the total number of lines
     *
     * @return Total number of lines
     */
    private int getTotalLines() {
        return lines.size();
    }

    /**
     * Scroll to a specific position
     *
     * @param y Scroll position
     */
    public void scrollTo(int y) {
        scrollY = Math.max(0, Math.min(y, getTotalLines() - getVisibleLines()));
        invalidate();
    }

    /**
     * Scroll by a specific amount
     *
     * @param dy Scroll amount
     */
    public void scrollBy(int dy) {
        scrollTo(scrollY + dy);
    }

    /**
     * Scroll to the bottom
     */
    public void scrollToBottom() {
        scrollTo(getTotalLines());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Clear the background
        canvas.drawColor(Color.BLACK);
        
        // Calculate the visible lines
        int visibleLines = getVisibleLines();
        int startLine = Math.max(0, Math.min(scrollY, getTotalLines() - visibleLines));
        int endLine = Math.min(startLine + visibleLines, getTotalLines());
        
        // Draw the text
        float y = PADDING + LINE_HEIGHT;
        for (int i = startLine; i < endLine; i++) {
            String line = lines.get(i);
            canvas.drawText(line, PADDING, y, textPaint);
            y += LINE_HEIGHT;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        
        // Ensure we can display at least one line
        int minHeight = LINE_HEIGHT + 2 * PADDING;
        int height = getMeasuredHeight();
        if (height < minHeight) {
            height = minHeight;
        }
        
        setMeasuredDimension(getMeasuredWidth(), height);
    }
}