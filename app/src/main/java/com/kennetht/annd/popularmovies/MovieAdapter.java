package com.kennetht.annd.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends ArrayAdapter<MovieObject> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    Context context;
    int layoutResourceId;
    List<MovieObject> data = null;
    
    public MovieAdapter(Context context, int resource, int layoutResourceId, List<MovieObject> objects) {
        super(context, resource, layoutResourceId, objects);
        this.context= context;
        this.layoutResourceId = layoutResourceId;
        this.data = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view;
        MovieObject movie = data.get(position);
        String posterPath = movie.getPoster();
        final String baseURL = "http://image.tmdb.org/t/p/w185";
        //Log.v(LOG_TAG, "poster Path : " + posterPath);

        if(convertView == null) {
            view = new ImageView(this.context);
        } else {
            view = (ImageView) convertView;
        }

        Uri uri = Uri.parse(baseURL)
                .buildUpon()
                .appendEncodedPath(posterPath)
                .build();

        String test = uri.toString();
        //Log.v(LOG_TAG, "Image URL = " + test);

        view.setAdjustViewBounds(true);
        Picasso.with(this.context).load(uri.toString()).into(view);
        return view;
    }
}
