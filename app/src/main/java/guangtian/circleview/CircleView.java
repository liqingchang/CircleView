package guangtian.circleview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by terry on 15-5-26.
 */
public class CircleView extends View {

    /**
     * 外层按钮个数
     */
    private static final int OUTER_BUTTON_COUNT = 4;

    private static final int BUTTON_INDEX_BOTTOM = 0;
    private static final int BUTTON_INDEX_LEFT = 1;
    private static final int BUTTON_INDEX_TOP = 2;
    private static final int BUTTON_INDEX_RIGHT = 3;
    private static final int BUTTON_INDEX_CENTER = 4;

    // 扇型画笔
    private Paint sectorPaint;
    // 内圆画笔
    private Paint innerCirclePaint;
    // 边框画笔
    private Paint edgePaint;
    // 点击效果画笔
    private Paint touchPaint;

    private boolean[] outerTouchs;
    private boolean innerTouch;

    private List<Bitmap> bitmap = new ArrayList<>();
    private Bitmap iconCenter;

    private int px;
    private int py;
    // 内圆半径
    private int internalCircleRadius = 96;
    // 外圆半径
    private int outerCircleRadius = internalCircleRadius * 3;
    // 每个扇型度数
    private int sweep = 360 / OUTER_BUTTON_COUNT;
    // 修正值
    private int skip = sweep / 2;

    private OnClickListener bottomClick;
    private OnClickListener leftClick;
    private OnClickListener rightClick;
    private OnClickListener topClick;
    private OnClickListener centerClick;


    public CircleView(Context context) {
        super(context);
        init(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        sectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sectorPaint.setAntiAlias(true);
        sectorPaint.setColor(0xfffafafa);
        sectorPaint.setStyle(Paint.Style.FILL);
        sectorPaint.setStrokeWidth(4);

        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint.setAntiAlias(true);
        innerCirclePaint.setColor(0xfffafafa);
        innerCirclePaint.setStyle(Paint.Style.FILL);

        edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        edgePaint.setAntiAlias(true);
        edgePaint.setColor(0xffa7a8ac);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(4);

        touchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        touchPaint.setAntiAlias(true);
        touchPaint.setColor(0xffa7a8ac);
        touchPaint.setStyle(Paint.Style.FILL);
        touchPaint.setStrokeWidth(4);

        outerTouchs = new boolean[OUTER_BUTTON_COUNT];

        bitmap.clear();
        bitmap.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_circle_minus));
        bitmap.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_circle_previous));
        bitmap.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_circle_plus));
        bitmap.add(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_circle_next));
        iconCenter = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_circle_ok);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        // 因为最终需要的是一个完整的圆形，所以取高宽的最小值让View始终是一个正方形
        int d = Math.min(measuredWidth, measuredHeight);

        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.UNSPECIFIED) {
            result = 200;
        } else {
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        px = width / 2;
        py = height;


        int start = 0;
        canvas.drawColor(0xf5f5f5); // 背景色

        // 外圆Rect
        RectF oval = new RectF();
        oval.set((width - 2 * outerCircleRadius) / 2, (height - 2 * outerCircleRadius) / 2, width - ((width - 2 * outerCircleRadius) / 2), height - ((height - 2 * outerCircleRadius) / 2));
        // 画扇型
        for (int i = 0; i < OUTER_BUTTON_COUNT; i++) {
            if (outerTouchs[i]) {
                canvas.drawArc(oval, start + skip, sweep, true, touchPaint); // 外圆
                canvas.drawArc(oval, start + skip, sweep, false, edgePaint); // 外圆边框
            } else {
                canvas.drawArc(oval, start + skip, sweep, true, sectorPaint); // 外圆
                canvas.drawArc(oval, start + skip, sweep, false, edgePaint); // 外圆边框
            }
            canvas.drawBitmap(bitmap.get(i), null, getIconRect(i, outerCircleRadius, px, py / 2, bitmap.get(i).getWidth(), bitmap.get(i).getHeight()), null);
            start = start + sweep;
        }

        RectF internalOval = new RectF();
        internalOval.set((width - 2 * internalCircleRadius) / 2, (height - 2 * internalCircleRadius) / 2, width - ((width - 2 * internalCircleRadius) / 2), height - ((height - 2 * internalCircleRadius) / 2));
        if (innerTouch) {
            canvas.drawArc(internalOval, 0, 360, false, touchPaint);
            canvas.drawArc(internalOval, 0, 360, false, edgePaint);
        } else {
            canvas.drawArc(internalOval, 0, 360, false, innerCirclePaint);
            canvas.drawArc(internalOval, 0, 360, false, edgePaint);
        }
        canvas.drawBitmap(iconCenter, null, getCenterIconRect(px, py / 2, iconCenter.getWidth(), iconCenter.getHeight()), null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int centerX = px;
        int centerY = py / 2;
        float tx = event.getX();
        float ty = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (isInSideCenterCircle(centerX, centerY, internalCircleRadius, tx, ty)) {
                    resetTouch();
                    innerTouch = true;
                    invalidate();
                    return true;
                }
                int touchArk = isInArc(outerCircleRadius, centerX, centerY, tx, ty);
                if (touchArk != -1) {
                    resetTouch();
                    outerTouchs[touchArk] = true;
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                resetTouch();
                invalidate();
                if (isInSideCenterCircle(centerX, centerY, internalCircleRadius, tx, ty) && centerClick != null) {
                    centerClick.onClick(this);
                } else {
                    touchArk = isInArc(outerCircleRadius, centerX, centerY, tx, ty);
                    switch (touchArk) {
                        case BUTTON_INDEX_BOTTOM:
                            if (bottomClick != null) {
                                bottomClick.onClick(this);
                            }
                            break;
                        case BUTTON_INDEX_LEFT:
                            if (leftClick != null) {
                                leftClick.onClick(this);
                            }
                            break;
                        case BUTTON_INDEX_RIGHT:
                            if (rightClick != null) {
                                rightClick.onClick(this);
                            }
                            break;
                        case BUTTON_INDEX_TOP:
                            if (topClick != null) {
                                topClick.onClick(this);
                            }
                            break;
                    }
                }
                return true;
        }
        return true;
    }

    private boolean isInSideCenterCircle(float centerX, float centerY, float radius, float co_ordinateX, float co_ordinateY) {
        return ((Math.pow((centerX - co_ordinateX), 2)) + (Math.pow((centerY - co_ordinateY), 2)) - Math.pow(radius, 2)) <= 0;
    }

    // 判断点击哪个扇型
    // 只适用于COUNT = 4的情况
    private int isInArc(float radius, float centerX, float centerY, float toCheckX, float toCheckY) {
        if ((Math.pow(centerX - toCheckX, 2) +
                Math.pow(centerY - toCheckY, 2) -
                Math.pow(radius, 2)) < 0) {
            double radian = Math.atan((toCheckY - centerY) / (toCheckX - centerX));
            // 角度
            double degree = Math.abs((180 * radian) / Math.PI);
            if (degree < skip) { // 只能是0、2
                if (toCheckX > centerX) {
                    return 3;
                } else {
                    return 1;
                }
            } else { // 只能是1、2
                if (toCheckY > centerY) {
                    return 0;
                } else {
                    return 2;
                }
            }
        }
        return -1;
    }

    private void resetTouch() {
        for (int i = 0; i < outerTouchs.length; i++) {
            outerTouchs[i] = false;
        }
        innerTouch = false;
    }

    private RectF getIconRect(int index, int radius, int centerX, int centerY, int iconWidth, int iconHeight) {
        int left;
        int right;
        int top;
        int bottom;
        RectF rectF = new RectF();
        if (index % 2 == 0) {
            left = centerX - iconWidth / 2;
            right = centerX + iconWidth / 2;
            if (index > 0) {
                top = centerY - radius + 30;
                bottom = top + iconHeight;
            } else {
                bottom = centerY + radius - 30;
                top = bottom - iconHeight;
            }
        } else {
            top = centerY - iconHeight / 2;
            bottom = centerY + iconHeight / 2;
            if (index > 1) {
                right = centerX + radius - 30;
                left = right - iconWidth;
            } else {
                left = centerX - radius + 30;
                right = left + iconWidth;
            }
        }
        rectF.set(left, top, right, bottom);
        return rectF;
    }

    private RectF getCenterIconRect(float centerX, float centerY, float iconWidth, float iconHeight) {
        RectF rectF = new RectF();
        rectF.set(centerX - iconWidth / 2, centerY - iconHeight / 2, centerX + iconWidth / 2, centerY + iconHeight / 2);
        return rectF;
    }

    public void setBottomClick(OnClickListener click) {
        bottomClick = click;
    }

    public void setLeftClick(OnClickListener click) {
        leftClick = click;
    }

    public void setRightClick(OnClickListener click) {
        rightClick = click;
    }

    public void setTopClick(OnClickListener click) {
        topClick = click;
    }

    public void setCenterClick(OnClickListener click) {
        centerClick = click;
    }
}
