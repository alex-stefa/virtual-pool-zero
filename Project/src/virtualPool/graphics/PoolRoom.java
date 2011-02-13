package virtualPool.graphics;

import java.io.File;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;

import virtualPool.loader.ModelLoadException;
import virtualPool.loader.WaveFrontLoader;
import virtualPool.scenegraph.Geometry;
import virtualPool.scenegraph.Rotation;
import virtualPool.scenegraph.Translation;
import virtualPool.util.Logs;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class PoolRoom extends Geometry
{
	public static final float ROOM_TOP = 280.0f;
	public static final float ROOM_FLOOR = 0.0f;
	public static final float ROOM_MAX_X = 300.0f;
	public static final float ROOM_MAX_Z = 300.0f;
	public static final float ROOM_MIN_X = -300.0f;
	public static final float ROOM_MIN_Z = -300.0f;
	public static final float ROOM_DOOR_MAX_X = 180.0f;
	public static final float ROOM_DOOR_MIN_X = 60.0f;
	
	private Chair chair;
	private Lamps lamps;
	
	// environment list id (called to draw the room)
	private int envListId = -1;
	
	// corners of room and door
	private float vertices[][] = {
		    { PoolRoom.ROOM_MIN_X, PoolRoom.ROOM_FLOOR, PoolRoom.ROOM_MIN_Z },
		    { PoolRoom.ROOM_MAX_X, PoolRoom.ROOM_FLOOR, PoolRoom.ROOM_MIN_Z },
		    { PoolRoom.ROOM_MAX_X, PoolRoom.ROOM_TOP, 	PoolRoom.ROOM_MIN_Z },
		    { PoolRoom.ROOM_MIN_X, PoolRoom.ROOM_TOP, 	PoolRoom.ROOM_MIN_Z }, 
		    { PoolRoom.ROOM_MIN_X, PoolRoom.ROOM_FLOOR, PoolRoom.ROOM_MAX_Z },
		    { PoolRoom.ROOM_MAX_X, PoolRoom.ROOM_FLOOR, PoolRoom.ROOM_MAX_Z },
		    { PoolRoom.ROOM_MAX_X, PoolRoom.ROOM_TOP, 	PoolRoom.ROOM_MAX_Z },
		    { PoolRoom.ROOM_MIN_X, PoolRoom.ROOM_TOP,	PoolRoom.ROOM_MAX_Z },
		    { PoolRoom.ROOM_DOOR_MIN_X, PoolRoom.ROOM_FLOOR, PoolRoom.ROOM_MIN_Z + 0.01f },
		    { PoolRoom.ROOM_DOOR_MAX_X, PoolRoom.ROOM_FLOOR, PoolRoom.ROOM_MIN_Z + 0.01f },
		    { PoolRoom.ROOM_DOOR_MAX_X, PoolRoom.ROOM_TOP * 0.85f, 	PoolRoom.ROOM_MIN_Z + 0.01f },
		    { PoolRoom.ROOM_DOOR_MIN_X, PoolRoom.ROOM_TOP * 0.85f, 	PoolRoom.ROOM_MIN_Z + 0.01f } 
		};

	private Texture doorTex, floorTex, wallTex, ceilTex;
	
	public PoolRoom()
	{
		super();
		try
		{
			chair = new Chair(WaveFrontLoader.load("models/pool/chair.obj", "textures"));
			lamps = new Lamps(WaveFrontLoader.load("models/pool/lamp.obj", "textures"));
		}
		catch (ModelLoadException e)
		{
			e.printStackTrace();
		}

		Translation t1 = new Translation(20 * PoolBall.RADIUS, 0, 80 * PoolBall.RADIUS);
		Rotation r1 = new Rotation(230, 0, 1, 0);
		t1.addChild(r1);
		r1.addChild(chair);
		
		Translation t2 = new Translation(-60 * PoolBall.RADIUS, 0, 70 * PoolBall.RADIUS);
		Rotation r2 = new Rotation(160, 0, 1, 0);
		t2.addChild(r2);
		r2.addChild(chair);
		
		Translation t3 = new Translation(-40 * PoolBall.RADIUS, 0, -60 * PoolBall.RADIUS);
		Rotation r3 = new Rotation(100, 0, 1, 0);
		t3.addChild(r3);
		r3.addChild(chair);

		// add 3 chairs as children, using the same 3D model
		this.addChild(t1);
		this.addChild(t2);
		this.addChild(t3);

		floorTex = loadTexture("textures/floor.jpg");
		wallTex = loadTexture("textures/wallpaper.jpg");
		ceilTex = loadTexture("textures/ceiling.jpg");
		doorTex = loadTexture("textures/door.jpg");
	}
	
	@Override
	public void render(GL gl)
	{
		// render lamp first because it holds the lights
		lamps.render(gl);
		
		// check if room interior has been constructed
		if (envListId < 0)
		{
			envListId = gl.glGenLists(1);

			gl.glNewList(envListId, GL.GL_COMPILE);

//				/* Draw XoZ axes for orientation */
//			
//				gl.glDisable(GL.GL_LIGHTING);
//				gl.glBegin(GL.GL_LINES);
//					float u = 4.0f;
//					// white axes
//					gl.glColor3f(1, 1, 1);
//					gl.glVertex3f(100 * u, 0, 0);
//					gl.glVertex3f(-100 * u, 0, 0);
//					gl.glVertex3f(0, 0, 100 * u);
//					gl.glVertex3f(0, 0, -100 * u);
//					// red OX
//					gl.glColor3f(1, 0, 0);
//					gl.glVertex3f(100 * u, 0, 0);
//					gl.glVertex3f(120 * u, 0, 0);
//					gl.glVertex3f(-100 * u, 0, 0);
//					gl.glVertex3f(-120 * u, 0, 0);
//					// blue OZ
//					gl.glColor3f(0, 0, 1);
//					gl.glVertex3f(0, 0, 100 * u);
//					gl.glVertex3f(0, 0, 120 * u);
//					gl.glVertex3f(0, 0, -100 * u);
//					gl.glVertex3f(0, 0, -120 * u);
//					// green +tips
//					gl.glColor3f(0, 1, 0);
//					gl.glVertex3f(105 * u, 0, 0);
//					gl.glVertex3f(115 * u, 0, 0);
//					gl.glVertex3f(0, 0, 105 * u);
//					gl.glVertex3f(0, 0, 115 * u);
//				gl.glEnd();
//				gl.glEnable(GL.GL_LIGHTING);
				
				
				/* Draw room walls, floor and ceiling & door */
				
				gl.glDisable(GL.GL_COLOR_MATERIAL);
				gl.glEnable(GL.GL_TEXTURE_2D);
							
				// set room material properties
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, new float[] {0.2f, 0.2f, 0.2f, 1.0f}, 0);
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, new float[] {0.9f, 0.9f, 0.9f, 1.0f}, 0);
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, new float[] {0.2f, 0.2f, 0.2f, 1.0f}, 0);
				gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, 40.0f);
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, new float[] {0.0f, 0.0f, 0.0f, 1.0f}, 0);

				wallTex.bind();
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
				gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
				RenderFace(gl, 1, 2, 3, 0, 1.0f, 1.0f);
			    RenderFace(gl, 4, 7, 6, 5, 1.0f, 1.0f);
			    RenderFace(gl, 0, 3, 7, 4, 1.0f, 1.0f);
			    RenderFace(gl, 5, 6, 2, 1, 1.0f, 1.0f);

			    float sizeX = PoolRoom.ROOM_MAX_X - PoolRoom.ROOM_MIN_X;
				float sizeZ = PoolRoom.ROOM_MAX_Z - PoolRoom.ROOM_MIN_Z;
			    
			    ceilTex.bind();
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
				gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
				RenderFace(gl, 6, 7, 3, 2, sizeX / ceilTex.getWidth(), sizeZ / ceilTex.getHeight());
			    
				floorTex.bind();
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
				gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
				RenderFace(gl, 4, 5, 1, 0, sizeX / floorTex.getWidth() * 6, sizeZ / floorTex.getHeight() * 6);

				doorTex.bind();
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
				gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
				gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
				RenderFace(gl, 9, 10, 11, 8, 1.0f, 1.0f);
				
			    gl.glDisable(GL.GL_TEXTURE_2D);
				gl.glEnable(GL.GL_COLOR_MATERIAL);

		    gl.glEndList();
		}
		
		// draw room interior
		gl.glCallList(envListId);
	}
	
	private void RenderFace(GL gl, int a, int b, int c, int d, float texS, float texT)
	{
	    gl.glBegin(GL.GL_QUADS);
	    	gl.glTexCoord2f(0.0f, 0.0f);
		    gl.glVertex3fv(vertices[a],0);
	
		    gl.glTexCoord2f(0.0f, texT);
		    gl.glVertex3fv(vertices[b],0);
	
		    gl.glTexCoord2f(texS, texT);
		    gl.glVertex3fv(vertices[c],0);
	
		    gl.glTexCoord2f(texS, 0.0f);
		    gl.glVertex3fv(vertices[d],0);
	    gl.glEnd();		
	}
	
	private Texture loadTexture(String filename)
	{
		try
		{
			Logs.log.info(" Loading texture: " + filename + " ...");
			return TextureIO.newTexture(ImageIO.read(new File(filename)), true);
		}
		catch (Exception e)
		{
			Logs.log.warning(" ... FAILED loading texture with exception: " + e.getMessage());
			return null;
		}
	}
}
