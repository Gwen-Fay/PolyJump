package audio;

import org.lwjgl.openal.AL10;
import org.lwjgl.util.vector.Vector3f;
/**
 * object wrapper for sources in OpenAL
 * @author Gwen
 *
 */
public class Source {
	private int sourceID;

	/**
	 * initalizes source with some default properties
     */
	public Source(){
		sourceID = AL10.alGenSources();
		AL10.alSourcef(sourceID,AL10.AL_ROLLOFF_FACTOR,3);
		AL10.alSourcef(sourceID,AL10.AL_REFERENCE_DISTANCE,6);
		AL10.alSourcef(sourceID,AL10.AL_MAX_DISTANCE,20);
	}

	/**
	 * Custom pramaters
	 * @param rolloff
	 * @param refDistance
	 * @param maxDistance
     */
	public Source(float rolloff, float refDistance, float maxDistance){
		sourceID = AL10.alGenSources();
		AL10.alSourcef(sourceID,AL10.AL_ROLLOFF_FACTOR,rolloff);
		AL10.alSourcef(sourceID,AL10.AL_REFERENCE_DISTANCE,refDistance);
		AL10.alSourcef(sourceID,AL10.AL_MAX_DISTANCE,maxDistance);
	}
	
	public void setGain(float gain){
		AL10.alSourcef(sourceID, AL10.AL_GAIN, gain);
	}
	public void setPitch(float pitch){
		AL10.alSourcef(sourceID, AL10.AL_PITCH, pitch);
	}
	public void setPosition(Vector3f position){
		AL10.alSource3f(sourceID, AL10.AL_POSITION, position.x,position.y,position.z);
	}
	public void setVelocity(Vector3f velocity){
		AL10.alSource3f(sourceID, AL10.AL_VELOCITY, velocity.x,velocity.y,velocity.z);
	}
	public void setLooping(boolean loop){
		AL10.alSourcei(sourceID, AL10.AL_LOOPING, loop? AL10.AL_TRUE : AL10.AL_FALSE);
	}
	public boolean isPlaying(){
		return AL10.alGetSourcei(sourceID, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}
	
	public void delete(){
		stop();
		AL10.alDeleteSources(sourceID);
	}
	
	public void pause(){
		AL10.alSourcePause(sourceID);
	}
	public void unPause(){
		AL10.alSourcePlay(sourceID);;
	}
	
	public void stop(){
		AL10.alSourceStop(sourceID);
	}
	
	public void play(int buffer){
		stop();
		AL10.alSourcei(sourceID, AL10.AL_BUFFER, buffer);
		AL10.alSourcePlay(sourceID);
	}
}
