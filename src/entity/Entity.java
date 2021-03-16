package entity;

import main.Main;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import audio.Source;

import java.util.ArrayList;
import java.util.List;

public abstract class Entity {


	
	public Vector3f position = new Vector3f(0,0,0);
	public Vector3f rotation = new Vector3f(0,0,0);
	public Vector3f scale = new Vector3f(1,1,1);
	
	private Source source = null;

	public List<EntityAnimation> animations = new ArrayList<>();
	public EntityAnimation currentAnimation;
	public VaoData currentVaoData;
	public boolean play = false;
	protected float time = 0;
	protected int frame =0;
	
	public int getVaoID() {
		return currentVaoData.getVao();
	}
	
	public int getVertCount() {
		return currentVaoData.getVertSize();
	}
	
	public void update(){

	}

	/**
	 * changes animation
	 * @param name
	 * @return
     */
	public boolean changeAnimation(String name){
		boolean found = false;
		for(EntityAnimation anim: animations){
			if(anim.getName().equals(name)){
				currentAnimation = anim;
				found = true;
				frame = 0;
				time = 0;
				break;
			}
		}
		return found;
	}

	public void setSource(Source source) {

		this.source = source;
	}

	/**
	 * advances frame is need to
	 */
	protected void animate(){
		if(play){
			time += Main.getDelta();
			if(time> currentAnimation.lengths.get(frame)){
				time = 0;
				if(frame >= currentAnimation.lengths.size() -1){
					frame = 0;
				}
				else{
					frame ++;
				}
				currentVaoData = currentAnimation.vaos.get(frame);
			}
		}
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
		Matrix4f.scale(scale, m, m);
		return m;
	}
}
