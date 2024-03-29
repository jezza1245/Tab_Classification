package base;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Scanner;

public class Main{
    static File folder = new File("resources/tab_files");
//    enum featureSet {
//        CHORD_USED, // Booleans as to wether the chord exists
//        CHORD_COUNTS, // Count of chord occurences
//        FRET_USED, // Booleans as to wether to fret was used
//        FRET_COUNT, // Count of occurences of fretted note
//        LARGEST_STRETCH, // Horizontal stretch + Vertical stretch for an individual chord
//        LARGEST_H_FRET_STRETCH, // Largest stretch from highest to lowest string in a chord
//        LARGEST_V_FRET_STRETCH, // largest stretch from highest to lowest fretted note in a chord
//        HIGHEST_FRET, // Highest fretted note
//        NUM_UNIQUE_CHORDS, // Number of unique chords played
//        //NUM_STRING_SKIPS, // total number of string skips
//        // most string skips in a chord
//        //LARGEST_STRINGS_SKIPPED, // highest number of string skips in single chord
//        //MOST_CHORD_CHANGES_IN_BAR,
//        NUMBER_OF_BARS,
//
//    }
    final static ArrayList<String> booleanValues = new ArrayList<String>(Arrays.asList(
            "false","true"
    ));
    final static ArrayList<String> grades = new ArrayList<>(Arrays.asList(
            "one","two","three","four","five","six","seven","eight"
    ));

    private static void writeArffFile(String fileName, Instances instances) throws Exception {
        ArffSaver s= new ArffSaver();
        s.setInstances(instances); // Ignore warning about reflection
        s.setFile(new File("src\\"+fileName+".arff"));
        s.writeBatch();
    }

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

    public Instances folderToInstances(File folder, ArrayList<Feature> features,Instances instances){
        /*
         Take a folder of tab folders/files and traverse...
         For each file it finds, create instance and add to instances
        */

        //Find grade from last character of folder name
        char grade = folder.getName().charAt(folder.getName().length()-1);
        System.out.println("PROCESSING "+folder.getName()+" FOLDER....");

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                // recursive call on new found directory
                folderToInstances(file, features, instances);
            } else {
                //write out feature vector for file, with grade from its folder
                //System.out.println("    ->"+file.getName());
                System.out.println("  "+file.getName());
                instances = parser.addTabDataToInstances(new Song(file), instances, features, grade);
            }
        }

        return instances;
    }

    public Instances folderToSongs(File folder, ArrayList<Feature> features,Instances instances){

        char grade = folder.getName().charAt(folder.getName().length()-1);
        System.out.println("PROCESSING "+folder.getName()+" FOLDER....");

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                folderToSongs(file, features, instances);
            } else {
                System.out.println("  "+file.getName());
                instances = parser.addTabDataToInstances(new Song(file), instances, features, grade);
            }
        }

        return instances;
    }
    private static ArrayList<Attribute> getAttributesForFeatureset(ArrayList<Feature> features){
        ArrayList<Attribute> attributes = new ArrayList<>();

        for(Feature feature: features){
            feature.getAttributes().forEach(featureVariable -> attributes.add(featureVariable));
        }

        return attributes;
    }

    public static Evaluation evaluate(AbstractClassifier classifier, Instances instances ) {
        try {
            instances.setClassIndex(instances.numAttributes()-1);
            //classifier.buildClassifier(instances);
            Evaluation eval = new Evaluation(instances);
            eval.crossValidateModel(classifier,instances,10, new Random(1));

            return eval;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    public static double getAccuracy(Evaluation evaluation){
        double numCorrect = evaluation.correct();
        double numIncorrect = evaluation.incorrect();
        double accuracy = (numCorrect/(numCorrect+numIncorrect)) * 100;
        return accuracy;
    }

    public static double testAccuracy(Classifier c, Instances test) throws Exception {
        int correct = 0;
        for(Instance i: test){
            double pred = c.classifyInstance(i);
            double y = i.classValue();
            if(pred == y){
                correct++;
            }
        }
        return (correct/(double)test.numInstances());
    }

    public static Instances getInstances(File file, ArrayList<Feature> features){

        ArrayList<Attribute> attributes = getAttributesForFeatureset(features);


        attributes.add(new Attribute("grade", grades));

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                // recursive call on new found directory
                folderToSectionInstances(file, features, instances, barsPerSection);
            } else {
                //write out feature vector for file, with grade from its folder
                //System.out.println("    ->"+file.getName());
                System.out.println("  "+file.getName());
                Song song = new Song(file);
                ArrayList<Song> sections = song.getSections(barsPerSection);
               // Instances songSectionsAsInstances = new Instances("temp",getAttributesForFeatureset(features),0);
                for(Song section: sections){
                    instances = TabParser.songToInstances(section, instances, features, grade);
                }
            }
        }

        return instances;
    }


    public static Instance songToInstance(Song song, ArrayList<Feature> features){

        // #################################################################

        String instances_name = features_names(features);

        Instances instances = new Instances(instances_name,attributes,0);
        instances = folderToInstances(file, features, instances);





        return instances;
    }

    public static String features_names(ArrayList<Feature> features) {
        String out = "";
        for(Feature feature: features){
            out += feature.getName();
            out += "_";
        }
        return out;
    }

    public static double testTrainAccuracy(Classifier c, Instances train, Instances test){
        double acc = -1;
        train.setClassIndex(train.numAttributes()-1);
        test.setClassIndex(test.numAttributes()-1);


        try{
            c.buildClassifier(train);
            Enumeration<Instance> instanceEnumeration = test.enumerateInstances();

            int numCorrect = 0;
            while(instanceEnumeration.hasMoreElements()){
                Instance next = instanceEnumeration.nextElement();
                double actual = next.classValue();
                double prediction = c.classifyInstance(next);
                if(actual == prediction) numCorrect++;
            }

            acc = numCorrect/test.numInstances();
        }catch (Exception e){
            e.printStackTrace();
        }

        return acc;
    }

    public static Instances[] splitData(Instances all, double proportion){

        Instances split[] = new Instances[2];
        int totalInstances = all.numInstances();

        int splitAt = (int) (proportion*totalInstances);

        all.randomize(new Random(42));
        split[0] = new Instances(all, 0, splitAt);
        split[1] = new Instances(all, splitAt, totalInstances-splitAt);

        return split;
    }

    public static void main(String[] args) {

        File testFile = new File("resources/test.tab");
        ArrayList<Feature> features = new ArrayList<>();

        // TEST SINGLE FEATURE
        features.addAll(
                Arrays.asList(
                        //new NumberUniqueChords(),
                        new ChordExists()
                ));

        Main main = new Main();
        if(features.contains(new ChordCounts())){
            main.parser.generateUniqueChords();
        }



        Instances instances = main.getInstances(folder,features);
        Instances split[] = splitData(instances,0.5);



        try{
            nb.buildClassifier(split[0]);
            System.out.println(getAccuracy(evaluate(new NaiveBayes(), instances)));

        }catch (Exception e){
            e.printStackTrace();
        }
        //System.out.println(getAccuracy(evaluate(new NaiveBayes(), instances)));
        //manualTestDebug(new NaiveBayes(),instances);

        //double mat[][] = evaluate(new NaiveBayes(), instances).confusionMatrix();

//        double grade = classifyTab(new NaiveBayes(), features, testFile);
//        System.out.println("GRADE: "+grade);

//        try{
//            writeArffFile(features_names(features),instances);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
        System.out.println();
    }
}



