package physics;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gwen on 8/9/2016.
 */
public class GJK {

    static List<Vector3f> simplex = new ArrayList<>();
    static Vector3f direction = new Vector3f(0,1,0);

    public static boolean intersect(PolyShape shapeA, PolyShape shapeB){

        Vector3f point = new Vector3f();

        Vector3f.sub(support(direction, shapeA),support(direction.negate(null), shapeB),point);
        simplex.add(point);
        point.negate(direction);

        while(true){
            Vector3f.sub(support(direction, shapeA),support(direction.negate(null), shapeB),point);
            if(Vector3f.dot(point,direction)<0){
                return false;
            }
            simplex.add(new Vector3f(point));
            if (doSimplex()) {
                return true;
            }
        }
    }


    private static Vector3f rot(Matrix4f m, Vector3f v){
        Vector4f t = new Vector4f();

        t.x = v.x;
        t.y = v.y;
        t.z = v.z;
        t.w = 1;
        m.transform(m, t,t);
        Vector3f pos =new Vector3f();
        pos.x = t.x;
        pos.y = t.y;
        pos.z = t.z;
        return pos;
    }

    private static Vector3f support(Vector3f direction, PolyShape shape){

        Matrix4f world = shape.getWorldMatrix();
        Vector3f pos =rot(world,shape.vertList.get(0));

        float size = Vector3f.dot(direction, pos);
        Vector3f current = new Vector3f(pos);

        for(int i=0;i<shape.vertList.size();i++){

            pos =rot(world,shape.vertList.get(i));
            Vector3f.add(pos, shape.position, pos);
            float test = Vector3f.dot(direction, pos);

            if(test> size){
                size = test;
                current = new Vector3f(pos);
            }
        }
        return current;
    }

    private static boolean doSimplex(){
        switch(simplex.size()) {

            case 3:
                triSimplex();
                return false;
            case 4:
                return tetraSimplex();
            case 2:
                lineSimplex();
                return false;
            case 1:
                return false;
            default:
                System.err.println("Wrong Sized Simplex");
                assert false;
                return true;
        }
    }

    private static void lineSimplex(){
        Vector3f ab = new Vector3f();
        Vector3f.sub(simplex.get(0),simplex.get(1),ab);

        if(Vector3f.dot(ab,simplex.get(1).negate(null))>0){
            Vector3f.cross(ab,simplex.get(1).negate(null), direction);
            Vector3f.cross(direction,ab,direction);
        }else{
            direction = simplex.get(1).negate(null);
            simplex.remove(0);
        }
    }

    private static void triSimplex(){
        Vector3f a = simplex.get(2);
        Vector3f b = simplex.get(1);
        Vector3f c = simplex.get(0);
        Vector3f an = a.negate(null);

        Vector3f abc = new Vector3f();
        Vector3f ab = new Vector3f();
        Vector3f ac = new Vector3f();

        Vector3f.sub(b,a,ab);
        Vector3f.sub(c,a,ac);

        Vector3f.cross(ab,ac,abc);

        Vector3f test = new Vector3f();
        Vector3f.cross(abc,ac,test);
        if(Vector3f.dot(test,an)>0){

            if(Vector3f.dot(ac,an)>0){

                Vector3f.cross(ac,an, direction);
                Vector3f.cross(direction,ab,direction);
                simplex.remove(1);

            }else{

                if(Vector3f.dot(ab, an)>0){
                    Vector3f.cross(ab,an, direction);
                    Vector3f.cross(direction,ab,direction);
                    simplex.clear();
                    simplex.add(b);
                    simplex.add(a);
                }
                else{
                    System.out.println("not sure if this is possible");
                    direction = new Vector3f(an);
                    simplex.add(a);
                }

            }

        }else{

            Vector3f.cross(ab,abc,test);
            if(Vector3f.dot(test,an)>0){

                if(Vector3f.dot(ab, an)>0){
                    Vector3f.cross(ab,an, direction);
                    Vector3f.cross(direction,ab,direction);
                    simplex.clear();
                    simplex.add(b);
                    simplex.add(a);
                }
                else{
                    System.out.println("not sure if this is possible");
                    direction = new Vector3f(an);
                    simplex.add(a);
                }

            }else if(Vector3f.dot(abc,an)>0){
                direction = new Vector3f(abc);
            }else{
                simplex.clear();
                simplex.add(b);
                simplex.add(c);
                simplex.add(a);
                abc.negate(direction);
            }
        }
    }
    private static boolean tetraSimplex(){
        Vector3f a = new Vector3f(simplex.get(3));
        Vector3f b = new Vector3f(simplex.get(2));
        Vector3f c = new Vector3f(simplex.get(1));
        Vector3f d =new Vector3f( simplex.get(0));

        Vector3f an = a.negate(null);

        Vector3f ba = new Vector3f();
        Vector3f ca = new Vector3f();
        Vector3f da = new Vector3f();
        Vector3f bc = new Vector3f();
        Vector3f cd = new Vector3f();
        Vector3f db = new Vector3f();

        Vector3f.sub(a,b,ba);
        Vector3f.sub(a,c,ca);
        Vector3f.sub(a,c,da);
        Vector3f.sub(c,b,bc);
        Vector3f.sub(d,c,cd);
        Vector3f.sub(b,d,db);

        Vector3f test = new Vector3f();
        Vector3f.cross(bc,ba,test);
        boolean isDone = true;

        if(Vector3f.dot(test,an)>0){
            simplex.remove(0);
            direction = new Vector3f(test);
            isDone = false;
        }

        Vector3f.cross(cd,ca,test);

        if(Vector3f.dot(test,an)>0){
            simplex.remove(2);
            direction = new Vector3f(test);
            isDone = false;
        }

        Vector3f.cross(db,da,test);

        if(Vector3f.dot(test,an)>0){
            simplex.remove(1);
            direction = new Vector3f(test);
            isDone = false;
        }

        return isDone;

    }
}
