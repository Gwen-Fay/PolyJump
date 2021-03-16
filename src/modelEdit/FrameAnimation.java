package modelEdit;

import java.util.ArrayList;
import java.util.List;

/**
 * simple struct, used to hold a frame, and a length of time
 * Created by gwen on 7/20/16.
 */
public class FrameAnimation {

    public List<Frame> frames = new ArrayList<>();
    public List<Float> lengths = new ArrayList<>();
    public String name;

    public FrameAnimation(String name){
        this.name = name;
    }
}
