package com.kristof.gameengine.object3d;

import com.kristof.gameengine.util.Vector3fExt;

public class Line {
	private final Vector3fExt point;
	private final Vector3fExt directionVector;
	
	public Line() {
		point = new Vector3fExt();
		directionVector = new Vector3fExt();
	}
	
	public Line(Vector3fExt point, Vector3fExt directionVector) {
		this.point = new Vector3fExt(point);
		this.directionVector = new Vector3fExt(directionVector.getNormalized());
	}
	
	public Vector3fExt getPoint() {
		return point.getCopy();
	}
	
	public Vector3fExt getDirectionVector() {
		return directionVector.getCopy();
	}
	
	public boolean isParallelWith(Line l2) {
		return directionVector.getCrossProductWith(l2.getDirectionVector()).getLengthSquare() < Vector3fExt.EQUALITY_RANGE_SQ;
	}
}
