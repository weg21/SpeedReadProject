package com.andrewgiang.textspritzer.app;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.andrewgiang.textspritzer.lib.DelayStrategy;
import com.andrewgiang.textspritzer.lib.Spritzer;
import com.andrewgiang.textspritzer.lib.SpritzerTextView;
import edu.pitt.cs.mips.hrv_exp.*;
import android.content.Context;
import android.util.Log;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumSet;

import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.sample.CameraSurfacePreview;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.PREVIEW_ROTATION_ANGLE;
import com.qualcomm.snapdragon.sdk.sample.saveFaceData;
import org.ejml.simple.SimpleMatrix;

public class MainActivity extends ActionBarActivity implements Camera.PreviewCallback {

    public static final String TAG = MainActivity.class.getName();
    private static Context context;
    private SpritzerTextView mSpritzerTextView;
    private SeekBar mSeekBarTextSize;
    private SeekBar mSeekBarWpm;
    private ProgressBar mProgressBar;
    SurfaceView mPreview;
    FrameLayout preview;
    DataStorage storage;
    saveFaceData saving;
    OrientationEventListener orientationEventListener;
    int deviceOrientation, presentOrientation;
    FacialProcessing faceProc;
    boolean fpFeatureSupported = false;
    Camera cameraObj;
    boolean faceDetected;
    TextView faceYawText, heartIndicator, numFaceText;
    Button start;
    int surfaceWidth = 0, surfaceHeight = 0;
    int smileValue = 0, leftEyeBlink = 0, rightEyeBlink = 0, faceRollValue = 0, pitch = 0, yaw = 0, horizontalGaze = 0, verticalGaze = 0, displayAngle;
    PointF gazePointValue = null;
    Point leftEyeCoord=null, rightEyeCoord=null;
    Rect faceRect = null;
    View myView, temperatureShape;
    Display display;
    FaceData[] faceArray = null;
    boolean landScapeMode = false;
    ImageView statusImage;
    ShapeDrawable tempBoundingBox = null;
    FrameLayout.LayoutParams paramsShape;


    //HeartBeat variables
    HeartBeatAlgorithm hb_hralgirthm;
    long hb_lastTime, hb_prev_timestamp, hb_interpolated_timestamp, hb_start_timestamp, hb_init_timestamp;
    boolean hb_isDetecting, hb_started, hb_firstTime_flag, hb_covered, focusing, startingFaceControl;
    int hb_lambda, hb_counter;//hb_positiveFrameCounter, hb_negativeFrameCounter,
    SimpleMatrix hb_I, hb_D2, hb_Temp1, hb_Temp2;
    double[] hb_interpolated_value, hb_tempBuffer;
    double hb_prev_value, hb_init_value;
    PointF leftCorner, rightCorner;
    int counting;

    //tracing back for making user more comfortable
    long beginTracingTime;
    int readingTracingIndex;
    long gapTracingTime;
    boolean tracingBool;
    boolean playingSpritzBegins;

    String directory;
    File dir;
    File file;
    StringBuilder texting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        counting=0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        Log.i("HeartBeatAlgorithm", "AppCreated");
        myView = new View(MainActivity.this);
        preview = (FrameLayout) findViewById(R.id.preview);
        tempBoundingBox = new ShapeDrawable(new RectShape());
        tempBoundingBox.getPaint().setColor(Color.YELLOW);//0xFFFFFFFF);
        tempBoundingBox.getPaint().setStyle(Paint.Style.STROKE);
        tempBoundingBox.getPaint().setStrokeWidth(10);
        temperatureShape = new View(context);
        temperatureShape.setBackground(tempBoundingBox);
        paramsShape = new FrameLayout.LayoutParams(50, 25);
        paramsShape.setMargins(0, 0, 0, 0);

        //heartbeat variables ini
        hb_hralgirthm = new HeartBeatAlgorithm();
        hb_lastTime = 0;
        hb_isDetecting = false;
//        hb_positiveFrameCounter = 0;
//        hb_negativeFrameCounter = 0;
        hb_lambda = 10;
        hb_counter = 0;
        double[][] temp = new double[98][100];
        for (int i = 0; i < 98; i++ ){
            temp[i][i] = 1;
            temp[i][i+1] = -2;
            temp[i][i+2] = 1;
        }
        hb_I = SimpleMatrix.identity(100);
        hb_D2 = new SimpleMatrix(temp);
        hb_Temp1 = ((hb_D2.transpose()).mult(hb_D2)).scale(hb_lambda * hb_lambda);
        hb_Temp2 = hb_I.minus((hb_I.plus(hb_Temp1)).invert());
        hb_interpolated_value = new double[50000];
        hb_prev_timestamp = 0;
        hb_interpolated_timestamp = 0;
        hb_prev_value = 0;
        hb_tempBuffer = new double[100];
        hb_started = false;
        hb_init_timestamp = 0;
        hb_init_value = 0;
        hb_firstTime_flag = false;
        leftCorner=new PointF(0,0);
        rightCorner=new PointF(0,0);;
        focusing=false;
        storage = DataStorage.getInstance(context);
        saving = saveFaceData.getInstance(context);
        start = (Button) findViewById(R.id.faceConrol);
        startingFaceControl=false;

        statusImage = (ImageView) findViewById(R.id.statusImage);
        statusImage.setVisibility(View.VISIBLE);
        fpFeatureSupported = FacialProcessing.isFeatureSupported(FacialProcessing.FEATURE_LIST.FEATURE_FACIAL_PROCESSING);

        beginTracingTime=0;
        readingTracingIndex=0;
        gapTracingTime=0;
        tracingBool=false;

        if (fpFeatureSupported && faceProc == null) {
            Toast.makeText(MainActivity.this, "Feature is supported", Toast.LENGTH_SHORT).show();
            faceProc = FacialProcessing.getInstance();  // Calling the Facial Processing Constructor.
            faceProc.setProcessingMode(FP_MODES.FP_MODE_VIDEO);
        } else {
            Toast.makeText(MainActivity.this, "Feature is NOT supported", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            cameraObj = Camera.open(1); // attempt to get a Camera instance
            Toast.makeText(MainActivity.this, "Front Camera is open", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d("TAG", "Camera Does Not exist");// Camera is not available (in use or does not exist)
        }

        mPreview = new CameraSurfacePreview(MainActivity.this, cameraObj, faceProc);
        preview.removeAllViews();
        preview = (FrameLayout) findViewById(R.id.preview);
        preview.addView(mPreview, 0);
        preview.addView(temperatureShape, 1, paramsShape);
        numFaceText = (TextView) findViewById(R.id.faceNum);
        faceYawText = (TextView) findViewById(R.id.yawVal);
        heartIndicator = (TextView) findViewById(R.id.heartIndicater);
        faceDetected=false;
        orientationListener();
        display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
/***********************************************************************/
        //Review the view and set text to be spritzed
        mSpritzerTextView = (SpritzerTextView) findViewById(R.id.spritzTV);
//==========
        directory = context.getExternalFilesDir(null) + "/readingContent/";
        dir = new File(directory);
        file = new File(dir, "spritzReading.txt");
        texting = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                texting.append(line);
                texting.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println("Error when reading the content into spritzer");
        }
//=================
        //mSpritzerTextView.setSpritzText("Galileo teacher and politician who lived in Florence from 1370 to 1450; at that time in the late 14th century, the family's surname shifted from Bonaiuti (or Buonaiuti) to Galilei. Galileo Bonaiuti was buried in the same church, the Basilica of Santa Croce in Florence, where about 200 years later his more famous descendant Galileo Galilei was also buried. When Galileo Galilei was eight, his family moved to Florence, but he was left with Jacopo Borghini for two years.[15] He then was educated in the Camaldolese Monastery at Vallombrosa, 35 km southeast of Florence.[15]");
        mSpritzerTextView.setSpritzText(texting.toString());
        mProgressBar = (ProgressBar) findViewById(R.id.spritz_progress);
        mSpritzerTextView.attachProgressBar(mProgressBar);
        mSpritzerTextView.setWpm(250);
        mSpritzerTextView.setOnClickControlListener(new SpritzerTextView.OnClickControlListener() {

            @Override
            public void onPause() {
                Toast.makeText(MainActivity.this, "Spritzer has been paused", Toast.LENGTH_SHORT).show();
                Log.i("HeartBeatAlgorithm", "AppPause");
                startingFaceControl=false;
            }

            @Override
            public void onPlay() {
                Toast.makeText(MainActivity.this, "Spritzer is playing", Toast.LENGTH_SHORT).show();
                startingFaceControl=true;
                Log.i("HeartBeatAlgorithm", "AppPlay");
            }
        });

        mSpritzerTextView.setOnCompletionListener(new Spritzer.OnCompletionListener() {
            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "Spritzer is finished", Toast.LENGTH_SHORT).show();
                storage.save("Galileo", System.currentTimeMillis());
                saving.save("FaceDataSaving",System.currentTimeMillis());
                Log.i("HeartBeatAlgorithm", "AppFinish");
                startingFaceControl=false;


            }
        });

        setupSeekBars();

    }

    /**
     * This is just shows two seek bars to change wpm and text size
     */
    private void setupSeekBars() {
        mSeekBarTextSize = (SeekBar) findViewById(R.id.seekBarTextSize);
        mSeekBarWpm = (SeekBar) findViewById(R.id.seekBarWpm);
        if (mSeekBarWpm != null && mSeekBarTextSize != null) {
            mSeekBarWpm.setMax(mSpritzerTextView.getWpm() * 2);

            mSeekBarTextSize.setMax((int) mSpritzerTextView.getTextSize() * 2);
            mSeekBarWpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress > 0) {
                        mSpritzerTextView.setWpm(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            mSeekBarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mSpritzerTextView.setTextSize(progress);

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mSeekBarWpm.setProgress(mSpritzerTextView.getWpm());
            mSeekBarTextSize.setProgress((int) mSpritzerTextView.getTextSize());
        }

    }

    public void OnClickStart(View view) {
        startingFaceControl=true;
    }



    private void orientationListener() {
        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceOrientation = orientation;
            }
        };

        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable();
        }

        presentOrientation = 90 * (deviceOrientation / 360) % 360;
    }

    public void setUI(int numFaces, int smileValue, int leftEyeBlink, int rightEyeBlink, int faceRollValue, int faceYawValue, int facePitchValue, PointF gazePointValue, int horizontalGazeAngle, int verticalGazeAngle) {
        numFaceText.setText("Faces: " + numFaces);
        faceYawText.setText("Yaw: " + faceYawValue);
        if(startingFaceControl){
            hb_covered=true;
            //We enable the eye gaze software to tracking the face data.
            if ((numFaces > 0) && gazePointValue.x<0.2&&gazePointValue.x>-0.5&&gazePointValue.y<0.7&&gazePointValue.y>-0.5) {
                statusImage.setImageResource(R.drawable.green);
                focusing=true;
                tracingBool=false;
                if(gapTracingTime>=1000){
                    mSpritzerTextView.setIndex(readingTracingIndex - 2);
                    mSpritzerTextView.play();
                    readingTracingIndex=0;
                    beginTracingTime=System.currentTimeMillis();
                    gapTracingTime=0;
                }
                else{
                    mSpritzerTextView.play();
                    gapTracingTime=0;
                    readingTracingIndex=0;
                    beginTracingTime=System.currentTimeMillis();
                }
            }
            else{
                if(!tracingBool){
                    tracingBool= true;
                    beginTracingTime=System.currentTimeMillis();
                    readingTracingIndex = mSpritzerTextView.getCurrentWordIndex();
                }
                else{
                    gapTracingTime=System.currentTimeMillis()-beginTracingTime;
                    if(gapTracingTime>=1000){
                        statusImage.setImageResource(R.drawable.red);
                        focusing = false;
                        mSpritzerTextView.pause();
                        hb_covered=false;
                    }
                }

            }
            storage.AddWordIndex(System.currentTimeMillis(), mSpritzerTextView.getCurrentWordIndex());
            saving.AddFaceDataSample(numFaces, smileValue, leftEyeBlink, rightEyeBlink, faceRollValue, faceYawValue, facePitchValue, gazePointValue, horizontalGazeAngle, verticalGazeAngle, System.currentTimeMillis());
        }
    }

    @Override
    protected void onPause() {
        Log.i("HeartBeatAlgorithm", "AppPaused");
        super.onPause();
        stopCamera();
    }

    @Override
    protected void onDestroy() {
        Log.i("HeartBeatAlgorithm", "AppClosed");
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        Log.i("HeartBeatAlgorithm", "AppResumed");
        super.onResume();
        if (cameraObj != null) {
            stopCamera();
        }
        startCamera(1);
    }

    /*
     * This is a function to stop the camera preview. Release the appropriate objects for later use.
     */
    public void stopCamera() {
        if (cameraObj != null) {
            cameraObj.stopPreview();
            cameraObj.setPreviewCallback(null);
            preview.removeAllViews();
            preview.addView(mPreview, 0);
            preview.addView(temperatureShape, 1, paramsShape);
            preview.bringChildToFront(temperatureShape);
            cameraObj.release();
            faceProc.release();
            faceProc = null;
        }

        cameraObj = null;
    }

    /*
     * This is a function to start the camera preview. Call the appropriate constructors and objects.
     * @param-cameraIndex: Will specify which camera (front/back) to start.
     */
    public void startCamera(int cameraIndex) {
        Log.e("SUCCESS", "Preview surface valid");
        Log.i("INFO", "Setting preview size: 176, 144");
        if (fpFeatureSupported && faceProc == null) {
            Log.e("TAG", "Feature is supported");
            faceProc = FacialProcessing.getInstance();// Calling the Facial Processing Constructor.
        }

        try {
            cameraObj = Camera.open(cameraIndex);// attempt to get a Camera instance
        } catch (Exception e) {
            Log.d("TAG", "Camera Does Not exist");// Camera is not available (in use or does not exist)
        }

        mPreview = new CameraSurfacePreview(MainActivity.this, cameraObj, faceProc);
        preview.removeAllViews();
        preview = (FrameLayout) findViewById(R.id.preview);
        preview.addView(mPreview,0);
        preview.addView(temperatureShape, 1, paramsShape);
        preview.bringChildToFront(temperatureShape);
        cameraObj.setPreviewCallback(MainActivity.this);
    }

    /*
     * Detecting the face according to the new Snapdragon SDK. Face detection will now take place in this function.
     * 1) Set the Frame
     * 2) Detect the Number of faces.
     * 3) If(numFaces > 0) then do the necessary processing.
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera arg1) {
        presentOrientation = (90 * Math.round(deviceOrientation / 90)) % 360;
//        int dRotation = display.getRotation();
        PREVIEW_ROTATION_ANGLE angleEnum = PREVIEW_ROTATION_ANGLE.ROT_0;
        displayAngle = 90;
        angleEnum = PREVIEW_ROTATION_ANGLE.ROT_90;
//                break;
//        switch (dRotation) {
//            case 0:
//                heartDataText.setText("angle: 0");
//                displayAngle = 90;
//                angleEnum = PREVIEW_ROTATION_ANGLE.ROT_90;
//                break;
//
//            case 1:
//                heartDataText.setText("angle: 1");
//                displayAngle = 0;
//                angleEnum = PREVIEW_ROTATION_ANGLE.ROT_0;
//                break;
//
//            case 2:
//                heartDataText.setText("angle: 2");
//                // This case is never reached.
//                break;
//
//            case 3:
//                heartDataText.setText("angle: 3");
//                displayAngle = 180;
//                angleEnum = PREVIEW_ROTATION_ANGLE.ROT_180;
//                break;
//        }

        if (faceProc == null) {
            faceProc = FacialProcessing.getInstance();
        }

        Parameters params = cameraObj.getParameters();
        Size previewSize = params.getPreviewSize();
        surfaceWidth = mPreview.getWidth();
        surfaceHeight = mPreview.getHeight();

        // Landscape mode - front camera
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            faceProc.setFrame(data, previewSize.width, previewSize.height, true, angleEnum);
            cameraObj.setDisplayOrientation(displayAngle);
            landScapeMode = true;
        }
        // Portrait mode - front camera
        else{
            faceProc.setFrame(data, previewSize.width, previewSize.height, true, angleEnum);
            cameraObj.setDisplayOrientation(displayAngle);
            landScapeMode = false;
        }

        int numFaces = faceProc.getNumFaces();

        if (numFaces == 0) {
            Log.d("TAG", "No Face Detected");
            setUI(0, 0, 0, 0, 0, 0, 0, null, 0, 0);
            //hb_covered=false;
        } else {
            Log.d("TAG", "Face Detected");
            faceArray = faceProc.getFaceData(EnumSet.of(FacialProcessing.FP_DATA.FACE_RECT,
                    FacialProcessing.FP_DATA.FACE_COORDINATES, FacialProcessing.FP_DATA.FACE_CONTOUR,
                    FacialProcessing.FP_DATA.FACE_SMILE, FacialProcessing.FP_DATA.FACE_ORIENTATION,
                    FacialProcessing.FP_DATA.FACE_BLINK, FacialProcessing.FP_DATA.FACE_GAZE, FacialProcessing.FP_DATA.FACE_CONTOUR));
            // faceArray = faceProc.getFaceData(); // Calling getFaceData() alone will give you all facial data except the
            // face
            // contour. Face Contour might be a heavy operation, it is recommended that you use it only when you need it.
            if (faceArray == null) {
                Log.e("TAG", "Face array is null");
                hb_covered=false;
            } else {
                if (faceArray[0].leftEyeObj == null) {
                    Log.e(TAG, "Eye Object NULL");
                } else {
                    Log.e(TAG, "Eye Object not NULL");
                }

                faceProc.normalizeCoordinates(surfaceWidth, surfaceHeight);
                for (int j = 0; j < numFaces; j++) {
                    smileValue = faceArray[j].getSmileValue();
                    leftEyeBlink = faceArray[j].getLeftEyeBlink();
                    rightEyeBlink = faceArray[j].getRightEyeBlink();
                    faceRollValue = faceArray[j].getRoll();
                    gazePointValue = faceArray[j].getEyeGazePoint();
                    pitch = faceArray[j].getPitch();
                    yaw = faceArray[j].getYaw();
                    horizontalGaze = faceArray[j].getEyeHorizontalGazeAngle();
                    verticalGaze = faceArray[j].getEyeVerticalGazeAngle();
                    faceRect = faceArray[j].rect;
                    leftEyeCoord = faceArray[j].leftEye;
                    rightEyeCoord = faceArray[j].rightEye;

                }

                leftCorner.set(faceRect.left + (2*faceRect.centerX()-2*faceRect.left) * 20 / 100, faceRect.top);
                paramsShape.width=(2*faceRect.centerX()-2*faceRect.left)*60/100;
                paramsShape.height=2*faceRect.centerY()-2*faceRect.top;
                paramsShape.setMargins((int) leftCorner.x, (int) leftCorner.y, 20, 20);
                setUI(numFaces, smileValue, leftEyeBlink, rightEyeBlink, faceRollValue, yaw, pitch, gazePointValue,
                        horizontalGaze, verticalGaze);
                //hb_covered=true;
            }
        }
        hb_lastTime = System.nanoTime();
        addSample(data, hb_lastTime / 1000000, leftCorner, paramsShape.width, paramsShape.height);
        returnBuffer(data);
    }

    void returnBuffer(byte buffer[])
    {
        try
        {
            cameraObj.addCallbackBuffer(buffer);
        }
        catch(Exception exception) { }
    }

//From here are the functions from HeartBeat.java
public void addSample(byte buffer[], long timestamp, PointF leftCorner, int pwidth, int pheight)
{
    if (!hb_started) {
        hb_started = true;
        hb_start_timestamp = timestamp;
    }
    if (hb_covered) {
//        hb_positiveFrameCounter++;
//        hb_negativeFrameCounter = 0;
        if (!hb_isDetecting) {
//            if (hb_positiveFrameCounter >= 5) {
                hb_isDetecting = true;
          //      hb_observer.onCovered();
                Log.i("HeartBeatAlgorithm", "onCovered");

                double light = -analyze(buffer, leftCorner, pwidth, pheight);
                hb_init_timestamp = timestamp;
                hb_init_value = light;
                Log.i("HeartBeatAlgorithm", "addSample," + (timestamp - hb_start_timestamp) + "," + (light - hb_init_value));

                hb_interpolated_value[hb_counter] = light;
                hb_counter++;
                hb_prev_timestamp = timestamp;
                hb_interpolated_timestamp = timestamp + 50;
                hb_prev_value = light;
                DataStorage.AddCoverMoment(timestamp, (int) light);
//            } else {
//                Log.i("HeartBeatAlgorithm", "addSample," + (timestamp - hb_start_timestamp) + "," + 0);
//            }
        } else {
            double light = -analyze(buffer, leftCorner, pwidth, pheight);

            while ( (hb_interpolated_timestamp <= timestamp) && (hb_interpolated_timestamp > hb_prev_timestamp)) {
                long deltaT1 = hb_interpolated_timestamp - hb_prev_timestamp;
                long deltaT2 = timestamp - hb_prev_timestamp;
                double deltaH2 = light - hb_prev_value;
                double deltaH1 = (deltaH2 * deltaT1)/deltaT2;
                double new_value = hb_prev_value + deltaH1;
                hb_interpolated_value[hb_counter] = new_value;
                hb_counter++;
                Log.i("Test", ""+new_value);
                if ( hb_counter <= 5) {
                    Log.i("HeartBeatAlgorithm", "addSample," + ((hb_counter-1)*50 + hb_init_timestamp - hb_start_timestamp) + "," + 0);
                } else {
                    if ( hb_counter <= 100 ) {

                        if (!hb_firstTime_flag) {
                            System.arraycopy(hb_interpolated_value, 3, hb_tempBuffer, 0, hb_counter - 3 );
                        }

                        double[][] V = new double[hb_counter - 5][hb_counter - 3];
                        for (int i = 0; i < hb_counter - 5; i++ ){
                            V[i][i] = 1;
                            V[i][i+1] = -2;
                            V[i][i+2] = 1;
                        }
                        SimpleMatrix V2 = SimpleMatrix.identity(hb_counter - 3);
                        SimpleMatrix V3 = new SimpleMatrix(V);
                        SimpleMatrix V4 = ((V3.transpose()).mult(V3)).scale(hb_lambda*hb_lambda);
                        SimpleMatrix V5 = V2.minus((V2.plus(V4)).invert());

                        SimpleMatrix tempMatrix = new SimpleMatrix(hb_counter - 3, 1, true, hb_tempBuffer);
                        SimpleMatrix newMatrix = V5.mult(tempMatrix);
                        Log.i("HeartBeatAlgorithm", "addSample," + ((hb_counter-1)*50 + hb_init_timestamp - hb_start_timestamp) + "," + newMatrix.get(hb_counter-4, 0));

                    }
                }
                // detrending
                if ( hb_counter == 100) {
                    if (!hb_firstTime_flag) {
                        hb_firstTime_flag = true;
                    }

                    hb_hralgirthm.begin_timestamp = 4950 + hb_init_timestamp - hb_start_timestamp;
                    System.arraycopy(hb_interpolated_value, 0, hb_tempBuffer, 0, 100 );

                    SimpleMatrix tempMatrix = new SimpleMatrix(100, 1, true, hb_tempBuffer);
                    SimpleMatrix newMatrix = hb_Temp2.mult(tempMatrix);

                    for (int i = 0; i < newMatrix.numRows(); i++ ) {
                        hb_hralgirthm.addSample(i*50 + hb_init_timestamp - hb_start_timestamp, newMatrix.get(i, 0));
                        DataStorage.AddSample(i * 50 + hb_init_timestamp - hb_start_timestamp, (int)newMatrix.get(i, 0));
                    }
                }

                if ( hb_counter > 100 ) {
                    System.arraycopy(hb_interpolated_value, hb_counter - 100, hb_tempBuffer, 0, 100 );

                    SimpleMatrix tempMatrix = new SimpleMatrix(100, 1, true, hb_tempBuffer);
                    SimpleMatrix newMatrix = hb_Temp2.mult(tempMatrix);

                    hb_hralgirthm.addSample((hb_counter - 1) * 50 + hb_init_timestamp - hb_start_timestamp, newMatrix.get(99, 0));
                    Log.i("HeartBeatAlgorithm", "addSample," + ((hb_counter - 1) * 50 + hb_init_timestamp - hb_start_timestamp) + "," + newMatrix.get(99, 0));
                    DataStorage.AddSample((hb_counter - 1) * 50 + hb_init_timestamp - hb_start_timestamp, (int)newMatrix.get(99, 0));
                }

                hb_interpolated_timestamp = hb_interpolated_timestamp + 50;
            }

            hb_prev_timestamp = timestamp;
            hb_prev_value = light;
        }
    } else {
//        hb_negativeFrameCounter++;
//        hb_positiveFrameCounter = 0;
        if (hb_isDetecting) {
//            if (hb_negativeFrameCounter >= 5) {
                hb_isDetecting = false;
                Log.i("HeartBeatAlgorithm", "onUncovered");
                hb_hralgirthm.restart();
                reset();
                Log.i("HeartBeatAlgorithm", "addSample," + (timestamp - hb_start_timestamp) + "," + 0);
                DataStorage.AddUncoverMoment(timestamp, 0);
//            } else {
//                Log.i("HeartBeatAlgorithm", "addSample," + (timestamp - hb_start_timestamp) + "," + 0);
//            }
        } else {
            Log.i("HeartBeatAlgorithm", "addSample," + (timestamp - hb_start_timestamp) + "," + 0);
        }
    }
}

    public double analyze(byte frame[], PointF leftCorner, int pwidth, int pheight)
    {
        float rX1=leftCorner.x/150*1280;
        float rX2=(leftCorner.x+pwidth)/150*1280;
        float rY1=leftCorner.y/200*720;
        float rY2=(leftCorner.y+pheight)/200*720;
        int pw=1280;
        int ph=720;
        final int frameSize = pw * ph;
        int sumY = 0,sumV = 0, sumU = 0, cursor = 0;
        for (int i = 0, ci = 0; i < ph; ++i, ci += 1) {
            for (int j = 0, cj = 0; j < pw; ++j, cj += 1) {
                int Y = (0xff & ((int) frame[ci * pw + cj]));
                int Cr = (0xff & ((int) frame[frameSize + (ci >> 1) * pw + (cj & ~1) + 0]));
                int Cb = (0xff & ((int) frame[frameSize + (ci >> 1) * pw + (cj & ~1) + 1]));
                Y = Y < 16 ? 16 : Y;
                if((cj>=rX1)&&(cj<=rX2)&&(ci>=rY1)&&(ci<=rY2)){
                    sumY = sumY + Y;
                    sumU = sumU + Cb;
                    sumV = sumV + Cr;
                    cursor++;
                }
            }
        }
        DataStorage.AddYSample(System.nanoTime(), -sumY);
        DataStorage.AddUSample(System.nanoTime(), -sumU);
        DataStorage.AddVSample(System.nanoTime(), -sumV);
        return sumY;
        //--------------------------------------------------------
        //Using Averaging Method

//        int shushu=0;
//        double sum = 0;
//        for( int i=0;i<1280; i++)
//        {
//            for(int j=0;j<720;j++)
//            {
//                if((i>=rX1)&&(i<=rX2)&&(j>=rY1)&&(j<=rY2)){
//                    int pos=1280*j+i;
//                    sum +=frame[pos];
//                    shushu++;
//                }
//            }
//        }
//        double avg=sum/shushu;
    //    heartDataText.setText("=="+counting+"\n Average:"+avg*36864+"\n Shushu:"+shushu+"rX1 rY1:"+rX1/1280+","+rY1/720+"\n rX2 rY2:"+rX2/1280+","+rY2/720);
        //return avg*36864;
        //return avg*4500;
        //Using skipstamp Method
    }

    public void reset() {
        hb_counter = 0;
        hb_interpolated_value = new double[50000];
        hb_prev_timestamp = 0;
        hb_interpolated_timestamp = 0;
        hb_prev_value = 0;
        hb_init_timestamp = 0;
    }



}
