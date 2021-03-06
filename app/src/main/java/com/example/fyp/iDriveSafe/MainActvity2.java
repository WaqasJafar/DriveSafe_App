package com.example.fyp.iDriveSafe;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.fyp.iDriveSafe.ui.camera.ScreenInterface;
import com.example.fyp.iDriveSafe.ui.camera.SignUiRunnable;
import com.example.fyp.iDriveSafe.ui.camera.SpeedUiRunnable;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActvity2 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, ScreenInterface, TextToSpeech.OnInitListener {
  private static final String TAG = "MainActivity2";
  JavaCameraView javaCameraView;
  TextRecognizer textRecognizer;
  ImageView signImageView;
  TextView speedTextView;
  FloatingActionButton fabSettings, fabResolutions, fabGps, fabSound;
  Animation FabOpen, FabClose, FabRotateCw, FabRotateAntiCw;
  Boolean isOpen = false;

  Mat mRgba, mGray, circles;
  Mat mRed, mGreen, mBlue, mHue_hsv, mSat_hsv, mVal_hsv, mHue_hls, mSat_hls, mLight_hls;
  Mat hsv, hls, rgba, gray;
  Mat mNew, mask, mEdges, laneZoneMat;
  Rect signRegion;
  MatOfPoint laneZone;

  Scalar darkGreen = new Scalar(0, 125, 0);
  Bitmap bm;
  Boolean newSignFlag = false;

  int imgWidth, imgHeight;
  int rows, cols, left, width;
  double top, middleX, bottomY;

  double vehicleCenterX1, vehicleCenterY1, vehicleCenterX2, vehicleCenterY2, laneCenterX, laneCenterY;

  SignUiRunnable signUiRunnable = new SignUiRunnable();
  SpeedUiRunnable speedUiRunnable = new SpeedUiRunnable();

  TextToSpeech ttsSpeed, ttsLane;
  int speedingCount = 0;
  ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
  ToneGenerator toneGen2 = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
  AudioManager audioManager;

  CountDownTimer timer;
  boolean isTimerRunning = false;

  BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
    @Override
    public void onManagerConnected(int status) {
      switch (status) {
        case BaseLoaderCallback.SUCCESS: {
          javaCameraView.enableView();
          break;
        }
        default: {
          super.onManagerConnected(status);
          break;
        }
      }
      super.onManagerConnected(status);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main2);

    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    fabSettings = (FloatingActionButton) findViewById(R.id.fab_settings);
    fabResolutions = (FloatingActionButton) findViewById(R.id.fab_resolution);
    fabGps = (FloatingActionButton) findViewById(R.id.fab_gps);
    fabSound = (FloatingActionButton) findViewById(R.id.fab_sound);

    getPermissions();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      setUpCameraServices();
    }

    IntentFilter filter = new IntentFilter("iDriveSafe.UPDATE_SPEED");
    this.registerReceiver(new LocationBroadcastReceiver(), filter);

    textRecognizer = new TextRecognizer.Builder(this).build();
    if (!textRecognizer.isOperational()) {
      Log.w(TAG, "Detector dependencies are not yet available.");

      IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
      boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

      if (hasLowStorage) {
        Toast.makeText(this,"Low Storage: Speed Limit detection will not work.", Toast.LENGTH_LONG).show();
        Log.w(TAG, "Low Storage");
      }
    }

    ttsSpeed = new TextToSpeech(this, this);
    ttsLane = new TextToSpeech(this, this);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
    javaCameraView.setVisibility(SurfaceView.VISIBLE);
    javaCameraView.setCvCameraViewListener(this);
        javaCameraView.enableFpsMeter();
    javaCameraView.setMaxFrameSize(imgWidth, imgHeight);

    speedTextView = (TextView) findViewById(R.id.speed_text_view);
    signImageView = (ImageView) findViewById(R.id.sign_image_view);

    setViewClickListeners();
    FabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
    FabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
    FabRotateCw = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);
    FabRotateAntiCw = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise);

    signUiRunnable.setSignImageView(signImageView);
    speedUiRunnable.setSpeedTextView(speedTextView);
    SharedPreferences sharedPreferences;
    sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
    signUiRunnable.setSignVal(sharedPreferences.getInt("last_speed", 0));
    Log.i(TAG, "onCreate: ---------------------------------------------" + sharedPreferences.getInt("last_speed", 0));
    signUiRunnable.run();

    // Timer to alert user of lane departure
    timer = new CountDownTimer(30000, 1000) {
      @Override
      public void onTick(long millisUntilFinished) {
        if (millisUntilFinished < 27500) {
          toneGen2.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 250);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsLane.speak("Lane and Speed Voilatiob Detected", TextToSpeech.QUEUE_ADD, null, "Lane and Speed Voilatiob Detected");
          }
        }
      }

      @Override
      public void onFinish() {
        Log.i(TAG, "onFinish: ---------- TIMER DONE ----------");
      }
    };

    displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

    Handler handlerData = new Handler();
    handlerData.postDelayed(new Runnable() {
      @Override
      public void run() {
        handlerData.postDelayed(new Runnable() {
          @Override
          public void run() {
          }
        }, 1000);
        Intent i = new Intent(MainActvity2.this, MainActivity.class);
        i.putExtra("flipcamera", "12");
        startActivity(i);
      }
    }, 10000);
  }

  @Override
  public void onCameraViewStarted(int w, int h) {
    rows = h;
    cols = w;
    left = rows / 8;
    width = cols - left;
    top = rows / 2.5;
    middleX = w /2;
    bottomY = h * .95;

    vehicleCenterX1 = middleX;
    vehicleCenterX2 = middleX;
    vehicleCenterY1 = bottomY-(rows/7);
    vehicleCenterY2 = bottomY-(rows/20);
    laneCenterX = 0;
    laneCenterY = (bottomY-(rows/7) + bottomY-(rows/20)) / 2;

    initializeAllMats();
  }

  @Override
  public void onCameraViewStopped() {
    releaseAllMats();
  }

  private Size ksize = new Size(5, 5);
  private double sigma = 3;
  private Point blurPt = new Point(3, 3);

  /******************************************************************************************
   * mRed, mGreen, mBlue, m-_hsv, m-_hls :  Mats of respective channels of ROI
   * mCombined : combined mat of canny edges and mask for yellow and white
   * hsv, hls, rgb : color space mats of ROI
   ******************************************************************************************/
  @Override
  public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
    mRgba = inputFrame.rgba();
    mGray = inputFrame.gray();

    Imgproc.blur(mGray, mGray, ksize, blurPt);
    Imgproc.GaussianBlur(mRgba, mRgba, ksize, sigma);

    Mat rgbaInnerWindow;
    Mat lines = new Mat();
    /* rgbaInnerWindow & mIntermediateMat = ROI Mats */
    rgbaInnerWindow = mRgba.submat((int)top, rows, left, width);
    rgbaInnerWindow.copyTo(rgba);
    Imgproc.cvtColor(rgbaInnerWindow, gray, Imgproc.COLOR_RGB2GRAY);
    Imgproc.cvtColor(rgbaInnerWindow, hsv, Imgproc.COLOR_RGB2HSV);
    Imgproc.cvtColor(rgbaInnerWindow, hls, Imgproc.COLOR_RGB2HLS);

    splitRGBChannels(rgba, hsv, hls);
    applyThreshold();
    Imgproc.erode(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
    Imgproc.dilate(mask, mask, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));
    Imgproc.Canny(mask, mEdges, 50, 150);

    Imgproc.resize(mEdges, mNew, new Size(imgWidth, imgHeight));
    Imgproc.HoughCircles(mGray, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 1000, 175, 120, 25, 125);

    if (circles.cols() > 0) {
      for (int x=0; x < Math.min(circles.cols(), 5); x++ ) {
        double circleVec[] = circles.get(0, x);

        if (circleVec == null) {
          break;
        }

        Point center = new Point((int) circleVec[0], (int) circleVec[1]);
        int radius = 1;
        radius = (int) circleVec[2];

        int val = (radius*2) + 20;
        // defines the ROI
        signRegion = new Rect((int) (center.x - radius - 10), (int) (center.y - radius - 10), val, val);

        if (!newSignFlag) {
//          analyzeObject(inputFrame.rgba(), signRegion, radius);
        }
      }
    }

    circles.release();

    Imgproc.HoughLinesP(mEdges, lines, 1, Math.PI/180, 50, 25, 85);
    if (lines.rows() > 0) {
      getAverageSlopes(lines);
    }

    rgbaInnerWindow.release();
    Imgproc.line(mRgba, new Point(vehicleCenterX1, vehicleCenterY1), new Point(vehicleCenterX2, vehicleCenterY2), darkGreen, 2, 8);
    Imgproc.rectangle(mRgba, new Point(left, top), new Point(cols-left, bottomY), darkGreen, 2);

    return mRgba;
  }

  public class LocationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "BroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
      double vehicleSpeed = Objects.requireNonNull(intent.getExtras()).getDouble("speed");
      Log.e(TAG, "onReceive: " + vehicleSpeed);

      speedUiRunnable.setSpeedVal(vehicleSpeed);
      runOnUiThread(speedUiRunnable);
      if (vehicleSpeed > signUiRunnable.getSignVal() && signUiRunnable.getSignVal() > 0) {
        speedingCount += 1;
        if (speedingCount >= 5) {
          try {
            toneGen1.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 200);
          } catch (Exception e) {
            toneGen1.release();
            toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 75);
            Log.e(TAG, "onReceive: ", e);
          }
        }
      } else {
        speedingCount = 0;
      }
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (javaCameraView != null)
      javaCameraView.disableView();

    SharedPreferences.Editor editor = getSharedPreferences("Prefs", MODE_PRIVATE).edit();
    Log.i(TAG, "onPause: Latest detected speed limit: " + signUiRunnable.getSignVal());
    editor.putInt("last_speed", signUiRunnable.getSignVal());
    editor.apply();
    timer.cancel();
    // stop updates to save battery
    stopService(new Intent(this, LocationService.class));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (javaCameraView != null)
      javaCameraView.disableView();
    stopService(new Intent(this, LocationService.class));
    ttsLane.shutdown();
    ttsSpeed.shutdown();
  }

  @Override
  protected void onResume() {
    SharedPreferences sharedPreferences = getSharedPreferences("Prefs", MODE_PRIVATE);
    super.onResume();
    if (OpenCVLoader.initDebug()) {
      Log.d(TAG, "OpenCV initialize success");
      mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }
    else {
      Log.d(TAG, "OpenCV initialize failed");
      OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
    }
    imgWidth = sharedPreferences.getInt("res_width", 1920);
    imgHeight = sharedPreferences.getInt("res_height", 1080);
    javaCameraView.setMaxFrameSize(imgWidth, imgHeight);
    javaCameraView.disableView();
    javaCameraView.enableView();

    setFullscreen();
    // restart location updates when back in focus
    Intent locationServiceIntent = new Intent(this, LocationService.class);
    if (sharedPreferences.getBoolean("gps_enabled", true)) {
      startService(locationServiceIntent);
      speedTextView.setText("0.0 km/hr");
    }
    else {
      stopService(locationServiceIntent);
      speedTextView.setText("GPS Disabled");
    }

  }

  public void splitRGBChannels(Mat rgb_split, Mat hsv_split, Mat hls_split) {
    List<Mat> rgbChannels = new ArrayList<>();
    List<Mat> hsvChannels = new ArrayList<>();
    List<Mat> hlsChannels = new ArrayList<>();

    Core.split(rgb_split, rgbChannels);
    Core.split(hsv_split, hsvChannels);
    Core.split(hls_split, hlsChannels);

    rgbChannels.get(0).copyTo(mRed);
    rgbChannels.get(1).copyTo(mGreen);
    rgbChannels.get(2).copyTo(mBlue);

    hsvChannels.get(0).copyTo(mHue_hsv);
    hsvChannels.get(1).copyTo(mSat_hsv);
    hsvChannels.get(2).copyTo(mVal_hsv);

    hlsChannels.get(0).copyTo(mHue_hls);
    hlsChannels.get(1).copyTo(mSat_hls);
    hlsChannels.get(2).copyTo(mLight_hls);
//
//
    for (int i = 0; i < rgbChannels.size(); i++){
      rgbChannels.get(i).release();
    }

    for (int i = 0; i < hsvChannels.size(); i++){
      hsvChannels.get(i).release();
    }

    for (int i = 0; i < hlsChannels.size(); i++){
      hlsChannels.get(i).release();
    }
  }

  public void applyThreshold() {
    Scalar lowerThreshold = new Scalar(210), higherThreshold = new Scalar(255);

    Core.inRange(mRed, lowerThreshold, higherThreshold, mRed);
//        Core.inRange(mGreen, new Scalar(225), new Scalar(255), mGreen);
//        Core.inRange(mBlue, new Scalar(200), new Scalar(255), mBlue);

//        Core.inRange(mHue_hsv, new Scalar(200), new Scalar(255), mHue_hsv);
//        Core.inRange(mSat_hsv, new Scalar(200), new Scalar(255), mSat_hsv);
    Core.inRange(mVal_hsv, lowerThreshold, higherThreshold, mVal_hsv);



    Core.bitwise_and(mRed, mVal_hsv, mask);
  }

  int curSpeedVal = 100;
  String signValue = "";
  Boolean isRunning = false;

  public void analyzeObject(final Mat img, final Rect roi, final int radius) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        isRunning = true;
        Mat copy;
        try {
          copy = new Mat(img, roi);
          // Creates a bitmap with size of detected circle and stores the Mat into it
          bm = Bitmap.createBitmap(Math.abs((radius * 2) + 20), Math.abs((radius * 2) + 20), Bitmap.Config.ARGB_8888);
          Utils.matToBitmap(copy, bm);
        } catch (Exception e) {
          bm = null;
        }

        if (bm != null) {
          Frame imageFrame = new Frame.Builder().setBitmap(bm).build();
          SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);

          for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

            if (!signValue.equals(textBlock.getValue())) {
              signValue = textBlock.getValue();
              setUISign(signValue);
            }
          }
        }
        isRunning = false;
      }
    };

    if (!isRunning) {
      Thread textDetectionThread = new Thread(runnable);
      textDetectionThread.run();
    }
  }

  public void setUISign(String val) {
    curSpeedVal = signUiRunnable.getSignVal();
    if (val.contains("60")) {
      signUiRunnable.setSignVal(60);
    } else if (val.contains("80")) {
      signUiRunnable.setSignVal(80);
    } else if (val.contains("100")) {
      signUiRunnable.setSignVal(100);
    } else if (val.contains("50")) {
      signUiRunnable.setSignVal(50);
    } else if (val.contains("120")) {
      signUiRunnable.setSignVal(120);
    } else if (val.contains("30")) {
      signUiRunnable.setSignVal(30);
    }
    Log.i(TAG, "setUISign:" + curSpeedVal + " -------------------------------" + signUiRunnable.getSignVal());
    if (curSpeedVal != signUiRunnable.getSignVal()) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        ttsSpeed.speak(signUiRunnable.getSignVal() + " kilometers per hour", TextToSpeech.QUEUE_FLUSH, null, "Speed Detected");
      }
    }
    runOnUiThread(signUiRunnable);
  }

  public void getAverageSlopes(Mat lines) {
    List<Double> left_slopes = new ArrayList<>();
    List<Double> right_slopes = new ArrayList<>();
    List<Double> left_y_intercept = new ArrayList<>();
    List<Double> right_y_intercept = new ArrayList<>();

    // Threshold zone for detected lanes, lines must be within this zone
    double zoneX1 = cols-left*2.5;
    double zoneX2 = left*2.5;
    Imgproc.line(mRgba, new Point(zoneX1, top), new Point(zoneX1, top+5), new Scalar(0, 155, 0), 2, 8);
    Imgproc.line(mRgba, new Point(zoneX2, top), new Point(zoneX2, top+5), new Scalar(0, 155, 0), 2, 8);

    for (int i=0; i<lines.rows(); i++) {
      double[] points = lines.get(i, 0);
      double x1, y1, x2, y2;

      try {
        x1 = points[0];
        y1 = points[1];
        x2 = points[2];
        y2 = points[3];

        Point p1 = new Point(x1, y1);
        Point p2 = new Point(x2, y2);

        double slope = (p2.y - p1.y) / (p2.x - p1.x);
        double y_intercept;

        if (slope > 0.375 && slope < 2.6) { // Right lane
          if (p1.x+left < zoneX1) {
            right_slopes.add(slope);
            y_intercept = p1.y - (p1.x * slope);
            right_y_intercept.add(y_intercept);
          }
        }
        else if (slope > -2.6 && slope < -0.375) { // Left lane
          if (p2.x+left > zoneX2) {
            left_slopes.add(slope);
            y_intercept = p1.y - (p1.x * slope);
            left_y_intercept.add(y_intercept);
          }
        }
      } catch (Error e) {
        Log.e(TAG, "onCameraFrame: ", e);
      }
    }

    double avg_left_slope = 0;
    double avg_right_slope = 0;
    double avg_left_y_intercept = 0;
    double avg_right_y_intercept = 0;

    for (int i=0; i< right_slopes.size(); i++) {
      avg_right_slope += right_slopes.get(i);
      avg_right_y_intercept += right_y_intercept.get(i);
    }
    avg_right_slope /= right_slopes.size();
    avg_right_y_intercept /= right_y_intercept.size();

    for (int i=0; i< left_slopes.size(); i++) {
      avg_left_slope += left_slopes.get(i);
      avg_left_y_intercept += left_y_intercept.get(i);
    }
    avg_left_slope /= left_slopes.size();
    avg_left_y_intercept /= left_y_intercept.size();

    // x = (y-b)/m
    // y = xm + b
    double newLeftTopX = ((-avg_left_y_intercept)/avg_left_slope) + left;
    double newRightTopX = ((0 - avg_right_y_intercept)/avg_right_slope) + left;

    Point rightLanePt = new Point((imgHeight - avg_right_y_intercept)/avg_right_slope, imgHeight);
    Point leftLanePt = new Point((0), (-left*avg_left_slope)+avg_left_y_intercept);

    Point topLeftPt = new Point(newLeftTopX, 0 + top);
    Point topRightPt = new Point(newRightTopX, 0 + top);
    Point bottomLeftPt = new Point(-500+left, ((-500*avg_left_slope)+avg_left_y_intercept)+top);
    Point bottomRightPt = new Point(rightLanePt.x + left, rightLanePt.y + top);

    if (right_slopes.size() != 0 && left_slopes.size() != 0) {
      double laneCenterX1 = (laneCenterY-top-avg_left_y_intercept)/avg_left_slope + left;
      double laneCenterX2 = (laneCenterY-top-avg_right_y_intercept)/avg_right_slope + left;
      laneCenterX = (laneCenterX1+laneCenterX2) / 2;

      laneZone = new MatOfPoint(topLeftPt, topRightPt, bottomRightPt, bottomLeftPt);
      laneZoneMat.setTo(new Scalar(0, 0, 0));
      Imgproc.fillConvexPoly(laneZoneMat, laneZone, new Scalar(255, 240, 160));
      Core.addWeighted(laneZoneMat, .5, mRgba, 1, 0, mRgba);
      laneZone.release();

      double distanceFromCenter = Math.sqrt((laneCenterX-vehicleCenterX1)*(laneCenterX-vehicleCenterX1) + (laneCenterY-laneCenterY)*(laneCenterY-laneCenterY));

      // If lane departure is detected, add an orange layer over output
      if (distanceFromCenter > 70) {
        if (!isTimerRunning) {
          timer.start();
          isTimerRunning = true;
          Log.i(TAG, "---------- LaneDrift Start: Timer STARTED ----------");
        }
        Core.add(mRgba, new Scalar(255, 128, 0), mRgba);
      } else if (isTimerRunning){
        timer.cancel();
        isTimerRunning = false;
        Log.i(TAG, "---------- LaneDeparture Stop: Timer STOPPED ----------");
        if (ttsLane.isSpeaking()) {
          ttsLane.stop();
        }
      }

      Imgproc.line(mRgba, new Point(vehicleCenterX1, laneCenterY), new Point(laneCenterX, laneCenterY), darkGreen, 2, 8);
      Imgproc.circle(mRgba, new Point(laneCenterX, laneCenterY), 4, new Scalar(0, 0, 255), 7);
    }
    else if (isTimerRunning) {
      timer.cancel();
      isTimerRunning = false;
      if (ttsLane.isSpeaking()) {
        ttsLane.stop();
      }
      Log.i(TAG, "---------- TIMER CANCELED: No lanes detected ----------");
    }

    if (left_slopes.size() != 0) {
      Imgproc.line(mRgba, topLeftPt, bottomLeftPt, new Scalar(225, 0, 0), 8);
    }
    if (right_slopes.size() != 0) {
      Imgproc.line(mRgba, bottomRightPt, topRightPt, new Scalar(0, 0, 225), 8);
    }
  }

  @Override
  public void onInit(int status) {
    ttsSpeed.setLanguage(Locale.ENGLISH);
    ttsLane.setLanguage(Locale.ENGLISH);
    ttsLane.setSpeechRate(0.9f);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void setUpCameraServices() {
    SharedPreferences sharedPreferences = getSharedPreferences("Prefs", MODE_PRIVATE);
    boolean firstLaunch = false;
    SharedPreferences.Editor editor = sharedPreferences.edit();
    try {
      firstLaunch = sharedPreferences.getBoolean("first_launch", true);
      Log.i(TAG, "setUpCameraServices: " + firstLaunch);
    } catch (Exception e) {
      Log.e(TAG, "setUpCameraServices: ", e);
    }

    if (firstLaunch) {
      editor.putBoolean("gps_enabled", true);
      editor.putBoolean("sound_enabled", true);
      CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
      try {
        assert manager != null;
        String cameraId = manager.getCameraIdList()[0];
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        assert map != null;

        for (android.util.Size size : map.getOutputSizes(SurfaceTexture.class)) {
          float ratio = (float) size.getWidth() / (float) size.getHeight();
          if (ratio >= 1.3 && size.getWidth() < 900) {
            imgHeight = size.getHeight();
            imgWidth = size.getWidth();
            break;
          }
        }
        editor.putInt("res_height", imgHeight);
        editor.putInt("res_width", imgWidth);
        Log.i(TAG, "setUpCameraServices: " + sharedPreferences);
      } catch (Error error) {
        Log.e(TAG, "onCreate: ", error);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
      editor.putBoolean("first_launch", false);
      editor.apply();
    }
    else {
      imgHeight = sharedPreferences.getInt("res_height", 1080);
      imgWidth = sharedPreferences.getInt("res_width", 1920);
    }

    if (sharedPreferences.getBoolean("sound_enabled", true)) {
      fabSound.setImageResource(R.drawable.volume_on_white_24dp);
      audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
    } else {
      fabSound.setImageResource(R.drawable.volume_off_white_24dp);
      audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
    }
  }

  private void setViewClickListeners() {
    fabSettings.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (isOpen) {
              fabResolutions.startAnimation(FabClose);
              fabGps.startAnimation(FabClose);
              fabSound.startAnimation(FabClose);
              fabSound.setClickable(false);
              fabSettings.startAnimation(FabRotateAntiCw);
              fabResolutions.setClickable(false);
              fabGps.setClickable(false);
              isOpen = false;
            } else {
              fabResolutions.startAnimation(FabOpen);
              fabGps.startAnimation(FabOpen);
              fabSound.startAnimation(FabOpen);
              fabSound.setClickable(true);
              fabSettings.startAnimation(FabRotateCw);
              fabResolutions.setClickable(true);
              fabGps.setClickable(true);
              isOpen = true;
            }
          }
        });
      }
    });

    fabResolutions.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), ResolutionSettingsActivity.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "Resolution Settings", Toast.LENGTH_SHORT).show();
      }
    });

    fabGps.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(getApplicationContext(), GpsSettingsActivity.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), "GPS Settings", Toast.LENGTH_SHORT).show();
      }
    });

    fabSound.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences sharedPreferences = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("sound_enabled", true)) {
          fabSound.setImageResource(R.drawable.volume_off_white_24dp);

          Toast.makeText(getApplicationContext(), "Alerts/warnings disabled", Toast.LENGTH_SHORT).show();
          editor.putBoolean("sound_enabled", false);
          try {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
          } catch (Exception e) {
            Log.e(TAG, "onClick: ", e);
          }
        } else {
          fabSound.setImageResource(R.drawable.volume_on_white_24dp);
          Toast.makeText(getApplicationContext(), "Alerts/warnings enabled", Toast.LENGTH_SHORT).show();

          editor.putBoolean("sound_enabled", true);
          try {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);
          } catch (Exception e) {
            Log.e(TAG, "onClick: ", e);
          }
        }
        editor.apply();
      }
    });

  }

  private void initializeAllMats() {
    mRgba = new Mat();
    mGray = new Mat();
    circles = new Mat();
    mRed = new Mat();
    mGreen = new Mat();
    mBlue = new Mat();
    mHue_hls = new Mat();
    mLight_hls = new Mat();
    mSat_hls = new Mat();
    mHue_hsv = new Mat();
    mSat_hsv = new Mat();
    mVal_hsv = new Mat();
    hsv = new Mat();
    hls = new Mat();
    gray = new Mat();
    rgba = new Mat();
    mNew = new Mat();
    mask = new Mat();
    mEdges = new Mat();
    laneZoneMat = new Mat(rows, cols, CvType.CV_8UC4);
  }

  private void releaseAllMats() {
    mRgba.release();
    mGray.release();
    circles.release();
    mRed.release();
    mGreen.release();
    mBlue.release();
    mHue_hls.release();
    mLight_hls.release();
    mSat_hls.release();
    mHue_hsv.release();
    mSat_hsv.release();
    mVal_hsv.release();
    hsv.release();
    hls.release();
    gray.release();
    rgba.release();
    mNew.release();
    mask.release();
    mEdges.release();
    laneZoneMat.release();
  }

  private void getPermissions() {
    if(!hasPermissions(this, PERMISSIONS)){
      Toast.makeText(getApplicationContext(),"Camera permission is needed or \nthis application will not work.", Toast.LENGTH_LONG).show();
      ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
    }
  }

  /* Checks if all the needed permissions are enabled and asks user if not */
  int PERMISSION_ALL = 1;
  String[] PERMISSIONS = {
          android.Manifest.permission.CAMERA,
          android.Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION
  };


  DisplayMetrics displayMetrics;

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    super.onTouchEvent(event);

    float disHeight = displayMetrics.heightPixels;
    float disWidth = displayMetrics.widthPixels;

    float x = event.getX();
    float y = event.getY();
    float z = imgHeight / disHeight;

    float scaledY = y*z;
    if (scaledY > imgHeight*0.25 && scaledY < imgHeight*0.75 && x > 125 && x < disWidth-125)
      top = scaledY;

    return true;
  }

  public static boolean hasPermissions(Context context, String... permissions) {
    if (context != null && permissions != null) {
      for (String permission : permissions) {
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void setFullscreen() {
    this.getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }
}
