package base;

import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TabParser {

    final static HashMap<String,Integer> uniques = new HashMap<>();

    static
    {
        ArrayList<String> uniqueChords = new ArrayList<>();
        uniqueChords = generateUniqueChords(Main.folder, uniqueChords);
        for(int i = 0; i < uniqueChords.size(); i++) uniques.put(uniqueChords.get(i), 0);
    }


    public static double[] concatenate(double a[], double b[]) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        double[] c = (double[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public static ArrayList<String> generateUniqueChords(File folder, ArrayList<String> uniques){

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                generateUniqueChords(file, uniques);
            } else if (file.getName().endsWith(".tab")){
                Song song = new Song(file);
                Iterator songIterator = song.getEventIterator();

                while(songIterator.hasNext()){
                    Event event = (Event)songIterator.next();
                    String chord = event.chord;
                    if(chord.length() > 0 && !uniques.contains(chord)){
                        uniques.add(chord);
                    }
                }
            }
        }

        return uniques;
    }


//    public static Instances fileToInstances(File file, Instances instances, ArrayList<Feature> features, int grade) {
//
//        double instanceData[] = new double[0];
//
//        for(Feature feature: features){
//            double[] newData = feature.getFeatureData(new Song(file));
//            instanceData = concatenate(instanceData,newData);
//        }
//
//        // add grade
//        double gradeData[] = new double[1];
//
//        gradeData[0] = grade<1 ? Utils.missingValue() : (grade-1);
//
//        instanceData = concatenate(instanceData, gradeData);
//        instances.add(new DenseInstance(1.0, instanceData));
//
//        return instances;
//    }

    public static Instances songToInstances(Song song, Instances instances, ArrayList<Feature> features, int grade) {

        double instanceData[] = new double[0];

        for(Feature feature: features){
            double[] newData = feature.getFeatureData(song);
            instanceData = concatenate(instanceData,newData);
        }


//        if (features.contains(Main.featureSet.LARGEST_V_FRET_STRETCH)) {
//            double data[] = new double[1];
//            int largestStretch = 0;
//            for (Bar b : bars) {
//                for (Event event : b.getEvents()) {
//                    int lowestFret = Integer.MAX_VALUE;
//                    int highestFret = 0;
//                    for (char c : event.chord.toCharArray()) {
//
//                        // ignore non-played and open notes
//                        if (c == ' ' || c == 'a') continue;
//
//                        int fret = c;
//                        if (fret < lowestFret) {
//                            lowestFret = fret;
//                        }
//                        if (fret > highestFret) {
//                            highestFret = fret;
//                        }
//                    }
//
//                    int stretch = highestFret - lowestFret;
//                    if (stretch > largestStretch) {
//                        largestStretch = stretch;
//                    }
//                }
//            }
//            data[0] = largestStretch;
//            instanceData = concatenate(instanceData, data);
//        }

//        if(features.contains(Main.featureSet.LARGEST_H_FRET_STRETCH)){
//            double data[] = new double[1];
//            int largestStretch = 0;
//            for (Bar b : bars) {
//                for (Event event : b.getEvents()) {
//                    int firstStringFretted = -1;
//                    int lastStringFretted = -1;
//                    int index = 0;
//                    for (char c : event.chord.toCharArray()) {
//                        // ignore non-played and open notes
//                        if (c == ' ' || c == 'a') {
//                            index++;
//                            continue;
//                        }
//                        if (firstStringFretted < 0) firstStringFretted = index;
//                        lastStringFretted = index;
//                        index++;
//                    }
//                    int stretch = lastStringFretted - firstStringFretted;
//                    if (stretch > largestStretch) {
//                        largestStretch = stretch;
//                    }
//                }
//            }
//            data[0] = largestStretch;
//            instanceData = concatenate(instanceData, data);
//        }


        // string skips
//        if(features.contains(myPackage.Main.featureSet.LARGEST_STRINGS_SKIPPED)){
//            double data[] = new double[1];
//            int largestNumSkips = 0;
//            for (myPackage.Bar b : bars) {
//                for (myPackage.Event event : b.getEvents()) {
//                    String chord = event.chord.trim();
//                    int skips = 0;
//                    for (char c : chord.toCharArray()) {
//                        if (c == ' ') skips++;
//                    }
//
//                    if (skips > largestNumSkips) largestNumSkips = skips;
//                }
//            }
//            data[0] = largestNumSkips;
//            instanceData = concatenate(instanceData, data);
//        }

        // add grade
        double gradeData[] = new double[1];

        gradeData[0] = grade<1 ? Utils.missingValue() : (grade-1);

        instanceData = concatenate(instanceData, gradeData);
        instances.add(new DenseInstance(1.0, instanceData));

        return instances;
    }


    public static ArrayList<Bar> getBars(File file){
        ArrayList<Bar> bars = new ArrayList<>();

        try{
            BufferedReader br = new BufferedReader(new FileReader(file));

            /*
                Process header information such as title and style data
                (probably not useful)
            */
            String line = br.readLine();
            //WHILE can get a line
            Bar bar = null;
            boolean firstBarLine = true;
            while(line!=null){
                // ############## Miscellanous #################

                // $ -> styling option
                if(line.startsWith("$")){
                    line = br.readLine();
                    continue;                }

                // { -> Printed title information
                else if(line.startsWith("{")){
                    line = br.readLine();
                    continue;
                }

                //EMPTY LINE -> NEW TAB LINE
                if(line.length()==0) {
                    line = br.readLine();
                    continue;
                }

                //e -> END OF FILE
                else if(line.startsWith("e")) {
                    break;
                }

                //% -> COMMENT
                else if(line.startsWith("%")) {
                    line = br.readLine();
                    continue;
                }

                //. -> Column of dots
                else if(line.startsWith(".")) {
                    line = br.readLine();
                    continue;
                }

                //b or B -> bar
                else if(line.startsWith("b") || line.startsWith("B")) {

                    // If first barline before
                    if(!firstBarLine){
                        bars.add(bar);
                    }else{
                        firstBarLine = false;
                    }
                    bar = new Bar();


                    line = br.readLine();
                    continue;
                }

                //Time Signature (C,c,Sx-y)
                else if(line.startsWith("C") || line.startsWith("c") || line.startsWith("S")) {
                    line = br.readLine();
                    continue;
                }

                // indent
                else if(line.startsWith("i")){
                    line = br.readLine();
                    continue;
                }
                // ################################################################################################

                Event event = new Event(line);
                // If no initial barline was sed (eg starting with notes before a barline)
                if (bar==null) {
                    firstBarLine = false; // Let system know
                    bar = new Bar();
                }
                bar.addEvent(event);

                line = br.readLine();
            }
        }catch(Exception e){
            System.out.println("ERROR PARSING TAB");
        }

        return bars;
    }

    public static void main(String[] args) {

        TabParser tabParser = new TabParser();

        File testFolder = new File("resources/test");
        ArrayList<String> uniques = tabParser.generateUniqueChords(testFolder, new ArrayList<>());
        System.out.println("Hello");

        //myPackage.TabParser parse = new myPackage.TabParser();
        //ArrayList<myPackage.Bar> bars = parse.getBars(new File("resources/tab_files/grade1/1_Calleno.tab"));
    }
}
