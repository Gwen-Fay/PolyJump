package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gwen on 8/6/2016.
 */
public class VaoData {
    private int vao;
    private int vertSize;
    private String name;

    public VaoData(int vao, int vertSize, String name){
        this.vao = vao;
        this.vertSize = vertSize;
        this.name = name;
    }

    public int getVao(){
        return vao;
    }

    public int getVertSize(){
        return vertSize;
    }
    public String getName(){
        return name;
    }
}
