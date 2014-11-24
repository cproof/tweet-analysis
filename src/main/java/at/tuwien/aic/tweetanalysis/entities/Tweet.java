package at.tuwien.aic.tweetanalysis.entities;

import twitter4j.GeoLocation;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas
 */
public class Tweet {
    private long id;
    
    private String content;
    private List<String> hashtags = new LinkedList<>();
    private List<String> urls = new LinkedList<>();
    private List<String> mentionedUsers = new LinkedList<>();
    private String author;
    private String language;
    
    private Date timestamp;
    
    private int retweetCount;
    private int favoriteCount;
    
    private GeoLocation location;

    public Tweet() {
        // only used for JSONTweetProvider
    }

    public Tweet(long id, String content, List<String> hashtags, List<String> urls, List<String> mentionedUsers, String author, String language, Date timestamp, int retweetCount, int favoriteCount, GeoLocation location) {
        this.id = id;
        this.content = content;
        this.hashtags = hashtags;
        this.urls = urls;
        this.mentionedUsers = mentionedUsers;
        this.author = author;
        this.language = language;
        this.timestamp = timestamp;
        this.retweetCount = retweetCount;
        this.favoriteCount = favoriteCount;
        this.location = location;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the hashtags
     */
    public List<String> getHashtags() {
        return hashtags;
    }

    /**
     * @param hashtags the hashtags to set
     */
    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }

    /**
     * @return the urls
     */
    public List<String> getUrls() {
        return urls;
    }

    /**
     * @param urls the urls to set
     */
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the retweetCount
     */
    public int getRetweetCount() {
        return retweetCount;
    }

    /**
     * @param retweetCount the retweetCount to set
     */
    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    /**
     * @return the favoriteCount
     */
    public int getFavoriteCount() {
        return favoriteCount;
    }

    /**
     * @param favoriteCount the favoriteCount to set
     */
    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    /**
     * @return the location
     */
    public GeoLocation getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    /**
     * @return the mentionedUsers
     */
    public List<String> getMentionedUsers() {
        return mentionedUsers;
    }

    /**
     * @param users the mentionedUsers to set
     */
    public void setMentionedUsers(List<String> users) {
        this.mentionedUsers = users;
    }

    /**
     * returns null if no language is set, lower-case ISO 3166
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public class Location {
        private float latitude;
        private float longitude;
    }
    
    @Override
    public String toString() {
        return "Tweet from " + this.getAuthor() + " with content: " + this.getContent();
    }
}
