
package com.example.pathmaker;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

public class PathView extends ImageView
{
	private final float MIN_DIST = 30.0f;
	private final float MAX_DIST = 50.0f;
	private final Context context;
	private Paint thinPaint = new Paint();
	private Paint thickPaint = new Paint();
	private ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
	private MapBundle bundle = new MapBundle();
	private Waypoint lastPoint;
	private Waypoint lastParent;
	private float cameraWidth;
	private float cameraHeight;

	public PathView(Context baseContext, int resourceID, float camWidth,
			float camHeight)
	{
		super(baseContext);

		cameraWidth = camWidth;
		cameraHeight = camHeight;

		context = baseContext;

		setPaints();

		setImageResource(resourceID);
	}

	private void setPaints()
	{
		thinPaint.setColor(Color.BLUE);
		thinPaint.setStyle(Paint.Style.STROKE);
		thinPaint.setStrokeWidth(8);
		thickPaint.setColor(Color.BLUE);
		thickPaint.setStyle(Paint.Style.STROKE);
		thickPaint.setStrokeWidth(16);
	}

	public void clearPath()
	{
		lastPoint = null;
		lastParent = null;
		bundle.paths.clear();
		waypoints.clear();
	}

	public void savePaths(String filename)
	{
		if (!bundle.paths.isEmpty())
		{
			bundle.save(context, filename);
		}
	}

	public void loadPaths(String filename)
	{
		bundle = MapBundle.load(context, filename);
		if (bundle != null)
		{
			for (MapPath p : bundle.paths)
			{
				p.resize(cameraWidth, cameraHeight);
				for (int i = 0; i < p.size(); i++)
				{
					waypoints.add(p.getWaypoint(i));
				}
			}
			unvisitNodes();
		}
		else
			Toast.makeText(context, "Could not load path", Toast.LENGTH_SHORT)
					.show();
	}

	private void unvisitNodes()
	{
		for (Waypoint wp : waypoints)
		{
			wp.visited = false;
		}
	}

	private float getDistance(Waypoint a, Waypoint b)
	{
		float dx = b.x - a.x;
		float dy = b.y - a.y;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	// Gets closest point already placed
	private Waypoint getClosestWaypoint(Waypoint wp)
	{
		float minDist = MAX_DIST;
		Waypoint retPt = null;
		for (Waypoint candidate : waypoints)
		{
			if (candidate == lastPoint || candidate == lastParent)
				continue;
			float dist = getDistance(candidate, wp);
			if (dist < minDist)
			{
				retPt = candidate;
				minDist = dist;
			}
		}
		return retPt;
	}

	private void connectPoints(Waypoint a, Waypoint b)
	{
		if (a.path != b.path)
		{
			bundle.paths.remove(a.path);
			// Switch all waypoints in a's path to b's path
			b.path.integrate(a.path);
		}
		a.addConnection(b);
		b.addConnection(a);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		Waypoint wp = new Waypoint((int) e.getX(), (int) e.getY());
		if (e.getAction() == MotionEvent.ACTION_DOWN)
		{
			Waypoint closest = getClosestWaypoint(wp);
			if (closest != null)
			{
				lastPoint = closest;
			}
			else
			{
				bundle.paths.add(new MapPath(wp, cameraWidth, cameraHeight));
				lastPoint = wp;
			}
			waypoints.add(wp);
		}
		else if (e.getAction() == MotionEvent.ACTION_MOVE)
		{
			if (getDistance(wp, lastPoint) > MIN_DIST)
			{
				lastPoint.path.addWaypoint(wp);
				connectPoints(wp, lastPoint);

				lastParent = lastPoint;
				lastPoint = wp;
				waypoints.add(wp);
			}
		}
		else if (e.getAction() == MotionEvent.ACTION_UP)
		{
			Waypoint closest = getClosestWaypoint(wp);
			if (closest != null)
			{
				connectPoints(lastPoint, closest);
			}
			lastPoint = null;
			lastParent = null;
		}
		return true;
	}

	private int maskBytes(int numBytes)
	{
		int mask = 0xff000000;
		// Ones
		if ((numBytes & 0x00000001) != 0)
			mask |= 0x000000ff;
		// Twos
		if ((numBytes & 0x00000002) != 0)
			mask |= 0x0000ff00;
		// Fours
		if ((numBytes & 0x00000004) != 0)
			mask |= 0x00ff0000;
		return mask;
	}

	private void recursiveDrawLine(Canvas canvas, Waypoint wp)
	{
		wp.visited = true;
		canvas.drawPoint(wp.x, wp.y, thickPaint);
		MapPath path = wp.path;
		for (Edge childEdge : wp.getConnections())
		{
			Waypoint child = path.getWaypoint(childEdge.edgeToId);
			canvas.drawLine(wp.x, wp.y, child.x, child.y, thinPaint);
			if (!child.visited)
			{
				recursiveDrawLine(canvas, child);
			}
		}
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);

		for (int i = 0; i < bundle.paths.size(); i++)
		{
			int color = maskBytes(i % 7 + 1);
			thinPaint.setColor(color);
			thickPaint.setColor(color);
			recursiveDrawLine(canvas, bundle.paths.get(i).getRoot());
		}
		unvisitNodes();

		invalidate();
	}

	public void onOrientationChanged(float camWidth, float camHeight)
	{
		cameraWidth = camWidth;
		cameraHeight = camHeight;
		bundle.resize(camWidth, camHeight);
	}
}
