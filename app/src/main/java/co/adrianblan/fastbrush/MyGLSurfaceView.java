/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.adrianblan.fastbrush;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.util.ArrayList;

import co.adrianblan.fastbrush.touch.TouchData;
import co.adrianblan.fastbrush.vector.Vector2;

/**
 *可以在屏幕上绘制OpenGL ES图形的视图容器。
 *这个视图也可以用来捕获触摸事件，比如用户。
 *与绘图对象交互。
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private VelocityTracker mVelocityTracker;

    private int[] savedPixelBuffer;

    public MyGLSurfaceView(Context context) {
        this(context,null);


    }

    public MyGLSurfaceView(Context context,AttributeSet attrs) {
        super(context,attrs);
        // Create an OpenGL ES 3.0 context.
        setEGLContextClientVersion(3);

        // Sets a paper background to the scene
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setBackgroundResource(R.drawable.paperbright);
        // 当SurfaceView和GLSurfaceView同时在一个布局里面，
        // 如果想让SurfaveView显示图片或者视频必须要调用SurfaceView.setZOrderOnTop(true),
        // 也就是说必须把SurfaceView置于Activity显示窗口的最顶层才能正常显示
        setZOrderOnTop(true);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

        // 仅在绘图数据发生更改时才呈现视图。
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        ////设置暂停的时候是否保持EglContext
        setPreserveEGLContextOnPause(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }
               // 基于线程原因，我们不能简单的在UI线程中调用OpenGL方法，例如，事件分发的方法中我们直接调用Renderer中的方法。除此之外，我们还需要考虑线程安全问题，即同时被UI线程和OpenGL渲染线程读写的变量。
               // 使用queueEvent()，则完全不必担心上述问题，因为最终所有方法都是在GLSUrfaceView.Renderer中的方法中调用的，也就是在渲染线程中使用的。
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.touchHasStarted();
                    }
                });

                // No break is intentional

            case MotionEvent.ACTION_MOVE:

                mVelocityTracker.addMovement(e);

                // Compute velocity in pixels per second
                mVelocityTracker.computeCurrentVelocity(1);
                //getHistorySize() 来获得历史的大小值,它可以返回当前事件可用的运动位置的数目
                final ArrayList<TouchData> touchDataList = new ArrayList<>(e.getHistorySize() + 1);
                Vector2 viewportPosition;

                Vector2 viewportVelocity =
                        new Vector2(VelocityTrackerCompat.getXVelocity(mVelocityTracker, e.getActionIndex()),
                        VelocityTrackerCompat.getYVelocity(mVelocityTracker, e.getActionIndex()));

                // Add previous touch coordinates
                for(int i = 0; i < e.getHistorySize(); i++) {
                    viewportPosition = new Vector2(e.getHistoricalX(i), e.getHistoricalY(i));

                            touchDataList.add(new TouchData(mRenderer.viewportToWorld(viewportPosition),
                                    viewportVelocity,
                                    e.getHistoricalSize(i), e.getHistoricalPressure(i)));
                }

                // Add current touch coordinates
                viewportPosition = new Vector2(e.getX(), e.getY());
                touchDataList.add(new TouchData(mRenderer.viewportToWorld(viewportPosition), viewportVelocity, e.getSize(), e.getPressure()));

                // Ensure we call switchMode() on the OpenGL thread.
                // queueEvent() is a method of GLSurfaceView that will do this for us.
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.addTouchData(touchDataList);
                    }
                });

                requestRender();
                break;

            case MotionEvent.ACTION_UP:

                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        mRenderer.touchHasEnded();
                    }
                });
                requestRender();
                break;
        }
        return true;
    }

    public void undo() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.undo();
            }
        });
        requestRender();
    }

    public void clearScreen() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.clearScreen();
            }
        });
        requestRender();
    }

    public void saveImage() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mRenderer.saveImage();
            }
        });
    }
    public MyGLRenderer getRenderer(){
        return mRenderer;
    }
    @Override
    public void onPause(){
        super.onPause();
        mRenderer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
    }
}
