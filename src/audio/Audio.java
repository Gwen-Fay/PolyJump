package audio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;
import org.lwjgl.util.vector.Vector3f;

/**
 * OpenAL Wrapper
 */
public class Audio {
	
	private static List<Integer> soundBuff = new ArrayList<Integer>();
	
	/**
	 * needed to initalize OpenAL
	 */
	public Audio(){
		try {
			AL.destroy();
			AL.create();
		} catch (LWJGLException e) {
			System.err.println("OpenAL failed to create!");
			e.printStackTrace();
		}
	}

    /**
     * sets position, zero velocity, of listener
     * @param position
     */
	public void setListenerData(Vector3f position){
		AL10.alListener3f(AL10.AL_POSITION,position.x,position.y,position.z);
		AL10.alListener3f(AL10.AL_VELOCITY,0f,0f,0f);
	}

    /**
     * sets position and velocity of listener
     * @param position
     * @param velocity
     */
    public void setListenerData(Vector3f position, Vector3f velocity){
        AL10.alListener3f(AL10.AL_POSITION,position.x,position.y,position.z);
        AL10.alListener3f(AL10.AL_VELOCITY,velocity.x,velocity.y,velocity.z);
    }

    /**
     * loads sound
     * @param file
     * @return
     */
	public int loadSound(String file){
		int buffer = AL10.alGenBuffers();
		soundBuff.add(buffer);
		
		WaveData waveFile = null;
		try {
			waveFile = WaveData.create(new BufferedInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			System.out.println("Err! File not found");
			e.printStackTrace();
		}
		AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
	
		return buffer;
	}

	//ToDo: add way to remove sound from memory

    /**
     * clean ups data
     */
	public void cleanUp(){
		for(int buff:soundBuff){
			AL10.alDeleteBuffers(buff);
		}
		AL.destroy();
	}
	
}
