package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entity.Billboard;
import entity.Box;
import entity.Capsule;
import entity.Cone;
import entity.ConvexHull;
import entity.Cylinder;
import entity.PointEntity;
import entity.PolyEntity;
import entity.Sphere;
import particle.ParticleSystem;
import render.Loader;
import render.Renderer;
import water.Water;

public class Main {
	
	static List<Light> lights = new ArrayList<Light>();
	static List<PointEntity> pointEnts = new ArrayList<PointEntity>();
	static List<PolyEntity> polyEnts = new ArrayList<PolyEntity>();
	static List<Water> waters = new ArrayList<Water>();
	static List<ParticleSystem> particles = new ArrayList<ParticleSystem>();

	private static long lastFrameTime =Sys.getTime()*1000/Sys.getTimerResolution();
	private static float delta;
	
	public static void main(String[] args) {
		
		Camera camera = new Camera();
		
		Loader loader = new Loader();
		Renderer renderer = new Renderer(camera, loader, null);
		

		Camera.createProjectionMatrix();
		
		Light light = new Light();
		light.color = new Vector3f(1f,1f,1f);
		light.position = new Vector3f(0,10000,10000);
		lights.add(light);
		
		Light light2 = new Light();
		light2.color = new Vector3f(1,1,1);
		light2.position = new Vector3f(0,10,0);
		light2.attenuation = new Vector3f(1,0.0f,0.0f);
		//lights.add(light2);

		/* creates model (temp) */
		
		PointEntity ent1 = new PointEntity("megamanDemo");
		ent1.position = new Vector3f(2,0.3f,2);
		ent1.rotation = new Vector3f(0,180,0);
		ent1.play = true;
		pointEnts.add(ent1);

		Random r = new Random();
		for(int i=0;i<50;i++){

			PointEntity ent = new PointEntity("megamanDemo");
			ent.position = new Vector3f(r.nextInt(10)-5,0.3f,r.nextInt(10)-5);
			ent.rotation = new Vector3f(0,180,0);
			ent.play = true;
			pointEnts.add(ent);
		}

		
		//ent1.scale = 2f;
		//camera.focus = ent1.position;
		Sphere sph = new Sphere("textures/default","textures/norm");
		sph.isTransparent = true;
		sph.position =  new Vector3f(-4,5f,-4);
		//camera.focus = sph.position;
		sph.scale = new Vector3f(2f,2f,2f);
		sph.textureScale = sph.scale;
		polyEnts.add(sph);
		
		Box ground1 = new Box("textures/default","textures/norm");
		ground1.scale = new Vector3f(10,10,10);
		ground1.position = new Vector3f(7,-5,3);
		ground1.textureScale = ground1.scale;
		
		Box ground2 = new Box("textures/default","textures/norm");
		ground2.scale = new Vector3f(10,10,10);
		ground2.position = new Vector3f(3,-5,-7);
		ground2.textureScale = ground2.scale;
		
		Box ground3 = new Box("textures/default","textures/norm");
		ground3.scale = new Vector3f(10,10,10);
		ground3.position = new Vector3f(-3,-5,7);
		ground3.textureScale = ground3.scale;
		
		Box ground4 = new Box("textures/default","textures/norm");
		ground4.scale = new Vector3f(10,10,10);
		ground4.position = new Vector3f(-7,-5,-3);
		ground4.textureScale = ground4.scale;
		
		Billboard ground5 = new Billboard("textures/default","textures/norm");
		ground5.scale = new Vector3f(4,2,4);
		ground5.position = new Vector3f(0,-10,0);
		ground5.textureScale = ground5.scale;
		
		polyEnts.add(ground1);
		polyEnts.add(ground2);
		polyEnts.add(ground3);
		polyEnts.add(ground4);
		polyEnts.add(ground5);
		
		Box box = new Box("textures/default","textures/norm");
		box.isTransparent = true;
		box.position = new Vector3f(4,1.5f,0);
		box.scale = new Vector3f(3f,3f,1f);
		box.textureScale = box.scale;
		polyEnts.add(box);
		
		GUIEntity gui = new GUIEntity("textures/norm");
		gui.scale = new Vector2f(0.5f,0.5f);
		
		Cylinder cyn= new Cylinder("textures/default","textures/norm");
		cyn.isTransparent = true;
		cyn.position = new Vector3f(-4,2,0);
		cyn.scale = new Vector3f(2f,4f,2f);
		cyn.textureScale = cyn.scale;
		polyEnts.add(cyn);
		
		Cone cone= new Cone("textures/default","textures/norm");
		cone.isTransparent = true;
		cone.position = new Vector3f(0,2,5);
		cone.scale = new Vector3f(3f,4f,3f);
		cone.textureScale = cone.scale;
		polyEnts.add(cone);
		
		ConvexHull hull= new ConvexHull("textures/default","textures/norm","env/Monkey");
		hull.isTransparent = true;
		hull.position = new Vector3f(4,2,2);
		hull.scale = new Vector3f(1f,1f,1f);
		hull.textureScale = hull.scale;
		polyEnts.add(hull);
		
		Capsule cap= new Capsule("textures/default","textures/norm",3,1);
		cap.isTransparent = true;
		cap.position = new Vector3f(0,1f,-4);
		polyEnts.add(cap);

		Billboard bill = new Billboard("textures/default","textures/norm");
		bill.position = new Vector3f(-4,2,4);
		bill.scale = new Vector3f(2,2,2);
		bill.textureScale = bill.scale;
		bill.isTransparent = true;
		bill.rotation.x = 45;
		polyEnts.add(bill);
		//alphbeta  8bitlim  8bitlimo
		/* create text */
		
		Vector4f[] colorSteps = new Vector4f[]{
				new Vector4f(1,0,0,1),
				new Vector4f(1,1,0,1),
				new Vector4f(0.2F,0.2F,0.2F,1),
				new Vector4f(0,0,0,1)
		};
		ParticleSystem ps = new ParticleSystem(100.0f,0.5f,0.01f,2.0f,1.0f,220, new Vector3f(5,5,-5), colorSteps);
		particles.add(ps);
		//ps.isAdditive = true;
		/* game loop */
		
		Water water = new Water(new Vector3f(0,-0.25f,0),new Vector2f(4,4));
		waters.add(water);
		
		while(!Display.isCloseRequested() && !(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))){
			calcDeltaTime();
			/* prepares to render Frame */
			System.out.println(1.0/delta);
			
			/* game logic */
			camera.thirdPersonMove();
			
			renderer.rendererShadowMap(pointEnts, polyEnts, light);
			//ent1.rotation.y += 0.3;
			loader.loadWater(waters);
			loader.loadPolyEnt(polyEnts);

			//renderer.loadText(text);
			loader.loadPointEntity(pointEnts);
			
			loader.loadParticles(particles);
			
			//renderer.loadGUI(gui);
			//for(Environment env:environments){
				//renderer.loadEnvironment(env);
				//ent.rotation.y += 0.3;
				//ent.rotation.z += 0.3;
			//}
			
			/* Render */
			renderer.render(camera,lights);

			Display.sync(Renderer.FPS_CAP);
			Display.update();
		}
		/* close game */
		renderer.cleanUp();
		Display.destroy();
		
	}

	private static void calcDeltaTime(){
		//lastFrameTime = Sys.getTime()*1000/Sys.getTimerResolution();
		long currentFrameTime = Sys.getTime()*1000/Sys.getTimerResolution();
		delta = (currentFrameTime - lastFrameTime)/1000f;
		lastFrameTime = currentFrameTime;

	}
	
	public static float getDelta(){
		return delta;
	}

}
