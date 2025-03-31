package com.mobiledev.androidstudio.syntax;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for XML code
 */
public class XmlSyntaxHighlighter implements SyntaxHighlighter {
    
    // Colors
    private static final int TAG_COLOR = Color.rgb(86, 156, 214);        // Blue
    private static final int ATTRIBUTE_NAME_COLOR = Color.rgb(156, 220, 254);  // Light blue
    private static final int ATTRIBUTE_VALUE_COLOR = Color.rgb(214, 157, 133); // Brown
    private static final int COMMENT_COLOR = Color.rgb(87, 166, 74);     // Green
    private static final int CDATA_COLOR = Color.rgb(200, 200, 200);     // Light gray
    private static final int ENTITY_COLOR = Color.rgb(215, 186, 125);    // Gold
    
    // Patterns
    private static final Pattern TAG_PATTERN = Pattern.compile("</?\\w+(?:\\s+[^>]*)?>");
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile("</?([\\w:-]+)");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\s+([\\w:-]+)\\s*=\\s*[\"']([^\"']*)[\"']");
    private static final Pattern ATTRIBUTE_NAME_PATTERN = Pattern.compile("([\\w:-]+)\\s*=");
    private static final Pattern ATTRIBUTE_VALUE_PATTERN = Pattern.compile("[\"']([^\"']*)[\"']");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    private static final Pattern CDATA_PATTERN = Pattern.compile("<!\\[CDATA\\[.*?\\]\\]>", Pattern.DOTALL);
    private static final Pattern ENTITY_PATTERN = Pattern.compile("&[\\w#]+;");
    
    @Override
    public void highlight(Editable editable) {
        String text = editable.toString();
        
        // Apply comment highlighting first
        highlightPattern(editable, COMMENT_PATTERN, COMMENT_COLOR);
        highlightPattern(editable, CDATA_PATTERN, CDATA_COLOR);
        
        // Highlight entities
        highlightPattern(editable, ENTITY_PATTERN, ENTITY_COLOR);
        
        // Highlight tags and tag names
        highlightTags(editable);
        
        // Highlight attributes in tags
        highlightAttributes(editable);
    }
    
    /**
     * Apply highlighting to text matching a pattern
     * @param editable The text to highlight
     * @param pattern The pattern to match
     * @param color The color to apply
     */
    private void highlightPattern(Editable editable, Pattern pattern, int color) {
        Matcher matcher = pattern.matcher(editable);
        
        while (matcher.find()) {
            // Skip if this span overlaps with an existing comment span
            if (hasOverlappingSpan(editable, matcher.start(), matcher.end(), COMMENT_COLOR, CDATA_COLOR)) {
                continue;
            }
            
            editable.setSpan(
                    new ForegroundColorSpan(color),
                    matcher.start(),
                    matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
    }
    
    /**
     * Highlight XML tags and tag names
     * @param editable The text to highlight
     */
    private void highlightTags(Editable editable) {
        Matcher tagMatcher = TAG_PATTERN.matcher(editable);
        
        while (tagMatcher.find()) {
            // Skip if this span overlaps with comment or CDATA
            if (hasOverlappingSpan(editable, tagMatcher.start(), tagMatcher.end(), COMMENT_COLOR, CDATA_COLOR)) {
                continue;
            }
            
            String tag = tagMatcher.group();
            
            // Highlight tag name
            Matcher tagNameMatcher = TAG_NAME_PATTERN.matcher(tag);
            if (tagNameMatcher.find()) {
                int tagNameStart = tagMatcher.start() + tagNameMatcher.start();
                int tagNameEnd = tagMatcher.start() + tagNameMatcher.end();
                
                editable.setSpan(
                        new ForegroundColorSpan(TAG_COLOR),
                        tagNameStart,
                        tagNameEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
    }
    
    /**
     * Highlight XML attributes and values
     * @param editable The text to highlight
     */
    private void highlightAttributes(Editable editable) {
        Matcher tagMatcher = TAG_PATTERN.matcher(editable);
        
        while (tagMatcher.find()) {
            // Skip if this span overlaps with comment or CDATA
            if (hasOverlappingSpan(editable, tagMatcher.start(), tagMatcher.end(), COMMENT_COLOR, CDATA_COLOR)) {
                continue;
            }
            
            String tag = tagMatcher.group();
            
            // Highlight attribute names
            Matcher attrNameMatcher = ATTRIBUTE_NAME_PATTERN.matcher(tag);
            while (attrNameMatcher.find()) {
                int attrNameStart = tagMatcher.start() + attrNameMatcher.start();
                int attrNameEnd = tagMatcher.start() + attrNameMatcher.end() - 1; // -1 to exclude '='
                
                editable.setSpan(
                        new ForegroundColorSpan(ATTRIBUTE_NAME_COLOR),
                        attrNameStart,
                        attrNameEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            
            // Highlight attribute values
            Matcher attrValueMatcher = ATTRIBUTE_VALUE_PATTERN.matcher(tag);
            while (attrValueMatcher.find()) {
                int attrValueStart = tagMatcher.start() + attrValueMatcher.start();
                int attrValueEnd = tagMatcher.start() + attrValueMatcher.end();
                
                editable.setSpan(
                        new ForegroundColorSpan(ATTRIBUTE_VALUE_COLOR),
                        attrValueStart,
                        attrValueEnd,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
    }
    
    /**
     * Check if a span would overlap with an existing span of the specified color
     * @param editable The text
     * @param start The start position
     * @param end The end position
     * @param colors The colors to check for overlap
     * @return True if an overlap would occur
     */
    private boolean hasOverlappingSpan(Editable editable, int start, int end, int... colors) {
        ForegroundColorSpan[] spans = editable.getSpans(start, end, ForegroundColorSpan.class);
        
        for (ForegroundColorSpan span : spans) {
            int spanColor = span.getForegroundColor();
            for (int color : colors) {
                if (spanColor == color) {
                    return true;
                }
            }
        }
        
        return false;
    }
}