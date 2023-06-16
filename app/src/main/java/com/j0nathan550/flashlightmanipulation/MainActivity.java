package com.j0nathan550.flashlightmanipulation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private Button enableFlashButton;
    private TextInputEditText secondsText;
    private Timer flashLightTimer;
    private Timer vibrateTimer;
    private boolean enableFlashLight = false;
    private boolean switching = false;
    private boolean isInVibrateMode = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch vibratorSwitch = findViewById(R.id.vibratorSwitch1);
        enableFlashButton = findViewById(R.id.button);
        secondsText = findViewById(R.id.secondsText);
        enableFlashButton.setOnClickListener(v -> {
            if (isInVibrateMode) {
                switching = !switching;

                vibrateTimer = new Timer();
                vibrateTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        enableVibrator(switching);
                    }
                }, 0, Integer.parseInt(Objects.requireNonNull(secondsText.getText()).toString()));
            }
            else {
                enableFlashLight = !enableFlashLight;
                if (enableFlashLight) {
                    flashLightTimer = new Timer();
                    flashLightTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            switching = !switching;
                            flashSwitch(switching);
                        }
                    }, 0, Integer.parseInt(Objects.requireNonNull(secondsText.getText()).toString()));
                    enableFlashButton.setText("Disable");
                } else {
                    flashLightTimer.cancel();
                    flashLightTimer = new Timer();
                    switching = false;
                    flashSwitch(false);
                    enableFlashButton.setText("Enable");
                }
            }
        });
        vibratorSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               isInVibrateMode = !isInVibrateMode;
               if (!isInVibrateMode){
                   switching = false;
                   vibrateTimer.cancel();
                   vibrateTimer = new Timer();
               }
            }
        });
    }
    private void enableVibrator(boolean input) {
        Vibrator v = null;
        if (input) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(Integer.parseInt(Objects.requireNonNull(secondsText.getText()).toString()));
            }
        }
        else {
            if (v != null) {
                v.cancel();
            }
        }
    }
    private void flashSwitch(boolean input) {

        if (this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                CameraManager camManager = (CameraManager)getSystemService(CAMERA_SERVICE);
                String cameraID;
                try {
                    cameraID = camManager.getCameraIdList()[0];
                    camManager.setTorchMode(cameraID,input);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            else
            {

                Camera camera = Camera.open();
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(p);

                SurfaceTexture texture = new SurfaceTexture(0);
                try {
                    camera.setPreviewTexture(texture);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (input) {
                    camera.startPreview();
                }else {

                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.stopPreview();
                    camera.release();
                }
            }


        }
        else {
            Toast.makeText(this, "No flash light found", Toast.LENGTH_SHORT).show();
        }
    }
}