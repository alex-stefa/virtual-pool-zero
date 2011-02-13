package virtualPool.loader;

import java.util.HashMap;

import javax.media.opengl.GL;

public class DisplayListCache
{
	private static HashMap<Object, Integer> listCache = new HashMap<Object, Integer>();

	public static void clear()
	{
		listCache.clear();
	}

	public static int get(Object objID)
	{
		if (listCache.containsKey(objID))
			return listCache.get(objID);
		else
			return -1;
	}

	public static void remove(Object objID, GL gl, int howMany)
	{
		Integer list = listCache.get(objID);

		if (list != null) gl.glDeleteLists(list, howMany);

		listCache.remove(objID);
	}

	/**
	 * Returns an integer identifier for an OpenGL display list based on the
	 * object being passed in. If the object already has a display list
	 * allocated, the existing ID is returned.
	 */
	public static int generateList(Object objID, GL gl, int howMany)
	{
		Integer list = null;

		list = listCache.get(objID);
		if (list == null)
		{
			list = new Integer(gl.glGenLists(howMany));
			listCache.put(objID, list);
		}

		return list;
	}
}
