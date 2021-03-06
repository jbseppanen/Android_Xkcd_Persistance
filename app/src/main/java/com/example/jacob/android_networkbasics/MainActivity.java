package com.example.jacob.android_networkbasics;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String RECENT = "Recent";
    public static final String NEXT = "Next";
    public static final String PREVIOUS = "Previous";
    public static final String RANDOM = "Random";
    public static final String SPECIFIC = "Specific";
    public static final String COMIC_KEY = "comic_request_key";

    private float x1, x2;
    static final int MIN_DISTANCE = 150;

    Context context;
    FloatingActionButton fab;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.button_back:
                    new offloadTask().execute(PREVIOUS);
//                    mTextMessage.setText(R.string.navigate_back);
                    return true;
                case R.id.button_random:
                    new offloadTask().execute(RANDOM);
//                    mTextMessage.setText(R.string.get_random);
                    return true;
                case R.id.button_forward:
                    new offloadTask().execute(NEXT);
//                    mTextMessage.setText(R.string.navigate_forward);
                    return true;
                case R.id.button_favorites:
                    Intent intent = new Intent(context, ViewFavoritesActivity.class);
                    startActivity(intent);
            }
            return false;
        }
    };

    //add swipe listener
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    // Left to Right swipe action
                    if (x2 > x1) {
                        new offloadTask().execute(PREVIOUS);
                    }

                    // Right to left swipe action
                    else {
                        new offloadTask().execute(NEXT);
                    }
                } else {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = findViewById(R.id.fab);
        context = this;

        XkcdDbDao.initializeInstance(context);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Toolbar topToolbar = findViewById(R.id.toolbar_top);
        setSupportActionBar(topToolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message;
                Bitmap currentBitmap = ((BitmapDrawable) fab.getDrawable()).getBitmap();
                Bitmap comparisonBitmap = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), android.R.drawable.btn_star_big_on, null)).getBitmap();
                if (currentBitmap == comparisonBitmap) {
                    fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.btn_star_big_off, null));
                    message = "Favorite removed";
                    XkcdDao.setFavorite(false);
                } else {
                    fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.btn_star_big_on, null));
                    message = "Favorite saved";
                    XkcdDao.setFavorite(true);
                }

                Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();


            }
        });


        int comicId = getIntent().getIntExtra(COMIC_KEY, -1);
        if (comicId != -1) {
            new offloadTask().execute(SPECIFIC, String.valueOf(comicId));
        } else {
            new offloadTask().execute(RECENT, null);
        }

    }

    public class offloadTask extends AsyncTask<String, Integer, XkcdComic> {

        @Override
        protected void onPostExecute(XkcdComic comic) {
            if (comic != null) {
                ((TextView) findViewById(R.id.text_title)).setText(comic.getTitle());
                ((ImageView) findViewById(R.id.image_comic)).setImageBitmap(comic.getBitmap());

                if (comic.getXkcdDbInfo().getFavorite() == 0) {
                    fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.btn_star_big_off, null));
                } else {
                    fab.setImageDrawable(ResourcesCompat.getDrawable(getResources(), android.R.drawable.btn_star_big_on, null));
                }

                if (comic.getNum() == 1) {
                    BottomNavigationView navigation = findViewById(R.id.navigation);
                    navigation.getMenu().getItem(0).setEnabled(false);
                } else if (comic.getNum() == XkcdDao.maxComicNumber) {
                    BottomNavigationView navigation = findViewById(R.id.navigation);
                    navigation.getMenu().getItem(1).setEnabled(false);
                } else {
                    BottomNavigationView navigation = findViewById(R.id.navigation);
                    navigation.getMenu().getItem(0).setEnabled(true);
                    navigation.getMenu().getItem(1).setEnabled(true);
                }

            }
        }

        @Override
        protected XkcdComic doInBackground(String... strings) {
            switch (strings[0]) {
                case RECENT:
                    return XkcdDao.getRecentComic();
                case NEXT:
                    return XkcdDao.getNextComic();
                case PREVIOUS:
                    return XkcdDao.getPreviousComic();
                case RANDOM:
                    return XkcdDao.getRandomComic();
                case SPECIFIC:
                    return XkcdDao.getComic(Integer.parseInt(strings[1]));
            }
            return null;
        }
    }


}
