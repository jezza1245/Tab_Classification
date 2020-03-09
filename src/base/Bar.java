package base;

import java.util.ArrayList;
import java.util.Iterator;

public class Bar {
    private ArrayList<Event> events;

    public Bar(){
        this.events = new ArrayList<>();
    }

    public void addEvent(Event e){ events.add(e); }
    public ArrayList<Event> getEvents(){
        return events;
    }

    public Iterator<Event> getEventIterator(){
        return new EventIterator();
    }

    public class EventIterator implements Iterator{

        int currentIndex = 0;

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
