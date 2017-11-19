package com.example.nick2.booksummary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton addNewText = (FloatingActionButton) findViewById(R.id.addNewText);
        addNewText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Start the text recognition activity
                Intent intent = new Intent(getBaseContext(), NewTextActivity.class);
                startActivity(intent);

            }
        });

    }

    private void displayTexts() {
        Log.d(TAG, "in displayTexts()");
        SharedPreferences preferences = getBaseContext().
                getSharedPreferences(getString(R.string.string_data_preference_key), Context.MODE_PRIVATE);

        Map<String, ?> userData = preferences.getAll();

        for (String title: userData.keySet()) {
            LinearLayout LLMenu = findViewById(R.id.LinearLayoutMain);

            TextView newText = new TextView(getBaseContext());
            newText.setText(title);
            newText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            newText.setTextColor(Color.BLACK);
            newText.setBackground(getDrawable(R.drawable.my_rounded_text_border));
            Log.d(TAG, "Title for doc: " + title);
            newText.setClickable(true);
            newText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            LLMenu.addView(newText);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        displayTexts();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
