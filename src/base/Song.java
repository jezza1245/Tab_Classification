package base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    public Song(File file){
        bars = new ArrayList<>();

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
