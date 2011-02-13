package virtualPool.graphics;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import virtualPool.scenegraph.Camera;
import virtualPool.scenegraph.Geometry;

public class Marker extends Geometry
{
	public static final float RADIUS = PoolBall.RADIUS * 2/3;
	public static final float HEIGHT = PoolBall.RADIUS * 2/3;
	
	private GLUquadric quad;
	private Camera camera;
	
	public boolean enabled;

	public Marker(Camera camera)
	{
		this.quad = glu.gluNewQuadric();
		this.camera = camera;
		this.enabled = false;
	}
	
	@Override
	public void render(GL gl)
	{
		if (enabled)
		{
			gl.glTranslatef(+camera.centerX, +camera.centerY, +camera.centerZ);
			gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
			gl.glDisable(GL.GL_LIGHTING);
			gl.glDisable(GL.GL_TEXTURE_2D);
			gl.glDisable(GL.GL_CULL_FACE);
			gl.glColor3f(0.95f, 0.95f, 0.70f);
			gl.glRotatef(+90.0f, 1.0f, 0.0f, 0.0f);
			glu.gluQuadricOrientation(quad, GLU.GLU_OUTSIDE);
			glu.gluQuadricDrawStyle(quad, GLU.GLU_FILL);
			glu.gluCylinder(quad, Marker.RADIUS, Marker.RADIUS, Marker.HEIGHT, 50, 50);
			gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
			gl.glPopAttrib();
			gl.glTranslatef(-camera.centerX, -camera.centerY, -camera.centerZ);
		}
	}
}
