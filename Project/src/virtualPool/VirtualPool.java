package virtualPool;
import virtualPool.graphics.PoolWindow;
import virtualPool.util.Configuration;
import virtualPool.util.Logs;

public class VirtualPool
{
	public static class RunningConfig
	{
		public static final String configFile = "config.properties";
		
		private static Configuration config = new Configuration(configFile);
		
		public static int 	SystemFps = (config.new IntegerProperty("system.fps", 100, 1, 500, 
				"Target number of Frames Per Second [1-100]")).getValue();
		public static boolean	SystemAntialiasingPoint = (config.new BooleanProperty("system.antialiasing.point", true, 
				"Enable point antialiasing [true/false]")).getValue();
		public static boolean SystemAntialiasingLine = (config.new BooleanProperty("system.antialiasing.line", true, 
				"Enable line antialiasing [true/false]")).getValue();
		public static boolean SystemAntialiasingPolygon = (config.new BooleanProperty("system.antialiasing.polygon", true, 
				"Enable polygon antialiasing [true/false]")).getValue();
		public static double 	GameSpeed = (config.new DoubleProperty("game.speed", 1, 0.01, 500, 
				"Control how fast balls are moving")).getValue();
		public static double 	GameFov = (config.new DoubleProperty("game.fov", 45, 1, 179, 
				"Perspective camera Field Of View [realistic=45]")).getValue();
		public static double 	GameCameraSensitivity = (config.new DoubleProperty("game.camera.sensitivity", 0.2, 0.001, 100, 
				"Camera sensitivity with respect to mouse movement")).getValue();
		public static double 	GameCameraSensitivityAdjustFactor = (config.new DoubleProperty("game.camera.sensitivity.adjust", 3, 0.01, 500, 
				"Camera sensitivity temporary adjustment factor")).getValue();
		public static double 	PhysicsFriction = (config.new DoubleProperty("physics.friction", 300, 0.01, 500, 
				"Drag coefficient")).getValue();
		public static double 	PhysicsHitforce = (config.new DoubleProperty("physics.hitforce", 1, 0.01, 500, 
				"Force of cue stick strike")).getValue();
		public static double 	PhysicsHitforceAdjustFactor = (config.new DoubleProperty("physics.hitforce.adjust", 3, 0.01, 500, 
				"Strike force temporary adjustment factor")).getValue();
		public static double 	PhysicsElasticity = (config.new DoubleProperty("physics.elasticity", 30, 0.01, 500, 
				"Kinetic energy loss coefficient")).getValue();
		public static String	KeysAim = (config.new StringProperty("keys.aim", "A", 1, 10, 
				"Enter aiming mode")).getValue();
		public static String	KeysReset = (config.new StringProperty("keys.reset", "R", 1, 10, 
				"Respawn balls on table")).getValue();
		public static String	KeysShoot = (config.new StringProperty("keys.shoot", "S", 1, 10, 
				"Enter cue stick strike mode")).getValue();
		public static String	KeysOverview = (config.new StringProperty("keys.overview", "X", 1, 10, 
				"Show table overview")).getValue();
		public static String	KeysMove = (config.new StringProperty("keys.move", "M", 1, 10, 
				"Move cue ball")).getValue();
		public static String	KeysViewpoint = (config.new StringProperty("keys.viewpoint", "V", 1, 10, 
				"Change viewpoint")).getValue();
		public static String	KeysQuit = (config.new StringProperty("keys.quit", "Escape", 1, 10, 
				"Quit game")).getValue();
		public static String	KeysPause = (config.new StringProperty("keys.pause", "Space", 1, 10, 
				"Pause game")).getValue();
		public static String	KeysForceAmplify = (config.new StringProperty("keys.adjust+", "Period", 1, 10, 
				"Sensitivity and hit force amplify trigger")).getValue();
		public static String	KeysForceDecrease = (config.new StringProperty("keys.adjust-", "Comma", 1, 10, 
				"Sensitivity and hit force soften trigger")).getValue();
	
		static
		{
			config.DoRewrite();
		}
	}
	
	public static void main(String[] args)
	{
		Logs.log.info("Hello pool!");
		
		// Make sure config is initialized
		try { Class.forName("virtualPool.VirtualPool$RunningConfig"); }
		catch (ClassNotFoundException e) { e.printStackTrace(); }
		
		new PoolWindow();
	}
}
