package render;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entity.PointEntity;
import entity.PolyEntity;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import main.Camera;
import main.GUIEntity;
import main.Light;
import main.Main;
import main.ShaderProgram;
import particle.ParticleSystem;
import shadows.ShadowBox;
import shadows.ShadowMapMasterRenderer;
import water.Water;

public class Renderer {

	public ShaderProgram pointEntShader, textShader, guiShader, polyEntShader, partShader, waterShader;
	public ShaderProgram postShader, vBlurShader, hBlurShader, decalShader;
	
	public static final int ENT_BIT = 1,
							TEXT_BIT = 2,
							GUI_BIT = 4,
							ENV_BIT = 8,
							PART_BIT = 16,
							WATER_BIT = 32;

	//normal
	public int mode = ENT_BIT | TEXT_BIT | GUI_BIT | ENV_BIT | PART_BIT | WATER_BIT;

	//mode = ENT_BIT | TEXT_BIT | GUI_BIT | ENV_BIT | PART_BIT ;
	
	public static int WIDTH = 1920;
	public static int HEIGHT = 1080;
	public static final int FPS_CAP = 120;
	
	protected static Vector4f clipPlane = new Vector4f(0,-1,0,100000f);	
	
	protected static ShadowMapMasterRenderer shadowRenderer;
	
	protected Fbo msFbo, mainFbo;
	
	protected Fbo refractFbo, reflectFbo;
	
	protected Fbo hBlurFbo, vBlurFbo;
	
	//vaoID for GUI Quad
	protected int quadID;
	
	private Loader loader;

	private static final float R = 1/64f;
	public static final float[] CUBE = new float[]{
			-R,-R,-R, //0
			-R,-R,R, // 1
			-R,R,-R, //2
			-R,R,R, //3
			R,R,-R, //4
			R,R,R,//5
			R,-R,R,//6
			R,-R,-R,//7
	};

	public static final int[] CUBE_INDEX = new int[]{
		0,1,2,3,4,5,6,3,1,0,6,7,4,0,2
	};

	public Renderer(Camera camera, Loader loader, Canvas canvas){
		
		this.loader = loader;
		
		createDisplay(canvas);
		
		shadowRenderer = new ShadowMapMasterRenderer(camera);
		
		quadID = GL30.glGenVertexArrays();
		Loader.addVao(quadID);
		GL30.glBindVertexArray(quadID);
		Loader.loadToVBO(0,new float[]{-1,1,-1,-1,1,1,1,-1},2);
		GL30.glBindVertexArray(0);

		
		pointEntShader = new ShaderProgram("src/shaders/shaderVert","src/shaders/shaderFrag",new int[] {0,1,2,3,4},new String[] {"pos","color","norm","glow","cube"});
		textShader = new ShaderProgram("src/shaders/fontVert","src/shaders/fontFrag",new int[] {0,1},new String[]{"pos","tex"});
		guiShader = new ShaderProgram("src/shaders/guiVert","src/shaders/guiFrag",new int[] {0},new String[]{"pos"});
		polyEntShader = new ShaderProgram("src/shaders/envVert","src/shaders/envFrag",new int[] {0,1,2},new String[]{"pos","tex","norm",});
		partShader = new ShaderProgram("src/shaders/partVert","src/shaders/shaderFrag",new int[] {0,1,2},new String[] {"pos","color","cube"});
		
		postShader = new ShaderProgram("src/postShaders/postVert","src/postShaders/postFrag",new int[] {0},new String[] {"pos"});
		hBlurShader = new ShaderProgram("src/postShaders/blurHorizVert","src/postShaders/blurFrag",new int[] {0},new String[] {"pos"});
		vBlurShader = new ShaderProgram("src/postShaders/blurVerticalVert","src/postShaders/blurFrag",new int[] {0},new String[] {"pos"});
		
		waterShader = new ShaderProgram("src/water/waterVert","src/water/waterFrag",new int[] {0},new String[] {"pos"});
		
		msFbo = new Fbo(WIDTH,HEIGHT, true);
		msFbo.createDepthAttachment();
		msFbo.createColorMSAttachment();
		
		mainFbo = new Fbo(WIDTH,HEIGHT, false);
		mainFbo.createTextureAttacment(Fbo.LIN);
		mainFbo.createDepthTextureAttahcment();
		
		refractFbo = new Fbo(WIDTH/2,HEIGHT/2, false);
		refractFbo.createTextureAttacment(Fbo.LIN);
		refractFbo.createDepthTextureAttahcment();
		
		reflectFbo = new Fbo(WIDTH/2,HEIGHT/2, false);
		reflectFbo.createTextureAttacment(Fbo.LIN);
		reflectFbo.createDepthAttachment();
		
		vBlurFbo = new Fbo(WIDTH/2,HEIGHT/2, false);
		vBlurFbo.createTextureAttacment(Fbo.LIN);
		
		hBlurFbo = new Fbo(WIDTH/2,HEIGHT/2, false);
		hBlurFbo.createTextureAttacment(Fbo.LIN);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
		
	}

	/**
	 * cleans up by deleting VAOs, VBOs, and Textures from memory
	 */
	public void cleanUp(){
		pointEntShader.cleanUp();
		textShader.cleanUp();
		guiShader.cleanUp();
		polyEntShader.cleanUp();
		partShader.cleanUp();
		postShader.cleanUp();
		hBlurShader.cleanUp();
		vBlurShader.cleanUp();

		Fbo.cleanUp();
	}
	
	/**
	 * Creates Display with LWJGL
	 */
	private void createDisplay(Canvas canvas){
		
		ContextAttribs attribs = new ContextAttribs(3,3).withForwardCompatible(true).withProfileCore(true);
		
		try {
			if(canvas != null){
				Display.setParent(canvas);
				Display.setDisplayMode(new DisplayMode(canvas.getWidth(),canvas.getHeight()));
				Display.create(new PixelFormat().withDepthBits(24), attribs);
				WIDTH = canvas.getWidth();
				HEIGHT = canvas.getHeight();

			}else{
				Display.setDisplayMode(new DisplayMode(WIDTH,HEIGHT));
				Display.create(new PixelFormat().withDepthBits(24), attribs);
			}

			GL11.glEnable(GL13.GL_MULTISAMPLE);
		} catch (LWJGLException e) {
			System.err.println("ERR! Failed to create Display");
			e.printStackTrace();
		}
		
		GL11.glViewport(0,0,WIDTH,HEIGHT);

		GL11.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);

	}
	
	
	public void rendererShadowMap(List<PointEntity> pointEnts, List<PolyEntity> polyEnts, Light sun){
		
		loader.loadPointEntity(pointEnts);
		loader.loadPolyEnt(polyEnts);
		
		shadowRenderer.render(loader.getPointEntities(), loader.getEnvironments(), sun);
		loader.clearLists();
	}
	
	public static int getShadowMapTex(){
		return shadowRenderer.getShadowMap();
	}
	
	/**
	 * Renders the scene of entites in the map
	 * @param camera
	 */
	public void render(Camera camera, List<Light> lights){
		
		List<Vector3f> position = new ArrayList<Vector3f>();
		List<Vector3f> color = new ArrayList<Vector3f>();
		List<Vector3f> att = new ArrayList<Vector3f>();
		for(Light light:lights){
			position.add(light.position);
			color.add(light.color);
			att.add(light.attenuation);
		}
		//entities
		pointEntShader.start();
		
		pointEntShader.setUniform(Camera.projection, "proj");
		pointEntShader.setUniform(camera.getViewMatrix(), "view");
		pointEntShader.setUniform(position,8,new Vector3f(0,0,0), "lightPos");
		pointEntShader.setUniform(color,8,new Vector3f(0,0,0), "lightColor");
		pointEntShader.setUniform(att,8,new Vector3f(1,0,0), "attenuation");
		pointEntShader.stop();

		if((mode & WATER_BIT)==WATER_BIT) {

			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

			reflectFbo.bindMe();

			float d;
			if (!loader.getWaters().isEmpty()) {
				Water w = loader.getWaters().get(0);
				clipPlane = new Vector4f(0, 1, 0, -w.position.y + 0.1f);
				d = 2 * (camera.position.y - w.position.y);
			} else {
				clipPlane = new Vector4f(0, 1, 0, 100000f);
				d = 0;

			}
			camera.position.y -= d;
			camera.rotation.x = -camera.rotation.x;
			camera.rotation.z = -camera.rotation.z;


			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			pointEntShader.start();
			pointEntShader.setUniform(camera.getViewMatrix(), "view");
			pointEntShader.stop();

			if ((mode & ENT_BIT) == ENT_BIT) {
				renderPointEntities(false);
			}
			if ((mode & ENV_BIT) == ENV_BIT) {
				renderPolyEntities(camera, position, color, att, false);
			}
			if ((mode & PART_BIT) == PART_BIT) {
				renderParticles(camera, false);
			}

			camera.position.y += d;
			camera.rotation.x = -camera.rotation.x;
			camera.rotation.z = -camera.rotation.z;
			pointEntShader.start();
			pointEntShader.setUniform(camera.getViewMatrix(), "view");
			pointEntShader.stop();

			Fbo.unbindCurrentFrameBuffer();

			refractFbo.bindMe();

			if (!loader.getWaters().isEmpty()) {
				clipPlane = new Vector4f(0, -1, 0, loader.getWaters().get(0).position.y + 0.1f);
			} else {
				clipPlane = new Vector4f(0, 1, 0, 100000f);
			}

			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			if ((mode & ENT_BIT) == ENT_BIT) {
				renderPointEntities(false);
			}
			if ((mode & ENV_BIT) == ENV_BIT) {
				renderPolyEntities(camera, position, color, att, false);
			}

			Fbo.unbindCurrentFrameBuffer();

			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
		}
		msFbo.bindMe();
		
		clipPlane = new Vector4f(0,-1,0,100000f);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
		
		if((mode & ENT_BIT)==ENT_BIT){ renderPointEntities(true); }
		if((mode & ENV_BIT)==ENV_BIT){renderPolyEntities(camera, position,color,att,true);}
		if((mode & PART_BIT)==PART_BIT) {renderParticles(camera,true); }
		if((mode & WATER_BIT)==WATER_BIT) {renderWater(camera,lights.get(0),true); }
		
		renderToScreen(camera);
		
		if((mode & GUI_BIT)==GUI_BIT) {renderGUIs(); }
		if((mode & TEXT_BIT)==TEXT_BIT) {renderFonts(); }
		
		loader.clearLists();

	}
	
	
	protected void renderToScreen(Camera camera){
		
		GL30.glBindVertexArray(quadID);
		GL20.glEnableVertexAttribArray(0);

		msFbo.resolveToFbo(mainFbo);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
;
		screen(mainFbo);
		
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
	}
	
	protected void vBlur(Fbo fbo){
		vBlurFbo.bindMe();
		vBlurShader.start();
		vBlurShader.setUniform(0, "colourTexture");
		vBlurShader.setUniform((float)vBlurFbo.getHeight(), "targetHeight");
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.getColorTex());
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		
		vBlurShader.stop();
	}
	
	protected void hBlur(Fbo fbo){
		hBlurFbo.bindMe();
		hBlurShader.start();
		hBlurShader.setUniform(0, "colourTexture");
		hBlurShader.setUniform((float)hBlurFbo.getWidth(), "targetWidth");
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.getColorTex());
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		
		hBlurShader.stop();
	}
	
	protected void screen(Fbo fbo){
		Fbo.unbindCurrentFrameBuffer();
		postShader.start();
		postShader.setUniform(0, "colourTexture");
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.getColorTex());
		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		
		postShader.stop();
	}
	
	/**
	 * Renders GUIS from batches
	 */
	protected void renderGUIs(){
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		GL30.glBindVertexArray(quadID);
		GL20.glEnableVertexAttribArray(0);
		guiShader.start();
			for(int id:loader.getGuis().keySet()){
				guiShader.setUniform(0, "textureSamp");
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
				
				for(GUIEntity gui:loader.getGuis().get(id)){
					guiShader.setUniform(gui.createTransformation(), "transformation");
					guiShader.setUniform(gui.color, "color");
					GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
				}
			}
		guiShader.stop();
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	protected void renderWater(Camera camera, Light light, boolean update){
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		waterShader.start();
		GL30.glBindVertexArray(quadID);
		GL20.glEnableVertexAttribArray(0);
		if(update) {
			Water.move += Water.WAVE_SPEED * Main.getDelta();
			Water.move %= 1;
		}
		waterShader.setUniform(Water.move, "move");
		waterShader.setUniform(camera.position, "cameraPos");
		
		waterShader.setUniform(light.position, "lightPos");
		waterShader.setUniform(light.color, "lightColor");
		
		waterShader.setUniform(Camera.projection, "proj");
		waterShader.setUniform(camera.getViewMatrix(), "view");
		
		waterShader.setUniform(Camera.NEAR_PLANE, "near");
		waterShader.setUniform(Camera.FAR_PLANE, "far");
		
		for(Water water : loader.getWaters()){
			waterShader.setUniform(water.getWorldMatrix(), "trans");
			waterShader.setUniform(0, "reflection");
			waterShader.setUniform(1, "refraction");
			waterShader.setUniform(2, "dudv");
			waterShader.setUniform(3, "depth");
			
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, reflectFbo.getColorTex());
			
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, refractFbo.getColorTex());
			
			GL13.glActiveTexture(GL13.GL_TEXTURE2);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, Water.DUDV_TEX);
			
			GL13.glActiveTexture(GL13.GL_TEXTURE3);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, refractFbo.getdepthTex());
			
			GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, 4);
		}
		
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		waterShader.stop();
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	protected void renderParticles(Camera camera, boolean update){
		
		GL11.glEnable(GL11.GL_BLEND);
		
		partShader.start();
		
		partShader.setUniform(Camera.projection, "proj");
		partShader.setUniform(camera.getViewMatrix(), "view");
	
		
		
		for(ParticleSystem ps : loader.getParticles()){
			
			if(ps.isAdditive){
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}else{
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			if(update) {
				ps.generateParticles();
			}
			
			GL30.glBindVertexArray(ps.getVaoID());
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);

			GL31.glDrawElementsInstanced(GL11.GL_TRIANGLE_STRIP, 15,GL11.GL_UNSIGNED_INT,0,ps.getCount());
			
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(2);
			GL30.glBindVertexArray(0);

		}
		
		partShader.stop();
		
		GL11.glDisable(GL11.GL_BLEND);
		
	}
	
	/**
	 * renders Entities from batches
	 */
	protected void renderPointEntities(boolean update){
			
		pointEntShader.start();
		
		pointEntShader.setUniform((float)ShadowMapMasterRenderer.SHADOW_MAP_SIZE, "mapSize");
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowRenderer.getShadowMap());
		
		pointEntShader.setUniform(0, "shadowMap");
		
		pointEntShader.setUniform(ShadowBox.SHADOW_DISTANCE, "shadowDist");
		pointEntShader.setUniform(shadowRenderer.getToShadowMapSpaceMatrix(), "toShadow");
		pointEntShader.setUniform(clipPlane, "plane");

		
		for(int id:loader.getPointEntities().keySet()){
			GL30.glBindVertexArray(id);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);
			GL20.glEnableVertexAttribArray(3);
			GL20.glEnableVertexAttribArray(4);


			List<PointEntity> batch = loader.getPointEntities().get(id);
			for(PointEntity entity:batch){

				pointEntShader.setUniform(entity.getWorldMatrix(), "trans");

				GL31.glDrawElementsInstanced(GL11.GL_TRIANGLE_STRIP, 15,GL11.GL_UNSIGNED_INT,0,entity.getVertCount());

				if(update) {
					entity.update();
				}
				
			}
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(2);
			GL20.glDisableVertexAttribArray(3);
			GL20.glDisableVertexAttribArray(4);
			GL30.glBindVertexArray(0);
			
		}
		
		pointEntShader.stop();
	}
	
	/**
	 * renders Entities from batches
	 */
	protected void renderPolyEntities(Camera camera, List<Vector3f> position, List<Vector3f> color, List<Vector3f> att, boolean update){
			
		polyEntShader.start();
		polyEntShader.setUniform(Camera.projection, "proj");
		
		polyEntShader.setUniform(camera.getViewMatrix(), "view");
		
		polyEntShader.setUniform(position,8,new Vector3f(0,0,0), "lightPos");
		polyEntShader.setUniform(color,8,new Vector3f(0,0,0), "lightColor");
		polyEntShader.setUniform(att,8,new Vector3f(1,0,0), "attenuation");
		polyEntShader.setUniform(clipPlane, "plane");
		
		polyEntShader.setUniform((float)ShadowMapMasterRenderer.SHADOW_MAP_SIZE, "mapSize");
		
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowRenderer.getShadowMap());

		polyEntShader.setUniform(0, "textureSamp");
		polyEntShader.setUniform(1, "normalMap");
		polyEntShader.setUniform(2, "shadowMap");
		
		polyEntShader.setUniform(ShadowBox.SHADOW_DISTANCE, "shadowDist");
		polyEntShader.setUniform(shadowRenderer.getToShadowMapSpaceMatrix(), "toShadow");
		
		for(int id:loader.getEnvironments().keySet()){
			
			GL30.glBindVertexArray(id);
			GL20.glEnableVertexAttribArray(0);
			GL20.glEnableVertexAttribArray(1);
			GL20.glEnableVertexAttribArray(2);

			List<PolyEntity> batch = loader.getEnvironments().get(id);
			for(PolyEntity entity:batch){
				
				GL13.glActiveTexture(GL13.GL_TEXTURE0);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entity.getTextureID());
				
				GL13.glActiveTexture(GL13.GL_TEXTURE1);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entity.getNormalID());
				
				if(entity.isTransparent){
					GL11.glDisable(GL11.GL_CULL_FACE);
				}

				polyEntShader.setUniform(entity.textureScale, "scale");
				polyEntShader.setUniform(entity.getWorldMatrix(), "trans");
				GL11.glDrawElements(GL11.GL_TRIANGLES, entity.getVertCount(),GL11.GL_UNSIGNED_INT,0);
				
				if(entity.isTransparent){
					GL11.glEnable(GL11.GL_CULL_FACE);
					GL11.glCullFace(GL11.GL_BACK);
				}

				if (update) {
					entity.update();
				}
			}
			GL20.glDisableVertexAttribArray(0);
			GL20.glDisableVertexAttribArray(1);
			GL20.glDisableVertexAttribArray(2);
			GL30.glBindVertexArray(0);
			
		}
		
		polyEntShader.stop();
	}	
	
	/**
	 * renders Fonts from batches
	 */
	protected void renderFonts(){
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		textShader.start();
		for(FontType font : loader.getTexts().keySet()){
			textShader.setUniform(0, "fontAtlas");
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.getTextureAtlas());
				    
			for(GUIText text : loader.getTexts().get(font)){
				GL30.glBindVertexArray(text.getMesh());
				GL20.glEnableVertexAttribArray(0);
				GL20.glEnableVertexAttribArray(1);
				
				textShader.setUniform(text.edge, "edge");
				textShader.setUniform(text.boarderEdge, "boarderEdge");
				textShader.setUniform(text.borderColor, "borderColor");
				textShader.setUniform(text.offset, "offset");
						
				textShader.setUniform(text.getColour(), "color");
				textShader.setUniform(text.getPosition(), "translation");
				GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, text.getVertexCount());
						
				GL20.glDisableVertexAttribArray(0);
				GL20.glDisableVertexAttribArray(1);
				GL30.glBindVertexArray(0);
			}
		}
		textShader.stop();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	/**
	 * returns aspectRatio of the game
	 * @return float aspectRatio
	 */
	public static float aspectRatio(){
		return (float)Display.getWidth() / (float)Display.getHeight();
	}
}
