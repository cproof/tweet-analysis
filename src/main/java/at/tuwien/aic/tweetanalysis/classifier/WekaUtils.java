package at.tuwien.aic.tweetanalysis.classifier;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import at.tuwien.aic.tweetanalysis.preprocessing.NaiveTweetPreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Date: 02.12.2014
 * Time: 17:17
 *
 * @author Stefan Victora
 */
public final class WekaUtils {

    public static final Logger log = LoggerFactory.getLogger(WekaUtils.class);

    private WekaUtils() {
    }

    public static Instances createEmptyInstances(int initialCapacity, boolean withSmilieFeatures) {
        HashMap<String, Double> featureMapTemplate = NaiveTweetPreprocessor.generateFeatureHashMapTemplate();

        /* create sentiment attribute */
        FastVector classAttributeValues = new FastVector(2);
        classAttributeValues.addElement("negative");
        classAttributeValues.addElement("positive");
        Attribute classAttribute = new Attribute("Sentiment", classAttributeValues);

        /* string attribute for tweet content */
        Attribute tweetContentAttribute = new Attribute("Tweet", (FastVector) null);

        /* create attribute list and add as first attribute the tweet content */
        FastVector attributeList = new FastVector(featureMapTemplate.size() + 2);
        attributeList.addElement(tweetContentAttribute);

        /* create attributes from feature map and add them as further attributes to the list */
        for (String featureName : featureMapTemplate.keySet()) {
            if (!withSmilieFeatures && featureName.contains("SMILIES")) {
                continue;
            }
            attributeList.addElement(new Attribute(featureName));
        }

        /* add the class attribute as last attribute */
        attributeList.addElement(classAttribute);

        /* create new container class that holds all instances */
        Instances instances = new Instances("Rel", attributeList, initialCapacity);
        instances.setClassIndex(instances.numAttributes() - 1); // last index is class

        return instances;
    }

    public static Instances addTweetsToUnprocessedInstances(Instances instances, List<? extends Tweet> tweets) {
        if (tweets == null || tweets.isEmpty()) return instances;

        /* add all the tweets to the instances */
        for (Tweet tweet : tweets) {
            addTweetToUnprocessedInstances(instances, tweet);
        }

        return instances;
    }

    public static Instances addTweetToUnprocessedInstances(Instances instances, Tweet tweet) {
        Instance tweetInstance = new Instance(instances.numAttributes());
        tweetInstance.setDataset(instances);

        /* initialise all attributes except the class with 0.0 */
        for (int i = 0; i < instances.numAttributes() - 1; i++) {
            tweetInstance.setValue(i, 0.0);
        }

        /* add tweet content */
        tweetInstance.setValue(0, tweet.getContent());

        /* add feature list values */
        HashMap<String, Double> featureMap = tweet.getFeatureMap();
        for (Map.Entry<String, Double> featureEntry : featureMap.entrySet()) {
            /* get corresponding attribute and set it to the value of feature  */
            Attribute attribute = instances.attribute(featureEntry.getKey());
            if (attribute != null) {
                tweetInstance.setValue(attribute, featureEntry.getValue());
            }
        }

        /* if sentiment is given, add it as well */
        if (tweet.getSentiment() != null) {
            tweetInstance.setClassValue(tweet.getSentiment().toString());
        }

        instances.add(tweetInstance);

        return instances;
    }

    public static void addTweetToInstances(Instances instances, Tweet tweet) {

    }

    public static void writeInstancesToDisk(Instances instances, String filename) {
        try {
            ConverterUtils.DataSink dataSink = new ConverterUtils.DataSink(filename);
            dataSink.write(instances);
        } catch (Exception e) {
            log.error("Failed writing the instances to disk! {}", e.getLocalizedMessage());
        }
    }


}
