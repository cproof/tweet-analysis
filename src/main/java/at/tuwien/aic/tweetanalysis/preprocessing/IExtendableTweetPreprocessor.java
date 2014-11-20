/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

/**
 *
 * @author Thomas
 */
public interface IExtendableTweetPreprocessor extends ITweetPreprocessor{
    
    /**
     * adds the given filter to the tweet preprocessor
     * @param filter 
     */
    public void addFilter(ITweetFilter filter);
    
    /**
     * add the given preprocessor to be applied in
     * the preprocessing
     * @param preprocessor 
     */
    public void addPreprocessor(ITweetPreprocessor preprocessor);
    
}
