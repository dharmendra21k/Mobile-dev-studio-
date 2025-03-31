package com.mobiledev.androidstudio.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for XML and HTML code
 */
public class XmlSyntaxHighlighter extends SyntaxHighlighter {
    // Regular expressions for different XML elements
    private static final Pattern PATTERN_TAGS = 
            Pattern.compile("</?[a-zA-Z][a-zA-Z0-9:._-]*|>|/>");
    
    private static final Pattern PATTERN_ATTRIBUTES = 
            Pattern.compile("\\s+[a-zA-Z][a-zA-Z0-9:._-]*=");
    
    private static final Pattern PATTERN_STRINGS = 
            Pattern.compile("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(\\\\.[^'\\\\]*)*'");
    
    private static final Pattern PATTERN_COMMENTS = 
            Pattern.compile("<!--[\\s\\S]*?-->");
    
    private static final Pattern PATTERN_CDATA = 
            Pattern.compile("<!\\[CDATA\\[[\\s\\S]*?\\]\\]>");
    
    @Override
    public HighlightResult highlight(String text) {
        HighlightResult result = new HighlightResult();
        
        // Add spans for different XML elements
        addPatternSpans(result, text, PATTERN_COMMENTS, COLOR_COMMENT);
        addPatternSpans(result, text, PATTERN_CDATA, COLOR_COMMENT);
        addPatternSpans(result, text, PATTERN_TAGS, COLOR_TAG);
        addPatternSpans(result, text, PATTERN_ATTRIBUTES, COLOR_ATTRIBUTE);
        addPatternSpans(result, text, PATTERN_STRINGS, COLOR_STRING);
        
        return result;
    }
    
    /**
     * Add spans for all matches of a pattern
     * 
     * @param result HighlightResult to add spans to
     * @param text Text to search in
     * @param pattern Pattern to search for
     * @param color Color to apply to matches
     */
    private void addPatternSpans(HighlightResult result, String text, Pattern pattern, int color) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.addColorSpan(color, matcher.start(), matcher.end());
        }
    }
}