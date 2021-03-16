package entity;

import org.lwjgl.util.vector.Vector3f;

import objConverter.ModelData;
import objConverter.ObjLoader;
import render.Loader;

public class Capsule extends PolyEntity{
	
	static ModelData cylinder = ObjLoader.loadOBJ("env/Cylinder16x16");
	static ModelData sphere = ObjLoader.loadOBJ("env/Sphere16x16");
	
	private static final String NAME = "CAPSULE";
	
	public Capsule(String textureName,String normalName, float radius, float height) {
		
		super();
		
		this.textureScale = new Vector3f(radius,height,radius);
		
		float[] cynPos = cylinder.getVertices();
		int[] cynInd = cylinder.getIndices();
		float[] cynTex = cylinder.getTextureCoords();
		float[] cynNorm = cylinder.getNormals();
		
		float[] sphPos = sphere.getVertices();
		int[] sphInd = sphere.getIndices();
		float[] sphTex = sphere.getTextureCoords();
		float[] sphNorm = sphere.getNormals();
		
		float[] pos = new float[cynPos.length+ 2*sphPos.length];
		int[] ind = new int[cynInd.length+ 2*sphInd.length];
		float[] tex = new float[cynTex.length+ 2*sphTex.length];
		float[] norm = new float[cynNorm.length+ 2*sphNorm.length];
		
		
		for(int i=0;i<cynPos.length/3;i++){
			pos[3*i] = cynPos[3*i] *radius;
			pos[3*i +1] = cynPos[3*i+1] * height;
			pos[3*i +2] = cynPos[3*i+2] *radius;
			
			Vector3f normV = new Vector3f(cynNorm[3*i] * radius,
					cynNorm[3*i+1] * height,cynNorm[3*i+2] * radius);
			normV.normalise();
			norm[3*i] = normV.x;
			norm[3*i+1] = normV.y;
			norm[3*i+2] = normV.z;
			
			tex[2*i] = cynTex[2*i];
			tex[2*i+1] = cynTex[2*i+1];
		}
		
		int l =cynPos.length;
		int s = sphPos.length;
		
		int start = l/3;
		for(int i=(l)/3;i<(l+s)/3;i++){
			pos[3*i] = sphPos[3*(i-(l)/3)] *radius;
			pos[3*i +1] = (sphPos[3*(i-(l)/3)+1] * radius)+(0.5f*height);
			pos[3*i +2] = sphPos[3*(i-(l)/3)+2] *radius;
			
			Vector3f normV = new Vector3f(sphNorm[3*(i-start)] * radius,
					sphNorm[3*(i-start)+1] * height,sphNorm[3*(i-start)+2] * radius);
			normV.normalise();
			norm[3*i] = normV.x;
			norm[3*i+1] = normV.y;
			norm[3*i+2] = normV.z;
			
			tex[2*i] = sphTex[2*(i-(start))];
			tex[2*i+1] = sphTex[2*(i-(start))+1];
		}
		
		for(int i=(l+s)/3;i<(l+s+s)/3;i++){
			pos[3*i] = sphPos[3*(i-(l+s)/3)] *radius;
			pos[3*i +1] = (sphPos[3*(i-(l+s)/3)+1] * radius)-(0.5f*height);
			pos[3*i +2] = sphPos[3*(i-(l+s)/3)+2] *radius;
			
			Vector3f normV = new Vector3f(sphNorm[3*(i-(l+s)/3)] * radius,
					sphNorm[3*(i-(l+s)/3)+1] * height,sphNorm[3*(i-(l+s)/3)+2] * radius);
			normV.normalise();
			norm[3*i] = normV.x;
			norm[3*i+1] = normV.y;
			norm[3*i+2] = normV.z;
			
			tex[2*i] = sphTex[2*(i-(l+s)/3)];
			tex[2*i+1] = sphTex[2*(i-(l+s)/3)+1];
		}
		
		for(int i=0;i<cynInd.length;i++){
			ind[i] = cynInd[i];
		}
	
		for(int i=cynInd.length;i<cynInd.length+(sphInd.length);i++){
			ind[i] = sphInd[i-(cynInd.length)] + (cynPos.length)/3;
		}
		
		for(int i=cynInd.length+sphInd.length;i<cynInd.length+(2*sphInd.length);i++){
			ind[i] = sphInd[i-(cynInd.length+sphInd.length)] + (cynPos.length+sphPos.length)/3;
		}
		
		Integer[] vaoData = loadedVAOs.get(NAME+","+radius+","+height);
		if(vaoData == null){
			loadedVAOs.put(NAME+","+radius+","+height, loadVAO(pos,ind,tex,norm));
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
