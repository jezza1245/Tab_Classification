package myPackage;

import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Iterator;

interface Features {
    String getName();
    ArrayList<Attribute> getAttributes();
    ArrayList<Double> getFeatureData(Song song);
}

class ChordCounts implements Features{

    ArrayList<String> uniques = new TabParser().generateUniqueChords(Main.folder, new ArrayList<>());

    @Override
    public String getName(){
        return "chordCounts";
    }

    @Override
    public ArrayList<Attribute> getAttributes(){

        ArrayList<Attribute> attributes = new ArrayList<>();

        uniques.forEach(chord -> {
            attributes.add(new Attribute(chord.replaceAll(" ", "|")));
        });

        return attributes;
    }

    @Override
    public ArrayList<Double> getFeatureData(Song song) {

        ArrayList<Double> data = new ArrayList<>();

        for (int i = 0; i < uniques.size(); i++) {

            int count = 0;

            Iterator<Event> iterator = song.getEventIterator();

            while(iterator.hasNext()) {
                Event event = iterator.next();

                if (event.chord.equals(uniques.get(i))) {
                    count++;
                }
            }

        }

        return data;

    }

}

//
//    public class ChordUsed{
//
//        public ArrayList<Attribute> getAttributes(){
//
//            ArrayList<Attribute> attributes = new ArrayList<>();
//
//            ArrayList<String> uniques = new TabParser().generateUniqueChords(Main.folder, new ArrayList<>());
//
//            uniques.forEach(chord -> {
//                attributes.add(new Attribute(chord.replaceAll(" ", "|")));
//            });
//
//            return attributes;
//        }
//
//    }
//
//    public class FretCounts{
//
//        public ArrayList<Attribute> getAttributes(){
//
//            ArrayList<Attribute> attributes = new ArrayList<>();
//
//            ArrayList<String> uniques = new TabParser().generateUniqueChords(Main.folder, new ArrayList<>());
//
//            uniques.forEach(chord -> {
//                attributes.add(new Attribute(chord.replaceAll(" ", "|")));
//            });
//
//            return attributes;
//        }
//
//    }
//
//    public class FretUsed{
//
//        public ArrayList<Attribute> getAttributes(){
//
//            ArrayList<Attribute> attributes = new ArrayList<>();
//
//            ArrayList<String> uniques = new TabParser().generateUniqueChords(Main.folder, new ArrayList<>());
//
//            uniques.forEach(chord -> {
//                attributes.add(new Attribute(chord.replaceAll(" ", "|")));
//            });
//
//            return attributes;
//        }
//
//    }
//
//
//
//
//}
