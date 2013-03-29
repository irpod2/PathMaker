
package com.example.pathmaker;

import java.io.Serializable;

public class Edge implements Serializable
{
	private static final long serialVersionUID = 7911569938578276537L;

	public int edgeToId;

	public Edge(int id)
	{
		edgeToId = id;
	}
}
