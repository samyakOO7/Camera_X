package com.example.camera_x;
import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraX;
import androidx.camera.core.FlashMode;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;


import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.camera_x.R;

import java.io.File;


public class MainActivity extends AppCompatActivity {
private int REQUEST_CODE_PERMISSIONS=101;
private String[] REQUIRED_PERMISSIONS=new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.READ_EXTERNAL_STORAGE"};
TextureView textureview;
FlashMode mFlashMode=FlashMode.OFF;
CameraX.LensFacing mLensFacing= CameraX.LensFacing.BACK;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      getSupportActionBar().hide();
      textureview=(TextureView) findViewById(R.id.tv);
       if(allpermissionsgranted())
       {
           find_cam();
       }
       else
       {
           ActivityCompat.requestPermissions( this,REQUIRED_PERMISSIONS,REQUEST_CODE_PERMISSIONS);
       }

    }
@SuppressLint("RestrictedApi")
public void find_cam(){
        startCamera();
        findViewById(R.id.button3).setOnClickListener(v -> {
            mLensFacing = (mLensFacing == mLensFacing.BACK ? mLensFacing.FRONT : mLensFacing.BACK);

            try {

                    CameraX.getCameraWithLensFacing(mLensFacing);
                    startCamera();

            } catch (CameraInfoUnavailableException e) {
                // Do nothing


            }

        });
    }

    private void startCamera() {
        CameraX.unbindAll();
        Rational aspectRatio=new Rational(textureview.getWidth(),textureview.getHeight());
        Size screen=new Size(textureview.getWidth(),textureview.getHeight());
        PreviewConfig previewConfig=new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).setLensFacing(mLensFacing).build();
        Preview preview=new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent=(ViewGroup)textureview.getParent();
                parent.removeView(textureview);
                parent.addView(textureview);
                textureview.setSurfaceTexture(output.getSurfaceTexture());
                update();

            }
        });

        ImageCaptureConfig imageCaptureConfig=new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY).setTargetRotation(
                getWindowManager().getDefaultDisplay().getRotation()).setFlashMode(mFlashMode).setLensFacing(mLensFacing).build();
        final ImageCapture Cap=new ImageCapture(imageCaptureConfig);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFlashMode==FlashMode.OFF)
                   Cap.setFlashMode(FlashMode.ON);
                else if(mFlashMode==FlashMode.ON)
                   Cap.setFlashMode(FlashMode.OFF);
            }
        });

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".png");
                Cap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        String msg="Photo Captured at "+ file.getAbsolutePath();
                        Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    String msg="Photo Capture Failed: " + message;
                        Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
                        if(cause!=null)
                        {
                            cause.printStackTrace();
                        }
                    }
                });

            }
        });
         CameraX.bindToLifecycle(this,preview,Cap);


    }

    private void update() {
        Matrix mx=new Matrix();
        float w= textureview.getMeasuredWidth();
        float h=textureview.getMeasuredHeight();
        float cx=w/2f;
        float cy=h/2f;
        int rotationDgr;
        int rotation=(int) textureview.getRotation();
        {
            switch (rotation)
            {
                case Surface
                        .ROTATION_0:
                    rotationDgr=0;
                break;
                case Surface.ROTATION_90:
                    rotationDgr=90;
                    break;
                case Surface.ROTATION_180:
                    rotationDgr=180;
                    break;
                case Surface.ROTATION_270:
                    rotationDgr=270;
                    break;
                default:
                    return;
            }
    mx.postRotate((float)rotationDgr,cx,cy);
            textureview.setTransform(mx);

        }
    }

    private boolean allpermissionsgranted(){
for(String permission:REQUIRED_PERMISSIONS)
{
    if(ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
        return false;
    }

    }
return true;

}
}
