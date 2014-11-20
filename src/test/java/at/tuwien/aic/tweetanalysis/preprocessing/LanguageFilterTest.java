/*
 */

package at.tuwien.aic.tweetanalysis.preprocessing;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Thomas
 */
public class LanguageFilterTest {
    private LanguageFilter lf;
    
    
    public LanguageFilterTest() {
    }
    
  
    
    @Before
    public void setUp() throws ITweetFilter.FilterException {
        List<String> languages = Arrays.asList(new String[]{"en","de"});
        this.lf = new LanguageFilter(languages);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of applyFilter method, of class LanguageFilter.
     */
    @Test
    public void testApplyFilter() {
        File f = new File("a");
        System.out.println(f.getAbsoluteFile());
        
        List<Tweet> tweets = new LinkedList<>();
        Tweet t = new Tweet();
        t.setContent("this is some english tweet!");
        tweets.add(t);
        
        t = new Tweet();
        t.setContent("Dies ist ein deutscher Tweet!");
        tweets.add(t);
        
        this.lf.applyFilter(tweets);
        
        assertEquals(tweets.get(0).getLanguage(),"en");
        assertEquals(tweets.get(1).getLanguage(),"de");
    }
    
}
