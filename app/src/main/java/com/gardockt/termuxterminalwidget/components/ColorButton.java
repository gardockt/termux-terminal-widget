package com.gardockt.termuxterminalwidget.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;

import com.gardockt.termuxterminalwidget.R;

import org.jetbrains.annotations.NotNull;

public class ColorButton extends AppCompatButton {

    private static final String XMLNS_ANDROID = "http://schemas.android.com/apk/res/android";

    private final Rect size = new Rect();
    private final Paint fillPaint = new Paint();
    private final Paint borderPaint = new Paint();

    public ColorButton(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public ColorButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        Drawable alphaCheckerboard = AppCompatResources.getDrawable(context, R.drawable.alpha_checkerboard);
        setBackground(new TileDrawable(alphaCheckerboard, Shader.TileMode.REPEAT));

        int fillPaintColor = 0x00000000;

        if (attrs != null) {
            String colorString = attrs.getAttributeValue(XMLNS_ANDROID, "color");
            if (colorString != null) {
                fillPaintColor = getColorFromAttributeValue(context, colorString);
            }
        }

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillPaintColor);

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(0xFF000000);
        borderPaint.setStrokeWidth(1);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        size.set(0, 0, right - left, bottom - top);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(size, fillPaint);
        canvas.drawRect(size, borderPaint);
    }

    public void setColor(int color) {
        fillPaint.setColor(color);
        invalidate();
    }

    public int getColor() {
        return fillPaint.getColor();
    }

    private int getColorFromAttributeValue(@NonNull Context context, @NotNull String value) {
        if (value.charAt(0) == '#') {
            return Color.parseColor(value);
        } else if (value.charAt(0) == '@') {
            int resId = Integer.parseInt(value.substring(1));
            return context.getColor(resId);
        } else {
            throw new RuntimeException("Unsupported color format");
        }
    }

    // https://stackoverflow.com/a/53795718
    private static class TileDrawable extends Drawable {

        private final Paint paint;

        public TileDrawable(Drawable drawable, Shader.TileMode tileMode) {
            paint = new Paint();
            paint.setShader(new BitmapShader(getBitmap(drawable), tileMode, tileMode));
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawPaint(paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        private Bitmap getBitmap(Drawable drawable) {
            if (drawable instanceof BitmapDrawable)
                return ((BitmapDrawable) drawable).getBitmap();
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }
}