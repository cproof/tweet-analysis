package at.tuwien.aic.tweetanalysis.classifier;

import at.tuwien.aic.tweetanalysis.entities.Tweet;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.CSVLoader;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.*;
import java.util.Random;

/**
 * The Weka-NaiveBayes-Classifier for Tweets
 *
 * @author Group 1
 */
public class WekaClassifier implements IClassifier {

//    private static final String _fileTestingDataset = "manualCreatedTestingData.csv";
//    private static final String _fileTestingDataset = "testingData.csv";
//    private static final String _fileTestingDataset = "src/main/resources/testingVectorised.arff";
//    private static final String _fileTrainingDataset = "manualCreatedTrainingData.csv";
//    private static final String _fileTrainingDataset = "trainingData.csv";
//    private static final String _fileTrainingDataset = "src/main/resources/trainingVectorised.arff";

    private static final String _fileDataset = "src/main/resources/processed-tweets.arff";

    private Instances _trainingDataset = null;
    private Instances _testingDataset = null;
    private Classifier _classifier = null;


    public WekaClassifier() {

        Instances dataset = getARFFDataset(_fileDataset);

        //set dataset
        _trainingDataset = vectorised(dataset);
        _trainingDataset = attributeSelectionFilter(_trainingDataset);
        _testingDataset = null;

        //In this case we use NaiveBayes Classifier.
        _classifier = (Classifier) new NaiveBayes();
        _classifier = trainAClassifier(_classifier,_trainingDataset);

        testClassifierWithFold(_classifier,_trainingDataset);
    }

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

    public WekaClassifier(String trainingDataset, String testingDataset) {
        _trainingDataset = getCSVDataset(trainingDataset);
        _testingDataset = getCSVDataset(testingDataset);

        //In this case we use NaiveBayes Classifier.
        _classifier = (Classifier) new NaiveBayes();
        _classifier = trainAClassifier(_classifier,_trainingDataset);

        testClassifier(_classifier,_trainingDataset,_testingDataset);
    }

    public WekaClassifier(InputStream model) {
        loadModel(model);
    }

    /**
    * Read in a new Dataset Instance from a CSV-File
    * @param INPUT_FILE_DATASET Path to the CSV-File
    * @return the Dataset Instances
    */
    private Instances getCSVDataset(final String INPUT_FILE_DATASET) {

        CSVLoader trainingLoader = new CSVLoader();
        Instances dataset = null;

        try {

            trainingLoader.setSource(new File(INPUT_FILE_DATASET));
            dataset = trainingLoader.getDataSet();
            dataset.setClassIndex(0);
        } catch (IOException ex) {
            System.err.println("Exception in getCSVDataset: " + ex.getMessage());
        }

//        dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }

    /**
     * Read in a new Dataset Instance from a ARFF-File
     * @param INPUT_FILE_DATASET Path to the ARFF-File
     * @return the Dataset Instances
     */
    private Instances getARFFDataset(final String INPUT_FILE_DATASET) {

        Instances dataset = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(INPUT_FILE_DATASET));
            ArffLoader.ArffReader arff = new ArffLoader.ArffReader(reader);
            dataset = arff.getData();
            dataset.setClassIndex(1);
        } catch (IOException ex) {
            System.err.println("Exception in getARFFDataset: " + ex.getMessage());
        }

        return dataset;
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
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
            System.out.println(eval.toMatrixString());

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

            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
            System.out.println(eval.toMatrixString());

        } catch (Exception ex) {
            System.err.println("Exception testing the Classifier: " + ex.getMessage());
        }
    }

    /**
     * Vecotrizes the Tweet for the Analysis
     *
     * @param input_instances The Instances to Vectorize
     * @return the Vectorized Instances
     */
    private Instances vectorised(Instances input_instances) {

        // Set the tokenizer
//        WordTokenizer tokenizer = new WordTokenizer();
//        tokenizer.setDelimiters(" ");

        // this tokenizer would be better
        NGramTokenizer tokenizer = new NGramTokenizer();
        tokenizer.setNGramMinSize(1);
        tokenizer.setNGramMaxSize(2);
        tokenizer.setDelimiters(" \n\t.,;:'\"()?!");

        // Set the filter
        StringToWordVector filter = new StringToWordVector();
        try {
            filter.setInputFormat(input_instances);
            filter.setAttributeIndices("first");
//            filter.setAttributeIndices("last");
            filter.setTokenizer(tokenizer);
            filter.setWordsToKeep(10000);
            filter.setMinTermFreq(1);
//            filter.setDoNotOperateOnPerClassBasis(true);
            //filter.setLowerCaseTokens(true);
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

    @Override
    public double[] classifyTweet(Tweet tweet) {

        //create the dataset manually
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("negative");
        fvClassVal.addElement("positive");
//        Attribute stringAttribute = new Attribute("Tweet", (FastVector) null);
//        Attribute classAttribute = new Attribute("Sentiment", fvClassVal);
        Attribute classAttribute = new Attribute("Sentiment", fvClassVal);

        Attribute stringAttribute = new Attribute("Tweet", (FastVector) null);

        FastVector fvWekaAttributes = new FastVector(2);
        fvWekaAttributes.addElement(stringAttribute);
        fvWekaAttributes.addElement(classAttribute);
//        fvWekaAttributes.addElement(classAttribute);
//        fvWekaAttributes.addElement(stringAttribute);

        // Create an empty instaces set
        Instances inst = new Instances("Rel", fvWekaAttributes, 1);
        // Set class index
        inst.setClassIndex(1);
//        inst.setClassIndex(0);

        //create a new instance and add to instances
        Instance iExample = new Instance(2);
        iExample.setDataset(inst);
        iExample.setValue(0, tweet.getContent());
//        iExample.setValue(1, tweet.getContent());
        inst.add(iExample);

        //System.out.println("instance before vectorisation: " + inst.firstInstance());
        Instances new_inst = vectorised(inst);
        //System.out.println("instance after vectorisation: " + new_inst.firstInstance());

        double[] fDistribution = new double[] {0.0,0.0};
        try {
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
    public void loadModel(String modelName) {
        try {
            this._classifier = (NaiveBayes) weka.core.SerializationHelper.read(modelName);
        } catch (Exception ex) {
            System.err.println("Exception loading the Model: " + ex.getMessage());
        }
    }

    @Override
    public void loadModel(InputStream modelStream) {
        try {
            this._classifier = (Classifier) weka.core.SerializationHelper.read(modelStream);
        } catch (Exception ex) {
            System.err.println("Exception loading the Model: " + ex.getMessage());
        }
    }

}
