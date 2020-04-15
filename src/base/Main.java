package base;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main{

    public static ArrayList<Attribute> attributes = new ArrayList<>();

    static File folder = new File("resources/tab_files");

    final static ArrayList<String> booleanValues = new ArrayList<String>(Arrays.asList(
            "false","true"
    ));
    final static ArrayList<String> grades = new ArrayList<String>(Arrays.asList(
            "one","two","three","four","five","six","seven","eight"
    ));


    // get file from classpath, resources folder
    private static void writeArffFile(String fileName, Instances instances) throws Exception {
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

    }

    public static double getAccuracy(Evaluation evaluation){
        double numCorrect = evaluation.correct();
        double numIncorrect = evaluation.incorrect();
        double accuracy = (numCorrect/(numCorrect+numIncorrect)) * 100;
        return accuracy;
    }

    public static double balancedAccuracy(Classifier c, Instances test) throws Exception{
        // Accounts for the numerical/linear properties of the categorical class (grades 1-8)
        // predicting 1 grade off is considered ok, predicting 7 grades of is bad
        double[][] lossMatrix = new double[][]{
                {  0,   0.2,    0.8,      1,     1,     1,     1,     1},
                {0.2,     0,    0.2,    0.8,     1,     1,     1,     1},
                {0.8,   0.2,    0.0,    0.2,   0.8,     1,     1,     1},
                {  1,   0.8,    0.2,      0,   0.2,   0.8,     1,     1},
                {  1,     1,    0.8,    0.2,   0.0,   0.2,   0.8,     1},
                {  1,     1,      1,    0.8,   0.2,     0,   0.2,   0.8},
                {  1,     1,      1,      1,   0.8,   0.2,     0,   0.2},
                {  1,     1,      1,      1,     1,    0.8,  0.2,     0}
        };

        double totalScore = 0.0;
        for(Instance i: test){
            int y = (int)i.classValue(); // actual value
            int pred = (int)c.classifyInstance(i); // predicted value

            double score = 1;
            score = score - lossMatrix[y][pred];
            totalScore += score;

        }
        return totalScore/test.numInstances();
    }

    public static double testAccuracy(Classifier c, Instances test) throws Exception {
        int correct = 0;
        for(Instance i: test){
            double y = i.classValue();
            double pred = c.classifyInstance(i);
            if(pred == y){
                correct++;
            }
        }
        return (correct/(double)test.numInstances());
    }

    public static Instances folderToInstances(File folder, ArrayList<Feature> features, Instances instances){
        /*
         Take a folder of tab folders/files and traverse...
         For each file it finds, create instance and add to instances
        */
        //Find grade from last character of folder name
        int grade = Character.getNumericValue(folder.getName().charAt(folder.getName().length()-1));
        System.out.println("PROCESSING "+folder.getName()+" FOLDER....");

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                // recursive call on new found directory
                folderToInstances(file, features, instances);
            } else {
                //write out feature vector for file, with grade from its folder
                //System.out.println("    ->"+file.getName());
                System.out.println("  "+file.getName());
                instances = TabParser.songToInstances(new Song(file), instances, features, grade);
            }
        }

        return instances;
    }

    public static Instances folderToSectionInstances(File folder, ArrayList<Feature> features, Instances instances, int barsPerSection){
        /*
         Take a folder of tab folders/files and traverse...
         For each file it finds, create instance and add to instances
        */
        //Find grade from last character of folder name
        int grade = Character.getNumericValue(folder.getName().charAt(folder.getName().length()-1));
        System.out.println("PROCESSING "+folder.getName()+" FOLDER....");

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

        instances = TabParser.songToInstances(song, instances, features, 0);

        return instances.get(0);
    }

    public static Instance fileToInstance(File file, ArrayList<Feature> features){

        // #################################################################

        String instances_name = features_names(features);

        Instances instances = new Instances(instances_name,attributes,0);

        instances = TabParser.songToInstances(new Song(file), instances, features, 0);

        return Main.songToInstance(new Song(file), features);
    }

    public static String features_names(ArrayList<Feature> features) {
        String out = "";
        for(Feature feature: features){
            out += feature.getName();
            out += "_";
        }
        return out;
    }

    public static double classifyTab(Classifier classifier, ArrayList<Feature> features, File file){

        //Main.attributes = getAttributesForFeatureset(features);
        Instances instances = new Instances(features_names(features),Main.attributes,0);
        instances = Main.folderToInstances(folder,features,instances);

        instances.setClassIndex(1);
        //instances.setClass(instances.attribute("grade"));

        try{
            classifier.buildClassifier(instances);
            double grade = classifier.classifyInstance(fileToInstance(file,features));
            return grade + 1.0;
        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }


    }

    public static void manualTestDebug(Classifier classifier, Instances instances) {
        try {
            instances.setClassIndex(instances.numAttributes()-1);
            classifier.buildClassifier(instances);
            for (Instance instance : instances) {
                double dist[] = classifier.distributionForInstance(instance);
                for (double d : dist) System.out.print(d + ",");
                System.out.println();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Instances[] splitData(Instances all, double proportion){

        Instances split[] = new Instances[2];
        int totalInstances = all.numInstances();

        int splitAt = (int) (proportion*totalInstances);

        all.randomize(new Random());
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
                        new NumberUniqueChords()
                ));

        // Get attributes
        Main.attributes = getAttributesForFeatureset(features);
        Main.attributes.add(new Attribute("grade", grades));

        // Get instances
        Instances instances = new Instances(features_names(features),Main.attributes,0);
        instances = Main.folderToSectionInstances(folder,features, instances,3);
        //instances = Main.folderToInstances(folder,features, instances);


        Instances split[] = splitData(instances, 0.5);
        split[0].setClassIndex(split[0].numAttributes() - 1);
        split[1].setClassIndex(split[1].numAttributes() - 1);


        NaiveBayes nb = new NaiveBayes();
        try{
            //writeArffFile(instances.relationName(),instances);
            nb.buildClassifier(split[0]);
            System.out.println(getAccuracy(evaluate(new NaiveBayes(), instances)));
            System.out.println(testAccuracy(nb,instances));
            System.out.println(balancedAccuracy(nb, instances));

        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println();
    }
}



