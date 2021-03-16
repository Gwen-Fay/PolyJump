package tree;

public class  Node<T> {
	
	private Node[] children = new Node[8];

	private int power;
	private int diameter;
	private int[] center;
	private static int count = 0;
	
	private T object;
			
	public  Node(int power, int[] center){
		
		this.power = power;
		this.center = center;
		
		diameter = (int)Math.pow(2, power);

		count+=1;
		
	}

	public void add(T object, int[] position) {
		if (power>=1){
			
			for(int i=0;i<8;i++){
				
				int[] offset = new int[]{0,0,0};
				
				offset[0] = (((i & 1) == 0) ? (int)Math.floor(diameter/4f) : -(int)Math.ceil(diameter/4f));
	            offset[1] = (((i & 2) == 0) ? (int)Math.floor(diameter/4f) : -(int)Math.ceil(diameter/4f));
	            offset[2] = (((i & 4) == 0) ? (int)Math.floor(diameter/4f): -(int)Math.ceil(diameter/4f));
	            
	    		
	            
	            boolean isInside = true;
	            for(int j=0;j<3;j++){

	            	if((position[j])<(center[j]+offset[j]) -(int)Math.floor(diameter/4f) || (position[j])>=(center[j]+offset[j]) +(int)Math.ceil(diameter/4f)){
	            		isInside = false;	
	            	}

	            }
	           
	            
	            if(isInside){
	            	int[] newCenter = new int[]{center[0]+offset[0],center[1]+offset[1],center[2]+offset[2]};
	            	
	            	if(children[i]==null){
	            		children[i] = new Node(power-1,newCenter);
	            	}
	            	
	            	
	            	children[i].add(object, position);
	            	
	            }
				
			}
		}else{
			
			this.object = object;
			
		}
	}
    
	public T get(int[] position){
		if (power>=1){
			
			for(int i=0;i<8;i++){
				
				int[] offset = new int[]{0,0,0};
				
				offset[0] = (((i & 1) == 0) ? (int)Math.floor(diameter/4f) : -(int)Math.ceil(diameter/4f));
	            offset[1] = (((i & 2) == 0) ? (int)Math.floor(diameter/4f) : -(int)Math.ceil(diameter/4f));
	            offset[2] = (((i & 4) == 0) ? (int)Math.floor(diameter/4f): -(int)Math.ceil(diameter/4f));
	            
	    		
	            
	            boolean isInside = true;
	            for(int j=0;j<3;j++){

	            	if((position[j])<(center[j]+offset[j]) -(int)Math.floor(diameter/4f) || (position[j])>=(center[j]+offset[j]) +(int)Math.ceil(diameter/4f)){
	            		isInside = false;	
	            	}

	            }
	           
	            
	            if(isInside){
	            	
	            	if(children[i]==null){
	            		return null;
	            	}else{
	            		return (T) children[i].get(position);
	            	}
	            }
				
			}
		}else{
			
			return object;
		}
		return null;
	}
	public boolean remove(int[] position){
if (power>=1){
			
			for(int i=0;i<8;i++){
				
				int[] offset = new int[]{0,0,0};
				
				offset[0] = (((i & 1) == 0) ? (int)Math.floor(diameter/4f) : -(int)Math.ceil(diameter/4f));
	            offset[1] = (((i & 2) == 0) ? (int)Math.floor(diameter/4f) : -(int)Math.ceil(diameter/4f));
	            offset[2] = (((i & 4) == 0) ? (int)Math.floor(diameter/4f): -(int)Math.ceil(diameter/4f));
	            
	    		
	            
	            boolean isInside = true;
	            for(int j=0;j<3;j++){

	            	if((position[j])<(center[j]+offset[j]) -(int)Math.floor(diameter/4f) || (position[j])>=(center[j]+offset[j]) +(int)Math.ceil(diameter/4f)){
	            		isInside = false;	
	            	}

	            }
	            
	            if(isInside){
	            	
	            	if(children[i]==null){
	            		boolean isEmpty = true;
	            		for(int j=0;j<8;j++){
	            			if(children[j] !=null){
	            				isEmpty = false;
	            			}
	            		}
	            		if(isEmpty){return true;}
	            	}else{
	            		if(children[i].remove(position)){
	            			children[i] = null;
	            			count-=1;
	            		}
	            		boolean isEmpty = true;
	            		for(int j=0;j<8;j++){
	            			if(children[j] !=null){
	            				isEmpty = false;
	            			}
	            		}
	            		return isEmpty;
	            	}
	            }
				
			}
		}else{

			return true;
		}
		return false;
	}
	   
}
