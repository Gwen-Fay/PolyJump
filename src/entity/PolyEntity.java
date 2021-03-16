package entity;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import main.Main;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

import render.Loader;

/**
 * Polygonal entity
 */
public abstract class PolyEntity extends Entity{

	public Vector3f textureScale = new Vector3f(1,1,1);

	protected int textureID;
	protected int normalID;
	
	protected static Map<String, Integer[]> loadedVAOs = new HashMap<String, Integer[]>();
	protected static Map<String, Integer> loadedTextures = new HashMap<String, Integer>();

	int vaoID;
	int vertCount;

	public boolean isTransparent = false;
	
	public PolyEntity(String name, String textureName, String normalName, float[] pos, int[] ind, float[] tex, float[] norm) {
		Integer[] vaoData = loadedVAOs.get(name);
		if(vaoData == null){
			loadedVAOs.put(name, loadVAO(pos,ind,tex,norm));
		}else{
			vaoID = vaoData[0];
			vertCount = vaoData[1];
		}
		
		Integer texID = loadedTextures.get(textureName);
		if(texID == null){
			textureID = Loader.loadTexture(textureName);
			loadedTextures.put(textureName, textureID);
		}else{
			textureID = texID;
		}
		
		Integer normID = loadedTextures.get(normalName);
		if(normID == null){
			normalID = Loader.loadTexture(normalName);
			loadedTextures.put(normalName, normalID);
		}else{
			normalID = normID;
		}
	}


	public PolyEntity(String name, String textureName, String normalName, float[] pos, int[] ind, float[] tex, float[] norm, int texID) {
		Integer[] vaoData = loadedVAOs.get(name);
		if(vaoData == null){
			loadedVAOs.put(name, loadVAO(pos,ind,tex,norm));
		}else{
			vaoID = vaoData[0];
			vertCount = vaoData[1];
		}
		
		textureID = texID;
	
		Integer normID = loadedTextures.get(normalName);
		if(normID == null){
			normalID = Loader.loadTexture(normalName);
			loadedTextures.put(normalName, normalID);
		}else{
			normalID = normID;
		}
	}
	
	protected PolyEntity(){
	}
	
	protected Integer[] loadVAO(float[] pos, int[] ind, float[] tex, float[] norm){
		int vaoID = GL30.glGenVertexArrays();
		Loader.addVao(vaoID);
		GL30.glBindVertexArray(vaoID);
		
		
		
		int vboID = GL15.glGenBuffers();
		Loader.addVbo(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = BufferUtils.createIntBuffer(ind.length);
		buffer.put(ind);
		buffer.flip();
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

		Loader.loadToVBO(0, pos, 3);
		Loader.loadToVBO(1, tex, 2);
		Loader.loadToVBO(2, norm, 3);
		
		//GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		
		this.vaoID = vaoID;
		this.vertCount = ind.length;
		
		return new Integer[]{this.vaoID, this.vertCount};
	}

	@Override
	public int getVertCount() {
		return vertCount;
	}

	@Override
	public int getVaoID() {
		return vaoID;
	}

	public int getTextureID() {
		return textureID;
	}
	
	public int getNormalID(){
		return normalID;
	}
	
	public abstract boolean isTileable();
	
	
	public abstract String getName();
}
