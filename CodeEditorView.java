package com.mobiledev.androidstudio.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.appcompat.widget.AppCompatEditText;

import com.mobiledev.androidstudio.syntax.SyntaxHighlighter;
import com.mobiledev.androidstudio.syntax.JavaSyntaxHighlighter;
import com.mobiledev.androidstudio.syntax.XmlSyntaxHighlighter;

/**
 * Custom view for code editing with syntax highlighting and line numbers
 */
public class CodeEditorView extends AppCompatEditText {
    
    private static final String TAG = "CodeEditorView";
    
    // UI constants
    private static final int LINE_NUMBER_MARGIN = 50;
    private static final int LINE_NUMBER_PADDING = 10;
    private static final int TAB_SIZE = 4;
    private static final String TAB_REPLACEMENT = "    ";
    
    // Line number paint
    private final Paint mLineNumberPaint;
    private final Paint mLineNumberBackgroundPaint;
    private final Paint mDividerPaint;
    
    // Text properties
    private final Rect mTextBounds = new Rect();
    private int mMaxLineNumberWidth;
    
    // Syntax highlighting
    private SyntaxHighlighter mSyntaxHighlighter;
    private String mFileExtension = "";
    
    // Auto indentation
    private int mIndent = 0;
    private boolean mIsHighlighting = false;
    
    public CodeEditorView(Context context) {
        this(context, null);
    }
    
    public CodeEditorView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }
    
    public CodeEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        // Set up line number paint
        mLineNumberPaint = new Paint();
        mLineNumberPaint.setTextSize(getTextSize() * 0.8f);
        mLineNumberPaint.setColor(Color.GRAY);
        mLineNumberPaint.setAntiAlias(true);
        mLineNumberPaint.setTextAlign(Paint.Align.RIGHT);
        
        // Line number background
        mLineNumberBackgroundPaint = new Paint();
        mLineNumberBackgroundPaint.setColor(Color.rgb(50, 50, 50));
        
        // Divider paint
        mDividerPaint = new Paint();
        mDividerPaint.setColor(Color.rgb(80, 80, 80));
        mDividerPaint.setStrokeWidth(1);
        
        initialize();
    }
    
    /**
     * Initialize the editor
     */
    private void initialize() {
        // Set editor appearance
        setBackgroundColor(Color.rgb(30, 30, 30));
        setTextColor(Color.WHITE);
        setTypeface(Typeface.MONOSPACE);
        setGravity(Gravity.TOP | Gravity.START);
        setPadding(LINE_NUMBER_MARGIN + LINE_NUMBER_PADDING, 0, 0, 0);
        setHorizontallyScrolling(true);
        
        // Set input properties
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        
        // Set default syntax highlighter
        setSyntaxHighlighter("java");
        
        // Add text watcher for syntax highlighting
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!mIsHighlighting) {
                    highlightSyntax();
                }
                
                // Calculate line numbers width
                calculateLineNumberWidth();
                
                // Auto-indentation
                handleAutoIndent(s);
            }
        });
        
        // Handle key events
        setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_TAB && event.getAction() == KeyEvent.ACTION_DOWN) {
                // Handle tab character with spaces
                int start = getSelectionStart();
                int end = getSelectionEnd();
                
                Editable editable = getText();
                if (start >= 0 && end >= 0) {
                    // Insert tab spaces
                    editable.replace(start, end, TAB_REPLACEMENT);
                }
                
                return true;
            }
            return false;
        });
    }
    
    /**
     * Set the syntax highlighter based on file extension
     * @param fileExtension The file extension (e.g. "java", "xml")
     */
    public void setSyntaxHighlighter(String fileExtension) {
        mFileExtension = fileExtension.toLowerCase();
        
        switch (mFileExtension) {
            case "java":
                mSyntaxHighlighter = new JavaSyntaxHighlighter();
                break;
            case "xml":
                mSyntaxHighlighter = new XmlSyntaxHighlighter();
                break;
            default:
                // Default to Java syntax highlighter
                mSyntaxHighlighter = new JavaSyntaxHighlighter();
                break;
        }
        
        // Apply initial highlighting
        highlightSyntax();
    }
    
    /**
     * Apply syntax highlighting to the text
     */
    private void highlightSyntax() {
        if (mSyntaxHighlighter == null || mIsHighlighting) {
            return;
        }
        
        mIsHighlighting = true;
        
        try {
            Editable editable = getText();
            
            // Remove existing spans
            ForegroundColorSpan[] spans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
            for (ForegroundColorSpan span : spans) {
                editable.removeSpan(span);
            }
            
            // Apply new highlighting
            mSyntaxHighlighter.highlight(editable);
        } catch (Exception e) {
            Log.e(TAG, "Error during syntax highlighting", e);
        } finally {
            mIsHighlighting = false;
        }
    }
    
    /**
     * Calculate the width needed for line numbers
     */
    private void calculateLineNumberWidth() {
        int lineCount = getLineCount();
        String lastLineNumber = String.valueOf(lineCount);
        
        mLineNumberPaint.getTextBounds(lastLineNumber, 0, lastLineNumber.length(), mTextBounds);
        mMaxLineNumberWidth = mTextBounds.width();
    }
    
    /**
     * Handle auto-indentation
     * @param editable The editable text
     */
    private void handleAutoIndent(Editable editable) {
        // Auto-indentation logic here
        // This would detect braces and adjust indentation level
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        // Draw line number background
        canvas.drawRect(0, 0, LINE_NUMBER_MARGIN, getHeight(), mLineNumberBackgroundPaint);
        
        // Draw divider
        canvas.drawLine(LINE_NUMBER_MARGIN, 0, LINE_NUMBER_MARGIN, getHeight(), mDividerPaint);
        
        // Draw line numbers
        int lineCount = getLineCount();
        for (int i = 0; i < lineCount; i++) {
            int baseline = getLineBounds(i, null);
            canvas.drawText(String.valueOf(i + 1), LINE_NUMBER_MARGIN - LINE_NUMBER_PADDING, baseline, mLineNumberPaint);
        }
        
        // Draw text
        super.onDraw(canvas);
    }
    
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection connection = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        return connection;
    }
}