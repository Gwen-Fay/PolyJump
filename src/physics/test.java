package physics;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Created by Gwen on 8/9/2016.
 */
public class test {
    public static void main(String[] args){

        PolyShape shapeA = new PolyShape();
        PolyShape shapeB = new PolyShape();

        shapeA.position = new Vector3f(0,0,0);
        shapeB.position = new Vector3f(0.80f,0.80f,0);

        shapeA.vertList.add(new Vector3f(1,0,0));
        shapeA.vertList.add(new Vector3f(-1,0,0));
        shapeA.vertList.add(new Vector3f(0,1,0));
        shapeA.vertList.add(new Vector3f(0,-1,0));
        shapeA.vertList.add(new Vector3f(0,0,1));
        shapeA.vertList.add(new Vector3f(0,0,-1));


        shapeB.vertList.add(new Vector3f(1,1,1));
        shapeB.vertList.add(new Vector3f(1,1,-1));
        shapeB.vertList.add(new Vector3f(1,-1,1));
        shapeB.vertList.add(new Vector3f(1,-1,-1));
        shapeB.vertList.add(new Vector3f(-1,1,1));
        shapeB.vertList.add(new Vector3f(-1,1,-1));
        shapeB.vertList.add(new Vector3f(-1,-1,1));
        shapeB.vertList.add(new Vector3f(-1,-1,-1));

        System.out.println("test2");
        System.out.println(GJK.intersect(shapeA,shapeB));
    }
}
