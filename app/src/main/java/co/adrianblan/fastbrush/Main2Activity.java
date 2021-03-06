package co.adrianblan.fastbrush;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Main2Activity extends AppCompatActivity {

    private MyGLSurfaceView mMyGLSurfaceView;
    private Button mClearButton;
    private Button mSaveButton;
    private Button mBitmapButton;
    private File mPhoto;
    private Bitmap mBitmap;
    private EditText mEText;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mMyGLSurfaceView = (MyGLSurfaceView) findViewById(R.id.signature_pad);
        mClearButton = (Button) findViewById(R.id.clear);
        mSaveButton = (Button) findViewById(R.id.save);
        mBitmapButton = (Button) findViewById(R.id.bitmap);
        mEText = (EditText) findViewById(R.id.modify_edit_text_view);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMyGLSurfaceView.clearScreen();
            }
        });
        mImageView = (ImageView) findViewById(R.id.iamgeview);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap signatureBitmap = mMyGLSurfaceView.getRenderer().getBitmap();
//                mImageView.setVisibility(View.VISIBLE);
//                mImageView.setImageBitmap(signatureBitmap);
                boolean b = addJpgSignatureToGallery(signatureBitmap);
                if (b){
                    saveBitmap(mPhoto);
                    mMyGLSurfaceView.clearScreen();
                }
                //                mMyGLSurfaceView.saveImage();
//                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/FastBrush"); //Creates app specific folder
//                path.mkdirs();
//                File imageFile = new File(path, "shiming" + ".png"); // Imagename.png
//                saveBitmap(imageFile);

            }
        });
    }

    private static String full_name = "";
    private static final String LAST_NAME = "img_";
    private int first_name = 1;

    public void saveBitmap(File path) {
        mBitmap = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            mBitmap = BitmapUtils.resizeImage(bitmap, 150, 150);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (mBitmap != null) {
            //根据Bitmap对象创建ImageSpan对象
            ImageSpan imageSpan = new ImageSpan(this, mBitmap);
            //创建一个SpannableString对象，以便插入用ImageSpan对象封装的图像
            full_name = LAST_NAME + first_name;
            String s = "[" + full_name + "]";
            first_name++;
            SpannableString spannableString = new SpannableString(s);
            //  用ImageSpan对象替换face
            spannableString.setSpan(imageSpan, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            //将选择的图片追加到EditText中光标所在位置
            int index = mEText.getSelectionStart(); //获取光标所在位置
            Editable edit_text = mEText.getEditableText();
            if (index < 0 || index >= edit_text.length()) {
                edit_text.append(spannableString);
            } else {
                edit_text.insert(index, spannableString);
            }
        }
    }

    public boolean addJpgSignatureToGallery(Bitmap signature) {
        boolean result = false;
        try {
            mPhoto = new File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, mPhoto);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        //图片的质量
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("SignaturePad", "Directory not created");
        }
        return file;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMyGLSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMyGLSurfaceView.onResume();
    }
}
