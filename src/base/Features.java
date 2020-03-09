package base;

import weka.core.Attribute;

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
