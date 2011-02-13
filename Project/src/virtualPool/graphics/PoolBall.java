package virtualPool.graphics;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import virtualPool.VirtualPool;
import virtualPool.scenegraph.Geometry;
import virtualPool.util.Logs;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public class PoolBall extends Geometry
{
	public static final float RADIUS = 2.8575f; // official WPA ball size (centimeters)
	
	private int number;
	private GLUquadric quad;
	private Texture texture;
	
	public float posX, posZ;
	public float vX, vZ;
	public boolean isPocketed;
	
	// matrix used to compute shadows
	private float[] shadowMat;
	
	// matrix used to compute rotations
	private float[] rotMat;
	private List<Movement> movements;
	
	// cueball = number 0
	public PoolBall(int number)
	{
		assert(number < 16 && number >=0);
		this.number = number;
		
		texture = loadTexture("textures/ball" + number + ".png");
		
		quad = glu.gluNewQuadric();
		isPocketed = false;
		moveTo(0, 0);
		setSpeed(0, 0);
		
		shadowMat = new float[16];
		for (int i = 0; i < 15; i++) shadowMat[i] = 0.0f;
		shadowMat[0] = shadowMat[5] = shadowMat[10] = 1.0f;
		
		movements = new LinkedList<Movement>();
		movements.add(new Movement((float) Math.random() * PoolBall.RADIUS, (float) Math.random() * PoolBall.RADIUS));
	}
	
	public void moveTo(float posX, float posZ)
	{
		this.posX = posX;
		this.posZ = posZ;
	}
	
	public void setSpeed(float vX, float vZ)
	{
		this.vX = vX;
		this.vZ = vZ;
	}
	
	public boolean isMoving()
	{
		return (vX != 0 || vZ != 0);
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public boolean isCueball()
	{
		return (number == 0);
	}
	
	public boolean isEightBall()
	{
		return (number == 8);
	}
	
	public boolean isSolid()
	{
		return (number < 8 && number > 0);
	}
	
	public boolean isStriped()
	{
		return (number > 8);
	}

	@Override
	public void render(GL gl)
	{

/*  Transforms happening before:
		gl.glTranslatef(0.0f, 0.0f, -pivotDistance);
		gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
		gl.glTranslatef(-centerX, -centerY, -centerZ);
*/

		if (!isPocketed)
		{
			glu.gluQuadricDrawStyle(quad, GLU.GLU_FILL);
		    glu.gluQuadricTexture(quad, true);
		    glu.gluQuadricNormals(quad, GLU.GLU_SMOOTH);
		    glu.gluQuadricOrientation(quad, GLU.GLU_OUTSIDE);
		    

			gl.glTranslatef(posX, PoolTable.TOP_Y + PoolBall.RADIUS, posZ);
			
//			gl.glBegin(GL.GL_LINES);
//				gl.glVertex3f(0.0f, 0.0f, 0.0f);
//				gl.glVertex3f(10.0f, 0.0f, 0.0f);
//			gl.glEnd();
			
			texture.enable();
			texture.bind();
			texture.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
			texture.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

			if (rotMat == null)
			{
				rotMat = new float[16];
				gl.glPushMatrix();
				gl.glLoadIdentity();
				gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, rotMat, 0);
				gl.glPopMatrix();
			}
			
			gl.glPushMatrix();
				gl.glLoadMatrixf(rotMat, 0);
				for (Movement m : movements)
				{
					float aX = -m.dZ;
					float aZ = m.dX;
					float module = (float) Math.sqrt(m.dX * m.dX + m.dZ * m.dZ);
					if (module == 0) continue;
					float angle = module / (2.0f * (float) Math.PI * PoolBall.RADIUS) * 360.0f;
					gl.glRotatef(-angle, aX, 0.0f, aZ);
				}
				movements.clear();
				gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, rotMat, 0);
			gl.glPopMatrix();

			gl.glPushMatrix();
				gl.glMultMatrixf(rotMat, 0);
				
				// texture needs to be flipped vertically before rendering
				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glPushMatrix();
					gl.glScaled(1, -1, 1);
					gl.glTranslated(0, -1, 0);
					gl.glMatrixMode(GL.GL_MODELVIEW);
					glu.gluSphere(quad, PoolBall.RADIUS, 20, 20);
					gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glPopMatrix();
				gl.glMatrixMode(GL.GL_MODELVIEW);

			gl.glPopMatrix();

			texture.disable();

			float[] lightPos;
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
			gl.glDisable(GL.GL_LIGHTING);
			gl.glColor3f(0.05f, 0.05f, 0.05f);

			lightPos = Lamps.lights[0].position;
			shadowMat[7] = -1.0f / (lightPos[1] + PoolBall.RADIUS);
			gl.glPushMatrix();
				gl.glTranslatef(+lightPos[0], +lightPos[1], +lightPos[2]);
				gl.glMultMatrixf(shadowMat, 0);
				gl.glTranslatef(-lightPos[0], -lightPos[1], -lightPos[2]);
				glu.gluSphere(quad, PoolBall.RADIUS, 10, 10);
			gl.glPopMatrix();

			lightPos = Lamps.lights[3].position;
			shadowMat[7] = -1.0f / (lightPos[1] + PoolBall.RADIUS);
			gl.glPushMatrix();
				gl.glTranslatef(+lightPos[0], +lightPos[1], +lightPos[2]);
				gl.glMultMatrixf(shadowMat, 0);
				gl.glTranslatef(-lightPos[0], -lightPos[1], -lightPos[2]);
				glu.gluSphere(quad, PoolBall.RADIUS, 10, 10);
			gl.glPopMatrix();

			gl.glPopAttrib();
		}
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
	
	public void move(float time)
	{
		float dX = vX * time;
		float dZ = vZ * time;
		
		posX += dX;
		posZ += dZ;
		
		// check if ball has been pocketed at middle
		if (((Math.abs(posX) < PoolBall.RADIUS * 1.5f) && (PoolTable.MAX_Z - Math.abs(posZ) < PoolBall.RADIUS)) &&
			(posZ * vZ > 0))
		{
			vX = vZ = 0;
			isPocketed = true;
		}

		// check if ball has been pocketed in corners
		if ((PoolTable.MAX_Z - Math.abs(posZ) < PoolBall.RADIUS) && (PoolTable.MAX_X - Math.abs(posX) < PoolBall.RADIUS))
		{
			vX = vZ = 0;
			isPocketed = true;
		}
		// respawn cueball
		if (isCueball() && isPocketed)
		{
			isPocketed = false;
			posZ = 0;
			posX = PoolTable.MAX_X / 2 + 2 * PoolBall.RADIUS;
		}
		
		movements.add(0, new Movement(dX, dZ));
		//movements.add(new Movement(dX, dZ));
	}
	
	public void applyFriction(float time)
	{
		if (time == 0.0f) return;
		double module = Math.sqrt(vX * vX + vZ * vZ);
		if (module < VirtualPool.RunningConfig.PhysicsFriction * time)
		{
			vX = 0;
			vZ = 0;
		}
		else
		{
			vX *= 1 - VirtualPool.RunningConfig.PhysicsFriction * time / module;
			vZ *= 1 - VirtualPool.RunningConfig.PhysicsFriction * time / module;
		}
	}
	
	public void applyEnergyLoss()
	{
		double module = Math.sqrt(vX * vX + vZ * vZ);
		if (module < VirtualPool.RunningConfig.PhysicsElasticity)
		{
			vX = 0;
			vZ = 0;
		}
		else
		{
			vX *= 1 - VirtualPool.RunningConfig.PhysicsElasticity / module;
			vZ *= 1 - VirtualPool.RunningConfig.PhysicsElasticity / module;
		}
	}
	
	private static class Movement
	{
		public float dX, dZ;
		public Movement(float dX, float dZ)
		{	this.dX = dX; this.dZ = dZ;   }
	}
}
