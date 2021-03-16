package main;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 * Camera object, used so the renderer knows from what perspective to render the scene.
 * @author gwen
 *
 */
public class Camera {
	
	public Vector3f position = new Vector3f(0,0,-5);
	public Vector3f rotation = new Vector3f(30,0,0);
	
	
	//used in thirdPersonMove 
	
	//point which camera always focuses:
	public Vector3f focus = new Vector3f(0,0,0);
	//distance from focus point:
	public float distance = 10;
	//angle around focus point:
	public float theta = 0;

	public static final float NEAR_PLANE = 0.2f;
	public static final float FAR_PLANE = 1000;
	
	public static float fov = 70;
	public static Matrix4f projection;
	public static Matrix4f view = new Matrix4f();
	
	public Camera(){
		if(projection ==null){createProjectionMatrix();}
	}
	
	public Camera(Camera camera){
		this.position = camera.position;
		this.rotation = camera.rotation;
		
		this.focus = camera.focus;
		this.distance = camera.distance;
		this.theta = camera.theta;
	}
	
	public Camera(Vector3f position, Vector3f rotation){
		this.position=position;
		this.rotation=rotation;
		
		if(projection ==null){createProjectionMatrix();}
	}
	
	/**
	 * creates and returns the view transformation.
	 * @return
	 */
	public Matrix4f getViewMatrix(){
		view.setIdentity();
		Matrix4f.rotate((float)Math.toRadians(rotation.x),new Vector3f(1,0,0), view, view);
		Matrix4f.rotate((float)Math.toRadians(rotation.y),new Vector3f(0,1,0), view, view);
		Matrix4f.rotate((float)Math.toRadians(rotation.z),new Vector3f(0,0,1), view, view);
		Matrix4f.translate(new Vector3f(-position.x,-position.y,-position.z),view,view);
		return view;
	}
	
	/**
	 * creates projecton transformation
	 */
	public static void createProjectionMatrix(){
		float aspectRatio = (float)Display.getWidth()/(float)Display.getHeight();
		float y_scale = (float)(1f/ Math.tan(Math.toRadians(fov/2f)));
		float x_scale = y_scale/aspectRatio;
		float fustrum_length = FAR_PLANE-NEAR_PLANE;
		
		projection = new Matrix4f();
		projection.m00=x_scale;
		projection.m11=y_scale;
		projection.m22=-((FAR_PLANE+NEAR_PLANE)/fustrum_length);
		projection.m23 = -1;
		projection.m32 = -((2*NEAR_PLANE*FAR_PLANE)/fustrum_length);
		projection.m33 = 0;

	}
	
	/**
	 * moves position and rotation based on focus, direction, and mouse inputs
	 */
	public void thirdPersonMove(){
		distance += Mouse.getDWheel() * 0.01f;
		if(Mouse.isButtonDown(1)){
			rotation.x -= Mouse.getDY() *0.1f;
			theta -= Mouse.getDX()*0.3f;
		}
		float h = (float) (distance*Math.cos(Math.toRadians(rotation.x)));
		float v = (float) (distance*Math.sin(Math.toRadians(rotation.x)));
		
		position.x = focus.x - (float)(h*Math.sin(Math.toRadians(theta)));
		position.y = focus.y + v;
		
		position.z = focus.z - (float)(h*Math.cos(Math.toRadians(theta)));
		
		rotation.y = 180-theta;
	}
	

}
