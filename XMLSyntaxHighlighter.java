package com.mobiledev.androidstudio.syntax;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.mobiledev.androidstudio.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syntax highlighter for XML/HTML code
 */
public class XMLSyntaxHighlighter implements SyntaxHighlighter {

    // Colors
    private final int mTagColor;
    private final int mAttrNameColor;
    private final int mAttrValueColor;
    private final int mCommentColor;
    private final int mEntityColor;
    
    // Patterns
    private static final Pattern TAG_PATTERN = 
            Pattern.compile("</?\\w+(?:\\s+[\\w:]+(?:\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)*\\s*/?>|</\\w+>");
    
    private static final Pattern TAG_NAME_PATTERN = 
            Pattern.compile("</?([\\w:]+)");
    
    private static final Pattern ATTR_NAME_PATTERN = 
            Pattern.compile("\\s([\\w:]+)\\s*=\\s*");
    
    private static final Pattern ATTR_VALUE_PATTERN = 
            Pattern.compile("=\\s*([\"'])([^\"']*)\\1");
    
    private static final Pattern COMMENT_PATTERN = 
            Pattern.compile("<!--.*?-->", Pattern.DOTALL);
    
    private static final Pattern ENTITY_PATTERN = 
            Pattern.compile("&[\\w#]+;");
    
    public XMLSyntaxHighlighter(Context context) {
        // Initialize colors from resources
        mTagColor = ContextCompat.getColor(context, R.color.syntax_tag);
        mAttrNameColor = ContextCompat.getColor(context, R.color.syntax_attr_name);
        mAttrValueColor = ContextCompat.getColor(context, R.color.syntax_attr_value);
        mCommentColor = ContextCompat.getColor(context, R.color.syntax_comment);
        mEntityColor = ContextCompat.getColor(context, R.color.syntax_entity);
    }
    
    @Override
    public void highlight(SpannableStringBuilder text) {
        // Reset spans by removing all ForegroundColorSpan instances
        ForegroundColorSpan[] spans = text.getSpans(0, text.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
            text.removeSpan(span);
        }
        
        // Apply highlighting in specific order
        highlightComments(text);
        highlightTags(text);
        highlightEntities(text);
    }
    
    /**
     * Highlight XML comments
     */
    private void highlightComments(SpannableStringBuilder text) {
        Matcher matcher = COMMENT_PATTERN.matcher(text);
        while (matcher.find()) {
            text.setSpan(new ForegroundColorSpan(mCommentColor), 
                    matcher.start(), 
                    matcher.end(), 
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    /**
     * Highlight XML tags, attributes and values
     */
    private void highlightTags(SpannableStringBuilder text) {
        String content = text.toString();
        Matcher tagMatcher = TAG_PATTERN.matcher(content);
        
        while (tagMatcher.find()) {
            int tagStart = tagMatcher.start();
            int tagEnd = tagMatcher.end();
            
            // Get the tag's content
            String tag = content.substring(tagStart, tagEnd);
            
            // Highlight tag name
            Matcher nameMatch = TAG_NAME_PATTERN.matcher(tag);
            if (nameMatch.find()) {
                // Apply span to the tag name in the original text
                text.setSpan(new ForegroundColorSpan(mTagColor),
                        tagStart + nameMatch.start(1),
                        tagStart + nameMatch.end(1),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // Highlight attribute names
            Matcher attrNameMatch = ATTR_NAME_PATTERN.matcher(tag);
            while (attrNameMatch.find()) {
                // Apply span to the attribute name in the original text
                text.setSpan(new ForegroundColorSpan(mAttrNameColor),
                        tagStart + attrNameMatch.start(1),
                        tagStart + attrNameMatch.end(1),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // Highlight attribute values
            Matcher attrValueMatch = ATTR_VALUE_PATTERN.matcher(tag);
            while (attrValueMatch.find()) {
                // Apply span to the attribute value in the original text (group 2 is the value without quotes)
                text.setSpan(new ForegroundColorSpan(mAttrValueColor),
                        tagStart + attrValueMatch.start(2),
                        tagStart + attrValueMatch.end(2),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            // Apply color to < and > and </
            text.setSpan(new ForegroundColorSpan(mTagColor),
                    tagStart,
                    tagStart + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            if (tag.startsWith("</")) {
                text.setSpan(new ForegroundColorSpan(mTagColor),
                        tagStart,
                        tagStart + 2,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            
            text.setSpan(new ForegroundColorSpan(mTagColor),
                    tagEnd - 1,
                    tagEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    /**
     * Highlight XML entities like &amp; &lt; etc.
     */
    private void highlightEntities(SpannableStringBuilder text) {
        Matcher matcher = ENTITY_PATTERN.matcher(text);
        while (matcher.find()) {
            text.setSpan(new ForegroundColorSpan(mEntityColor), 
                    matcher.start(), 
                    matcher.end(), 
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}