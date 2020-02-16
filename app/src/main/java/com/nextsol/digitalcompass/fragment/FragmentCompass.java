package com.nextsol.digitalcompass.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.nextsol.digitalcompass.R;
import com.nextsol.digitalcompass.Utils.SOTWFormatter;
import com.nextsol.digitalcompass.model.Compass;


public final class FragmentCompass extends Fragment {
    private static final String TAG = "CompassActivity";

    private static Compass compass;
    private static ImageView imageViewarrowView, imageViewDial;
    private static float currentAzimuth;
    private static SOTWFormatter sotwFormatter;
    public static TextView textViewDgree, textViewMagnetic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_compass1, container, false);
        init(view);
        compass.start();
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        compass.start();

    }

    @Override
    public void onResume() {
        super.onResume();
        compass.start();
    }

    @Override
    public void onPause() {
        super.onPause();
         compass.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        compass.stop();
    }

    private void init(View view) {
        sotwFormatter = new SOTWFormatter(getActivity());
        imageViewarrowView = (ImageView) view.findViewById(R.id.image_hands_Compass);
        imageViewDial = (ImageView) view.findViewById(R.id.image_dial_Compass);

        textViewDgree = (TextView) view.findViewById(R.id.textviewDir_Compass);
        textViewMagnetic = (TextView) view.findViewById(R.id.textviewMagnetic_Compass);
        compass = new Compass(getActivity());
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);


    }

    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);
                        adjustSotwLabel(azimuth);
                    }
                });
            }

            @Override
            public void onNewMagnetic(float magnetic) {

                textViewMagnetic.setText((int) (magnetic) + "μT");
            }
        };
    }

    private void adjustArrow(float azimuth) {
        Log.d(TAG, "will set rotation from " + currentAzimuth + " to "
                + azimuth);

        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currentAzimuth = azimuth;

        an.setDuration(1);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        imageViewDial.startAnimation(an);
        textViewDgree.setText(sotwFormatter.format(azimuth));
    }

    private void adjustSotwLabel(float azimuth) {
        //  sotwLabel.setText(sotwFormatter.format(azimuth));
    }

}
