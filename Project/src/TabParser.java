import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.ListIterator;

public class TabParser {

    public static double[] concatenate(double a[], double b[]) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        double[] c = (double[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }

    public ArrayList<String> generateUniqueChords(File folder, ArrayList<String> uniques){

        for(File file : folder.listFiles()){
            if (file.isDirectory()) {
                this.generateUniqueChords(file, uniques);
            } else if (file.getName().endsWith(".tab")){
                ArrayList<Event> events = this.getEvents(file);
                events.forEach((event -> {
                    String chord = event.chord;
                    if(!uniques.contains(chord)){
                        uniques.add(chord);
                    }
                }));
            }
        }

        return uniques;
    }


    public Instances addTabDataToInstances(File file, Instances instances, ArrayList<String> features){

        ArrayList<Event> events = this.getEvents(file);
        double instanceData[] = new double[0];

        if(features.contains("chordExists") || features.contains("chordCounts")){

            ArrayList<String> uniqueChords = new TabParser().generateUniqueChords(new File("resources/tab_files"), new ArrayList<>());
            double data1[] = new double[uniqueChords.size()];

            // Goes through events and creates feature set of binary values where 1: relevant chord exists
            // 0: relevant chord does not exist
            for(int i = 0; i<uniqueChords.size(); i++){

                int count = 0;
                for(Event event: events){
                    if(event.chord.equals(uniqueChords.get(i))){
                        count++;
                    }
                }

                if(features.contains("chordExists")){
                    data1[i] = (count>0) ? 1 : 0;
                }else{
                    data1[i] = count;
                }

            }
            instanceData = concatenate(instanceData,data1);

        }
        System.out.println("----- Before -----");
        System.out.println(instances);

        instances.add(new DenseInstance(1.0, instanceData));

        System.out.println("----- After ----");
        System.out.println(instances);

        return instances;
    }


    public ArrayList<Event> getEvents(File file){
        ArrayList<Event> events = new ArrayList<Event>();

        try{
            BufferedReader br = new BufferedReader(new FileReader(file));

            /*
                Process header information such as title and style data
                (probably not useful)
            */
            String line = br.readLine();
            //WHILE can get a line
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
                    line = br.readLine();
                    continue;
                }

                //Time Signature (C,c,Sx-y)
                else if(line.startsWith("C") || line.startsWith("c") || line.startsWith("S")) {
                    line = br.readLine();
                    continue;
                }
                // ################################################################################################

                Event event = new Event(line);
                events.add(event);

                line = br.readLine();
            }
        }catch(Exception e){
            System.out.println("ERROR PARSING TAB");
        }

        return events;
    }

    public static void main(String[] args) {

        TabParser tabParser = new TabParser();

        File testFolder = new File("resources/test");
        ArrayList<String> uniques = tabParser.generateUniqueChords(testFolder, new ArrayList<>());
        System.out.println("Hello");
    }
}
