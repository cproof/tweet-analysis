/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Thomas
 */
public class ExtendableTweetPreprocessor implements IExtendableTweetPreprocessor{

    private final List<ITweetPreprocessor> preprocessors = new LinkedList<>();
    private final List<ITweetFilter> filtersBefore = new LinkedList<>();
    private final List<ITweetFilter> filtersAfter = new LinkedList<>();
    
    @Override
    public void addFilter(ITweetFilter filter) {
        switch(filter.getApplyMode()) {
            case BEFORE:
                this.filtersBefore.add(filter);
                break;
            case AFTER:
                this.filtersAfter.add(filter);
                break;
        }
    }

    @Override
    public void addPreprocessor(ITweetPreprocessor preprocessor) {
        this.preprocessors.add(preprocessor);
    }

    @Override
    public Tweet preprocess(Tweet tweet) {
        System.out.println("Depreciated in ExtendableTweetPreprocessor.java");
        return tweet;
    }

    @Override
    public List<Tweet> preprocess(List<Tweet> tweets) {
        //apply filters
        for (ITweetFilter filter : this.filtersBefore) {
            tweets = filter.applyFilter(tweets);
        }
        
        //do preprocessing
        for (ITweetPreprocessor preprocessor : this.preprocessors) {
            tweets = preprocessor.preprocess(tweets);
        }
        
        //apply filters
        for (ITweetFilter filter : this.filtersAfter) {
            tweets = filter.applyFilter(tweets);
        }
        
        return tweets;
    }
    
}
