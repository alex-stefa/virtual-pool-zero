package virtualPool.scenegraph;

import javax.media.opengl.GL;


public class PerspectiveCamera extends Camera
{
	public float fovY, aspect, near, far;

	public PerspectiveCamera(float fovY, float aspect, float near, float far)
	{
		this.fovY = fovY;
		this.aspect = aspect;
		this.near = near;
		this.far = far;
	}

	@Override
	public void render(GL gl)
	{
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fovY, aspect, near, far);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		align(gl);
	}
}
