/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Will do standard preprocessing
 * Language detection, stemming, our own stemming
 */
public class StandardTweetPreprocessor implements ITweetPreprocessor {
    private static final String[] allowedLangs = {"de","en"};
    
    private final IExtendableTweetPreprocessor preprocessor;
    
    public StandardTweetPreprocessor() {
        this.preprocessor = new ExtendableTweetPreprocessor();
        
        try {    
            ITweetFilter lang = new LanguageFilter(Arrays.asList(allowedLangs));
            this.preprocessor.addFilter(lang);
            
            ITweetPreprocessor naive = new NaiveTweetPreprocessor();
            this.preprocessor.addPreprocessor(naive);
        } catch (ITweetFilter.FilterException ex) {
            Logger.getLogger(StandardTweetPreprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public Tweet preprocess(Tweet tweet) {
        return this.preprocessor.preprocess(tweet);
    }

    @Override
    public List<Tweet> preprocess(List<Tweet> tweets) {
        return this.preprocessor.preprocess(tweets);
    }
    
}
