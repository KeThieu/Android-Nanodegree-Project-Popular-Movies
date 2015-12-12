package com.kennetht.annd.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.kennetht.annd.popularmovies.MovieContainers.MovieObject;

public class MainActivity extends AppCompatActivity implements Grid_Fragment.CallbackMain {

    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private static String mSortType;
    private static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSortType = preferences.getString(getString(R.string.sortByKey), getString(R.string.popularity_desc));

        if(findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
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
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelect(MovieObject m) {
        if(mTwoPane) {
            //Tablet

            Bundle args = new Bundle();
            args.putParcelable(MovieDetailActivityFragment.ARG_MOVIE_TAG, m);

            MovieDetailActivityFragment mf = new MovieDetailActivityFragment();
            mf.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, mf, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            //Phone, just start an intent
            Intent intent = new Intent(this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailActivityFragment.ARG_MOVIE_TAG, m);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String curSortType = preferences.getString(getString(R.string.sortByKey), getString(R.string.popularity_desc));

        if(curSortType != null && !curSortType.equals(mSortType)) {
            //sort type has changed, call the Grid_Fragment to update the movieData
            Grid_Fragment gf = (Grid_Fragment) getSupportFragmentManager().findFragmentById(R.id.fragment_grid);
            if(gf != null) {
                gf.updateMovies();
            }
            mSortType = curSortType;
        }
    }
}
