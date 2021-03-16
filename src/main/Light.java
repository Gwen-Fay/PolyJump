package main;

import org.lwjgl.util.vector.Vector3f;

/**
 * Light object
 * @author gwen
 *
 */
public class Light {
	public Vector3f position = new Vector3f(0,0,0);
	public Vector3f color = new Vector3f(1,1,1);
	public Vector3f attenuation = new Vector3f(1,0,0);
	
	public Light(){
	}
	
	public Light(Vector3f position){
		this.position = position;
	}
	
	public Light(Vector3f position, Vector3f color){
		this.color = color;
	}
	
}
