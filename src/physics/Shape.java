package physics;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Created by Gwen on 8/9/2016.
 */
public class Shape {

    Vector3f position = new Vector3f(0,0,0);
    Vector3f rotation = new Vector3f(0,0,0);
    Vector3f scale = new Vector3f(1,1,1);

    public Matrix4f getWorldMatrix(){
        Matrix4f m = new Matrix4f();
        m.setIdentity();
        Matrix4f.translate(position,m,m);
        Matrix4f.rotate((float)Math.toRadians(rotation.x),new Vector3f(1,0,0), m, m);
        Matrix4f.rotate((float)Math.toRadians(rotation.y),new Vector3f(0,1,0), m, m);
        Matrix4f.rotate((float)Math.toRadians(rotation.z),new Vector3f(0,0,1), m, m);
        Matrix4f.scale(scale, m, m);
        return m;
    }
}
