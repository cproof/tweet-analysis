/*
 * Does a simple stop word removal based on a list of stop words
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Thomas
 */
public class StopwordRemoverPreprocessor implements ITweetPreprocessor{
    private static final Set<String> stopWordsGeneral = new HashSet(Arrays.asList(new String[] {
            ".", ",", ":", "-", "'", "\"", "?", "!", ",", ";", "tweet", "twitter", "rt", "retweet", "(", ")", "[", "]", "{", "}", "~", "+", "*", "%"
    }));
    
    //stopwords from http://www.ranks.nl/stopwords
    private static final Set<String> stopWordsEnglish = new HashSet(Arrays.asList(new String[] {
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "ought", "our", "ours	ourselves", "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"
    }));
    
    private static final Set<String> stopWordsGerman = new HashSet(Arrays.asList(new String[] {
            "aber", "als", "am", "an", "auch", "auf", "aus", "bei", "bin", "bis", "bist", "da", "dadurch", "daher", "darum", "das", "daß", "dass", "dein", "deine", "dem", "den", "der", "des", "dessen", "deshalb", "die", "dies", "dieser", "dieses", "doch", "dort", "du", "durch", "ein", "eine", "einem", "einen", "einer", "eines", "er", "es", "euer", "eure", "für", "hatte", "hatten", "hattest", "hattet", "hier 	hinter", "ich", "ihr", "ihre", "im", "in", "ist", "ja", "jede", "jedem", "jeden", "jeder", "jedes", "jener", "jenes", "jetzt", "kann", "kannst", "können", "könnt", "machen", "mein", "meine", "mit", "muß", "mußt", "musst", "müssen", "müßt", "nach", "nachdem", "nein", "nicht", "nun", "oder", "seid", "sein", "seine", "sich", "sie", "sind", "soll", "sollen", "sollst", "sollt", "sonst", "soweit", "sowie", "und", "unser 	unsere", "unter", "vom", "von", "vor", "wann", "warum", "was", "weiter", "weitere", "wenn", "wer", "werde", "werden", "werdet", "weshalb", "wie", "wieder", "wieso", "wir", "wird", "wirst", "wo", "woher", "wohin", "zu", "zum", "zur", "über"
            }));
    
    @Override
    public Tweet preprocess(Tweet tweet) {
        this.preprocessTweet(tweet);
        return tweet;
    }

    @Override
    public List<Tweet> preprocess(List<Tweet> tweets) {
        for (Tweet t : tweets) {
            this.preprocessTweet(t);
        }
        return tweets;
    }
    
    
    private void preprocessTweet(Tweet tweet) {
        String content = tweet.getContent();

        //temporary replace urls since they can have stopwords in them
        for (int i=0;i<tweet.getUrls().size();i++) {
            String url = tweet.getUrls().get(i);
            content = content.replace(url, "URL_" + i + "_REPLACED_BY_STOPWORD_REMOVER_PREPROCESSOR");
        }
        
        //replace duplicate spaces
        content = content.replaceAll("[\r\n\t]"," ");
//        content = content.replaceAll("\\s", " ");
//        content = content.replaceAll("\\s{2,}", " ");

        content = content.replaceAll("\\s+", " ");

        //remove symbols
        for(String stopsymbol : stopWordsGeneral) {
            content = content.replace(stopsymbol, " ");
        }
        
        Set<String> stopWords = stopWordsEnglish;
        if (tweet.getLanguage() != null && tweet.getLanguage().equals("de")) {
            stopWords = stopWordsGerman;
        }
        
        //remove stopwords
        String newContent = "";
        for (String word : content.split(" ")) {
            if (!stopWords.contains(word.toLowerCase())) {
                newContent += " " + word;
            }
        }
        content = newContent.trim();
        
        content = content.replaceAll("\\s+", " ");
        
        //substitute back the urls
        for (int i=0;i<tweet.getUrls().size();i++) {
            String url = tweet.getUrls().get(i);
            content = content.replace("URL_" + i + "_REPLACED_BY_STOPWORD_REMOVER_PREPROCESSOR", url);
        }
        
        tweet.setContent(content);
    }
}
