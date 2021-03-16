package modelEdit;

import tree.OctNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * contains all point data for a single frame
 * Created by gwen on 7/20/16.
 */
public class Frame {
    //public File entity;
    //public File edit;
    public String name;

    public List<Vert> verts = new ArrayList<Vert>();
    public OctNode<Vert> octVerts = new OctNode<Vert>(10,new int[]{0,0,0});

    public Frame(){

    }
    public Frame(String name){
        this.name = name;
    }
}
