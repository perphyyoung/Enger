package edu.perphy.enger.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;

/**
 * 加强版的EditText,可以响应DrawableLeft 和 DrawableRight的点击事件
 * 要实现响应点击,先设置setDrawableListener
 *
 * @author xing
 * @version 1.1
 */
public class XAutoCompleteTextView extends MaterialAutoCompleteTextView {

    private DrawableLeftListener mLeftListener;

    final int DRAWABLE_LEFT = 0;

    public XAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public XAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XAutoCompleteTextView(Context context) {
        super(context);
    }

    public void setDrawableLeftListener(DrawableLeftListener listener) {
        this.mLeftListener = listener;
    }

    public interface DrawableLeftListener {
        void onDrawableLeftClick(View view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mLeftListener != null) {
                    Drawable drawableLeft = getCompoundDrawables()[DRAWABLE_LEFT];
                    if (drawableLeft != null && event.getRawX() <= (getLeft() + drawableLeft.getBounds().width())) {
                        mLeftListener.onDrawableLeftClick(this);
                        return true;//返回false避免点击时EditText会获取焦点的问题
                    }
                }
                break;
        }

        return super.onTouchEvent(event);
    }
}
