package com.aerospike.recommendation.dataimport.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.aerospike.client.AerospikeException;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.Value;

/**
 * The Movie class model the data stored about a movie
 * stored in Aerospike
 * @author peter
 *
 */
public class Movie implements IRecord {
	public static final String RATING = "rating";
	public static final String WATCHED_BY = "watchedBy";
	public static final String TITLE = "title";
	public static final String YEAR_OF_RELEASE = "yearOfRelease";
	public static final String MOVIE_ID = "movieId";
	public static final String SUM_OF_RATINGS = "ratings_sum";
	public static final String RATINGS_COUNT = "ratings_count";
	public static final String PRODUCT_SET = "MOVIE_TITLES";
	String movieId;
	long yearOfRelease;
	String title;
	int sumOfRatings = 0;
	int countOfRatings = 0;
	List<WatchedRated> watchedBy;

	public Movie(JSONObject json){
		super();
		this.fromJSON(json);
	}
	public Movie(String movieId){
		super();
		this.movieId = movieId;
	}
	public Movie(String movieId, String yearString, String title) {
		this(movieId);
		try{
			this.yearOfRelease = Integer.parseInt(yearString);
		}catch (NumberFormatException e){
			this.yearOfRelease = 0;
		}
		this.title = title;
	}
	public Movie(String movieId, int year, String title) {
		this(movieId);
		this.yearOfRelease = year;
		this.title = title;
	}
	@SuppressWarnings("unchecked")
	public Movie(String movieId, Record movieRecord) {
		this(movieId);
		fromRecord(movieRecord);
	}

	public List<WatchedRated> getWatchedBy() {
		return watchedBy;
	}
	public void setWatchedBy(List<WatchedRated> watchedBy) {
		this.watchedBy = watchedBy;
	}
	public String getMovieId() {
		return movieId;
	}
	public long getYearOfRelease() {
		return yearOfRelease;
	}
	public String getTitle() {
		return title;
	}

	public void setYearOfRelease(int yearOfRelease) {
		this.yearOfRelease = yearOfRelease;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getRating() {
		return this.sumOfRatings/this.countOfRatings;
	}
	public void add(WatchedRated watchedRated){
		if (this.watchedBy == null)
			this.watchedBy = new ArrayList<WatchedRated>();
		this.watchedBy.add(watchedRated);
		this.sumOfRatings += watchedRated.getRating();
		this.countOfRatings++;
	}

	@SuppressWarnings("unchecked")
	public void fromRecord(Record record) {
		this.yearOfRelease = (Integer) record.getValue(YEAR_OF_RELEASE);
		this.title = (String) record.getValue(TITLE);
		this.countOfRatings = (Integer) record.getValue(RATINGS_COUNT);
		this.sumOfRatings = (Integer) record.getValue(SUM_OF_RATINGS);
		List<WatchedRated> watched =  (List<WatchedRated>) record.getValue(WATCHED_BY);
		if (watched != null){
			for (WatchedRated mr : this.watchedBy){
				add(mr);
			}
		}
	}
	public List<WatchedRated> trimWatched(int toSize){
		if (this.watchedBy.size() > toSize)
			this.watchedBy = this.watchedBy.subList(0, toSize-1);
		return this.watchedBy;
	}
	/**
	 * Sort the watched movies so that the latest review
	 * is at the start of the list
	 * @return
	 */
	public List<WatchedRated> sortWatched(){
		Collections.sort(this.watchedBy);
		return this.watchedBy;
	}
	/* (non-Javadoc)
	 * @see com.aerospike.recommendation.model.IRecord#asBins()
	 */
	@Override
	public Bin[] asBins(){
		Bin[] bins = new Bin[]{
				new Bin(MOVIE_ID, Value.get(movieId)),
				new Bin(YEAR_OF_RELEASE, Value.get(yearOfRelease)),
				new Bin(TITLE, Value.get(title)),
				new Bin(RATINGS_COUNT, Value.get(countOfRatings)),
				new Bin(SUM_OF_RATINGS, Value.get(sumOfRatings))};
		return bins;
	}
	/* (non-Javadoc)
	 * @see com.aerospike.recommendation.model.IRecord#getKey(java.lang.String, java.lang.String)
	 */
	@Override
	public Key getKey(String namespace, String set) throws AerospikeException{
		return new Key(namespace, set, this.movieId);
	}

	@Override
	public String toString() {
		return ("MOVIE [ID=" + this.movieId 
				+ ", title=" + this.title
				+ ", year=" + this.yearOfRelease 
				+ ", count=" + this.countOfRatings 
				+ ", sum=" + this.sumOfRatings 
				+ "]");
	}
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put(MOVIE_ID, movieId);
		json.put(YEAR_OF_RELEASE, yearOfRelease);
		json.put(TITLE, title);
		json.put(RATINGS_COUNT, countOfRatings);
		json.put(SUM_OF_RATINGS, sumOfRatings);
		if (this.watchedBy != null){
			JSONArray watched = new JSONArray();
			watched.addAll(this.watchedBy);
			json.put(WATCHED_BY, watched);
		}
		return json;
	}
	@SuppressWarnings("unchecked")
	public void fromJSON(JSONObject json){
		this.movieId = (String) json.get(MOVIE_ID);
		this.yearOfRelease =  (Long) json.get(YEAR_OF_RELEASE);
		this.title = (String) json.get(TITLE);
		this.watchedBy = new ArrayList<WatchedRated>();
		JSONArray watched = (JSONArray) json.get(WATCHED_BY);
		this.countOfRatings = 0;
		this.sumOfRatings = 0;
		if (watched != null){
			for (Object obj : watched){
				JSONObject wr = (JSONObject) obj;
				wr.put("key", this.countOfRatings);
				WatchedRated watchedRated = new WatchedRated(wr);
				add(watchedRated);
			}
		}
	}
	public int getCountOfRatings() {
		return countOfRatings;
	}
	
	
}
