package com.kennetht.annd.popularmovies.MovieContainers;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieObject implements Parcelable {
    //This MovieObject is designed to hold the extra data
    //of a movie taken from a JSONObject
    private String original_Title;
    private String movieID;
    private String poster_path;
    private String overview;
    private Double user_rating;
    private String release_Date;

    public MovieObject() {
        //default constructor
        this.original_Title = null;
        this.poster_path = null;
        this.movieID = null;
        this.overview = null;
        this.user_rating = -1.0; //-1 assumes doesn't exist
        this.release_Date = null;
    }

    public MovieObject(String title, String movieID, String poster, String summary, Double rating, String releaseDate) {
        this.original_Title = title;
        this.movieID = movieID;
        this.poster_path = poster;
        this.overview = summary;
        this.user_rating = rating;
        this.release_Date = releaseDate;
    }

    //Getter methods
    public String getTitle() {return this.original_Title;}

    public String getMovieID() {return this.movieID;}

    public String getPoster() {return this.poster_path;}

    public String getOverview() {return this.overview;}

    public Double getRating() {return this.user_rating;}

    public String getRelease() {return this.release_Date;}


    //Parcelable needed implementations
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags) {
        outParcel.writeString(this.original_Title);
        outParcel.writeString(this.movieID);
        outParcel.writeString(this.poster_path);
        outParcel.writeString(this.overview);
        outParcel.writeDouble(this.user_rating);
        outParcel.writeString(this.release_Date);
    }

    public static final Parcelable.Creator<MovieObject> CREATOR
            = new Parcelable.Creator<MovieObject>() {
        public MovieObject createFromParcel(Parcel in) {
            return new MovieObject(in);
        }

        public MovieObject[] newArray(int size) {
            return new MovieObject[size];
        }
    };

    private MovieObject(Parcel in) {
        original_Title = in.readString();
        movieID = in.readString();
        poster_path = in.readString();
        overview = in.readString();
        user_rating = in.readDouble();
        release_Date = in.readString();
    }
}
