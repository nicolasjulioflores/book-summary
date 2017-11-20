package com.example.nick2.booksummary;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DispSummaryFragment extends DialogFragment {

    private static final String TAG = "DispSummaryFragment";

    //Flag if summary is generated or not
    private Boolean summaryGenerated=false;

    private static String title="Title";
    private static String content;

    //TextView textBox;
    private View thisView;

    static DispSummaryFragment newInstance(int num,String title1, String content1) {
        DispSummaryFragment f = new DispSummaryFragment();

        f.title=title1;
        f.content=content1;


        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int style = DialogFragment.STYLE_NORMAL, theme = 0;

        setStyle(style, theme);
    }

    public void onResume(){
        super.onResume();

        //Display things if not displayed
        if (!summaryGenerated) {
            summaryGenerated=true;

            TextView titlebox = thisView.findViewById(R.id.titleView);
            titlebox.setVisibility(View.VISIBLE);
            titlebox.setText("Summary for " + title);
            TextView textBox = thisView.findViewById(R.id.text);
            textBox.setText(content);

            Button saveButton = thisView.findViewById(R.id.saveButton);
            saveButton.setVisibility(View.VISIBLE);

            ImageView saveButtonContainer = thisView.findViewById(R.id.saveButtonContainer);
            saveButtonContainer.setVisibility(View.VISIBLE);

            saveButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    saveSummary(title, content);
                }
            });

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_disp_summary, container, false);
        thisView=v;

        return v;
    }

    private void saveSummary(String title,String content) {
        SharedPreferences preferences = getActivity().getBaseContext().getSharedPreferences(
                getString(R.string.summary_key), Context.MODE_PRIVATE);

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
            writer.write(content);
            writer.flush();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
            return;
        }

        preferences.edit()
                .putString(title, textFile.getAbsolutePath())
                .apply();

        //Hide save button if saving is successful
        Button saveButton =thisView.findViewById(R.id.saveButton);
        ImageView saveButtonContainer = thisView.findViewById(R.id.saveButtonContainer);
        saveButton.setVisibility(View.INVISIBLE);
        saveButton.setClickable(false);
        saveButtonContainer.setVisibility(View.INVISIBLE);
        Snackbar.make(thisView, "Saved!",
                Snackbar.LENGTH_SHORT)
                .show();
    }


    private File createStringStorageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "STRING_" + timeStamp;
        File storageDir =  new File(getActivity().getBaseContext().getFilesDir(), "strings");

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                Log.e(TAG, "Error making the new storage directory");
            }

        }

        File stringStorage = new File(storageDir, imageFileName + ".txt");


        return stringStorage;
    }

}