package base;

import java.util.ArrayList;
import java.util.Arrays;

public class Event {

    // These parts make up the rhythm information of an event
    private ArrayList<String> rhythmFlags = new ArrayList<String>(
            Arrays.asList("B","W","w","0","1","2","3","4","5","L","x")
    );

    // These parts make up the chord information of an event
    private char[] chordValues = {' ','a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p'};

    public String rhythmIndicator = "x"; // Rhythm flag/indicator displayed (if empty, same as previous note/event)
    //public char duration; // Directly related to rhythm indicator however contains the duration of the event
    public String chord = "";


    public Event(String eventLine){
        this.extractRhythmComponents(eventLine);
        this.extractChordComponents(eventLine);
    }

    private void extractRhythmComponents(String eventLine){
        // Rhythm Indicator
        for(String rhythmIndicator: rhythmFlags){
            if(eventLine.contains(rhythmIndicator)){
                this.rhythmIndicator = rhythmIndicator;
                break;
            }
        }
    }

    private void extractChordComponents(String eventLine){
        String possibleValues = new String(chordValues);
        for(char note: eventLine.toCharArray()){
            if(possibleValues.indexOf(note) != -1){
                chord+=note;
            }
        }

        // Ignore trailing spaces
        char chordChars[] = chord.toCharArray();
        for(int i=chordChars.length-1; i>=0; i--){
            if(chordChars[i]==' '){
                chord = chord.substring(0,i);
            }else{
                break;
            }
        }
    }



    public static void main(String[] args) {
        System.out.println("Hello");
        Event event = new Event("1 ab cd");
    }
}
