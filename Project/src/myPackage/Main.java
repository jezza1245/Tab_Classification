package myPackage;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Main{
    TabParser parser = new TabParser();
    static File folder = new File("resources/tab_files");
    enum featureSet {
        CHORD_USED, // Booleans as to wether the chord exists
        CHORD_COUNTS, // Count of chord occurences
        FRET_USED, // Booleans as to wether to fret was used
        FRET_COUNT, // Count of occurences of fretted note
        LARGEST_STRETCH, // Horizontal stretch + Vertical stretch for an individual chord
        LARGEST_H_FRET_STRETCH, // Largest stretch from highest to lowest string in a chord
        LARGEST_V_FRET_STRETCH, // largest stretch from highest to lowest fretted note in a chord
        HIGHEST_FRET, // Highest fretted note
        NUM_UNIQUE_CHORDS, // Number of unique chords played
        //NUM_STRING_SKIPS, // total number of string skips
                            // most string skips in a chord
        //LARGEST_STRINGS_SKIPPED, // highest number of string skips in single chord
        //MOST_CHORD_CHANGES_IN_BAR,
        NUMBER_OF_BARS,

    }
    ArrayList<String> booleanValues = new ArrayList<String>(Arrays.asList(
            "false","true"
    ));
    ArrayList<String> grades = new ArrayList<String>(Arrays.asList(
            "one","two","three","four","five","six","seven","eight"
    ));

    // get file from classpath, resources folder
    private File getFileFromResources(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile());
        }
    }

    // get file from classpath, resources folder
    private void writeArffFile(String fileName, Instances instances) throws Exception {
        ArffSaver s= new ArffSaver();
        s.setInstances(instances); // Ignore warning about reflection
        s.setFile(new File("src\\"+fileName+".arff"));
        s.writeBatch();
    }

    // Load arff data into instances object
    public static Instances loadData(String filePath){
        Instances train;
        try{
            FileReader reader = new FileReader(filePath);
            train = new Instances(reader);
        }catch(Exception e){
            System.out.println("Exception caught: "+e);
            train = null;
        }
        return train;
    }

    public Instances folderToInstances(File folder, ArrayList<? extends Features> features,Instances instances, TabParser parser){
        /*
         Take a folder of tab folders/files and traverse...
         For each file it finds, create instance and add to instances
        */


        //Find grade from last character of folder name
        char grade = folder.getName().charAt(folder.getName().length()-1);
        //System.out.println("PROCESSING "+folder.getName()+" FOLDER....");

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                // recursive call on new found directory
                folderToInstances(file, features, instances, parser);
            } else {
                //write out feature vector for file, with grade from its folder
                //System.out.println("    ->"+file.getName());
                instances = parser.addTabDataToInstances(file, instances, features, grade);
            }
        }

        return instances;
    }

    private ArrayList<Attribute> getAttributesForFeatureset(ArrayList<? extends Features> features){
        ArrayList<Attribute> attributes = new ArrayList<>();

        for(Features feature: features){
            feature.getAttributes().forEach(featureVariable -> attributes.add(featureVariable));
        }

//        if(features.contains(featureSet.LARGEST_V_FRET_STRETCH)) attributes.add(new Attribute("VerticalFretStretch"));
//        if(features.contains(featureSet.LARGEST_H_FRET_STRETCH)) attributes.add(new Attribute("HorizontalFretStretch"));
//        if(features.contains(featureSet.HIGHEST_FRET)) attributes.add(new Attribute("HighestFret"));
//        if(features.contains(featureSet.NUM_UNIQUE_CHORDS)) attributes.add(new Attribute("NumUniqueChords"));
//        //if(features.contains(featureSet.NUM_STRING_SKIPS)) attributes.add(new Attribute("StringSkips"));
//        if(features.contains(featureSet.NUMBER_OF_BARS)) attributes.add(new Attribute("NUM_BARS"));

        return attributes;
    }

    public double getAccuracy(AbstractClassifier classifier, ArrayList<? extends Features> features){

        Main main = new Main();

        ArrayList<Attribute> attributes = main.getAttributesForFeatureset(features);


        attributes.add(new Attribute("grade", grades));

        // #################################################################

        String instances_name = "";
        for(Features feature: features){
            instances_name+=feature.getName();
            instances_name+="_";
        }

        Instances instances = new Instances(instances_name,attributes,0);
        instances = main.folderToInstances(folder, features, instances, new TabParser());


        try{
            instances.setClassIndex(instances.numAttributes()-1);
            classifier.buildClassifier(instances);
            Evaluation eval = new Evaluation(instances);
            eval.crossValidateModel(classifier,instances,10, new Random(1));
            System.out.println("Evaluation Done!");
            double numCorrect = eval.correct();
            double numIncorrect = eval.incorrect();
            double accuracy = (numCorrect/(numCorrect+numIncorrect)) * 100;
            return accuracy;
        }catch (Exception e){
            e.printStackTrace();
        }

        return -1;
    }

    public String features_names(ArrayList<? extends Features> features) {
        String out = "";
        for(Features feature: features){
            out += feature.getName();
            out += "_";
        }
        return out;
    }

    public static void main(String[] args) {
        Main main = new Main();
        ArrayList<? extends Features> features = new ArrayList<>();

        // TEST SINGLE FEATURE
        features.add(new ChordCounts());

        NaiveBayes nb = new NaiveBayes();
        double accuracy = main.getAccuracy(nb,features);

        // TEST ALL FEATURES
//        for(featureSet f: myPackage.Main.featureSet.values()){
//
//            features = new ArrayList<>(Arrays.asList(f));
//
//            NaiveBayes nb = new NaiveBayes();
//
//            double accuracy = main.getAccuracy(nb,features);
//            System.out.println("Accuracy: for " + f + " :" + accuracy);
//
//        }


        //features = new ArrayList<>(Arrays.asList(featureSet.LARGEST_STRINGS_SKIPPED));
//        NaiveBayes nb = new NaiveBayes();
//        double accuracy = main.getAccuracy(nb,features);
//        System.out.println("Accuracy: for " + main.features_names(features) + " :" + accuracy);


    }
}



