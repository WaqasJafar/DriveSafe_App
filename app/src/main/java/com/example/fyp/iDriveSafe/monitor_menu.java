package com.example.fyp.iDriveSafe;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.DateFormat;
import java.util.Date;

import br.com.bloder.magic.view.MagicButton;

public class monitor_menu extends Fragment {
    MagicButton b;
    ImageView  magic_Button1;
    SeekBar s;
    TextView ttv;
    private String key_2 = "waqas project";
    private String key_4 = "senstivity";
    private static final int pic_id = 123;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root;
        root = inflater.inflate(R.layout.activity_monitor_menu,container,false);
        b = (MagicButton) root.findViewById(R.id.magic_button);
//        magic_Button1 = (MagicButton) root.findViewById(R.id.magic_button1);
        magic_Button1 = root.findViewById(R.id.magic_button1);
        s = (SeekBar)root.findViewById(R.id.seekBar2);
        ttv = (TextView)root.findViewById(R.id.textView21);
        if(s.getProgress() == 0)
        {
            ttv.setText("0.5 second");
        }
        else if(s.getProgress() == 1)
        {
            ttv.setText("0.75 second");
        }
        else if(s.getProgress() == 2)
        {
            ttv.setText("1 seconds");
        }
        else if(s.getProgress() == 3)
        {
            ttv.setText("1.25 seconds");
        }
        else if(s.getProgress() == 4)
        {
            ttv.setText("1.5 seconds");
        }
        else if(s.getProgress() == 5)
        {
            ttv.setText("1.75 seconds");
        }
        else if(s.getProgress() == 6)
        {
            ttv.setText("2 seconds");
        }
        else if(s.getProgress() == 7)
        {
            ttv.setText("2.25 seconds");
        }
        else
        {
            ttv.setText("2.5 seconds");
        }
        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                         @Override
                                         public void onStopTrackingTouch(SeekBar seekBar) {

                                         }

                                         @Override
                                         public void onStartTrackingTouch(SeekBar seekBar) {

                                         }

                                         @Override
                                         public void onProgressChanged(SeekBar seekBar, int progress,
                                                                       boolean fromUser) {
                                             if(s.getProgress() == 0)
                                             {
                                                 ttv.setText("0.5 second");
                                             }
                                             else if(s.getProgress() == 1)
                                             {
                                                 ttv.setText("0.75 second");
                                             }
                                             else if(s.getProgress() == 2)
                                             {
                                                 ttv.setText("1 seconds");
                                             }
                                             else if(s.getProgress() == 3)
                                             {
                                                 ttv.setText("1.25 seconds");
                                             }
                                             else if(s.getProgress() == 4)
                                             {
                                                 ttv.setText("1.5 seconds");
                                             }
                                             else if(s.getProgress() == 5)
                                             {
                                                 ttv.setText("1.75 seconds");
                                             }
                                             else if(s.getProgress() == 6)
                                             {
                                                 ttv.setText("2 seconds");
                                             }
                                             else if(s.getProgress() == 7)
                                             {
                                                 ttv.setText("2.25 seconds");
                                             }
                                             else
                                             {
                                                 ttv.setText("2.5 seconds");
                                             }


                                         }
                                     });

                b.setMagicButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(root.getContext(), com.example.fyp.iDriveSafe.FaceTrackerActivity.class);
                i.putExtra(key_4,""+s.getProgress());
                i.putExtra(key_2,DateFormat.getDateTimeInstance().format(new Date()));
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                getActivity().finish();

            }

        });


//        magic_Button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent i = getPackageManager().getLaunchIntentForPackage("com.iDriveSafe Back");
////                startActivity(i);
//                Intent camera_intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//                startActivityForResult(camera_intent, 1);
//                //intent.addCategory(Intent.CATEGORY_OPENABLE);
//                //intent.setAction(Intent.ACTION_GET_CONTENT);
//
//            }
//        });

        magic_Button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(root.getContext(), com.example.fyp.iDriveSafe.MainActvity2.class);
                i.putExtra(key_4,""+s.getProgress());
                i.putExtra(key_2,DateFormat.getDateTimeInstance().format(new Date()));
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                getActivity().finish();

            }
        });

        return root;

    }

    private PackageManager getPackageManager() {

    return  null;}



}
