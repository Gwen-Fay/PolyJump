package audio;

import java.io.IOException;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.util.vector.Vector3f;
/**
 * test player for OpenAL
 * @author Gwen
 *
 */
public class Test {

	static Audio audio;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		audio = new Audio();
		
		Vector3f position = new Vector3f(0,0,0);
		audio.setListenerData(position);
		
		AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE_CLAMPED);
		
		int buffer= audio.loadSound("res/sound/bounce.wav");
		Source source = new Source();
		source.setLooping(true);
		source.play(buffer);
		position = new Vector3f(0,0,0);
		source.setPosition(position);
		
		
		char c = ' ';
		while(c!='q'){

			position.x -=0.03f;
			source.setPosition(position);
			System.out.println(position.x);
			Thread.sleep(10);
			
		}

		source.delete();
		audio.cleanUp();
		
	}

}
