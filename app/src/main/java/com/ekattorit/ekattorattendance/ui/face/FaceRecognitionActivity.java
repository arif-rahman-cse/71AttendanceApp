package com.ekattorit.ekattorattendance.ui.face;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import com.ekattorit.ekattorattendance.MainActivity;
import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.SimilarityClassifier;
import com.ekattorit.ekattorattendance.databinding.ActivityFaceRecognitionBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.home.HomeActivity;
import com.ekattorit.ekattorattendance.ui.scan.ScanPreviewActivity;
import com.ekattorit.ekattorattendance.ui.scan.model.RpNewScan;
import com.ekattorit.ekattorattendance.utils.AppConfig;
import com.ekattorit.ekattorattendance.utils.AppProgressBar;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceRecognitionActivity extends AppCompatActivity {
    private static final String TAG = "FaceRecognitionActivity";

    ActivityFaceRecognitionBinding binding;
    Context context = FaceRecognitionActivity.this;
    int OUTPUT_SIZE = 192; //Output size of model
    int inputSize = 112;  //Input size for model
    int[] intValues;
    boolean isModelQuantized = false;
    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    int cam_face = CameraSelector.LENS_FACING_BACK; //Default Back Camera
    boolean start = true, flipX = false;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    FaceDetector detector;
    Interpreter tfLite;
    String modelFile = "mobile_face_net.tflite"; //model name

    float IMAGE_MEAN = 128.0f;
    float IMAGE_STD = 128.0f;
    float[][] embeedings;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    UserCredentialPreference userCredentialPreference;

    String supervisorCurrentAddress;
    double latitude, longitude;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_face_recognition);
        registered = readFromSP(); //Load saved faces from memory when app starts
        userCredentialPreference = UserCredentialPreference.getPrefarences(FaceRecognitionActivity.this);

        supervisorCurrentAddress = getIntent().getStringExtra(AppConfig.ADDRESS);
        latitude = getIntent().getDoubleExtra(AppConfig.LATITUDE, 0.0);
        longitude = getIntent().getDoubleExtra(AppConfig.LONGITUDE, 0.0);


        //Camera Permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        //On-screen switch to toggle between Cameras.
        binding.ivCameraFlip.setOnClickListener(v -> {
            if (cam_face == CameraSelector.LENS_FACING_BACK) {
                cam_face = CameraSelector.LENS_FACING_FRONT;
                flipX = true;
            } else {
                cam_face = CameraSelector.LENS_FACING_BACK;
                flipX = false;
            }
            cameraProvider.unbindAll();
            cameraBind();
        });


        //Load model
        try {
            tfLite = new Interpreter(loadModelFile(FaceRecognitionActivity.this, modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Initialize Face Detector
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .build();
        detector = FaceDetection.getClient(highAccuracyOpts);
        cameraBind();

        binding.btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);

        });


    }


    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //Load Faces from Shared Preferences.Json String to Recognition object
    private HashMap<String, SimilarityClassifier.Recognition> readFromSP() {
        SharedPreferences sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE);
        String defValue = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
        String json = sharedPreferences.getString("map", defValue);
        // System.out.println("Output json"+json.toString());
        TypeToken<HashMap<String, SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>() {
        };
        HashMap<String, SimilarityClassifier.Recognition> retrievedMap = new Gson().fromJson(json, token.getType());
        // System.out.println("Output map"+retrievedMap.toString());

        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet()) {
            float[][] output = new float[1][OUTPUT_SIZE];
            ArrayList arrayList = (ArrayList) entry.getValue().getExtra();
            arrayList = (ArrayList) arrayList.get(0);
            for (int counter = 0; counter < arrayList.size(); counter++) {
                output[0][counter] = ((Double) arrayList.get(counter)).floatValue();
            }
            entry.getValue().setExtra(output);

            //System.out.println("Entry output "+entry.getKey()+" "+entry.getValue().getExtra() );

        }
//        System.out.println("OUTPUT"+ Arrays.deepToString(outut));
        //Toast.makeText(context, "Recognitions Loaded", Toast.LENGTH_SHORT).show();
        return retrievedMap;
    }

    //Bind camera and preview view
    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        //previewView = findViewById(R.id.previewView);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cam_face)
                .build();

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {

                InputImage image = null;


                @SuppressLint("UnsafeExperimentalUsageError")
                // Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)

                Image mediaImage = imageProxy.getImage();

                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                    System.out.println("Rotation " + imageProxy.getImageInfo().getRotationDegrees());
                }

                System.out.println("ANALYSIS");

                //Process acquired image to detect faces
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {

                                                if (faces.size() != 0) {
                                                    Face face = faces.get(0); //Get first face from detected faces
                                                    System.out.println(face);

                                                    //mediaImage to Bitmap
                                                    Bitmap frame_bmp = toBitmap(mediaImage);

                                                    int rot = imageProxy.getImageInfo().getRotationDegrees();

                                                    //Adjust orientation of Face
                                                    Bitmap frame_bmp1 = rotateBitmap(frame_bmp, rot, false, false);


                                                    //Get bounding box of face
                                                    RectF boundingBox = new RectF(face.getBoundingBox());

                                                    //Crop out bounding box from whole Bitmap(image)
                                                    Bitmap cropped_face = getCropBitmapByCPU(frame_bmp1, boundingBox);

                                                    if (flipX)
                                                        cropped_face = rotateBitmap(cropped_face, 0, flipX, false);
                                                    //Scale the acquired Face to 112*112 which is required input for model
                                                    Bitmap scaled = getResizedBitmap(cropped_face, 112, 112);

                                                    if (start)
                                                        recognizeImage(scaled); //Send scaled bitmap to create face embeddings.
                                                    System.out.println(boundingBox);
                                                    try {
                                                        Thread.sleep(10);  //Camera preview refreshed every 10 millisec(adjust as required)
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    if (registered.isEmpty())
                                                        binding.status.setText("Add Face");
                                                    else
                                                        binding.status.setText("No Face Detected!");
                                                    binding.status.setTextColor((getResources().getColor(R.color.yellow)));
                                                }

                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        })
                                .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                    @Override
                                    public void onComplete(@NonNull Task<List<Face>> task) {

                                        imageProxy.close(); //v.important to acquire next frame for analysis
                                    }
                                });


            }
        });


        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);


    }

    public void recognizeImage(final Bitmap bitmap) {
        Log.d(TAG, "recognizeImage: ");
        // set Face to Preview
        //face_preview.setImageBitmap(bitmap);

        //Create ByteBuffer to store normalized image

        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4);

        imgData.order(ByteOrder.nativeOrder());

        intValues = new int[inputSize * inputSize];

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();

        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);

                }
            }
        }
        //imgData is input to our model
        Object[] inputArray = {imgData};

        Map<Integer, Object> outputMap = new HashMap<>();


        embeedings = new float[1][OUTPUT_SIZE]; //output of model will be stored in this variable

        outputMap.put(0, embeedings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap); //Run model


        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "?";

        //Compare new face with saved Faces.
        if (registered.size() > 0) {

            final Pair<String, Float> nearest = findNearest(embeedings[0]);//Find closest matching face

            if (nearest != null) {

                final String name = nearest.first;
                label = name;
                distance = nearest.second;
                if (distance < 1.000f) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                {
                    binding.animationView.setVisibility(View.VISIBLE);
                    binding.status.setText("Face Match");
                    binding.status.setTextColor((getResources().getColor(R.color.green_deep)));
                    insertAttendance(name);
                } else {
                    binding.status.setText("Unknown");
                    binding.status.setTextColor((getResources().getColor(R.color.red)));
                }
                System.out.println("nearest: " + name + " - distance: " + distance);


            }
        }


    }

    private void insertAttendance(String employeeId) {
        Log.d(TAG, "insertAttendance: ");

        Call<RpNewScan> newScanCall = RetrofitClient
                .getInstance()
                .getApi()
                .addNewAttendance(employeeId.trim(), latitude, longitude, supervisorCurrentAddress, userCredentialPreference.getUserId());

        newScanCall.enqueue(new Callback<RpNewScan>() {
            @Override
            public void onResponse(Call<RpNewScan> call, Response<RpNewScan> response) {
                Log.d(TAG, "onResponse: " + response.code());
                //binding.attendancePb.setVisibility(View.GONE);
                //binding.btnCompleteScan.setAlpha(1f);
                //binding.btnCompleteScan.setClickable(true);
                RpNewScan rpNewScan = response.body();
                if (response.code() == 201) {
                    Intent intent = new Intent(FaceRecognitionActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();

                } else if (response.code() == 200) {
                    Intent intent = new Intent(FaceRecognitionActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();


                } else {
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                        Toast.makeText(FaceRecognitionActivity.this, " কিছু একটা সমস্যা হয়েছে " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<RpNewScan> call, Throwable t) {
                //binding.attendancePb.setVisibility(View.GONE);
                //binding.btnCompleteScan.setClickable(true);
                //binding.btnCompleteScan.setAlpha(1f);
                Toast.makeText(FaceRecognitionActivity.this, " কিছু একটা সমস্যা হয়েছে " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
            }
        });
    }

    //Compare Faces by distance between face embeddings
    private Pair<String, Float> findNearest(float[] emb) {

        Pair<String, Float> ret = null;
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {

            final String name = entry.getKey();
            final float[] knownEmb = ((float[][]) entry.getValue().getExtra())[0];

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff * diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
        }

        return ret;

    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    private static Bitmap getCropBitmapByCPU(Bitmap source, RectF cropRectF) {
        Bitmap resultBitmap = Bitmap.createBitmap((int) cropRectF.width(),
                (int) cropRectF.height(), Bitmap.Config.ARGB_8888);
        Canvas cavas = new Canvas(resultBitmap);

        // draw background
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setColor(Color.WHITE);
        cavas.drawRect(//from  w w  w. ja v  a  2s. c  om
                new RectF(0, 0, cropRectF.width(), cropRectF.height()),
                paint);

        Matrix matrix = new Matrix();
        matrix.postTranslate(-cropRectF.left, -cropRectF.top);

        cavas.drawBitmap(source, matrix, paint);

        if (source != null && !source.isRecycled()) {
            source.recycle();
        }

        return resultBitmap;
    }

    private static Bitmap rotateBitmap(
            Bitmap bitmap, int rotationDegrees, boolean flipX, boolean flipY) {
        Matrix matrix = new Matrix();

        // Rotate the image back to straight.
        matrix.postRotate(rotationDegrees);

        // Mirror the image along the X or Y axis.
        matrix.postScale(flipX ? -1.0f : 1.0f, flipY ? -1.0f : 1.0f);
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        // Recycle the old bitmap if it has changed.
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    //IMPORTANT. If conversion not done ,the toBitmap conversion does not work on some devices.
    private static byte[] YUV_420_888toNV21(Image image) {

        int width = image.getWidth();
        int height = image.getHeight();
        int ySize = width * height;
        int uvSize = width * height / 4;

        byte[] nv21 = new byte[ySize + uvSize * 2];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer(); // V

        int rowStride = image.getPlanes()[0].getRowStride();
        assert (image.getPlanes()[0].getPixelStride() == 1);

        int pos = 0;

        if (rowStride == width) { // likely
            yBuffer.get(nv21, 0, ySize);
            pos += ySize;
        } else {
            long yBufferPos = -rowStride; // not an actual position
            for (; pos < ySize; pos += width) {
                yBufferPos += rowStride;
                yBuffer.position((int) yBufferPos);
                yBuffer.get(nv21, pos, width);
            }
        }

        rowStride = image.getPlanes()[2].getRowStride();
        int pixelStride = image.getPlanes()[2].getPixelStride();

        assert (rowStride == image.getPlanes()[1].getRowStride());
        assert (pixelStride == image.getPlanes()[1].getPixelStride());

        if (pixelStride == 2 && rowStride == width && uBuffer.get(0) == vBuffer.get(1)) {
            // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
            byte savePixel = vBuffer.get(1);
            try {
                vBuffer.put(1, (byte) ~savePixel);
                if (uBuffer.get(0) == (byte) ~savePixel) {
                    vBuffer.put(1, savePixel);
                    vBuffer.position(0);
                    uBuffer.position(0);
                    vBuffer.get(nv21, ySize, 1);
                    uBuffer.get(nv21, ySize + 1, uBuffer.remaining());

                    return nv21; // shortcut
                }
            } catch (ReadOnlyBufferException ex) {
                // unfortunately, we cannot check if vBuffer and uBuffer overlap
            }

            // unfortunately, the check failed. We must save U and V pixel by pixel
            vBuffer.put(1, savePixel);
        }

        // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
        // but performance gain would be less significant

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int vuPos = col * pixelStride + row * rowStride;
                nv21[pos++] = vBuffer.get(vuPos);
                nv21[pos++] = uBuffer.get(vuPos);
            }
        }

        return nv21;
    }

    private Bitmap toBitmap(Image image) {

        byte[] nv21 = YUV_420_888toNV21(image);


        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        //System.out.println("bytes"+ Arrays.toString(imageBytes));

        //System.out.println("FORMAT"+image.getFormat());

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}