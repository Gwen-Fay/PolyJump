package particle;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import main.Main;

public class Particle {

	public static final float GRAVITY = -9.8f;
	
	public Vector3f position = new Vector3f(10000,10000,10000);
	public Vector3f velocity = new Vector3f(0,0,0);
	public float gravityEffect =1;
	public float lifeLength =1;
	public float scale=1;
	
	public Vector4f color = new Vector4f(1,1,1,1);
	public Vector4f[] colorRange; 
	
	private Vector3f change = new Vector3f();
	private float timeElapsed = 0;
	public boolean isAlive = false;

	public Particle(Vector4f[] colorRange){
		this.colorRange = colorRange;
	}
	
	public void setParticle(Vector3f position,Vector3f velocity,float gravityEffect, float lifeLength, float scale){
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.scale = scale;
		isAlive = true;
		timeElapsed = 0;
	}
	
	public boolean update(){
		velocity.y += GRAVITY * gravityEffect * Main.getDelta();
		
		change.set(velocity);
		change.scale(Main.getDelta());
		Vector3f.add(change, position, position);
		timeElapsed+=Main.getDelta();
		
		isAlive = timeElapsed<lifeLength;
		if(isAlive){
			float age = (colorRange.length-1)*(timeElapsed/lifeLength);
			Vector4f prev = colorRange[(int) Math.floor(age)];
			Vector4f next = colorRange[(int) Math.ceil(age)];
			
			float step = (float) (age - Math.floor(age));
			color.x = prev.x + (step * (next.x-prev.x));
			color.y = prev.y + (step * (next.y-prev.y));
			color.z = prev.z + (step * (next.z-prev.z));
			color.w = prev.w + (step * (next.w-prev.w));
		}
		
		
		return isAlive;
		
	}
	
}
