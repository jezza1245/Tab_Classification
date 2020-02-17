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

    File folder = new File("resources/tab_files");
    enum featureSet {
        CHORD_USED,
        CHORD_COUNTS,
        FRET_USED,
        FRET_COUNT,
        LARGEST_FRET_STRETCH,
        HIGHEST_FRET,
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

    public Instances folderToInstances(File folder, ArrayList<featureSet> features,Instances instances, TabParser parser){
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
                folderToInstances(file, features, instances, parser);
            } else {
                //write out feature vector for file, with grade from its folder
                System.out.println("    ->"+file.getName());
                instances = parser.addTabDataToInstances(file, instances, features, grade);
            }
        }

        return instances;
    }

    public double getAccuracy(ArrayList<featureSet> features){

        TabParser parser = new TabParser();
        ArrayList<String> uniques;
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();

        // ##############  Set up attributes  ################################
        if(features.contains(featureSet.CHORD_COUNTS) || features.contains(featureSet.CHORD_USED)){

            uniques = parser.generateUniqueChords(folder, new ArrayList<>());

            if(features.contains(featureSet.CHORD_USED)) {
                uniques.forEach(chord -> {
                    attributes.add(new Attribute(chord.replaceAll(" ", "|"), booleanValues));
                });
            }
            if(features.contains(featureSet.CHORD_COUNTS)) {
                uniques.forEach(chord -> {
                    attributes.add(new Attribute(chord.replaceAll(" ", "|")));
                });
            }
        }


        if(features.contains(featureSet.FRET_COUNT) || features.contains(featureSet.FRET_USED)){

            if(features.contains(featureSet.FRET_COUNT)) {
                for (int i = (int) 'b'; i <= (int) 'p'; i++) {
                    attributes.add(new Attribute(Character.toString((char) i)));
                }
            }
            if(features.contains(featureSet.FRET_USED)) {
                for (int i = (int) 'b'; i <= (int) 'p'; i++) {
                    attributes.add(new Attribute(Character.toString((char) i), booleanValues));
                }
            }
        }

        if(features.contains(featureSet.LARGEST_FRET_STRETCH)) attributes.add(new Attribute("LargestFretStretch"));
        if(features.contains(featureSet.HIGHEST_FRET)) attributes.add(new Attribute("HighestFret"));

        attributes.add(new Attribute("grade", grades));

        // #################################################################

        String instances_name = "";
        for(featureSet feature: features){
            instances_name+=feature.name();
            instances_name+="_";
        }
        Instances instances = new Instances(instances_name,attributes,0);
        instances = new Main().folderToInstances(folder, features, instances, parser);

        NaiveBayes nb = new NaiveBayes();
        try{
            instances.setClassIndex(instances.numAttributes()-1);
            nb.buildClassifier(instances);
            Evaluation eval = new Evaluation(instances);
            eval.crossValidateModel(nb,instances,10, new Random(1));
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

    public String features_names(ArrayList<featureSet> features) {
        String out = "";
        for(featureSet feature: features){
            out += feature.name();
            out += "_";
        }
        return out;
    }

    public static void main(String[] args) {
        Main main = new Main();

        ArrayList<featureSet> features;

        features = new ArrayList<>(Arrays.asList(featureSet.FRET_USED));
        double accuracy = main.getAccuracy(features);
        System.out.println("Accuracy: for " + main.features_names(features) + " :" + accuracy);


    }


}