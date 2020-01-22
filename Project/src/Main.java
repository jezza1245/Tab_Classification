import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class Main{

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
        s.setInstances(instances);
        s.setFile(new File("src\\main\\resources\\"+fileName+".arff"));
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

    public Instances folderToInstances(File folder, ArrayList<String> features,Instances instances, TabParser parser){
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
                instances = parser.addTabDataToInstances(file, instances, features);
            }
        }

        return instances;
    }


    public static void main(String[] args) {
        Main main = new Main();
        /* Current Features:
            chordExists = {
                each feature represents a single chord which is set to 1 if the chord exists in the
                tab, else 0
            }
            chordCount = {
            each feature represents a single chord which is set equal to the number of occurences of
            the chord in the tab
            }
        */
        // Create list of features to be included

        //-------------------------  TESTING
        ArrayList<String> features = new ArrayList<String>(Arrays.asList("chordExists"));

        File folder = new File("resources/tab_files");
        TabParser parser = new TabParser();
        ArrayList<String> uniques = parser.generateUniqueChords(folder, new ArrayList<>());

        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        ArrayList<String> classVal = new ArrayList<String>();
        classVal.add("false");
        classVal.add("true");
        uniques.forEach(chord -> {
                    attributes.add(new Attribute(chord.replaceAll(" ","|"),classVal));
        });


        Instances instances = new Instances("TestSet",attributes,0);

        instances = main.folderToInstances(folder, features, instances, parser);

        try{
            main.writeArffFile("test2",instances);
        }catch (Exception e){
            System.out.println("Error creating arff file");
        }


    }


}