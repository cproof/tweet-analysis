package at.tuwien.aic.tweetanalysis.classifier;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.InputStream;
import java.util.*;

/**
 * The Weka-NaiveBayes-Classifier for Tweets
 *
 * @author Group 1
 */
public class WekaClassifier implements IClassifier {

    public static final Logger log = LoggerFactory.getLogger(WekaClassifier.class);

    private static final String _fileDataset = "/trainingData/manuallyCreatedTraindata/processed-tweets.arff";

    private Instances _trainingDataset = null;
    private Instances _testingDataset = null;
    private Classifier _classifier = null;
    private NGramTokenizer _tokenizer = null;


    public WekaClassifier() throws Exception {
        //set dataset: vectorice (yummy. vector rice :D) with StringToWordVector and select attributes

        _tokenizer = initTheTokenizer(); //init the tokenizer

        Instances dataSet = loadInstancesFromFile(_fileDataset);
        _trainingDataset = vectorised(dataSet);
        _trainingDataset = attributeSelectionFilter(_trainingDataset);
        _testingDataset = null;

        // In this case we use SMO Classifier. (for Support Vector Machines)
        SMO smo = new SMO();
        smo.setBuildLogisticModels(true); // classify instance returns the probability
        _classifier = trainAClassifier(smo, _trainingDataset);

        testClassifierWithFold(_classifier, _trainingDataset);
    }

    public WekaClassifier(String trainingDataset, String testingDataset) throws Exception {
        _tokenizer = initTheTokenizer(); //init the tokenizer

        _trainingDataset = vectorised(loadInstancesFromFile(trainingDataset));
        _trainingDataset = attributeSelectionFilter(_trainingDataset);

        _testingDataset = vectorised(loadInstancesFromFile(testingDataset));
        _testingDataset = attributeSelectionFilter(_testingDataset);

        // In this case we use SMO Classifier. (for Support Vector Machines)
        SMO smo = new SMO();
        smo.setBuildLogisticModels(true); // classify instance returns the probability
        _classifier = trainAClassifier(smo, _trainingDataset);

        testClassifier(_classifier, _trainingDataset, _testingDataset);
    }

    public WekaClassifier(InputStream model, InputStream trainingInstances) {
        loadModel(model, trainingInstances);

        _tokenizer = initTheTokenizer();
        _testingDataset = null; // not used
    }

    private Instances loadInstancesFromFile(String resourceFileName) throws Exception {
        return loadInstancesFromStream(WekaClassifier.class.getResourceAsStream(resourceFileName));
    }

    private Instances loadInstancesFromStream(InputStream instanceStream) throws Exception {
        ConverterUtils.DataSource trainingDataSource =
                new ConverterUtils.DataSource(instanceStream);

        Instances dataSet = trainingDataSource.getDataSet();
        dataSet.setClassIndex(dataSet.numAttributes() -1);
        return dataSet;
    }

    /**
     * Train the Classifier with a training data set
     *
     * @param classifier the used classifier
     * @param trainingdataset the training Dataset
     * @return the trained Classifier
    */
    private Classifier trainAClassifier(Classifier classifier, Instances trainingdataset) {
        try {
            classifier.buildClassifier(trainingdataset);
        } catch (Exception ex) {
            System.err.println("Exception training classifier: " + ex.getMessage());
        }
        return classifier;
    }

    /**
     * Test the Classifier and print out some statistics
     *
     * @param classifier The Classifier
     * @param trainingData the training Dataset
     * @param testingData the testing Dataset
     */
    private void testClassifier(Classifier classifier, Instances trainingData, Instances testingData) {
        try {
            Evaluation eval = new Evaluation(trainingData);

            eval.evaluateModel(classifier, testingData);
            log.info(eval.toSummaryString("\nResults\n======\n", false));
            log.info(eval.toMatrixString());

        } catch (Exception ex) {
            System.err.println("Exception testing the Classifier: " + ex.getMessage());
        }
    }

    /**
     * Test the Classifier with folding the training_data and print out some statistics
     *
     * @param classifier The Classifier
     * @param trainingData the training Dataset
     */
    private void testClassifierWithFold(Classifier classifier, Instances trainingData) {
        try {
            Evaluation eval = new Evaluation(trainingData);
            Random rand = new Random(1); // using seed = 1
            int folds = 10;
            eval.crossValidateModel(classifier, trainingData, folds, rand);

            log.info(eval.toSummaryString("\nResults\n======\n", false));
            log.info(eval.toMatrixString());

        } catch (Exception ex) {
            System.err.println("Exception testing the Classifier: " + ex.getMessage());
        }
    }

    private NGramTokenizer initTheTokenizer() {
        // Set the tokenizer
        NGramTokenizer tokenizer = new NGramTokenizer();
        tokenizer.setNGramMinSize(1);
        tokenizer.setNGramMaxSize(2);
        tokenizer.setDelimiters(" \n\t.,;:'\"()?!");
        return tokenizer;
    }

    /**
     * Vecotrizes the Tweet for the Analysis
     *
     * @param input_instances The Instances to Vectorize
     * @return the Vectorized Instances
     */
    private Instances vectorised(Instances input_instances) {

        // Set the filter
        StringToWordVector filter = new StringToWordVector();
        try {
            filter.setInputFormat(input_instances);
            filter.setAttributeIndices("first");
            filter.setTokenizer(_tokenizer);
            filter.setWordsToKeep(10000);
            filter.setMinTermFreq(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances outputInstances = null;
        try {
            outputInstances = Filter.useFilter(input_instances, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputInstances;
    }

    /**
     * The Attribute Selection Filter for the Analysis
     *
     * @param trainingDataset The Instances to Vectorize
     * @return the Selected Attributes Instances
     */
    private Instances attributeSelectionFilter(Instances trainingDataset) {
        AttributeSelection attributeSelection = new AttributeSelection();
        attributeSelection.setEvaluator(new ChiSquaredAttributeEval());
        Ranker search = new Ranker();
        search.setThreshold(0.0);
        attributeSelection.setSearch(search);

        try {
            attributeSelection.setInputFormat(trainingDataset);
            trainingDataset = Filter.useFilter(trainingDataset, attributeSelection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trainingDataset;
    }

    /**
     * Takes a preprocessed String and matches into the given trainingsdata-space
     *
     * @param tweet the tweet containing zhe String so Tokenize and match into the trainingsdata-space
     * @param instances_dataset The instances-space to match into
     * @return the String in the given instances-space
     */
    private Instances mergeStringTweetIntoDataset(Tweet tweet, Instances instances_dataset) throws Exception {

        // use the tokenizer (HAS TO BE THE SAME AS THE ONE FOR THE MODEL)
        _tokenizer.tokenize(tweet.getContent());

        //create instance with same attributes but only one empty instance
        Instances new_instances = new Instances(instances_dataset,1);
        Instance inst = new Instance(new_instances.numAttributes());
        inst.setDataset(new_instances);

        //set the values to a zero value without the nominal i.e. the last value
        for (int i = 0; i < new_instances.numAttributes() - 1; i++) {
            inst.setValue(i, 0);
        }
        //run throu the new string an match to the attributes: set the presence to 1
        while(_tokenizer.hasMoreElements()) {
            String tmp = _tokenizer.nextElement().toString();
            //System.out.println("Token: " + tmp);
            if(new_instances.attribute(tmp)!=null) // if the instance exists
                inst.setValue(new_instances.attribute(tmp), 1);
        }

        /* also check feature model */
        HashMap<String, Double> featureMap = tweet.getFeatureMap();
        for (Map.Entry<String, Double> featureEntry : featureMap.entrySet()) {
            /* get corresponding attribute and set it to the value of feature  */
            Attribute attribute = new_instances.attribute(featureEntry.getKey());
            if (attribute != null) {
                inst.setValue(attribute, featureEntry.getValue());
            }
        }
        new_instances.add(inst);

        // to sparse instance (saves dataspace)
        NonSparseToSparse toSparseeee = new NonSparseToSparse();
        toSparseeee.setInputFormat(new_instances);

        return Filter.useFilter(new_instances, toSparseeee);
    }

    @Override
    public void testClassifierAgainstPreprocessedTweets(List<Tweet> tweets) {
        try {
            Evaluation eval = new Evaluation(_trainingDataset);

            //create instance with same attributes but only one empty instance
            Instances new_instances = new Instances(_trainingDataset,tweets.size());

            for(Tweet t : tweets) {
                _tokenizer.tokenize(t.getContent());

                Instance inst = new Instance(new_instances.numAttributes());
                inst.setDataset(new_instances);

                //set the values to a zero value without the nominal i.e. the last value
                for (int i = 0; i < new_instances.numAttributes() - 1; i++) {
                    inst.setValue(i, 0);
                }

                //run throu the new string an match to the attributes: set the presence to 1
                while(_tokenizer.hasMoreElements()) {
                    String tmp = _tokenizer.nextElement().toString();
                    if(new_instances.attribute(tmp)!=null) // if the instance exists
                        inst.setValue(new_instances.attribute(tmp), 1);
                }

                /* also check feature model */
                HashMap<String, Double> featureMap = t.getFeatureMap();
                for (Map.Entry<String, Double> featureEntry : featureMap.entrySet()) {
                    /* get corresponding attribute and set it to the value of feature  */
                    Attribute attribute = new_instances.attribute(featureEntry.getKey());
                    if (attribute != null) {
                        inst.setValue(attribute, featureEntry.getValue());
                    }
                }
                /* set sentiment */
                inst.setClassValue(t.getSentiment().toString());

                new_instances.add(inst);

            }

            // to sparse instance (saves dataspace)
            NonSparseToSparse toSparseeee = new NonSparseToSparse();
            toSparseeee.setInputFormat(new_instances);

            Instances sparseInstances =  Filter.useFilter(new_instances, toSparseeee);

            eval.evaluateModel(_classifier, sparseInstances);
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
            System.out.println(eval.toMatrixString());

        } catch (Exception ex) {
            System.err.println("Exception testing the Classifier: " + ex.getMessage());
        }
    }

    @Override
    public double[] classifyTweet(Tweet tweet) {

        /*
        //create the dataset manually (same as the input or the model)
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("negative");
        fvClassVal.addElement("positive");
        Attribute classAttribute = new Attribute("Sentiment", fvClassVal);

        Attribute stringAttribute = new Attribute("Tweet", (FastVector) null);

        FastVector fvWekaAttributes = new FastVector(2);
        fvWekaAttributes.addElement(stringAttribute);
        fvWekaAttributes.addElement(classAttribute);

        // Create an empty instaces set
        Instances inst = new Instances("Rel", fvWekaAttributes, 1);
        // Set class index
        inst.setClassIndex(1);

        //create a new instance and add to instances
        Instance iExample = new Instance(2);
        iExample.setDataset(inst);
        iExample.setValue(0, tweet.getContent());
        inst.add(iExample);

        Instances new_inst = vectorised(inst);
        */

        double[] fDistribution = new double[] {0.0,0.0};
        try {
            Instances new_inst = mergeStringTweetIntoDataset(tweet, _trainingDataset);
            fDistribution = _classifier.distributionForInstance(new_inst.firstInstance());
        } catch (Exception ex) {
            System.err.println("Exception using classifier: " + ex.getMessage());
        }

        return fDistribution;
    }

    @Override
    public void saveModel(String modelName) {
        try {
            weka.core.SerializationHelper.write(modelName, _classifier);
        } catch (Exception ex) {
            System.err.println("Exception saving the Model: " + ex.getMessage());
        }
    }

    @Override
    public void loadModel(String modelName, String instancesFileName) {
        try {
            _classifier = (Classifier) weka.core.SerializationHelper.read(modelName);
            _trainingDataset = loadInstancesFromFile(instancesFileName);
        } catch (Exception ex) {
            System.err.println("Exception loading the Model: " + ex.getMessage());
        }
    }

    @Override
    public void loadModel(InputStream modelStream, InputStream instancesStream) {
        try {
            _classifier = (Classifier) weka.core.SerializationHelper.read(modelStream);
            _trainingDataset = loadInstancesFromStream(instancesStream);
        } catch (Exception ex) {
            System.err.println("Exception loading the Model: " + ex.getMessage());
        }
    }

}
