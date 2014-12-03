/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Will do standard preprocessing
 * Language detection, stemming, our own stemming
 */
public class StandardTweetPreprocessor implements ITweetPreprocessor {
    public static final org.slf4j.Logger log = LoggerFactory.getLogger(StandardTweetPreprocessor.class);

    private static final String[] allowedLangs = {"de", "en"};

    private final IExtendableTweetPreprocessor preprocessor;

    public StandardTweetPreprocessor() {
        this.preprocessor = new ExtendableTweetPreprocessor();

        try {
            ITweetFilter lang = new LanguageFilter(Arrays.asList(allowedLangs));
            this.preprocessor.addFilter(lang);

            ITweetPreprocessor naive = new NaiveTweetPreprocessor();
            this.preprocessor.addPreprocessor(naive);

            ITweetPreprocessor english = new CustomEnglishPreprocesor();
            this.preprocessor.addPreprocessor(english);

            ITweetPreprocessor stopwords = new StopwordRemoverPreprocessor();
            this.preprocessor.addPreprocessor(stopwords);

            ITweetPreprocessor snowballStemmer = new SnowballPreprocessor();
            this.preprocessor.addPreprocessor(snowballStemmer);


        } catch (ITweetFilter.FilterException ex) {
            log.error("Failed to create standard tweet preprocessor: {}", ex.getLocalizedMessage());
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
