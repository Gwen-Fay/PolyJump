# PolyJump
A 3D OpenGL Renderer written in Java with Voxel Support.

This was my first coding project out of college. It's writen in Java. The origional plan was to make a 3D SciFi called "PolyJump();",
but I never got far enough into making the renderer before I abandoned this project. I leave it abandoned here as-is.  

Below are some screenshots from this old project:

![Scene](https://imgur.com/cneTWiN.gif)  
A view on the whole test scene.  
<br />
<br />
![VoxelParticles](https://imgur.com/kxWAXN9.gif)  
The particle system renders points as boxes, the same size as one voxel.  
<br />
<br />
![Blink](https://imgur.com/ML4x5JN.gif)  
The Voxels are animated, watch him blink.  
<br />
<br />
![Shadow](https://imgur.com/bUWiot1.gif)  
Geometry will cast shadows, even on the Voxel Models.  
<br />
<br />
![Reflection](https://imgur.com/hflC6cb.gif)  
This quad has a reflect shader, giving the illusion of water.  
<br />
<br />
![Editor](https://imgur.com/yHgNXMX.gif)  
This ugly mess is the voxel editor. I think I am running it in a different resolution than it was designed for, It wasn't this ugly when I worked on this 5 years ago. I made this GUI extremely quickly (and badly) because I wanted to quickly create a voxel model to test out the lighting features. My plan was, to go back and redo this. 

I guess I should explain how the Voxels actually work. Each model is an oct-tree, containing the position, color, and normal of each voxel. A voxel has a pre-programed normal, and thus the whole voxel is colored one color regardless of light. The model editor could create Voxels, and assign them a color and a normal. Normals could then be tweaked to have different effects. I had plans to support randomizing the normals by some amount to create a bump-map like effect. 

When I first made this, I was so excited by what kinds of crazy shapes could be made with this simple editor. Now honestly I am embarased by how bad the UI is.
