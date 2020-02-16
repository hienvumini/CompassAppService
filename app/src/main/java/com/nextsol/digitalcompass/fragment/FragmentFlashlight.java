package com.nextsol.digitalcompass.fragment;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.nextsol.digitalcompass.R;

import java.util.List;

import es.dmoral.toasty.Toasty;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class FragmentFlashlight extends Fragment {
    LinearLayout linearLayoutSOS;
    Switch aSwitchLight;
    ImageView imageViewLight, imageViewFlash_on, imageView_Flash_off;
    CameraManager cameraManager;
    boolean isSOS = false;
    boolean isturnlight = false;
    CountDownTimer countDownTimer;
    Camera camera;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_flashlight, container, false);


        init(view);
        listener();


        return view;
    }

    private void listener() {
        linearLayoutSOS.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                isSOS = !isSOS;

                try {
                    setStatusSOS();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }


            }
        });
        aSwitchLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchLight(isChecked);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init(View view) {
        linearLayoutSOS = (LinearLayout) view.findViewById(R.id.linelayoutSOS);
        aSwitchLight = (Switch) view.findViewById(R.id.switch_Flashlight);
        imageViewLight = (ImageView) view.findViewById(R.id.light);
        imageViewFlash_on = (ImageView) view.findViewById(R.id.flash_on_Flashlight);
        imageView_Flash_off = (ImageView) view.findViewById(R.id.flash_off_Flashlight);
        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void switchLight(boolean isTurnOn) {
        if (isTurnOn) {
            imageViewLight.setVisibility(View.VISIBLE);
            imageView_Flash_off.setVisibility(View.INVISIBLE);
            imageViewFlash_on.setVisibility(View.VISIBLE);


        } else {

            imageViewLight.setVisibility(View.INVISIBLE);
            imageView_Flash_off.setVisibility(View.VISIBLE);
            imageViewFlash_on.setVisibility(View.INVISIBLE);
        }
        try {
            if (cameraManager.getCameraIdList().length > 0 && cameraManager.getCameraIdList() != null) {
                try {
                    cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], isTurnOn);
                } catch (NoSuchMethodError error) {
                    Toasty.error(getActivity(),"Flashlight is unavailable").show();
                }

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setStatusSOS() throws CameraAccessException {
        if (isSOS) {
            Toast.makeText(getActivity(), "SOS ON", Toast.LENGTH_SHORT).show();
            linearLayoutSOS.setBackground(getActivity().getResources().getDrawable(R.drawable.botronsospress));
            turnSOSFlashLight(isSOS);


        } else {
            Toast.makeText(getActivity(), "SOS OFF", Toast.LENGTH_SHORT).show();
            linearLayoutSOS.setBackground(getActivity().getResources().getDrawable(R.drawable.botron));
            turnSOSFlashLight(isSOS);

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void turnSOSFlashLight(boolean statusSOS) throws CameraAccessException {

        if (countDownTimer == null) {
            countDownTimer = new CountDownTimer(5000, 500) {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onTick(long millisUntilFinished) {
                    switchLight(isturnlight);
                    isturnlight = !isturnlight;
                }

                @Override
                public void onFinish() {
                    switchLight(false);
                    this.start();
                }
            };

        }

        if (statusSOS) {

            countDownTimer.start();
            Log.d("111113", "turnSOSFlashLight: Mo " + statusSOS);
        } else {

            countDownTimer.cancel();
            switchLight(false);

            Log.d("111113", "turnSOSFlashLight: Tat " + statusSOS);

        }


    }

    public boolean hasFlash() {
        if (camera == null) {
            return false;
        }

        Camera.Parameters parameters = camera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }


}
