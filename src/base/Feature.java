package base;

import weka.core.Attribute;

import java.util.ArrayList;

public interface Feature {
    String getName();
    ArrayList<Attribute> getAttributes();
    double[] getFeatureData(Song song);
}