package modelEdit;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * Vert object to keep track of properties of a point
 * @author gwen
 *
 */
public class Vert {
	public Vector3f position;
	public Vector4f color;
	public Vector3f normal;
	public float specular;
	public float glow;
	
	//Model Editor GUI stuff
	public static Vector4f cursor1Color = new Vector4f(0,0,1,1);
	public static Vector4f cursor2Color = new Vector4f(0,1,0,1);
	public static Vector4f cursor3Color = new Vector4f(1,0,0,1);
	public static Vector4f sellectedColor = new Vector4f(1,1,1,1);
	
	public Vert(){	
	}
	
	public Vert(Vert vert){
		this.position = new Vector3f(vert.position);
		this.color = new Vector4f(vert.color);
		this.normal = new Vector3f(vert.normal);
		this.specular = vert.specular;
		this.glow = vert.glow;
	}
}
