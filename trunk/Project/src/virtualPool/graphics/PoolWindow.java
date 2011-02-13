package virtualPool.graphics;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import virtualPool.VirtualPool;
import virtualPool.scenegraph.PerspectiveCamera;
import virtualPool.util.FPSCounter;
import virtualPool.util.Logs;

import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.j2d.TextRenderer;


public class PoolWindow implements GLEventListener 
{
	private GLU glu = new GLU();
	
	private GLCanvas canvas;
	private FPSAnimator animator;
	private FPSCounter counter;
	private TextRenderer statusRenderer;
	
	private PoolGame poolGame;
	private PoolRoom poolRoom;
	private Marker marker;
	private PerspectiveCamera camera;
	
	private boolean ballsRolling;
	public static GameState gameState;
	public static enum GameState { AIM, VIEWPOINT, ABOVE, MOVE, PAUSE, SHOOT, ZOOM};
	private AdjustStatus adjustStatus;
	private enum AdjustStatus { AMPLIFY, NORMAL, DECREASE };
	
	// robot object used for resetting mouse position to center screen
	private Robot robot;

	// rotation factor around X and Y axes
	private float rotX = 0.0f;
	private float rotY = 0.0f;

	// scale factor for zooming
	private float zoomFactor = 1.0f;
	// zooming limits */
	private static final float MIN_SCALE = 0.1f;
	private static final float MAX_SCALE = 2.0f;
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
		
	private MouseInputAdapter mouseInputAdapter = new MouseInputAdapter()
	{
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (gameState == GameState.PAUSE) return;
			
			if (SwingUtilities.isLeftMouseButton(e))
				zoom(e.getPoint());
			else
				resetMouseLocation();
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			if (gameState == GameState.PAUSE)
				return;
			if ((gameState == GameState.AIM) ||
				(gameState == GameState.ABOVE))
				look(e.getPoint());
			if (gameState == GameState.MOVE)
				moveCue(e.getPoint());
			if (gameState == GameState.VIEWPOINT)
				moveViewpoint(e.getPoint());
			if (gameState == GameState.SHOOT)
				moveStick(e.getPoint());
		}
	};
	
	//////////////////////////////////////////////////////////////////////////////////////////////////

	private WindowAdapter windowAdapter = new WindowAdapter()
	{
		@Override
		public void windowClosing(WindowEvent e)
		{
			doClose();
		}
	};
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	private KeyAdapter keyAdapter = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			//System.out.println("You pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
			
			if ((gameState != GameState.PAUSE) && KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysPause))
			{
				gameState = GameState.PAUSE;
				return;
			}

			if (((gameState == GameState.PAUSE) && KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysPause)) ||
				KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysAim))
			{
				resetMouseLocation();
				gameState = GameState.AIM;
				poolGame.aimAtCue();
				poolGame.resetBallDistance();
				camera.setRotation(rotX, rotY);
				camera.centerX = poolGame.getAimX();
				camera.centerZ = poolGame.getAimZ();
				return;
			}
			
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysQuit))
			{
				doClose();
				return;
			}
			
			if (gameState == GameState.PAUSE) return;
			
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysMove))
			{
				gameState = GameState.MOVE;
				return;
			}
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysViewpoint))
			{
				gameState = GameState.VIEWPOINT;
				return;
			}
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysShoot))
			{
				gameState = GameState.SHOOT;
				return;
			}
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysOverview))
			{
				gameState = GameState.ABOVE;
				return;
			}
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysForceAmplify))
			{
				if (adjustStatus == AdjustStatus.AMPLIFY)
					adjustStatus = AdjustStatus.NORMAL;
				else
					adjustStatus = AdjustStatus.AMPLIFY;
				return;
			}
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysForceDecrease))
			{
				if (adjustStatus == AdjustStatus.DECREASE)
					adjustStatus = AdjustStatus.NORMAL;
				else
					adjustStatus = AdjustStatus.DECREASE;
				return;
			}
			if (KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysReset))
			{
				gameState = GameState.AIM;
				adjustStatus = AdjustStatus.NORMAL;
				poolGame.resetBalls();
				poolGame.resetBallDistance();
				poolGame.aimAtCue();
				camera.setRotation(rotX, rotY);
				camera.centerX = poolGame.getAimX();
				camera.centerZ = poolGame.getAimZ();
				return;
			}
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			if (gameState == GameState.PAUSE) return;
			
			if (((gameState == GameState.ABOVE) && KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysOverview)) ||
				((gameState == GameState.VIEWPOINT) && KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysViewpoint)) ||
				((gameState == GameState.MOVE) && KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysMove)) ||
				((gameState == GameState.SHOOT) && KeyEvent.getKeyText(e.getKeyCode()).equals(VirtualPool.RunningConfig.KeysShoot)))
			{
				poolGame.resetBallDistance();
				gameState = GameState.AIM;
				return;
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Constructor for your program, this sets up the window (Frame), creates a
	 * GLCanvas and starts the Animator
	 */
	public PoolWindow()
	{
		// create Robot object used to keep mouse in screen center
		try
		{
			robot = new Robot();
		}
		catch (AWTException e)
		{
			Logs.log.severe("Could not instantiate Robot: " + e);
			e.printStackTrace();
		}
 		
		canvas = new GLCanvas();
		canvas.addGLEventListener(this);
		canvas.addMouseListener(mouseInputAdapter);
		canvas.addMouseMotionListener(mouseInputAdapter);
		canvas.addKeyListener(keyAdapter);
		canvas.setFocusable(true);
		
		// hide mouse cursor by creating a custom blank cursor
		BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "blank cursor");
		canvas.setCursor(blankCursor);

		Frame frame = new Frame("Virtual Pool (beta) - Alex Stefanescu - ian 2011");
		frame.add(canvas);
		//frame.setUndecorated(true);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.addWindowListener(windowAdapter);
		
		frame.setVisible(true);
		
//		// try to go fullscreen.. does not work :(
//		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//		if (gd.isFullScreenSupported())
//		{
//			try
//			{
//				System.setProperty("sun.java2d.noddraw", "true");
//				gd.setFullScreenWindow(frame);
//				//Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//			    //gd.setDisplayMode((new DisplayMode(dim.width, dim.height, 32, DisplayMode.REFRESH_RATE_UNKNOWN)));
//			    frame.validate();
//			} 
//			catch (Exception e)
//			{
//				Logs.log.warning("Failed to enter fullscreen mode: " + e);
//				e.printStackTrace();
//			    gd.setFullScreenWindow(null);
//			}
//		}
		
		gameState = GameState.PAUSE;
		adjustStatus = AdjustStatus.NORMAL;
		ballsRolling = false;
		
		// get focus in order to receive key events
		canvas.requestFocus();

		animator = new FPSAnimator(canvas, VirtualPool.RunningConfig.SystemFps);
		
		// start the animation when all window events have been processed
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				animator.start();
			}
		});
	}

	/**
	 * Init() will be called when your program starts
	 */
	public void init(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		
		// Set auto normalization
		gl.glEnable(GL.GL_NORMALIZE);
		//gl.glEnable(GL.GL_RESCALE_NORMAL);

		//gl.glEnable(GL.GL_FOG);
		gl.glEnable(GL.GL_CULL_FACE);
		//gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
	
		// Enable depth testing
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LEQUAL);
		gl.glClearDepth(1.0f);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		
		// Enable antialiasing
		if (VirtualPool.RunningConfig.SystemAntialiasingPoint) gl.glEnable(GL.GL_POINT_SMOOTH);
		if (VirtualPool.RunningConfig.SystemAntialiasingLine) gl.glEnable(GL.GL_LINE_SMOOTH);
		if (VirtualPool.RunningConfig.SystemAntialiasingPolygon) gl.glEnable(GL.GL_POLYGON_SMOOTH);

		// Enable lighting
		gl.glEnable(GL.GL_LIGHTING);

		// Add some ambient light
		float[] model_ambient = { 0.1f, 0.1f, 0.1f, 1.0f };
		gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, model_ambient, 0);
		// gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		gl.glLightModeli(GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
		
		statusRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 20), true, true);
		
		counter = new FPSCounter(drawable, 13);
		
		poolGame = new PoolGame();
		poolRoom = new PoolRoom();

		poolGame.resetBalls();
		
		camera = new PerspectiveCamera((float) VirtualPool.RunningConfig.GameFov, 
				(float) drawable.getWidth() / drawable.getHeight(), 2, 20000);
		camera.centerY = PoolTable.TOP_Y + PoolBall.RADIUS;
		
		poolGame.aimAtCue();
		camera.centerX = poolGame.getAimX();
		camera.centerZ = poolGame.getAimZ();
		
		marker = new Marker(camera);
						
		camera.addChild(marker);
		camera.addChild(poolRoom);
		camera.addChild(poolGame);
		
		gameState = GameState.AIM;
	}

	/**
	 * display() will be called repeatedly by the Animator when Animator is done
	 * it will swap buffers and update the display.
	 */
	public void display(GLAutoDrawable drawable)
	{
		GL gl = drawable.getGL();
		
		// clear the buffers
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		// reset the modelview matrix
	    gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		// set distance between camera and pivoting point
		camera.pivotDistance = (PoolStick.LENGTH + PoolStick.IDLE_DISTANCE) * zoomFactor;
				
		// make sure the rotations don't keep growing
		if (Math.abs(rotY) >= 360.0f) rotY = 0.0f;
		if (rotX > 90.0f) rotX = 90.0f;
		if (rotX < 0.0f) rotX = 00.0f;
		
		// if overview mode is on, block rotation to top view
		float rotXc = rotX;
		if (gameState == GameState.ABOVE) rotXc = 90.0f;
		
		// rotate the model based on mouse inputs
		camera.setRotation(rotXc, rotY);
		poolGame.setRotation(rotXc, rotY);
		
		// if watching animation, do not move cue
		if (!ballsRolling) poolGame.aimAtCue();

		// if balls stopped moving, go to aiming mode
		if (ballsRolling && !poolGame.isMoving())
		{
			ballsRolling = false;
			gameState = GameState.AIM;
			poolGame.aimAtCue();
			poolGame.resetBallDistance();
			camera.setRotation(rotX, rotY);
			camera.centerX = poolGame.getAimX();
			camera.centerZ = poolGame.getAimZ();
		}

		// marker drawn only when changing viewpoint
		marker.enabled = (gameState == GameState.VIEWPOINT);

		// draw everything!
		if (gameState == GameState.ABOVE)
		{
			float oldX = camera.centerX;
			float oldZ = camera.centerZ;
			float oldPivot = camera.pivotDistance;
			camera.centerX = 0;
			camera.centerZ = 0;
			camera.pivotDistance = PoolTable.MAX_X * 2;
			camera.traverse(gl);
			camera.centerX = oldX;
			camera.centerZ = oldZ;
			camera.pivotDistance = oldPivot;
		}
		else
			camera.traverse(gl);
		
		
		// draw FPS counter
		counter.draw();
		
		// display status
		gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
		statusRenderer.setColor(Color.YELLOW);
		statusRenderer.beginRendering(canvas.getWidth(), canvas.getHeight());
			String message = "VIRTUAL POOL Zero ~ Hold S to shoot!";
			if (adjustStatus == AdjustStatus.AMPLIFY) message += " ~ [AMPLIFY]";
			if (adjustStatus == AdjustStatus.DECREASE) message += " ~ [PRECISE]";
			if (gameState == GameState.PAUSE) message += " ~ [GAME PAUSED]";
			statusRenderer.draw(message, 10, 10);
		statusRenderer.endRendering();
		gl.glPopAttrib();
				
		gl.glFlush();
	}

	/**
	 * reshape() specifies what happens when the user reshapes the window.
	 */
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		GL gl = drawable.getGL();

		// set the new viewport
		gl.glViewport(0, 0, width, height);

		if (height <= 0) height = 1; // avoid a divide by zero error!
		camera.aspect = (float) width / height;
	}

	/**
	 * Called by the drawable when the display mode or the display device
	 * associated with the GLAutoDrawable has changed. However, you may leave
	 * this method empty during your project.
	 */
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
			boolean deviceChanged)
	{

	}

	//////////////////////////////////////////////////////////////////////////////////////////////////

	private void zoom(Point MousePt)
	{
		Point delta = new Point();
		delta.x = MousePt.x - canvas.getWidth()/2;
		delta.y = MousePt.y - canvas.getHeight()/2;

		float addition = -(delta.x + delta.y) / 250.0f * getCameraSensitivity();
		float zoomFactorNew = zoomFactor + addition;
		
		if (zoomFactorNew < MAX_SCALE && zoomFactorNew > MIN_SCALE)
			zoomFactor = zoomFactorNew;
		
		resetMouseLocation();
	}
	
	private void look(Point MousePt)
	{
		Point delta = new Point();
		delta.x = MousePt.x - canvas.getWidth()/2;
		delta.y = MousePt.y - canvas.getHeight()/2;

		rotY += delta.x * getCameraSensitivity();
		rotX += delta.y * getCameraSensitivity();
		
		resetMouseLocation();
	}
	
	private void moveViewpoint(Point MousePt)
	{
		Point delta = new Point();
		delta.x = MousePt.x - canvas.getWidth()/2;
		delta.y = MousePt.y - canvas.getHeight()/2;

		delta.x *= getCameraSensitivity();
		delta.y *= getCameraSensitivity();
		
		rotatePoint(delta, rotY);
		
		camera.centerX += delta.x;
		camera.centerZ += delta.y;
		
		if (camera.centerX > +PoolTable.MAX_X) camera.centerX = +PoolTable.MAX_X;
		if (camera.centerX < -PoolTable.MAX_X) camera.centerX = -PoolTable.MAX_X;
		if (camera.centerZ > +PoolTable.MAX_Z) camera.centerZ = +PoolTable.MAX_Z;
		if (camera.centerZ < -PoolTable.MAX_Z) camera.centerZ = -PoolTable.MAX_Z;
		
		resetMouseLocation();
	}
	
	private void moveCue(Point MousePt)
	{
		Point delta = new Point();
		delta.x = MousePt.x - canvas.getWidth()/2;
		delta.y = MousePt.y - canvas.getHeight()/2;

		delta.x *= getCameraSensitivity();
		delta.y *= getCameraSensitivity();
		
		rotatePoint(delta, rotY);
		
		poolGame.moveCue(delta.x, delta.y);
		poolGame.aimAtCue();
		camera.centerX = poolGame.getAimX();
		camera.centerZ = poolGame.getAimZ();
		
		resetMouseLocation();
	}
	
	private void moveStick(Point MousePt)
	{
		float addition = (MousePt.y - canvas.getHeight()/2) * 0.1f * getHitForce();
		
		if (!ballsRolling)
			ballsRolling = poolGame.adjustBallDistance(addition);
	
		resetMouseLocation();
	}

	private void resetMouseLocation()
	{
		Point topLeft = canvas.getLocationOnScreen();
		robot.mouseMove(topLeft.x + canvas.getWidth()/2, topLeft.y + canvas.getHeight()/2);
	}
	
	private void rotatePoint(Point point, float angle)
	{
		double rad = Math.toRadians(angle);
		double xr = point.x * Math.cos(rad) - point.y * Math.sin(rad);
		double yr = point.x * Math.sin(rad) + point.y * Math.cos(rad);
		point.x = (int) xr;
		point.y = (int) yr;
	}
	
	private void doClose()
	{
		// Run this on another thread than the AWT event queue to
		// make sure the call to Animator.stop() completes before exiting
		(new Thread()
		{
			@Override
			public void run()
			{
				animator.stop();
				System.exit(0);
			}
		}).start();
	}
	
	private float getCameraSensitivity()
	{
		if (adjustStatus == AdjustStatus.NORMAL)
			return (float) VirtualPool.RunningConfig.GameCameraSensitivity;
		if (adjustStatus == AdjustStatus.AMPLIFY)
			return (float) (VirtualPool.RunningConfig.GameCameraSensitivity * VirtualPool.RunningConfig.GameCameraSensitivityAdjustFactor);
		if (adjustStatus == AdjustStatus.DECREASE)
			return (float) (VirtualPool.RunningConfig.GameCameraSensitivity / VirtualPool.RunningConfig.GameCameraSensitivityAdjustFactor);
		return 1.0f;
	}

	private float getHitForce()
	{
		if (adjustStatus == AdjustStatus.NORMAL)
			return (float) VirtualPool.RunningConfig.PhysicsHitforce;
		if (adjustStatus == AdjustStatus.AMPLIFY)
			return (float) (VirtualPool.RunningConfig.PhysicsHitforce * VirtualPool.RunningConfig.PhysicsHitforceAdjustFactor);
		if (adjustStatus == AdjustStatus.DECREASE)
			return (float) (VirtualPool.RunningConfig.PhysicsHitforce / VirtualPool.RunningConfig.PhysicsHitforceAdjustFactor);
		return 1.0f;
	}
}
