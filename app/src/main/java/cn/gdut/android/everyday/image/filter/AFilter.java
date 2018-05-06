
package cn.gdut.android.everyday.image.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import cn.gdut.android.everyday.utils.MatrixUtils;
import cn.gdut.android.everyday.utils.ShaderUtils;


/**
 * Description:
 */
public abstract class AFilter {

    /**
     * 单位矩阵
     */
    public static final float[] OM= MatrixUtils.getOriginalMatrix();

    private Context mContext;
    private int mProgram;
    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int glHMatrix;
    private int hIsHalf;
    private int glHUxy;
    private Bitmap mBitmap;

    /**
     * 顶点坐标句柄
     */
    protected int mHPosition;
    /**
     * 纹理坐标句柄
     */
    protected int mHCoord;
    /**
     * 总变换矩阵句柄
     */
    protected int mHMatrix;
    /**
     * 默认纹理贴图句柄
     */
    protected int mHTexture;

    private FloatBuffer bPos;
    private FloatBuffer bCoord;

    private float[] matrix= Arrays.copyOf(OM,16);
    private int textureType=0;      //默认使用Texture2D0
    private int textureId;
    private boolean isHalf;

    private float uXY;

    private String vertex;
    private String fragment;
    private float[] mViewMatrix=new float[16];
    private float[] mProjectMatrix=new float[16];
    private float[] mMVPMatrix=new float[16];

    private final float[] sPos={
            -1.0f,1.0f,
            -1.0f,-1.0f,
            1.0f,1.0f,
            1.0f,-1.0f
    };

    private final float[] sCoord={
            0.0f,0.0f,
            0.0f,1.0f,
            1.0f,0.0f,
            1.0f,1.0f,
    };

    public AFilter(Context context, String vertex, String fragment){
        this.mContext=context;
        this.vertex=vertex;
        this.fragment=fragment;
        ByteBuffer bb= ByteBuffer.allocateDirect(sPos.length*4);
        bb.order(ByteOrder.nativeOrder());
        bPos=bb.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);
        ByteBuffer cc= ByteBuffer.allocateDirect(sCoord.length*4);
        cc.order(ByteOrder.nativeOrder());
        bCoord=cc.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);
    }

    public final void create(){
        onCreate();
    }

    public final void setSize(int width,int height){
        onSizeChanged(width,height);
    }

    public void setHalf(boolean half){
        this.isHalf=half;
    }

    public void setImageBuffer(int[] buffer,int width,int height){
        mBitmap= Bitmap.createBitmap(buffer,width,height, Bitmap.Config.RGB_565);
    }

    public final int getTextureId(){
        return textureId;
    }

    public final void setTextureId(int textureId){
        this.textureId=textureId;
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap=bitmap;
    }

    public void draw(){
        onClear();
        onUseProgram();
        onSetExpandData();
        onBindTexture();
        onDraw();
    }

    /**
     * 清除画布
     */
    protected void onClear(){
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    protected void onUseProgram(){
        GLES20.glUseProgram(mProgram);
    }

    /**
     * 设置其他扩展数据
     */
    protected void onSetExpandData(){
        GLES20.glUniformMatrix4fv(mHMatrix,1,false,matrix,0);
    }

    /**
     * 绑定默认纹理
     */
    protected void onBindTexture(){
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,getTextureId());
        GLES20.glUniform1i(mHTexture,textureType);
    }

    /**
     * 实现此方法，完成程序的创建，可直接调用createProgram来实现
     */
    protected abstract void onCreate();
    public void onSizeChanged(int width,int height) {
        GLES20.glViewport(0,0,width,height);

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float sWH=w/(float)h;
        float sWidthHeight=width/(float)height;
        uXY=sWidthHeight;
        if(width>height){
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight*sWH,sWidthHeight*sWH, -1,1, 3, 5);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight/sWH,sWidthHeight/sWH, -1,1, 3, 5);
            }
        }else{
            if(sWH>sWidthHeight){
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1/sWidthHeight*sWH, 1/sWidthHeight*sWH,3, 5);
            }else{
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH/sWidthHeight, sWH/sWidthHeight,3, 5);
            }
        }
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    protected final void createProgram(String vertex,String fragment){
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        mProgram= ShaderUtils.createProgram(mContext.getResources(),vertex,fragment);
        glHPosition= GLES20.glGetAttribLocation(mProgram,"vPosition");
        glHCoordinate= GLES20.glGetAttribLocation(mProgram,"vCoordinate");
        glHTexture= GLES20.glGetUniformLocation(mProgram,"vTexture");
        glHMatrix= GLES20.glGetUniformLocation(mProgram,"vMatrix");
        hIsHalf= GLES20.glGetUniformLocation(mProgram,"vIsHalf");
        glHUxy= GLES20.glGetUniformLocation(mProgram,"uXY");
        onDrawCreatedSet(mProgram);
    }

    protected final void createProgramByAssetsFile(String vertex,String fragment){
        createProgram(vertex,fragment);
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected void onDraw(){
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        onDrawSet();
        GLES20.glUniform1i(hIsHalf,0);
        GLES20.glUniform1f(glHUxy,uXY);
        GLES20.glUniformMatrix4fv(glHMatrix,1,false,mMVPMatrix,0);
        GLES20.glEnableVertexAttribArray(glHPosition);
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        GLES20.glUniform1i(glHTexture, 0);
//        createTexture();
        GLES20.glVertexAttribPointer(glHPosition,2, GLES20.GL_FLOAT,false,0,bPos);
        GLES20.glVertexAttribPointer(glHCoordinate,2, GLES20.GL_FLOAT,false,0,bCoord);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4);
    }

    public abstract void onDrawSet();
    public abstract void onDrawCreatedSet(int mProgram);
}
