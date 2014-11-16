package at.tuwien.aic.tweetanalysis.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

/**
 * @author Group 1
 */
public class WekaClassifier {

    Instances _trainingDataset = null;
    Instances _testingDataset = null;
    private Classifier _classifier = null;


    public WekaClassifier() {

    }

    /*
    * Read in a new Instance of training Sets
    * Saves the Results in the trainingDataset variable
    */
    private void getTrainingDataset(final String INPUT_FILENAME) {
        try {
            //reading the training dataset from CSV file
            CSVLoader trainingLoader = new CSVLoader();
            trainingLoader.setSource(new File(INPUT_FILENAME));
            _trainingDataset = trainingLoader.getDataSet();
            _trainingDataset.setClassIndex(_trainingDataset.numAttributes() - 1);
        } catch (IOException ex) {
            System.out.println("Exception getTrainingDataset: " + ex.getMessage());
        }
    }

    /*
    * Read in a new Instance of testing Sets
    * Saves the Results in the testingDataset variable
    */
    private void getTestingDataset(final String INPUT_FILENAME) {
        try {
            //reading the training dataset from CSV file
            CSVLoader trainingLoader = new CSVLoader();
            trainingLoader.setSource(new File(INPUT_FILENAME));
            _testingDataset = trainingLoader.getDataSet();
            _testingDataset.setClassIndex(_testingDataset.numAttributes() - 1);
        } catch (IOException ex) {
            System.out.println("Exception getTrainingDataset: " + ex.getMessage());
        }
    }

    /*
    * Train the Classifier with the training data set
    */
    public void trainClassifier(final String INPUT_FILENAME) {
        getTrainingDataset(INPUT_FILENAME);

        //In this case we use NaiveBayes Classifier.
        _classifier = (Classifier) new NaiveBayes();

        try {
            //classifier training code
            _classifier.buildClassifier(_trainingDataset);
        } catch (Exception ex) {
            System.out.println("Exception training classifier: " + ex.getMessage());
        }
    }

    /**
     * testing the classifier
     * evaluate classifier and print some statistics
     */
    public void evaluate(final String INPUT_FILENAME) {

        getTestingDataset(INPUT_FILENAME);

        try {

            Evaluation eval = new Evaluation(_trainingDataset);

            eval.evaluateModel(_classifier, _testingDataset);
            System.out.println(eval.toSummaryString("\nResults\n======\n", false));
            System.out.println(eval.toMatrixString());

        } catch (Exception ex) {
            System.out.println("Exception testing classifier: " + ex.getMessage());
        }
    }

    /*
    * Use the Classifier with the training data set
    */
    public void useClassifier() {
        try {
            double[] fDistribution = _classifier.distributionForInstance(_testingDataset.firstInstance());

            // Get the likelihood of each classes
            // fDistribution[0] is the probability of being “positive”
            // fDistribution[1] is the probability of being “negative”
            System.out.println("Evaluation of a String");
            System.out.println("probability of being positive: " + fDistribution[0]);
            System.out.println("probability of being negative: " + fDistribution[1]);

        } catch (Exception ex) {
            System.out.println("Exception using classifier: " + ex.getMessage());
        }
    }
}
