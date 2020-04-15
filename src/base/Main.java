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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Scanner;

public class Main{

    public static ArrayList<Attribute> attributes = new ArrayList<>();

    static File folder = new File("resources/tab_files");

    final static ArrayList<String> booleanValues = new ArrayList<String>(Arrays.asList(
=======
    static File folder = new File("resources/tab_files");
    
    TabParser parser = new TabParser();

    final static ArrayList<String> booleanValues = new ArrayList<>(Arrays.asList(
>>>>>>> Stashed changes
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

        instances = TabParser.songToInstances(song, instances, features, 0);

        return instances.get(0);
    }

    public static Instance fileToInstance(File file, ArrayList<Feature> features){

        // #################################################################

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

    public static void main(String[] args) {

        File testFile = new File("resources/test.tab");
        ArrayList<Feature> features = new ArrayList<>();

        // TEST SINGLE FEATURE
        features.addAll(
                Arrays.asList(
                        //new NumberUniqueChords(),
                        new ChordExists()
                ));

        Instances instances = Main.getInstances(folder,features);

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



