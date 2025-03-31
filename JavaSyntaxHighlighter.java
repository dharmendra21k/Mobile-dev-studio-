package com.mobiledev.androidstudio.syntax;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for Java code
 */
public class JavaSyntaxHighlighter implements SyntaxHighlighter {
    
    // Colors
    private static final int KEYWORD_COLOR = Color.rgb(86, 156, 214);     // Blue
    private static final int TYPE_COLOR = Color.rgb(78, 201, 176);        // Teal
    private static final int STRING_COLOR = Color.rgb(214, 157, 133);     // Brown
    private static final int COMMENT_COLOR = Color.rgb(87, 166, 74);      // Green
    private static final int NUMBER_COLOR = Color.rgb(181, 206, 168);     // Light green
    private static final int ANNOTATION_COLOR = Color.rgb(220, 220, 170); // Yellow
    
    // Java keywords
    private static final List<String> KEYWORDS = Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
            "true", "false", "null"
    );
    
    // Java built-in types
    private static final List<String> TYPES = Arrays.asList(
            "String", "Object", "Integer", "Boolean", "Character", "Float", "Double", "Long", "Short",
            "Byte", "StringBuilder", "List", "ArrayList", "Map", "HashMap", "Set", "HashSet",
            "Collection", "Iterator", "Thread"
    );
    
    // Patterns
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");
    private static final Pattern TYPE_PATTERN = Pattern.compile("\\b(" + String.join("|", TYPES) + ")\\b");
    private static final Pattern STRING_PATTERN = Pattern.compile("\".*?\"");
    private static final Pattern CHAR_PATTERN = Pattern.compile("'.'");
    private static final Pattern SINGLE_LINE_COMMENT_PATTERN = Pattern.compile("//.*");
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?([fFL])?\\b");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("@\\w+");
    
    @Override
    public void highlight(Editable editable) {
        String text = editable.toString();
        
        // Apply comment highlighting first (to avoid highlighting keywords within comments)
        highlightPattern(editable, SINGLE_LINE_COMMENT_PATTERN, COMMENT_COLOR);
        highlightPattern(editable, MULTI_LINE_COMMENT_PATTERN, COMMENT_COLOR);
        
        // Apply string highlighting (to avoid highlighting keywords within strings)
        highlightPattern(editable, STRING_PATTERN, STRING_COLOR);
        highlightPattern(editable, CHAR_PATTERN, STRING_COLOR);
        
        // Apply keyword and other highlighting
        highlightPattern(editable, KEYWORD_PATTERN, KEYWORD_COLOR);
        highlightPattern(editable, TYPE_PATTERN, TYPE_COLOR);
        highlightPattern(editable, NUMBER_PATTERN, NUMBER_COLOR);
        highlightPattern(editable, ANNOTATION_PATTERN, ANNOTATION_COLOR);
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
            // Skip if this span overlaps with an existing comment or string span
            if (hasOverlappingSpan(editable, matcher.start(), matcher.end())) {
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
     * Check if a span would overlap with an existing comment or string span
     * @param editable The text
     * @param start The start position
     * @param end The end position
     * @return True if an overlap would occur
     */
    private boolean hasOverlappingSpan(Editable editable, int start, int end) {
        ForegroundColorSpan[] spans = editable.getSpans(start, end, ForegroundColorSpan.class);
        
        for (ForegroundColorSpan span : spans) {
            int spanColor = span.getForegroundColor();
            if (spanColor == COMMENT_COLOR || spanColor == STRING_COLOR) {
                return true;
            }
        }
        
        return false;
    }
}