package social.entourage.android.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import social.entourage.android.R;

/**
 * Created by Mihai Ionescu on 02/05/2018.
 */
public class HalfCircleView extends View {

    Paint paint;

    public HalfCircleView(final Context context) {
        super(context);
        init(null, 0);
    }

    public HalfCircleView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HalfCircleView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.HalfCircleView, defStyle, 0);

        paint.setColor(a.getColor(R.styleable.HalfCircleView_shapeColor, ContextCompat.getColor(getContext(), R.color.background_accent_dark_translucent)));

        a.recycle();
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        canvas.translate(0, -getHeight());
        canvas.drawArc(new RectF(0, 0, getWidth(), getHeight()*2), 0, 180, false, paint);

    }

}
