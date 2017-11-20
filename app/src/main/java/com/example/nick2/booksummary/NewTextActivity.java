package com.example.nick2.booksummary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.xml.datatype.Duration;

public class NewTextActivity extends AppCompatActivity {
    private static final String TAG = "NewTextActivity";

    int mStackLevel=0;

    // The layout elements in the activity
    private EditText TitleBox;


    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 25;

    private String mCurrentImagePath;

    private Activity thisActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_text);

        thisActivity=this;

        if (getResources().getString(R.string.NEW_TEXT_ACTION).equals(getIntent().getAction())) {
            Log.d(TAG, "NEW Text Activity started");
        } else {
            Log.d(TAG, "EDIT Text Activity started");
            setDefaults();
        }


        FloatingActionButton startCamera = findViewById(R.id.startCamera);
        startCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check if a camera is there
                if (!checkCameraHardware(getBaseContext())) return;


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

                TitleBox = findViewById(R.id.title);
                String title = TitleBox.getText().toString();
                if (title.equals("")) {
                    setTitleDialog("Missing Title");
                } else if (titleInUse(title)) {
                    setTitleDialog("Title already in use");
                } else {
                    // Save the data in prefs
                    saveDataAndQuit(title);
                }

            }
        });

        FloatingActionButton summarizeText = (FloatingActionButton) findViewById(R.id.summarizeButton);
        summarizeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            EditText capturedStringBox = findViewById(R.id.capturedString);
            String capturedString = capturedStringBox.getText().toString();

            //Make sure Title exists and is allowed
                TitleBox = findViewById(R.id.title);
                String title = TitleBox.getText().toString();
                if (title.equals("")) {
                    setTitleDialog("Missing Title");
                } else {
                    //Attempts to summarize
                    if (capturedString.equals("")){
                        Toast.makeText(thisActivity,"No string to summarize",Toast.LENGTH_SHORT);
                        Log.d("ApkTAG","No string to summarize");
                    } else {
                        //Opens a Dialog window which asks the number of sentences to use in summary
                        //The Dialog window automatically calls summaryDialog(title,capturedString);
                        startDialog(title,capturedString);
                        //summaryDialog(title,capturedString);
                    }
                }




            }

        });
    }

    private void setDefaults() {
        Bundle extras = getIntent().getExtras();

        String title = extras.getString(getResources().getString(R.string.TITLE));

        EditText titleBox = findViewById(R.id.title);
        titleBox.setText(title);

        String textPath = extras.getString(getResources().getString(R.string.SAVED_STRING));

        String text = readStringFromPath(textPath);

        if (text == null) {
            Log.e(TAG, "read null string from file");
            return;
        } else {
            Log.d(TAG, "default text: " + text);
        }

        EditText textBox = findViewById(R.id.capturedString);
        textBox.setText(text);

    }

    private String readStringFromPath(String path) {
        File textFile = new File(path);
        String text;
        try {
            byte[] bytes = new byte[(int)textFile.length()];
            FileInputStream in = new FileInputStream(textFile);
            in.read(bytes);
            text = new String(bytes);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return null;
        }

        return text;
    }



    private void setTitleDialog(String reason) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(reason);
        alertDialog.setMessage("Enter a title for your text:");

        final EditText input = new EditText(this);
        input.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                EditText TitleBox = findViewById(R.id.title);
                String newTitle = input.getText().toString();

                //TODO: If title in use -> show a toast
                if (titleInUse(newTitle)) {

                }

                saveDataAndQuit(newTitle);

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

    private void saveDataAndQuit(String title) {
        SharedPreferences preferences = getBaseContext().getSharedPreferences(
                getString(R.string.string_data_preference_key), Context.MODE_PRIVATE);

        EditText capturedStringBox = findViewById(R.id.capturedString);

        // Store the string in a file
        File textFile;
        try {
            textFile = createStringStorageFile();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return;
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(textFile, true));
            writer.write(capturedStringBox.getText().toString());
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return;
        }

        preferences.edit()
                .putString(title, textFile.getAbsolutePath())
                .apply();

        finish();
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

            Log.d(TAG, capturedString);

            // If text was found then set it as the 'string'
            EditText capturedStringBox = findViewById(R.id.capturedString);

            String baseString = capturedStringBox.getText().toString();
            baseString += "\n"+capturedString+"\n";
            capturedStringBox.setText(baseString);
            capturedStringBox.setBackground(getDrawable(R.drawable.my_rounded_text_border));
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

    private File createStringStorageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "STRING_" + timeStamp;
        File storageDir =  new File(getBaseContext().getFilesDir(), "strings");

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "Error making the new storage directory");
            }

        }

        return new File(storageDir, imageFileName + ".txt");
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

    //Switches Both save button and summarize button
    private void switchSaveButton() {
        EditText capturedStringBox = findViewById(R.id.capturedString);

        FloatingActionButton saveButton = findViewById(R.id.saveButton);
        if (capturedStringBox.getText().toString().equals("")) saveButton.hide();
        else saveButton.show();

        FloatingActionButton summarizeButton = findViewById(R.id.summarizeButton);
        if (capturedStringBox.getText().toString().equals("")) summarizeButton.hide();
        else summarizeButton.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    void summaryDialog(String title, String content, Integer numSentences) {
        mStackLevel++;

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);


        // Create and show the dialog.
        DialogFragment newFragment = DispSummaryFragment.newInstance(mStackLevel,title,content,numSentences);
        newFragment.show(ft, "dialog");
    }

    // Create a dialog box that disappears when the user correctly types in their password
    private void startDialog(final String title, final String content) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Enter Number of Sentences to use in Summary");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for OK button here
                String numSentencesString=input.getText().toString();
                int numSentences;
                try {
                    numSentences = Integer.parseInt(numSentencesString);
                } catch (Exception e){
                    //Set default value to 5
                    numSentences = 5;
                }
                summaryDialog(title,content,numSentences);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Put actions for CANCEL button here, or leave in blank
            }
        });
        alert.show();


    }


}
