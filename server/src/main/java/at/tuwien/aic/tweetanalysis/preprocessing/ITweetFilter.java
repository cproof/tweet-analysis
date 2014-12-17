package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import java.util.List;

/**
 *
 * @author Thomas
 */
public interface ITweetFilter {
    public enum applyMode {BEFORE, AFTER};
    
    /**
     * Apply the filter to the list of tweets
     * @param tweets
     * @return 
     */
    public List<Tweet> applyFilter(List<Tweet> tweets);
    
    /**
     * get the mode of the filter
     * @return  BEFORE: if to be applied before preprocessing, AFTER otherwise
     */
    public applyMode getApplyMode();
    
    public class FilterException extends Exception {
        public FilterException(Exception ex) {
            super(ex);
        }
    }
}
