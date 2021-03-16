package entity;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Main;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

import render.Loader;
import render.Renderer;

/**
 * Game Rendered Entity
 * @author gwen
 *
 */
public class PointEntity extends Entity{

	private static Map<String, List<EntityAnimation>> loadedAnim = new HashMap<>();
	private static Map<String, Float[]> loadedLightData = new HashMap<>();



	/**
	 * Loads file into VAOs if needed,
	 * and creates and Entity.
	 * @param fileName - name of .ent file
	 *
	 */
	public PointEntity(String fileName){
		
		List<EntityAnimation> vaos = loadedAnim.get(fileName);
		if(vaos == null){
			loadedAnim.put(fileName, loadFiles(fileName));
		}else{
			this.animations = vaos;
			currentAnimation = animations.get(0);
			currentVaoData = currentAnimation.vaos.get(0);
		}
		
	}

	/**
	 * called once per render cycle
     */
	@Override
	public void update(){
		animate();
	}



	/**
	 * reads a single .ent file and turns it into a VaoData
	 * @param fileName
	 * @param frameName
     * @return
     */
	private VaoData readFile(String fileName, String frameName){
		try {

			float[] pos = null;
			float[] norms = null;
			float[] color = null;
			float[] glow = null;

			//Frame frame = new Frame(frameName);
			//frames.add(frame);
			BufferedReader reader = new BufferedReader(new FileReader("res/models/"+fileName+"/"+frameName+".ent"));

			String line;

			line = reader.readLine();

			if(!(line == null) && !(line == "")){

				String[] Data;

				List<Float> positions = new ArrayList<Float>();
				List<Float> normals = new ArrayList<Float>();
				List<Float> colors = new ArrayList<Float>();
				List<Float>	lightData = new ArrayList<Float>();

				while ((line = reader.readLine()) != null)
				{
					if(!line.startsWith("#")){
						Data = line.split(",");
						positions.add(Float.parseFloat(Data[0]));
						positions.add(Float.parseFloat(Data[1]));
						positions.add(Float.parseFloat(Data[2]));

						normals.add(Float.parseFloat(Data[3]));
						normals.add(Float.parseFloat(Data[4]));
						normals.add(Float.parseFloat(Data[5]));

						colors.add(Float.parseFloat(Data[6]));
						colors.add(Float.parseFloat(Data[7]));
						colors.add(Float.parseFloat(Data[8]));
						colors.add(Float.parseFloat(Data[9]));

						lightData.add(Float.parseFloat(Data[10]));
						lightData.add(Float.parseFloat(Data[11]));

					}
				}
				pos = new float[positions.size()];
				norms = new float[normals.size()];
				color = new float[colors.size()];
				glow = new float[lightData.size()];

				for(int i = 0;i<positions.size();i++){
					pos[i] = positions.get(i);
				}
				for(int i = 0;i<normals.size();i++){
					norms[i] = normals.get(i);
				}
				for(int i = 0;i<colors.size();i++){
					color[i] = colors.get(i);
				}
				for(int i = 0;i<lightData.size();i++){
					glow[i] = lightData.get(i);
				}
			}
			reader.close();

			int vaoID = GL30.glGenVertexArrays();
			Loader.addVao(vaoID);
			GL30.glBindVertexArray(vaoID);
			Loader.bindIndicicesBuffer(Renderer.CUBE_INDEX);
			Loader.loadToInstancedVBO(0,pos,3);
			Loader.loadToInstancedVBO(1,color,4);
			Loader.loadToInstancedVBO(2,norms,3);
			Loader.loadToInstancedVBO(3,glow,2);


			Loader.loadToVBO(4,Renderer.CUBE,3);

			GL30.glBindVertexArray(0);

			return new VaoData(vaoID,pos.length/3, frameName);

		} catch (FileNotFoundException e) {
			System.err.println("Can not open file!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("Can not read file!");
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Adds Animation Data from the animation.dat file
	 * @param animationData
	 * @param vaosData
     */
	private void addAnimation(String[] animationData, List<VaoData> vaosData){
		EntityAnimation animation = new EntityAnimation(new String(animationData[0]));
		for(int i=1;i<animationData.length;i+=2){
			VaoData selectedVao = null;
			for(VaoData vaoData : vaosData){
				if(animationData[i].equals(vaoData.getName())){
					selectedVao = vaoData;
					break;
				}
			}
			float time = Float.parseFloat(animationData[i+1]);
			animation.vaos.add(selectedVao);
			animation.lengths.add(time);
			//System.out.println(selectedFrame.name);
		}
		animations.add(animation);

	}

	/**
	 * loads all files associated with a model from the models folder
	 * @param fileName
	 * @return
     */
	private List<EntityAnimation> loadFiles(String fileName){

		//reading animation.dat


		File fileData = new File("res/models/" + fileName + "/" + "animation.dat");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileData));
			String line;
			List<VaoData> vaoData = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				if (!line.startsWith("#")) {
					if (line.equals("Frames:")) {

						//reads frames
						line = reader.readLine();
						while(line.startsWith("#")){
							line = reader.readLine();
						}

						String[] s = line.split(",");
						//animations.clear();

						for(String frame:s){

							vaoData.add(readFile(fileName, frame));
						}
						currentVaoData = vaoData.get(0);


					}
					if (line.startsWith("Animations:")) {


						while((line = reader.readLine())  != null) {

							if (!line.startsWith("#")) {
								String[] animationData = line.split(",");

								addAnimation(animationData, vaoData);
							}
						}

					}

					if(!animations.isEmpty()){
						currentAnimation = animations.get(0);
					}

				}
			}
			return this.animations;
		} catch (FileNotFoundException e) {
			System.err.println("Can not open file!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			System.err.println("Can not read file!");
			e.printStackTrace();
			return null;
		}

	}
	
	
	/**
	 * Copy Constructor
	 * @param oldEnt
	 */
	public PointEntity(PointEntity oldEnt){
		this.animations = oldEnt.animations;
		this.currentAnimation = oldEnt.currentAnimation;
		this.currentVaoData = oldEnt.currentVaoData;

		position = new Vector3f(oldEnt.position);
		rotation = new Vector3f(oldEnt.rotation);
		scale = oldEnt.scale;
	}
	
	/**
	 * Copy Constructor, with position of new Entity
	 * @param oldEnt
	 */
	public PointEntity(PointEntity oldEnt, Vector3f position){
		this.animations = oldEnt.animations;
		this.currentAnimation = oldEnt.currentAnimation;
		this.currentVaoData = oldEnt.currentVaoData;

		this.position = position;
		rotation = new Vector3f(oldEnt.rotation);
		scale = oldEnt.scale;
	}
	
	/**
	 * clears maps with VAO data on them.
	 */
	public static void cleanUp(){
		loadedAnim.clear();
		loadedLightData.clear();
	}
	
}
