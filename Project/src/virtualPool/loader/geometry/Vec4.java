package virtualPool.loader.geometry;

public class Vec4
{
	public float x, y, z, w;

	public Vec4()
	{
		this(0, 0, 0);
	}

	public Vec4(float _x, float _y, float _z)
	{
		x = _x;
		y = _y;
		z = _z;
		w = 1.0f;
	}

	public Vec4(float _x, float _y, float _z, float _w)
	{
		x = _x;
		y = _y;
		z = _z;
		w = _w;
	}

	public Vec4(Vec4 v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
		w = v.w;
	}
}
