package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import org.tartarus.snowball.ext.germanStemmer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Thomas
 */
public class SnowballPreprocessor implements ITweetPreprocessor{

    private final Map<String, SnowballStemmer> stemmers = new HashMap<>();
    private final List<String> ignoreKeywords = Arrays.asList(new String[] {"POSITIVESMILE","NEGATIVESMILE"});
    
    @Override
    public Tweet preprocess(Tweet tweet) {
        String lang = tweet.getLanguage();
        if (lang == null) {
            //no stemming possible when there is no language defined
            return tweet;
        }
        SnowballStemmer stemmer = this.getStemmer(lang);

        if (stemmer == null) {
            return tweet;
        }

        String newContent = "";
        for (String part : tweet.getContent().split(" ")) {
            //if a username or a hashtag or a url -> do nothing, could be important
            if (tweet.getHashtags().contains(part) || tweet.getUrls().contains(part) ||
                    tweet.getMentionedUsers().contains(part) || ignoreKeywords.contains(part)) {
                newContent += part + " ";
                continue;
            }

            stemmer.setCurrent(part);
            stemmer.stem();
            newContent += stemmer.getCurrent() + " ";
        }
        newContent = newContent.trim();
        
        tweet.setContent(newContent);
        return tweet;
    }

    @Override
    public List<Tweet> preprocess(List<Tweet> tweets) { 
        for (Tweet t : tweets) {
            this.preprocess(t);
        }
        
        return tweets;
    }
    
    private SnowballStemmer getStemmer(String lang) {
        SnowballStemmer ret = stemmers.get(lang);
        if (ret == null) {
            //create new
            switch(lang) {
                case "en":
                    ret = new englishStemmer();
                    break;
                case "de":
                    ret = new germanStemmer();
                    break;
            }
            
            stemmers.put(lang, ret);
            
        }
        
        return ret;
    }
    
}
