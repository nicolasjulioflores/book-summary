package com.example.nick2.booksummary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class NewTextActivity extends AppCompatActivity {
    private static final String TAG = "NewTextActivity";



    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 25;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_text);

        FloatingActionButton startCamera = findViewById(R.id.startCamera);
        startCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if a camera is there
                if (!checkCameraHardware(getBaseContext())) return;

                // Set good defaults for capturing text.
                boolean autoFocus = true;
                boolean useFlash = false;

                // Request Camera Permissions
                // Check for the camera permission before accessing the camera.  If the
                // permission is not granted yet, request permission.
                // If has camera permissions, then start camera
                int rc = ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA);
                if (rc == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    requestCameraPermission();
                }


            }
        });


        switchSaveButton();
        FloatingActionButton saveText = (FloatingActionButton) findViewById(R.id.saveButton);
        saveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Save Button hit");

                EditText TitleBox = findViewById(R.id.title);

                if (TitleBox.getText().toString() == null) {
                    // TODO:
                    // Show a snackbar or something to let them fill in their title
                    // They then should be able to proceed with saving
                }

                // TODO:
                // Save it and add it as a new view to MainActivity
                // Once saved, this activity should finish and start a new editSavedTextActivity()

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        switchSaveButton();
    }


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void processImage(Bitmap image) {
        Context context = getApplicationContext();

        Log.d(TAG, "processImage called");

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // Don't think i have to set a processor
        // textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }

            return;
        }

        Frame imageFrame = new Frame.Builder().setBitmap(image).build();

        SparseArray<TextBlock> textBlock = textRecognizer.detect(imageFrame);
        StringBuilder capturedString = new StringBuilder();
        for (int i = 0; i < textBlock.size(); ++i) {
            TextBlock item = textBlock.valueAt(i);
            if (item != null && item.getValue() != null) {
                //Log.d(TAG, "Text detected! " + item.getValue());

                capturedString.append(item.getValue());


            }
        }
        Log.d(TAG, capturedString.toString());

        // If text was found then set it as the 'string'
        EditText capturedStringBox = findViewById(R.id.capturedString);

        String baseString = capturedStringBox.getText().toString();
        baseString += capturedString.toString();
        capturedStringBox.setText(baseString);


    }

    private void takePicture() {
        // Calls the camera to 'Take a picture'; Request is received in onActivity Result
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Retrieves the image from the camera and calls the function processImage() on it
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d(TAG, "Received image");
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            processImage(image);
        }

    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        View parentLayout = findViewById(android.R.id.content);
        Snackbar.make(parentLayout, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");

            // we have permission, so create take the picture
            takePicture();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void switchSaveButton() {
        EditText capturedStringBox = findViewById(R.id.capturedString);
        FloatingActionButton saveButton = findViewById(R.id.saveButton);
        if (capturedStringBox.getText().toString().equals("")) saveButton.hide();
        else saveButton.show();
    }


}
