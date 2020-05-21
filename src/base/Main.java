package base;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.*;

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

    public static double balancedTestAccuracy(Classifier c, Instances test) throws Exception{
        // Accounts for the numerical/linear properties of the categorical class (grades 1-8)
        // predicting 1 grade off is considered ok, predicting 7 grades of is bad
        double[][] lossMatrix = new double[][]{
                {0.0,   0.2,    0.8,      1,     1,     1,     1,     1},
                {0.2,   0.0,    0.2,    0.8,     1,     1,     1,     1},
                {0.8,   0.2,    0.0,    0.2,   0.8,     1,     1,     1},
                {  1,   0.8,    0.2,    0.0,   0.2,   0.8,     1,     1},
                {  1,     1,    0.8,    0.2,   0.0,   0.2,   0.8,     1},
                {  1,     1,      1,    0.8,   0.2,   0.0,   0.2,   0.8},
                {  1,     1,      1,      1,   0.8,   0.2,   0.0,   0.2},
                {  1,     1,      1,      1,     1,    0.8,  0.2,   0.0}
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
        double correct = 0;
        double total = test.numInstances();
        for(Instance i: test){
            double y = i.classValue();
            double pred = c.classifyInstance(i);
            if(pred == y){
                correct++;
            }
        }
        double accuracy = correct/total;
        return accuracy;
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
                Song song = new Song(file,grade);
                ArrayList<Song> sections = song.getSections(barsPerSection);
               // Instances songSectionsAsInstances = new Instances("temp",getAttributesForFeatureset(features),0);
                for(Song section: sections){
                    instances = TabParser.songToInstances(section, instances, features, grade);
                }
            }
        }

        return instances;
    }

    public static ArrayList<Song> folderToSongs(File folder, ArrayList<Song> songs){
        int grade = Character.getNumericValue(folder.getName().charAt(folder.getName().length()-1));
        System.out.println("PROCESSING "+folder.getName()+" FOLDER....");

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                // recursive call on new found directory
                folderToSongs(file, songs);
            } else {
                System.out.println("  "+file.getName());
                Song song = new Song(file,grade);
                songs.add(song);
            }
        }

        return songs;
    }

    public static ArrayList<ArrayList<Song>> splitSongs(ArrayList<Song> songs, double trainPortion){
        Collections.shuffle(songs);
        ArrayList<ArrayList<Song>> split = new ArrayList<>();
        split.add(new ArrayList<>());
        split.add(new ArrayList<>());
        int gradeCounts[] = new int[9];

        // Find how many of each grade so we can split evenly
        for(Song s: songs){
            int grade = s.getGrade();
            gradeCounts[grade]++;
        }

        // Split songs
        int gradesAdded[] = new int[9];
        for(Song s: songs){
            int grade = s.getGrade();
            // if we havnt added half of the current grade to train...
            if(gradesAdded[grade] < gradeCounts[grade]*trainPortion){
                split.get(0).add(s);
            }else{ // else add to test
                split.get(1).add(s);
            }

            gradesAdded[grade]++;
        }

        return split;
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

        Instances instances = new Instances(features_names(features), Main.attributes,0);

        Song song = new Song(file);
        Instance instance = songToInstance(song,features);
        instances.add(instance);
        instances.setClassIndex(instances.numAttributes() - 1);

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

    public static Instances generateInstancesFromSongs(ArrayList<Song> songs, Instances instances,ArrayList<Feature> features){
        for(Song s: songs){
            instances = TabParser.songToInstances(s,instances,features,s.getGrade());
        }
        return instances;
    }

    public static Instances generateSectionInstancesFromSongs(ArrayList<Song> songs, Instances instances,ArrayList<Feature> features, int barsPerSection){
        for(Song s: songs){
            ArrayList<Song> sections = s.getSections(barsPerSection);
            for(Song section: sections){
                instances = TabParser.songToInstances(section, instances, features, s.getGrade());
            }
            instances = TabParser.songToInstances(s,instances,features,s.getGrade());
        }
        return instances;
    }

    public static void runTests(ArrayList<Song> allSongs, ArrayList<Feature> features, int barsPerSection){
        //allSongs = folderToSongs(folder, allSongs);
        //Instances instancesTemplate = new Instances(features_names(features),Main.attributes,0);

        double normalAccuracy = 0;
        double balancedAccuracy = 0;
        int runs = 20;
        for(int i = 0; i < runs; i++){
            NaiveBayes nb = new NaiveBayes();

            ArrayList<ArrayList<Song>> split = splitSongs(allSongs, 0.5);
            TabParser.rebuildUniqueChords(split.get(0));

            Instances train;
            Instances test;
            if(barsPerSection > 0) {
                train = generateSectionInstancesFromSongs(split.get(0), new Instances(features_names(features), Main.attributes, 0), features,barsPerSection);
                test = generateSectionInstancesFromSongs(split.get(1), new Instances(features_names(features), Main.attributes, 0), features,barsPerSection);
            }else {
                train = generateInstancesFromSongs(split.get(0), new Instances(features_names(features), Main.attributes, 0), features);
                test = generateInstancesFromSongs(split.get(1), new Instances(features_names(features), Main.attributes, 0), features);
            }

            train.setClassIndex(train.numAttributes() - 1);
            test.setClassIndex(test.numAttributes() - 1);

            try {
                nb.buildClassifier(train);

                double normalSampleAccuracy = testAccuracy(nb,test);
                double balancedSampleAccuracy = balancedTestAccuracy(nb, test);

                if(normalSampleAccuracy > 1 || balancedTestAccuracy(nb, test) > 1){
                    System.out.println("a");
                }

                normalAccuracy += normalSampleAccuracy;
                balancedAccuracy += balancedSampleAccuracy;
            }catch (Exception e){
                i--;
            }
        }

        double meanNormalAccuracy = normalAccuracy/(double)runs;
        double meanBalancedAccuracy = balancedAccuracy/(double)runs;

        if(meanBalancedAccuracy > 1 || meanNormalAccuracy > 1){
            System.out.println("here");
        }
        System.out.printf("%10s  %10s\n",meanNormalAccuracy,meanBalancedAccuracy);
    }

    public static void evaluateTab(File file){

    }

    public static void buildAndEstimate(File file){
        ArrayList<Feature> features = new ArrayList<>(); // empty features array
        ArrayList<Song> allSongs = new ArrayList<>(); // empty song array
        allSongs = folderToSongs(folder, allSongs); // fill song array with all songs

        features.add(new ChordCounts()); // add feature
        TabParser.rebuildUniqueChords(allSongs); // build BOW data from training set

        // Get attributes
        Main.attributes = getAttributesForFeatureset(features);
        Main.attributes.add(new Attribute("grade", grades)); //add grade as attribute

        NaiveBayes nb = new NaiveBayes();
        Instances train = generateInstancesFromSongs(allSongs, new Instances(features_names(features), Main.attributes, 0), features);
        train.setClassIndex(train.numAttributes() -1);

        try{
            nb.buildClassifier(train);
            Song song = new Song(file);
            double grade = nb.classifyInstance(songToInstance(song,features));
            System.out.println(song.name);
            System.out.println("Estimated: Grade "+(int)grade+1);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        if(args.length > 0){
            File inputFile;
            if((inputFile = new File(args[0])) != null){
                buildAndEstimate(inputFile);
            }else{
                System.out.println("Incorrect file path");
            }


        }else {

            File testFile = new File("resources/test.tab");

//        ###########################################
//        Test song train/test splitting

//        ArrayList<Song> allSongs = new ArrayList<>();
//        allSongs = folderToSongs(folder, allSongs);
//
//        ArrayList<ArrayList<Song>> split = splitSongs(allSongs, 0.2);
//        int gradeCheck[] = new int[9];
//        for(Song s: split.get(0)){
//            gradeCheck[s.getGrade()]++;
//        }
//        System.out.println("TRAIN");
//        for(int i=0; i<gradeCheck.length ;i++){
//            System.out.println("  -> grade "+i+" => "+gradeCheck[i]);
//        }
//
//        gradeCheck = new int[9];
//        for(Song s: split.get(1)){
//            gradeCheck[s.getGrade()]++;
//        }
//        System.out.println("TEST");
//        for(int i=0; i<gradeCheck.length ;i++){
//            System.out.println("  -> grade "+i+" => "+gradeCheck[i]);
//        }

            ArrayList<Feature> features = new ArrayList<>(); // empty features array
            ArrayList<Song> allSongs = new ArrayList<>(); // empty song array
            allSongs = folderToSongs(folder, allSongs); // fill song array with all songs

//        features.addAll(
//                Arrays.asList(
//                        //new NumberUniqueChords()
//                        new ChordExists()
//                        new FretExists(),
//                        new HighestFret(),
//                        new LargestFretStretch()
//                ));

            //features.add(new ChordExists()); // add feature
            features.add(new ChordCounts()); // add feature
            //features.add(new LargestFretStretch()); // add feature
            //features.add(new LargestNumStringSkips()); // add feature
            //features.add(new LargestSingleStringSkip()); // add feature


            ArrayList<ArrayList<Song>> split = splitSongs(allSongs, 0.5);
            TabParser.rebuildUniqueChords(split.get(0)); // build BOW data from training set

            // Get attributes
            Main.attributes = getAttributesForFeatureset(features);
            Main.attributes.add(new Attribute("grade", grades)); //add grade as attribute

            runTests(allSongs, features, 0);

            for (int i = 0; i < 50; i++) {
                runTests(allSongs, features, 0);
            }

//        NaiveBayes nb; // naive bayes classifier
//
//        // ### Build train test instances (either as full song or as individual sections)
//        Instances train = generateInstancesFromSongs(split.get(0), new Instances(features_names(features), Main.attributes, 0), features);
//        //Instances train = generateSectionInstancesFromSongs(split.get(0), new Instances(features_names(features), Main.attributes, 0), features, 20);
//
//        Instances test = generateInstancesFromSongs(split.get(1), new Instances(features_names(features), Main.attributes, 0), features);
//        //Instances test = generateSectionInstancesFromSongs(split.get(1), new Instances(features_names(features), Main.attributes, 0), features, 20);
//
//
//        // set class indices
//        train.setClassIndex(train.numAttributes() - 1);
//        test.setClassIndex(test.numAttributes() - 1);
//
//        nb = new NaiveBayes();
//        try {
//            //writeArffFile("train",train);
//            //writeArffFile("test",test);
//
//            nb.buildClassifier(train);
//            System.out.print(testAccuracy(nb, test)+"  ");
//            System.out.println(balancedTestAccuracy(nb, test));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        }
    }
}



