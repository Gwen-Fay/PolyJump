package render;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import entity.PointEntity;
import entity.PolyEntity;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontMeshCreator.TextMeshData;
import main.GUIEntity;
import particle.ParticleSystem;
import water.Water;

public class Loader {
	
	protected static List<Integer> vaos = new ArrayList<Integer>();
	protected static List<Integer> vbos = new ArrayList<Integer>();
	protected static List<Integer> textures = new ArrayList<Integer>();
	
	protected Map<Integer,List<PointEntity>> pointEntities;
	protected Map<Integer,List<PolyEntity>> environments;
	protected Map<FontType,List<GUIText>> texts;
	protected  Map<Integer,List<GUIEntity>> guis;
	
	protected List<Water> waters = new ArrayList<Water>();
	protected  List<ParticleSystem> particles = new ArrayList<ParticleSystem>();
	
	
	public Loader(){
		pointEntities = new HashMap<Integer,List<PointEntity>>();
		guis = new HashMap<Integer,List<GUIEntity>>();
		texts = new HashMap<FontType,List<GUIText>>();
		environments = new HashMap<Integer,List<PolyEntity>>();
	}
	
	public void clearLists(){
		pointEntities.clear();
		environments.clear();
		texts.clear();
		guis.clear();
		waters.clear();
		particles.clear();
	}
	
	public Map<Integer, List<PointEntity>> getPointEntities() {
		return pointEntities;
	}



	public Map<Integer, List<PolyEntity>> getEnvironments() {
		return environments;
	}



	public Map<FontType, List<GUIText>> getTexts() {
		return texts;
	}



	public Map<Integer, List<GUIEntity>> getGuis() {
		return guis;
	}



	public List<Water> getWaters() {
		return waters;
	}



	public List<ParticleSystem> getParticles() {
		return particles;
	}



	/**
	 * Adds IDs to list to be cleaned up at end.
	 * @param vaoID
	 */
	public static void addVao(int vaoID){
		vaos.add(vaoID);
	}
	/**
	 * Adds IDs to list to be cleaned up at end.
	 * @param vboID
	 */
	public static void addVbo(int vboID){
		vbos.add(vboID);
	}
	
	public static int addTexture(int tex){
		textures.add(tex);
		return tex;
	}
	
	public void cleanUp(){
		for(int vao:vaos){
			GL30.glDeleteVertexArrays(vao);
		}
		for(int texture:textures){
			GL11.glDeleteTextures(texture);
		}
		for(int vbo:vbos){
			GL15.glDeleteBuffers(vbo);
		}
	}

	public static void bindIndicicesBuffer(int[] indicies){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = BufferUtils.createIntBuffer(indicies.length);
		buffer.put(indicies);
		buffer.flip();
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
	}

	/**
	 * loads data to a vbo, and returns the vbo id
	 * @param attribNumber
	 * @param data
	 * @param length
	 * @return
	 */
	public static int loadToVBO(int attribNumber, float[] data, int length){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attribNumber, length, GL11.GL_FLOAT, false,0,0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vboID;
	}

	public static int loadToInstancedVBO(int attribNumber, float[] data, int length){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();

		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attribNumber, length, GL11.GL_FLOAT, false,0,0);
		GL33.glVertexAttribDivisor(attribNumber, 1);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return vboID;
	}
	
	/**
	 * changes data in a vbo
	 * @param attribNumber
	 * @param data
	 * @param length
	 * @param vboID
	 */
	public static void changeVBO(int attribNumber, float[] data, int length, int vboID){
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attribNumber, length, GL11.GL_FLOAT, false,0,0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * loads font data to VAO and returns vao id.
	 * @param pos
	 * @param tex
	 * @return
	 */
	public static int[] loadFontVAO(float[] pos, float[] tex){
		int id[] = new int[3];
		int vaoID = GL30.glGenVertexArrays();
		addVao(vaoID);
		id[0]=vaoID;
		GL30.glBindVertexArray(vaoID);
		id[1]=loadToVBO(0,pos,2);
		id[2]=loadToVBO(1,tex,2);
		GL30.glBindVertexArray(0);
		
		return id;
	}
	
	/**
	 * changes the vbo data in a GUITexts vao
	 * @param pos
	 * @param tex
	 * @param vaoID
	 * @param posVbo
	 * @param texVbo
	 */
	public static void changeFontVAO(float[] pos, float[] tex, int vaoID, int posVbo, int texVbo){
		GL30.glBindVertexArray(vaoID);
		changeVBO(0,pos,2,posVbo);
		changeVBO(1,tex,2, texVbo);
		GL30.glBindVertexArray(0);
	}
	
	public void loadWater(List<Water> w){
		waters.addAll(w);
	}
	
	/**
	 * adds GUI to the GUI map to be processed later
	 * needs to be done each Frame we render the GUI
	 */
	public void loadGUI(List<GUIEntity> loadGuis){
		for(GUIEntity gui : loadGuis){
			int id = gui.getTextureID();
			List<GUIEntity> batch = guis.get(id);
			if(batch!=null){
				batch.add(gui);
			}else{
				List<GUIEntity> newBatch = new ArrayList<GUIEntity>();
				newBatch.add(gui);
				guis.put(id, newBatch);
			}
		}
	}
	
	/**
	 * adds entity to the entities map to be processed later
	 * needs to be done each Frame we render the entity
	 */
	public void loadPointEntity(List<PointEntity> loadEnts){
		for(PointEntity ent : loadEnts){
			int id = ent.getVaoID();
			List<PointEntity> batch = pointEntities.get(id);
			if(batch!=null){
				batch.add(ent);
			}else{
				List<PointEntity> newBatch = new ArrayList<PointEntity>();
				newBatch.add(ent);
				pointEntities.put(id, newBatch);
			}
		}
	}

	public int getVaoCount(){
		return vaos.size();
	}
	
	public void loadParticles(List<ParticleSystem> listPs){
		particles.addAll(listPs);
	}
	

	/**
	 * adds entity to the entities map to be processed later
	 * needs to be done each Frame we render the entity
	 */
	public void loadPolyEnt(List<PolyEntity> loadEnts){
		for(PolyEntity ent : loadEnts){
			int id = ent.getVaoID();
			List<PolyEntity> batch = environments.get(id);
			if(batch!=null){
				batch.add(ent);
		
			}else{
				List<PolyEntity> newBatch = new ArrayList<PolyEntity>();
				newBatch.add(ent);
				environments.put(id, newBatch);
			}
		}
	}
	/**
	 * adds GUIText's data to a VAo
	 */
	public void loadNewText(GUIText text){
		FontType font = text.getFont();
		TextMeshData data = font.loadText(text);
		int id[] = loadFontVAO(data.getVertexPositions(), data.getTextureCoords());
		text.setMeshInfo(id[0],data.getVertexCount(),id[1],id[2]);
	}
	
	/**
	 * adds GUIText to the texts map to be processed later
	 * needs to be done each Frame we render the GUIText

	 */
	public void loadText(List<GUIText> loadTexts){
		for(GUIText text : loadTexts){
			FontType font = text.getFont();
			
			List<GUIText> batch = texts.get(font);
			if(batch!=null){
				batch.add(text);
			}else{
				List<GUIText> newBatch = new ArrayList<GUIText>();
				newBatch.add(text);
				texts.put(font, newBatch);
			}
		}
	}
	
	/**
	 * updates a GUIText so that its content reflects its string

	 */
	public static void updateText(GUIText text){
		FontType font = text.getFont();
		TextMeshData data = font.loadText(text);
		changeFontVAO(data.getVertexPositions(),data.getTextureCoords(),text.getMesh(),text.getPosVbo(), text.getTexVbo());
	}
	
	/**
	 * loads a texture to memory
	 * @return
	 */
	public static int loadTexture(String fileName){
		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG", new FileInputStream("res/"+fileName+".png"));
			
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 10);
			if(GLContext.getCapabilities().GL_EXT_texture_filter_anisotropic){
				float amount = Math.min(4f, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
				GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
			}else{
				System.out.println("Anisotropic Filtering Not Suported!");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int textureID = texture.getTextureID();
		textures.add(textureID);
		return textureID;
	}

	/**
	 * removes a text from its batch

	 */
	public void removeText(GUIText text){
		List<GUIText> textBatch = texts.get(text.getFont());
		textBatch.remove(text);
		if(textBatch.isEmpty()){
			texts.remove(textBatch);
		}
		
	}
}
