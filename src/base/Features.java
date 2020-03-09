package base;

import weka.core.Attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
