package entity;

import objConverter.ModelData;
import objConverter.ObjLoader;

public class Billboard extends PolyEntity{
	
	static ModelData md = ObjLoader.loadOBJ("env/Billboard16x16");
	
	private static final String NAME = "BILLBOARD";
	
	public Billboard(String textureName, String normalName) {
		
		super(NAME, textureName,normalName, md.getVertices(), md.getIndices(), md.getTextureCoords(), md.getNormals());
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
