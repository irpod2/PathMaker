
package com.example.pathmaker;

import java.util.ArrayList;

public class Waypoint
{
	private ArrayList<Edge> edges;
	public boolean visited = false;
	public int id;
	public int x;
	public int y;
	public MapPath path;

	// The fundamental unit of travel. A character would ideally walk between
	// connected waypoints in the actual game.
	// For description of design choices see MapPath.java or the Readme.
	public Waypoint(int pX, int pY)
	{
		x = pX;
		y = pY;
		path = null;
		edges = new ArrayList<Edge>();
	}

	public Waypoint(int pX, int pY, int ID, ArrayList<Edge> edgeList)
	{
		x = pX;
		y = pY;
		id = ID;
		edges = edgeList;
	}

	public String serialize()
	{
		String me = "{" + String.valueOf(id) + "(" + String.valueOf(x) + ","
				+ String.valueOf(y) + ")";
		for (Edge e : edges)
		{
			me += "[" + String.valueOf(e.edgeToId) + "]";
		}
		me += "}";
		return me;
	}

	public static Waypoint createFromString(String wpString)
	{
		try
		{
			if (wpString.charAt(0) == '{')
			{
				int openParen = wpString.indexOf('(');
				int id = Integer.parseInt(wpString.substring(1, openParen));
				int comma = wpString.indexOf(',');
				int x = Integer.parseInt(wpString.substring(openParen + 1,
						comma));
				int closeParen = wpString.indexOf(')');
				int y = Integer.parseInt(wpString.substring(comma + 1,
						closeParen));
				String edgeString = wpString.substring(closeParen + 1);
				ArrayList<Edge> edges = new ArrayList<Edge>();
				while (edgeString.length() != 0)
				{
					if (edgeString.charAt(0) == '[')
					{
						int closeBracket = edgeString.indexOf(']');
						int edgeToId = Integer.parseInt(edgeString.substring(1,
								closeBracket));
						edges.add(new Edge(edgeToId));
						edgeString = edgeString.substring(closeBracket + 1);
					}
					else if (edgeString.charAt(0) == '}')
						edgeString = "";
				}
				Waypoint wp = new Waypoint(x, y, id, edges);
				return wp;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	// When a path gets integrated into another, the edge ids all change, but
	// they are offset by a constant number corresponding to the number of
	// Waypoints in the new Path at the time of integration.
	public void addToEdgeIds(int amount)
	{
		for (Edge e : edges)
			e.edgeToId += amount;
	}

	// Connects this waypoint to wp
	public void addConnection(Waypoint wp)
	{
		if (!edges.contains(wp.id))
			edges.add(new Edge(wp.id));
	}

	public ArrayList<Edge> getConnections()
	{
		return edges;
	}
}
