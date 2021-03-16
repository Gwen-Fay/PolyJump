package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains all data for an animation
 */
public class EntityAnimation {
    public List<VaoData> vaos = new ArrayList<VaoData>();
    public List<Float> lengths = new ArrayList<Float>();

    private String name;

    public EntityAnimation(String name){
        this.name = name;
    }

    public String getName(){
        return new String(name);
    }
}
