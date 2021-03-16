package main;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import render.Loader;

/**
 * Game rendered GUI Object
 * @author gwen
 *
 */
public class GUIEntity {
	private int textureID;
	public Vector2f position = new Vector2f(0,0);
	public Vector2f scale = new Vector2f(1,1);
	public Vector4f color = new Vector4f(0,0,0,0);
	public float rotation = 0;
	
	private static Map<String, Integer> loadedGUI = new HashMap<String, Integer>();
	
	/**
	 * Loads Textures if needed and Creates a GUI Entity 
	 * @param texture
	 */
	public GUIEntity(String texture){
		Integer texID = loadedGUI.get(texture);
		if(texID == null){
			textureID = Loader.loadTexture(texture);
			loadedGUI.put(texture, textureID);
		}else{
			textureID = texID;
		}
	}
	public GUIEntity(int texID){
		textureID = texID;
	}
	
	public int getTextureID(){
		return textureID;
	}
	
	public Matrix4f createTransformation(){
		Matrix4f transf = new Matrix4f();
		transf.setIdentity();
		Matrix4f.translate(position, transf, transf);
		Matrix4f.scale(new Vector3f(scale.x,scale.y,1), transf, transf);
		Matrix4f.rotate((float)Math.toRadians(rotation), new Vector3f(0,0,1), transf, transf);
		return transf;
	}
	
	/**
	 * clears map with TextureIDs
	 */
	public static void cleanUp(){
		loadedGUI.clear();
	}
}
