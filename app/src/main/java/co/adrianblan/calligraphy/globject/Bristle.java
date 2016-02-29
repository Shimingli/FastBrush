package co.adrianblan.calligraphy.globject;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import co.adrianblan.calligraphy.utils.GLhelper;
import co.adrianblan.calligraphy.utils.Utils;
import co.adrianblan.calligraphy.vector.Vector3;

/**
 * Writing primitive for the brush.
 */
public class Bristle {

    public static final float LENGTH = 2.0f;
    private static final float VERTICAL_OFFSET = 0.0f;
    private static final float TIP_LENGTH = 0.1f;

    private Vector3 top;
    private Vector3 bottom;

    private final FloatBuffer vertexBuffer;

    private int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    public Bristle() {
        vertexBuffer = GLhelper.initFloatBuffer(6);

        float radiusAngle = (float) (Math.random() * 2 * Math.PI);
        float verticalAngle = (float) Math.random();

        float horizontal = (float) Math.cos(radiusAngle) * verticalAngle;
        float vertical = (float) Math.sin(radiusAngle) * verticalAngle;

        float upperRadius = 0.4f;
        float lowerRadius = 0.2f;

        top = new Vector3(upperRadius * horizontal, upperRadius * vertical + VERTICAL_OFFSET, 0f);
        bottom = new Vector3(lowerRadius * horizontal, lowerRadius * vertical, -LENGTH + TIP_LENGTH * verticalAngle);

        mProgram = GLES20.glCreateProgram();
        GLhelper.loadShaders(mProgram, GLobject.DEFAULT_VERTEX_SHADER_CODE,
                GLobject.DEFAULT_FRAGMENT_SHADER_CODE);
    }

    public void draw(float[] mvpMatrix, Vector3 brushPosition) {

        updateBristlePosition(brushPosition);

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLhelper.checkGlError("glGetAttribLocation");

        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, GLobject.DEFAULT_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                GLobject.DEFAULT_VERTEX_STRIDE, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, Utils.blackColor, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLhelper.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLhelper.checkGlError("glUniformMatrix4fv");

        // Draw line
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private void updateBristlePosition(Vector3 brushPosition) {
        Vector3 absoluteStart = top.add(brushPosition);
        Vector3 absoluteEnd = bottom.add(brushPosition);

        vertexBuffer.clear();
        vertexBuffer.put(absoluteStart.toFloatArray());
        vertexBuffer.put(absoluteEnd.toFloatArray());
        vertexBuffer.position(0);
    }
}