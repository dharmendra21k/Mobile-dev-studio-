package com.mobiledev.androidstudio.syntax;

import android.text.Editable;

/**
 * Interface for syntax highlighting implementations
 */
public interface SyntaxHighlighter {
    
    /**
     * Apply syntax highlighting to the text
     * @param editable The text to highlight
     */
    void highlight(Editable editable);
}