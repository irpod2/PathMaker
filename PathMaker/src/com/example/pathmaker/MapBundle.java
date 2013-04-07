
package com.example.pathmaker;

import java.util.ArrayList;

public class MapBundle
{
	// Basically just a container for paths that is also serializable
	public ArrayList<MapPath> paths;

	public MapBundle()
	{
		paths = new ArrayList<MapPath>();
	}

	public MapBundle(ArrayList<MapPath> p)
	{
		paths = p;
	}

	public String serialize()
	{
		String me = "$";
		for (MapPath p : paths)
		{
			me += p.serialize();
		}
		me += "$";
		return me;
	}

	public static MapBundle createFromString(String bundleString)
	{
		if (bundleString.length() > 1 && bundleString.charAt(0) == '$')
		{
			ArrayList<MapPath> paths = new ArrayList<MapPath>();
			String pathString = bundleString.substring(1);
			while (pathString.length() > 0 && pathString.charAt(0) == '<')
			{
				int closeBracket = pathString.indexOf('>') + 1;
				MapPath p = MapPath.createFromString(pathString.substring(0,
						closeBracket));
				if (p != null)
					paths.add(p);
				else
					break;
				pathString = pathString.substring(closeBracket);
			}
			if (pathString.charAt(0) == '$')
			{
				MapBundle bundle = new MapBundle(paths);
				return bundle;
			}
		}
		return null;
	}

	public MapPath getPrimaryPath()
	{
		return paths.get(0);
	}
}
