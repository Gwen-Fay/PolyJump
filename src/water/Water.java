package water;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import render.Loader;

public class Water {
	
	public Vector3f position;
	public Vector3f rotation= new Vector3f(-90,0,0);
	public Vector2f scale;
	
	public static final String DUDV_PNG = "water/waterDUDV";
	public static final int DUDV_TEX = Loader.loadTexture(DUDV_PNG);
	
	public static final float WAVE_SPEED = 0.03f;
	public static float move = 0;
	
	public Water(Vector3f position, Vector2f scale) {
		super();
		this.position = position;
		this.scale = scale;
	}
	
	/**
	 * returns world transformation
	 * @return Matrix4f World transformation
	 * 
	 */
	public Matrix4f getWorldMatrix(){
		Matrix4f m = new Matrix4f();
		m.setIdentity();
		Matrix4f.translate(position,m,m);
		Matrix4f.rotate((float)Math.toRadians(rotation.x),new Vector3f(1,0,0), m, m);
		Matrix4f.rotate((float)Math.toRadians(rotation.y),new Vector3f(0,1,0), m, m);
		Matrix4f.rotate((float)Math.toRadians(rotation.z),new Vector3f(0,0,1), m, m);
		Matrix4f.scale(new Vector3f(scale.x/2.0f,scale.y/2.0f,1), m, m);
		return m;
	}
}
