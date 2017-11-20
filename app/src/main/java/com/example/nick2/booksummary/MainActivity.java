package com.example.nick2.booksummary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Snackbar mDeleteSnackbar;
    private List<CardView> Deck;
    private NavigationView navigationView;

    //Key to select either texts or summaries
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("BookSummary");
        setContentView(R.layout.activity_navigating);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        key=getString(R.string.string_data_preference_key);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addNewText);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Clear tutorial background
                //Set background image
                ScrollView svv= findViewById(R.id.svv);
                svv.setBackgroundColor(Color.WHITE);

                // Start the text recognition activity
                Intent intent = new Intent(getBaseContext(), NewTextActivity.class);
                intent.setAction(getResources().getString(R.string.NEW_TEXT_ACTION));
                startActivityForResult(intent,1);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set background image
        ScrollView svv= findViewById(R.id.svv);
        svv.setBackground(getDrawable(R.drawable.homebackground));

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigating, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete ) {
            if (mDeleteSnackbar == null || !mDeleteSnackbar.isShown()) {

                View parentLayout = findViewById(R.id.lly);
                mDeleteSnackbar = Snackbar.make(parentLayout, R.string.delete_snackbar,
                        Snackbar.LENGTH_INDEFINITE);
                mDeleteSnackbar.show();
            } else if (mDeleteSnackbar != null && mDeleteSnackbar.isShown()) {
                deleteSelectedViews(key);
                mDeleteSnackbar.dismiss();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //For now, just refresh the view
        if(navigationView.getMenu().findItem(R.id.nav_texts).isChecked()){
            setTitle("Saved Texts");
            displayTexts();
        }
        if(navigationView.getMenu().findItem(R.id.nav_summary).isChecked()){
            setTitle("Saved Summaries");
            displaySummaries();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_summary) {
            //Set background image
            ScrollView svv= findViewById(R.id.svv);
            svv.setBackgroundColor(Color.WHITE);
            setTitle("Saved Summaries");

            displaySummaries();
            key=getString(R.string.summary_key);
        } else if (id == R.id.nav_texts) {
            //Set background image
            ScrollView svv= findViewById(R.id.svv);
            svv.setBackgroundColor(Color.WHITE);
            setTitle("Saved Texts");

            displayTexts();
            key=getString(R.string.string_data_preference_key);
        } else if (id == R.id.nav_home) {
            //Set background image
            ScrollView svv= findViewById(R.id.svv);
            svv.setBackgroundColor(Color.WHITE);
            setTitle("BookSummary");

            //Clear views
            LinearLayout LLMenu = findViewById(R.id.lly);
            LLMenu.removeAllViews();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Gathers stored summaries from the file and displays it in the window
    private void displaySummaries() {
        Log.d("APKTAG", "in displaySummaries()");
        SharedPreferences preferences = getBaseContext().
                getSharedPreferences(getString(R.string.summary_key), Context.MODE_PRIVATE);

        final Map<String, ?> userData = preferences.getAll();


        // The layout that will house all the cards
        LinearLayout childLayout = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        childLayout.setLayoutParams(linearParams);
        childLayout.setOrientation(LinearLayout.VERTICAL);
        for (final String title: userData.keySet()) {
            Log.d("APKTAG", "Title for doc: " + title);

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

                    if (mDeleteSnackbar != null && mDeleteSnackbar.isShown()) {
                        handleDeleteClick(v);
                    } else {
                        String savedString = (String) userData.get(title);

                        Intent intent = new Intent(getBaseContext(), NewTextActivity.class);
                        intent.setAction(getResources().getString(R.string.EDIT_TEXT_ACTION));
                        intent.putExtra(getResources().getString(R.string.TITLE), title);
                        intent.putExtra(getResources().getString(R.string.SAVED_STRING), savedString);
                        startActivity(intent);
                    }
                }
            });


            newCard.addView(newText);

            addToDeck(newCard);

            childLayout.addView(newCard);
        }
//        LinearLayout LLMenu = findViewById(R.id.LinearLayoutMain);
        LinearLayout LLMenu = findViewById(R.id.lly);
        LLMenu.removeAllViews();

        LLMenu.addView(childLayout);
    }

    //Gathers stored texts from the file and displays it in the window
    private void displayTexts() {
        Log.d("APKTAG", "in displayTexts()");
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
            Log.d("APKTAG", "Title for doc: " + title);

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

                    if (mDeleteSnackbar != null && mDeleteSnackbar.isShown()) {
                        handleDeleteClick(v);
                    } else {
                        String savedString = (String) userData.get(title);

                        Intent intent = new Intent(getBaseContext(), NewTextActivity.class);
                        intent.setAction(getResources().getString(R.string.EDIT_TEXT_ACTION));
                        intent.putExtra(getResources().getString(R.string.TITLE), title);
                        intent.putExtra(getResources().getString(R.string.SAVED_STRING), savedString);
                        startActivity(intent);
                    }
                }
            });


            newCard.addView(newText);

            addToDeck(newCard);

            childLayout.addView(newCard);
        }
//        LinearLayout LLMenu = findViewById(R.id.LinearLayoutMain);
        LinearLayout LLMenu = findViewById(R.id.lly);

        LLMenu.removeAllViews();

        LLMenu.addView(childLayout);
    }

    private void addToDeck(CardView card) {
        if (Deck == null) {
            Deck = new ArrayList<CardView>();
        }
        Deck.add(card);

        //card.setId(getResources().getInteger(R.integer.RANDOM_BASE) + Deck.indexOf(card));
    }

    private void handleDeleteClick(View v) {

        CardView clickedCard = (CardView)v;

        if (clickedCard.getTag() == null || getResources().getString(R.string.NOT_CLICKED).equals(clickedCard.getTag())) {
            clickedCard.setTag(getResources().getString(R.string.CLICKED));
            clickedCard.setBackgroundColor(getResources().getColor(R.color.complementColor));
        } else {
            clickedCard.setTag(getResources().getString(R.string.NOT_CLICKED));
            clickedCard.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

    }

    private void deleteSelectedViews(String key) {
        if (Deck == null) return;
        for (Iterator<CardView> iterator = Deck.iterator(); iterator.hasNext();) {
            CardView card = iterator.next();
            if (card.getTag() != null && getResources().getString(R.string.CLICKED).equals(card.getTag())) {
                TextView titleBox = (TextView) card.getChildAt(0);

                Log.d("DeleteViews", "Title for view:" + titleBox.getText().toString());

                SharedPreferences preferences = getBaseContext().
                        getSharedPreferences(key, Context.MODE_PRIVATE);

                String title = titleBox.getText().toString();
                String path = preferences.getString(title, null);

                Log.d("Deleteviews", "Path to textfile: " + path);

                File textFile = new File(path);

                // Delete the textFile
                textFile.delete();

                // Clear info from prefs
                preferences.edit().remove(title).apply();

                // Delete the textView
                card.removeView(titleBox);

                // Delete the cardView
                ((ViewManager)card.getParent()).removeView(card);

                // Delete the cardView in the deck
                iterator.remove();


            }

        }

    }


}
