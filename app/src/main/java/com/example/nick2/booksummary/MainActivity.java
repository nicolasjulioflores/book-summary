package com.example.nick2.booksummary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
                intent.setAction(getResources().getString(R.string.NEW_TEXT_ACTION));
                startActivity(intent);

            }
        });

    }

    private void displayTexts() {
        Log.d(TAG, "in displayTexts()");
        SharedPreferences preferences = getBaseContext().
                getSharedPreferences(getString(R.string.string_data_preference_key), Context.MODE_PRIVATE);

        final Map<String, ?> userData = preferences.getAll();


        // The layout that will house all the cards
        LinearLayout childLayout = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        childLayout.setLayoutParams(linearParams);
        childLayout.setOrientation(LinearLayout.VERTICAL);
        for (final String title: userData.keySet()) {
            Log.d(TAG, "Title for doc: " + title);

            // Set the layout for the new CardViews to be added
            CardView newCard = new CardView(this);

            newCard.setCardElevation(4);
            newCard.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            newCard.setContentPadding(padding, padding, padding, padding);

            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    height
            );
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            params.setMargins(margin, margin, margin, margin);

            newCard.setLayoutParams(params);
            newCard.setClickable(true);

            // Now add the title to the card
            TextView newText = new TextView(getBaseContext());
            newText.setText(title);
            newText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f);
            newText.setTextColor(Color.WHITE);
            newText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));


            newCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String savedString = (String) userData.get(title);
                    Log.d(TAG, "PATH TO STRING:" + savedString);

                    Intent intent = new Intent(getBaseContext(), NewTextActivity.class);
                    intent.setAction(getResources().getString(R.string.EDIT_TEXT_ACTION));
                    intent.putExtra(getResources().getString(R.string.TITLE), title);
                    intent.putExtra(getResources().getString(R.string.SAVED_STRING), savedString);
                    startActivity(intent);
                }
            });



//            // Add a delete button the card
//            Button deleteButton = new Button(getBaseContext());
//            RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//
//            rel_btn.leftMargin = 9;
//            rel_btn.topMargin = 9;
//            rel_btn.width = 10;
//            rel_btn.height = 10;
//            newCard.addView(deleteButton);

            newCard.addView(newText);


            childLayout.addView(newCard);
        }
        LinearLayout LLMenu = findViewById(R.id.LinearLayoutMain);
        LLMenu.addView(childLayout);
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
