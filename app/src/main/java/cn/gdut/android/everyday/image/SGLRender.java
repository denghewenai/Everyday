/*
 *
 * SGLRender.java
 * 
 * Created by Wuwang on 2016/10/15
 */
package cn.gdut.android.everyday.image;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.View;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import cn.gdut.android.everyday.image.filter.AFilter;
import cn.gdut.android.everyday.image.filter.ColorFilter;
import cn.gdut.android.everyday.image.filter.ContrastColorFilter;


/**
 * Description:
 */
public class SGLRender implements GLSurfaceView.Renderer {

    private AFilter mFilter;
    private Bitmap bitmap;
    private int width, height;
    private boolean refreshFlag = false;
    private boolean isPreview = false;
    private EGLConfig config;

    private ByteBuffer mBuffer;

    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[2];

    public SGLRender(View mView) {
        mFilter = new ContrastColorFilter(mView.getContext(), ColorFilter.Filter.NONE);
    }

    public void setFilter(AFilter filter) {
        refreshFlag = true;
        mFilter = filter;
        if (bitmap != null) {
            mFilter.setBitmap(bitmap);
        }
    }

    public void setImageBuffer(int[] buffer, int width, int height) {
        bitmap = Bitmap.createBitmap(buffer, width, height, Bitmap.Config.RGB_565);
        mFilter.setBitmap(bitmap);
    }

    public void refresh() {
        refreshFlag = true;
    }

    public AFilter getFilter() {
        return mFilter;
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
        mFilter.setBitmap(bitmap);
    }

    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.config = config;
        mFilter.create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        mFilter.onSizeChanged(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(refreshFlag && width!=0 && height!=0){
            int[] texture = createTexture();
            mFilter.setTextureId(texture[0]);

            refreshFlag = false;
            mFilter.create();
            mFilter.onSizeChanged(width,height);
            mFilter.draw();
        }
    }

    private int[] createTexture() {
        int[] texture = new int[1];
        if (bitmap != null && !bitmap.isRecycled()) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            return texture;
        }
        return texture;
    }
}
