package base;

import weka.core.Attribute;

import java.lang.reflect.Array;
import java.util.*;

class ChordCounts implements Feature{

    @Override
    public String getName(){
        return "chordCounts";
    }

    @Override
    public ArrayList<Attribute> getAttributes(){

        ArrayList<Attribute> attributes = new ArrayList<>(); //Create empty arraylist for attributes

        // For every unique chord in TabParser, add a new attribute for that chord
        TabParser.uniques.forEach( (chord,value) ->
                attributes.add(new Attribute(chord.replaceAll(" ", "|")+"_count")) // Replace ' ' with '|' for readability
        );

        return attributes;
    }

    @Override
    public double[] getFeatureData(Song song) {

        HashMap<String,Integer> counts = (HashMap<String,Integer>) TabParser.uniques.clone(); // Copy uniques hashmap, to use for counting
        Iterator<Event> iterator = song.getEventIterator();

        while(iterator.hasNext()) { //While song has another event/chord
            Event event = iterator.next();
            if(counts.containsKey(event.chord)){ //If chord is known chord
                int currentCount = counts.get(event.chord); //Get current count from hashmap for this song
                counts.replace(event.chord,currentCount,currentCount+1); // Increase count of times its been seen in this song
            }
        }

        double[] dataOut = new double[counts.size()]; // create new empty double array to return
        int index = 0;

        Iterator it = counts.keySet().iterator();
        while(it.hasNext()){
            Object chord = it.next();
            dataOut[index++]=counts.get(chord);
        }

        return dataOut;

    }

}

class ChordExists implements Feature{

    @Override
    public String getName(){
        return "chordExists";
    }

    @Override
    public ArrayList<Attribute> getAttributes(){

        ArrayList<Attribute> attributes = new ArrayList<>(); //Create empty arraylist for attributes

        // For every unique chord in TabParser, add a new attribute for that chord
        TabParser.uniques.forEach( (chord,value) ->
                attributes.add(new Attribute(chord.replaceAll(" ", "|")+"_exists",Main.booleanValues)) // Replace ' ' with '|' for readability
        );

        return attributes;
    }

    @Override
    public double[] getFeatureData(Song song) {

        HashMap<String,Integer> counts = TabParser.uniques; // Copy uniques hashmap, to use for keeping track of whats seen
        Iterator<Event> iterator = song.getEventIterator();

        while(iterator.hasNext()) { //While song has another event/chord
            Event event = iterator.next();
            if(counts.containsKey(event.chord)){ //If chord is known chord

                // Set value to 1, to indicate its been found already
                if(counts.get(event.chord)==0) counts.replace(event.chord,0,1);
            }
        }

        double[] dataOut = new double[counts.size()]; // create new empty double array to return
        int index = 0;

        Iterator it = counts.keySet().iterator();
        while(it.hasNext()){
            Object chord = it.next();
            dataOut[index++]=counts.get(chord);
        }

        return dataOut;

    }

}

class FretCounts implements Feature{

    @Override
    public String getName() {
        return "fretCounts";
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        ArrayList<Attribute> fretAttributes = new ArrayList<>();
        for(int c=(int)'b'; c<=(int)'p'; c++){
            fretAttributes.add(new Attribute("fret_"+(char)c+"_count"));
        }
        return fretAttributes;
    }

    @Override
    public double[] getFeatureData(Song song) {
        HashMap<Character,Integer> counts = new HashMap<>(15); // Copy uniques hashmap, to use for keeping track of whats seen
        Iterator<Event> iterator = song.getEventIterator();

        while(iterator.hasNext()) { //While song has another event/chord
            Event event = iterator.next();

            for(char c: event.chord.toCharArray()){
                if(counts.containsKey(c)){ //If chord is known chord
                    // Set value to 1, to indicate its been found already
                    if(counts.get(c)==0) counts.replace(c,0,1);
                }
            }

        }

        double[] dataOut = new double[counts.size()]; // create new empty double array to return
        int index = 0;

        Iterator it = counts.keySet().iterator();
        while(it.hasNext()){
            Object chord = it.next();
            dataOut[index++]=counts.get(chord);
        }

        return dataOut;
    }
}

class FretExists implements Feature{

    @Override
    public String getName() {
        return "fretExists";
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        ArrayList<Attribute> fretAttributes = new ArrayList<>();
        for(int c=(int)'b'; c<=(int)'p'; c++){
            fretAttributes.add(new Attribute("fret_"+(char)c+"_exists",Main.booleanValues));
        }
        return fretAttributes;
    }

    @Override
    public double[] getFeatureData(Song song) {
        double dataOut[] = new double[15]; // Copy uniques hashmap, to use for keeping track of whats seen
        Iterator<Event> iterator = song.getEventIterator();

        while(iterator.hasNext()) { //While song has another event/chord
            Event event = iterator.next();

            for(char c: event.chord.toCharArray()) {
                if(c != ' ' && c != 'a') {
                    dataOut[(int)(c)-98] = 1.0;
                }
            }

        }

        return dataOut;
    }
}

class NumberUniqueChords implements Feature{

    @Override
    public String getName() {
        return "NumUniqueChords";
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        return new ArrayList<Attribute>(Arrays.asList(new Attribute("NumUniqueChords")));
    }

    @Override
    public double[] getFeatureData(Song song) {

        Iterator<Event> iterator = song.getEventIterator();
        HashSet<String> uniques = new HashSet<>();

        while(iterator.hasNext()) { //While song has another event/chord
            Event event = iterator.next();
            uniques.add(event.chord); //If not currently in set, it will be added
        }

        int numChords = uniques.size();

        return new double[]{numChords};
    }
}

class HighestFret implements Feature{

    @Override
    public String getName() {
        return "HighestFret";
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        return new ArrayList<Attribute>(Arrays.asList(new Attribute("HighestFret")));
    }

    @Override
    public double[] getFeatureData(Song song) {

        Iterator<Event> iterator = song.getEventIterator();
        int highestFret = 0;

        while(iterator.hasNext()) { //While song has another event/chord

            Event event = iterator.next();

            for(char c: event.chord.toCharArray()){
                //ignore non-played and open notes
                if (c == ' ' || c == 'a') {
                    continue;
                }
                if (c > highestFret) {
                    highestFret = c; // If new highest fret, update
                }
            }

        }

        return new double[]{highestFret - 97}; // -97 so 'b' (first fret) is 1 and not 98 (character value)
    }
}


// TODO following features...

class LargestFretStretch implements Feature{

    @Override
    public String getName() {
        return "LargestFretStretch";
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        return new ArrayList<Attribute>(Arrays.asList(new Attribute("LargestFretStretch")));

    }

    @Override
    public double[] getFeatureData(Song song) {

        int largestStretch = 0;
        Iterator<Event> songIterator = song.getEventIterator();

        while(songIterator.hasNext()) {
            String chord = songIterator.next().chord;
            int lowestFret = Integer.MAX_VALUE;
            int highestFret = 0;

            for (char c : chord.toCharArray()) {

                // ignore non-played and open notes
                if (c == ' ' || c == 'a') continue;

                int fret = c;
                if (fret < lowestFret) {
                    lowestFret = fret;
                }
                if (fret > highestFret) {
                    highestFret = fret;
                }
            }

            int stretch = highestFret - lowestFret;
            if (stretch > largestStretch) {
                largestStretch = stretch;
            }
        }


        return new double[]{largestStretch};
    }
}

class LargestStringStretch implements Feature{

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        return null;
    }

    @Override
    public double[] getFeatureData(Song song) {
        return new double[0];
    }
}

class RythymChanges implements Feature{

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        return null;
    }

    @Override
    public double[] getFeatureData(Song song) {
        return new double[0];
    }
}

class w implements Feature{

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ArrayList<Attribute> getAttributes() {
        return null;
    }

    @Override
    public double[] getFeatureData(Song song) {
        return new double[0];
    }
}
