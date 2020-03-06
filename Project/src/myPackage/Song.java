package myPackage;

import java.util.ArrayList;
import java.util.Iterator;

public class Song {

    private ArrayList<Bar> bars;

    public Song(){
        this.bars = new ArrayList<>();
    }
    public Song(ArrayList<Bar> bars){
        this.bars = bars;
    }

    public void addBar(Bar bar){ bars.add(bar); }


    public Iterator<Bar> getBarIterator(){
        return new BarIterator();
    }

    private class BarIterator implements Iterator{

        int currentIndex = 0;

        @Override
        public boolean hasNext() {
            return bars.size() > currentIndex;
        }

        @Override
        public Object next() {
            return bars.get(currentIndex++);
        }

    }

    public Iterator<Event> getEventIterator(){
        return new EventIterator();
    }

    private class EventIterator implements Iterator{

        int currentIndex = 0;
        private ArrayList<Event> events = new ArrayList<>();

        public EventIterator(){
            bars.forEach(bar -> {
                events.addAll(bar.getEvents());
            });
        }

        @Override
        public boolean hasNext() {
            return events.size() > currentIndex;
        }

        @Override
        public Object next() {
            return events.get(currentIndex++);
        }

    }


}
