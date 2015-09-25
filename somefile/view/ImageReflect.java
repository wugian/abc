package com.xgimi.gimicinema.view;

import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.view.View;

public class ImageReflect {
    private static int reflectImageHeight = 100;

    public static Bitmap convertViewToBitmap(View paramView) {
        paramView.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
        paramView.layout(0, 0, paramView.getMeasuredWidth(), paramView.getMeasuredHeight());
        paramView.buildDrawingCache();
        return paramView.getDrawingCache();
    }

    public static Bitmap createCutReflectedImage(Bitmap paramBitmap, int paramInt) {
        int i = paramBitmap.getWidth();
        int j = paramBitmap.getHeight();
        Bitmap localBitmap2 = null;
        if (j <= paramInt + reflectImageHeight) {
            localBitmap2 = null;
        } else {
            Matrix localMatrix = new Matrix();
            localMatrix.preScale(1.0F, -1.0F);
            // System.out.println(j - reflectImageHeight -
            // paramInt);
            Bitmap localBitmap1 = Bitmap.createBitmap(paramBitmap, 0, j - reflectImageHeight - paramInt, i, reflectImageHeight, localMatrix, true);
            localBitmap2 = Bitmap.createBitmap(i, reflectImageHeight, Bitmap.Config.ARGB_8888);
            Canvas localCanvas = new Canvas(localBitmap2);
            localCanvas.drawBitmap(localBitmap1, 0.0F, 0.0F, null);
            LinearGradient localLinearGradient = new LinearGradient(0.0F, 0.0F, 0.0F, localBitmap2.getHeight(), -2130706433, 16777215, TileMode.CLAMP);
            Paint localPaint = new Paint();
            localPaint.setShader(localLinearGradient);
            localPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            localCanvas.drawRect(0.0F, 0.0F, i, localBitmap2.getHeight(), localPaint);
            if (!localBitmap1.isRecycled())
                localBitmap1.recycle();
            System.gc();
        }
        return localBitmap2;
    }

    public static Bitmap createReflectedImage(Bitmap paramBitmap, int paramInt) {
        int i = paramBitmap.getWidth();
        int j = paramBitmap.getHeight();
        Bitmap localBitmap2;
        if (j <= paramInt) {
            localBitmap2 = null;
        } else {
            Matrix localMatrix = new Matrix();
            localMatrix.preScale(1.0F, -1.0F);
            Bitmap localBitmap1 = Bitmap.createBitmap(paramBitmap, 0, j - paramInt, i, paramInt, localMatrix, true);
            localBitmap2 = Bitmap.createBitmap(i, paramInt, Bitmap.Config.ARGB_8888);
            Canvas localCanvas = new Canvas(localBitmap2);
            localCanvas.drawBitmap(localBitmap1, 0.0F, 0.0F, null);
            LinearGradient localLinearGradient = new LinearGradient(0.0F, 0.0F, 0.0F, localBitmap2.getHeight(), -2130706433, 16777215, TileMode.CLAMP);
            Paint localPaint = new Paint();
            localPaint.setShader(localLinearGradient);
            localPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            localCanvas.drawRect(0.0F, 0.0F, i, localBitmap2.getHeight(), localPaint);
        }
        return localBitmap2;
    }

    /**
     * use to lessen pic 50%
     *
     * @param path sd card path
     * @return bitmap
     */
    public final static Bitmap lessenUriImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); //此时返回 bm 为空
        options.inJustDecodeBounds = false; //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = (int) (options.outHeight / (float) 320);
        if (be <= 0)
            be = 1;
        options.inSampleSize = be; //重新读入图片，注意此时已经把 options.inJustDecodeBounds 设回 false 了
        bitmap = BitmapFactory.decodeFile(path, options);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        System.out.println(w + " " + h); //after zoom
        return bitmap;
    }
}