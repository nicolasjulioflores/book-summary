package com.example.nick2.booksummary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class NewTextActivity extends AppCompatActivity {
    private static final String TAG = "NewTextActivity";
    private static final String IMAGE_PATH = "imagePath";


    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 25;

    private String mCurrentImagePath;

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
                String title = TitleBox.getText().toString();
                if (title.equals("")) {
                    setTitleDialog("Missing Title");
                } else if (titleInUse(title)) {
                    setTitleDialog("Title already in use");
                }

                EditText capturedStringBox = findViewById(R.id.capturedString);

                // Save the data in prefs
                SharedPreferences preferences = getBaseContext().getSharedPreferences(
                        getString(R.string.string_data_preference_key), Context.MODE_PRIVATE);

                preferences.edit()
                        .putString(title, capturedStringBox.getText().toString())
                        .apply();

                finish();
            }
        });
    }

    // Tells the user to set the title if their title is missing or already in use
    private void setTitleDialog(String reason) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(reason);
        alertDialog.setMessage("Enter a title for your text:");

        final EditText input = new EditText(this);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                EditText TitleBox = findViewById(R.id.title);
                String newTitle = input.getText().toString();
                TitleBox.setText(newTitle);

            }
        });

        alertDialog.show();


    }

    // Checks the preferences tab to see if the title is already in use
    private boolean titleInUse(String title){
        SharedPreferences preferences = getBaseContext().getSharedPreferences(
                getString(R.string.string_data_preference_key), Context.MODE_PRIVATE);

        for (String key : preferences.getAll().keySet()) {
            if (title.equals(key)) return true;
        }

        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
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
    private void processImage(final Bitmap image) {


        Log.d(TAG, "processImage called");

        new ProcessImageFilesTask().execute(image);

        // Delete the file used
        new File(mCurrentImagePath).delete(); // then delete it

    }

    private class ProcessImageFilesTask extends AsyncTask<Bitmap, String, String> {
        protected String doInBackground(Bitmap... image) {

            Context context = getApplicationContext();

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
                        Toast.makeText(getBaseContext(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
                        Log.w(TAG, getString(R.string.low_storage_error));
                    }

                return null;
            }

            publishProgress("Processing String");

            Frame imageFrame = new Frame.Builder().setBitmap(image[0]).build();

            SparseArray<TextBlock> textBlock = textRecognizer.detect(imageFrame);
            StringBuilder capturedString = new StringBuilder();
            for (int i = 0; i < textBlock.size(); ++i) {
                TextBlock item = textBlock.valueAt(i);
                if (item != null && item.getValue() != null) {
                    //Log.d(TAG, "Text detected! " + item.getValue());

                    capturedString.append(item.getValue());


                }
            }



            return capturedString.toString();
        }

        protected void onProgressUpdate(String... progress) {
            Toast.makeText(getBaseContext(),
                    progress[0],
                    Toast.LENGTH_SHORT ).show();
        }

        protected void onPostExecute(String capturedString) {
            if (capturedString == null) {
                Log.e(TAG, "No text to parse");
                return;
            }

            Log.d(TAG, capturedString.toString());

            // If text was found then set it as the 'string'
            EditText capturedStringBox = findViewById(R.id.capturedString);

            String baseString = capturedStringBox.getText().toString();
            baseString += capturedString;
            capturedStringBox.setText(baseString);
            switchSaveButton();

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir =  new File(getBaseContext().getFilesDir(), "images");

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "Error making the new storage directory");
            }

        }

        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName, // prefix
                    ".jpg", // suffix
                    storageDir // directory
            );
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        return image;
    }


    private void takePicture() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "Error creating photo file");
            }


            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getBaseContext(),
                        "com.example.nick2.booksummary.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                mCurrentImagePath = photoFile.getAbsolutePath();
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Retrieves the image from the camera and calls the function processImage() on it
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Received image");
                Bundle extras = intent.getExtras();
                processImage(BitmapFactory.decodeFile(mCurrentImagePath)); // process the image

            } else {
                Log.d(TAG, "Error in image capture, result code:" + Integer.toString(resultCode));
            }
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
