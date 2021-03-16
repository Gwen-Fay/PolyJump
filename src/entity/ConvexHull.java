package entity;

import objConverter.ModelData;
import objConverter.ObjLoader;
import render.Loader;

public class ConvexHull extends PolyEntity{
	
	public ModelData md;
	
	private static final String NAME = "CONVEX_HULL";
	
	public ConvexHull(String textureName, String normalName, String fileName) {
		super();
		md = ObjLoader.loadOBJ(fileName);
		Integer[] vaoData = loadedVAOs.get(fileName);
		if(vaoData == null){
			
			float[] pos = md.getVertices();
			int[] ind = md.getIndices();
			float[] tex = md.getTextureCoords();
			float[] norm = md.getNormals();
			
			loadedVAOs.put(fileName, loadVAO(pos,ind,tex,norm));
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

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return NAME;
	}

	@Override
	public boolean isTileable() {
		return false;
	}
}
