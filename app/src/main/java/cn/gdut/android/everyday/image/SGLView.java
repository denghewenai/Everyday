/*
 *
 * SGLView.java
 * 
 * Created by Wuwang on 2016/10/15
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package cn.gdut.android.everyday.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import cn.gdut.android.everyday.image.filter.AFilter;


/**
 * Description:
 */
public class SGLView extends GLSurfaceView {

    private Bitmap currentBitmap;
    private int mBmpWidth;
    private int mBmpHeight;
    private boolean isPreview = false;

    private SGLRender render;

    public SGLView(Context context) {
        this(context, null);
    }

    public SGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        render = new SGLRender(this);

        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

//        try {
//            render.setImage(BitmapFactory.decodeStream(getResources().getAssets().open("texture/fengj.png")));
//            requestRender();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        currentBitmap = bitmap;
        mBmpWidth = bitmap.getWidth();
        mBmpHeight = bitmap.getHeight();
        render.setImage(bitmap);
        requestRender();
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public SGLRender getRender() {
        return render;
    }

    public void setFilter(AFilter filter) {
        render.setFilter(filter);
    }

}
