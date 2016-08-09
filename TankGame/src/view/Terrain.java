package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import model.Constants;
import model.NoiseGenerator;
import model.RenderHelper;
import model.Renderable;
import model.Tank;
import model.Vector;

public class Terrain implements Renderable {

	private static final Terrain terrain = new Terrain();

	public static Terrain getTerrain() {
		return terrain;
	}

	private int[] originalHeightMap;

	private NoiseGenerator terrainGenerator;
	private BufferedImage terrainImage = GraphicsEnvironment
			.getLocalGraphicsEnvironment().getDefaultScreenDevice()
			.getDefaultConfiguration()
			.createCompatibleImage(Constants.WIDTH, Constants.HEIGHT);
	private Graphics2D terrainGraphics = terrainImage.createGraphics();
	private Area area;

	private Terrain() {

	}

	public boolean doesTerrainExistAt(Vector position) {
		return area.contains(position.asPoint2D());
	}
	
	private Vector getClosestPointTo(Vector point){
		Vector closest = new Vector(1E6D, 1E6D);
		Vector prev = null;
		for (PathIterator iter = area.getPathIterator(null); !iter.isDone(); iter.next()){
			double[] coords = new double[6];
			int type = iter.currentSegment(coords);
			if (type == PathIterator.SEG_MOVETO){
				prev = new Vector(coords[0], coords[1]);
			} else if (type == PathIterator.SEG_LINETO){
				Vector now = new Vector(coords[0], coords[1]);
				Vector diff = point.subtract(now);
				Vector orth = diff.getOrthogonalComponent(now.subtract(prev));
				Vector tempClosest = point.subtract(orth);
				double segDistSq = now.subtract(prev).getNormSquared();
				double prevDistSq = prev.subtract(tempClosest).getNormSquared();
				double nowDistSq = now.subtract(tempClosest).getNormSquared();
				if (prevDistSq >= segDistSq){
					tempClosest = now;
				} else if (nowDistSq >= segDistSq){
					tempClosest = prev;
				}
				//System.out.println(tempClosest + ", " + prev + ", " + now + ", " + orth);
				if (point.subtract(tempClosest).getNormSquared() < point.subtract(closest).getNormSquared()){
					closest = tempClosest;
					//System.out.println(point.subtract(tempClosest).getNormSquared());
				}
				prev = now;
			} else if (type == PathIterator.SEG_QUADTO){
				Vector p1 = new Vector(coords[0], coords[1]);
				Vector p2 = new Vector(coords[2], coords[3]);
				double prevTest = 0.5D;
				double test = 0.5D;
				do {
					double temp = test;
					test = prevTest - getQuadraticDistanceDerivative(point, prev, p1, p2, prevTest) / getQuadraticDistanceDerivativeDerivative(point, prev, p1, p2, prevTest);
					prevTest = temp;
				} while (Math.abs(prevTest - test) > 0.05D);
				Vector testPoint = prev.multiply((1 - test) * (1 - test)).add(p1.multiply(2 * (1 - test) * test)).add(p2.multiply(test * test));
				double testDistSq = point.subtract(testPoint).getNormSquared();
				double prevDistSq = point.subtract(prev).getNormSquared();
				double nowDistSq = point.subtract(p2).getNormSquared();
				if (testDistSq > prevDistSq || testDistSq > nowDistSq){
					if (prevDistSq < nowDistSq){
						testPoint = prev;
					} else {
						testPoint = p2;
					}
				}
				if (closest.subtract(point).getNormSquared() > testPoint.subtract(point).getNormSquared()){
					closest = testPoint;
				}
				prev = p2;
			} else if (type == PathIterator.SEG_CUBICTO){
				Vector p1 = new Vector(coords[0], coords[1]);
				Vector p2 = new Vector(coords[2], coords[3]);
				Vector p3 = new Vector(coords[4], coords[5]);
				double prevTest = 0.5D;
				double test = 0.5D;
				do {
					double temp = test;
					test = prevTest - getCubicDistanceDerivative(point, prev, p1, p2, p3, prevTest) / getCubicDistanceDerivativeDerivative(point, prev, p1, p2, p3, prevTest);
					prevTest = temp;
				} while (Math.abs(prevTest - test) > 0.05D);
				Vector testPoint = prev.multiply((1 - test) * (1 - test) * (1 - test)).add(p1.multiply(3 * (1 - test) * (1 - test) * test)).add(p2.multiply(3 * (1 - test) * test * test)).add(p3.multiply(test * test * test));
				double testDistSq = point.subtract(testPoint).getNormSquared();
				double prevDistSq = point.subtract(prev).getNormSquared();
				double nowDistSq = point.subtract(p3).getNormSquared();
				if (testDistSq > prevDistSq || testDistSq > nowDistSq){
					if (prevDistSq < nowDistSq){
						testPoint = prev;
					} else {
						testPoint = p3;
					}
				}
				if (closest.subtract(point).getNormSquared() > testPoint.subtract(point).getNormSquared()){
					closest = testPoint;
				}
				prev = p3;
			}
		}
		return closest;
	}
	private double getCubicDistanceDerivative(Vector point, Vector p0, Vector p1, Vector p2, Vector p3, double t){
		return - 6 * p0.getX() * p0.getX() - 6 * p0.getY() * p0.getY() + 6 * p0.getX() * p1.getX() + 6 * p0.getY() * p1.getY() + 6 * p0.getX() * point.getX() - 6 * p1.getX() * point.getX() + 6 * p0.getY() * point.getY() - 
				 6 * p1.getY() * point.getY() + (30 * p0.getX() * p0.getX() + 30 * p0.getY() * p0.getY() - 60 * p0.getX() * p1.getX() + 18 * p1.getX() * p1.getX() - 60 * p0.getY() * p1.getY() + 
						    18 * p1.getY() * p1.getY() + 12 * p0.getX() * p2.getX() + 12 * p0.getY() * p2.getY() - 12 * p0.getX() * point.getX() + 24 * p1.getX() * point.getX() - 12 * p2.getX() * point.getX() - 
						    12 * p0.getY() * point.getY() + 24 * p1.getY() * point.getY() - 12 * p2.getY() * point.getY()) * t + (-60 * p0.getX() * p0.getX() - 60 * p0.getY() * p0.getY() + 
						    180 * p0.getX() * p1.getX() - 108 * p1.getX() * p1.getX() + 180 * p0.getY() * p1.getY() - 108 * p1.getY() * p1.getY() - 72 * p0.getX() * p2.getX() + 
						    54 * p1.getX() * p2.getX() - 72 * p0.getY() * p2.getY() + 54 * p1.getY() * p2.getY() + 6 * p0.getX() * p3.getX() + 6 * p0.getY() * p3.getY() + 6 * p0.getX() * point.getX() - 
						    18 * p1.getX() * point.getX() + 18 * p2.getX() * point.getX() - 6 * p3.getX() * point.getX() + 6 * p0.getY() * point.getY() - 18 * p1.getY() * point.getY() + 18 * p2.getY() * point.getY() - 
						    6 * p3.getY() * point.getY()) * t * t + (60 * p0.getX() * p0.getX() + 60 * p0.getY() * p0.getY() - 240 * p0.getX() * p1.getX() + 216 * p1.getX() * p1.getX() - 
						    240 * p0.getY() * p1.getY() + 216 * p1.getY() * p1.getY() + 144 * p0.getX() * p2.getX() - 216 * p1.getX() * p2.getX() + 36 * p2.getX() * p2.getX() + 
						    144 * p0.getY() * p2.getY() - 216 * p1.getY() * p2.getY() + 36 * p2.getY() * p2.getY() - 24 * p0.getX() * p3.getX() + 24 * p1.getX() * p3.getX() - 
						    24 * p0.getY() * p3.getY() + 24 * p1.getY() * p3.getY()) * t * t * t + (-30 * p0.getX() * p0.getX() - 30 * p0.getY() * p0.getY() + 150 * p0.getX() * p1.getX() - 
						    180 * p1.getX() * p1.getX() + 150 * p0.getY() * p1.getY() - 180 * p1.getY() * p1.getY() - 120 * p0.getX() * p2.getX() + 270 * p1.getX() * p2.getX() - 
						    90 * p2.getX() * p2.getX() - 120 * p0.getY() * p2.getY() + 270 * p1.getY() * p2.getY() - 90 * p2.getY() * p2.getY() + 30 * p0.getX() * p3.getX() - 60 * p1.getX() * p3.getX() + 
						    30 * p2.getX() * p3.getX() + 30 * p0.getY() * p3.getY() - 60 * p1.getY() * p3.getY() + 30 * p2.getY() * p3.getY()) * t * t * t * t + (6 * p0.getX() * p0.getX() + 
						    6 * p0.getY() * p0.getY() - 36 * p0.getX() * p1.getX() + 54 * p1.getX() * p1.getX() - 36 * p0.getY() * p1.getY() + 54 * p1.getY() * p1.getY() + 36 * p0.getX() * p2.getX() - 
						    108 * p1.getX() * p2.getX() + 54 * p2.getX() * p2.getX() + 36 * p0.getY() * p2.getY() - 108 * p1.getY() * p2.getY() + 54 * p2.getY() * p2.getY() - 12 * p0.getX() * p3.getX() + 
						    36 * p1.getX() * p3.getX() - 36 * p2.getX() * p3.getX() + 6 * p3.getX() * p3.getX() - 12 * p0.getY() * p3.getY() + 36 * p1.getY() * p3.getY() - 36 * p2.getY() * p3.getY() + 
						    6 * p3.getY() * p3.getY()) * t * t * t * t * t;
	}
	
	private double getCubicDistanceDerivativeDerivative(Vector point, Vector p0, Vector p1, Vector p2, Vector p3, double t){
		return 30 * p0.getX() * p0.getX() + 30 * p0.getY() * p0.getY() - 60 * p0.getX() * p1.getX() + 18 * p1.getX() * p1.getX() - 60 * p0.getY() * p1.getX() + 18 * p1.getX() * p1.getX() + 
				 12 * p0.getX() * p2.getX() + 12 * p0.getY() * p2.getY() - 12 * p0.getX() * point.getX() + 24 * p1.getX() * point.getX() - 12 * p2.getX() * point.getX() - 12 * p0.getY() * point.getY() + 
				 24 * p1.getX() * point.getY() - 12 * p2.getY() * point.getY() + 
				 2 * (-60 * p0.getX() * p0.getX() - 60 * p0.getY() * p0.getY() + 180 * p0.getX() * p1.getX() - 108 * p1.getX() * p1.getX() + 180 * p0.getY() * p1.getX() - 
				    108 * p1.getX() * p1.getX() - 72 * p0.getX() * p2.getX() + 54 * p1.getX() * p2.getX() - 72 * p0.getY() * p2.getY() + 54 * p1.getX() * p2.getY() + 6 * p0.getX() * p3.getX() + 
				    6 * p0.getY() * p3.getY() + 6 * p0.getX() * point.getX() - 18 * p1.getX() * point.getX() + 18 * p2.getX() * point.getX() - 6 * p3.getX() * point.getX() + 6 * p0.getY() * point.getY() - 
				    18 * p1.getX() * point.getY() + 18 * p2.getY() * point.getY() - 6 * p3.getY() * point.getY()) * t + 
				 3 * (60 * p0.getX() * p0.getX() + 60 * p0.getY() * p0.getY() - 240 * p0.getX() * p1.getX() + 216 * p1.getX() * p1.getX() - 240 * p0.getY() * p1.getX() + 216 * p1.getX() * p1.getX() + 
				    144 * p0.getX() * p2.getX() - 216 * p1.getX() * p2.getX() + 36 * p2.getX() * p2.getX() + 144 * p0.getY() * p2.getY() - 216 * p1.getX() * p2.getY() + 
				    36 * p2.getY() * p2.getY() - 24 * p0.getX() * p3.getX() + 24 * p1.getX() * p3.getX() - 24 * p0.getY() * p3.getY() + 24 * p1.getX() * p3.getY()) * t * t + 
				 4 * (-30 * p0.getX() * p0.getX() - 30 * p0.getY() * p0.getY() + 150 * p0.getX() * p1.getX() - 180 * p1.getX() * p1.getX() + 150 * p0.getY() * p1.getX() - 
				    180 * p1.getX() * p1.getX() - 120 * p0.getX() * p2.getX() + 270 * p1.getX() * p2.getX() - 90 * p2.getX() * p2.getX() - 120 * p0.getY() * p2.getY() + 
				    270 * p1.getX() * p2.getY() - 90 * p2.getY() * p2.getY() + 30 * p0.getX() * p3.getX() - 60 * p1.getX() * p3.getX() + 30 * p2.getX() * p3.getX() + 30 * p0.getY() * p3.getY() - 
				    60 * p1.getX() * p3.getY() + 30 * p2.getY() * p3.getY()) * t * t * t + 
				 5 * (6 * p0.getX() * p0.getX() + 6 * p0.getY() * p0.getY() - 36 * p0.getX() * p1.getX() + 54 * p1.getX() * p1.getX() - 36 * p0.getY() * p1.getX() + 54 * p1.getX() * p1.getX() + 
				    36 * p0.getX() * p2.getX() - 108 * p1.getX() * p2.getX() + 54 * p2.getX() * p2.getX() + 36 * p0.getY() * p2.getY() - 108 * p1.getX() * p2.getY() + 54 * p2.getY() * p2.getY() - 
				    12 * p0.getX() * p3.getX() + 36 * p1.getX() * p3.getX() - 36 * p2.getX() * p3.getX() + 6 * p3.getX() * p3.getX() - 12 * p0.getY() * p3.getY() + 36 * p1.getX() * p3.getY() - 
				    36 * p2.getY() * p3.getY() + 6 * p3.getY() * p3.getY()) * t * t * t * t;
	}
	
	public Vector getInnerImpactLocation(Shape outsideShape, Shape insideShape,
			Vector outsidePosition, Vector insidePosition) {
		Vector displacement = insidePosition.subtract(outsidePosition);
		if (displacement.isZero()) {
			return insidePosition;
		}
		double distance = displacement.getNorm();
		double min = 0D;
		double max = distance;
		double mid = distance * 0.5D;
		Shape minShape = outsideShape;
		Shape maxShape = insideShape;
		while (Math.abs(max - min) > 0.25D) {
			mid = (max + min) * 0.5D;
			Vector midDiff = displacement.setLength(mid);
			AffineTransform translation = AffineTransform.getTranslateInstance(
					midDiff.getX(), midDiff.getY());
			Shape midShape = translation.createTransformedShape(outsideShape);
			if (isDivideBetween(midShape, maxShape)) {
				min = mid;
				minShape = midShape;
			} else if (isDivideBetween(minShape, midShape)) {
				max = mid;
				maxShape = midShape;
			} else {
				return insidePosition;
			}
		}
		Vector ret = displacement.setLength(max).add(outsidePosition);
		return ret;
	}
	
	public Vector getNormalVectorWith(Shape shape){
		Area tempArea = new Area(shape);
		tempArea.intersect(area);
		Rectangle2D r = tempArea.getBounds2D();
		if (!r.isEmpty()){
			Vector center = new Vector(r.getCenterX(), r.getCenterY());
			return getSurfaceNormalVector(center);
		} else {
			Vector position = new Vector(shape.getBounds2D().getCenterX(), shape.getBounds2D().getCenterY());
			Vector point = getClosestPointTo(position);
			Vector normal =  getSurfaceNormalVector(point);
			return normal;
		}
	}
	
	public int getOriginalHeightValue(int x) {
		return originalHeightMap[x];
	}
	
	public Vector getOuterImpactLocation(Shape outsideShape, Shape insideShape, Vector outsidePosition, Vector insidePosition){
		Vector displacement = insidePosition.subtract(outsidePosition);
		if (displacement.isZero()){
			return insidePosition;
		}
		double distance = displacement.getNorm();
		double min = 0D;
		double max = distance;
		double mid = distance * 0.5D;
		Shape minShape = outsideShape;
		Shape maxShape = insideShape;
		while (Math.abs(max - min) > 0.25D){
			 mid = (max + min) * 0.5D;
			 Vector midDiff = displacement.setLength(mid);
			 AffineTransform translation = AffineTransform.getTranslateInstance(midDiff.getX(), midDiff.getY());
			 Shape midShape = translation.createTransformedShape(outsideShape);
			 if (isDivideBetween(midShape, maxShape)){
				 min = mid;
				 minShape = midShape;
			 } else if (isDivideBetween(minShape, midShape)){
				 max = mid;
				 maxShape = midShape;
			 } else {
				 return outsidePosition;
			 }
		}
		return displacement.setLength(min).add(outsidePosition);
	}

	private double getQuadraticDistanceDerivative(Vector point, Vector p0, Vector p1, Vector p2, double t) {
		return 4D * (-p0.getX() + p1.getX() + (p0.getX() - 2 * p1.getX() + p2.getX()) * t) * (-point.getX() + 2 * (-p0.getX() + p1.getX()) * t + (p0.getX() - 2 * p1.getX() + p2.getX()) * t * t) + 4D * (-p0.getY() + p1.getY() + (p0.getY() - 2 * p1.getY() + p2.getY()) * t) * (-point.getY() + 2 * (-p0.getY() + p1.getY()) * t + (p0.getY() - 2 * p1.getY() + p2.getY()) * t * t);
	}
	
	private double getQuadraticDistanceDerivativeDerivative(Vector point, Vector p0, Vector p1, Vector p2, double t) {
		return 12 * p0.getX() * p0.getX() + 12 * p0.getY() * p0.getY() - 24 * p0.getX() * p1.getX() + 8 * p1.getX() * p1.getX() - 24 * p0.getY() * p1.getY() + 8 * p1.getY() * p1.getY() + 4 * p0.getX() * p2.getX() + 4 * p0.getY() * p2.getY() - 4 * p0.getX() * point.getX() + 8 * p1.getX() * point.getX() - 4 * p2.getX() * point.getX() - 4 * p0.getY() * point.getY() + 8 * p1.getY() * point.getY() - 4 * p2.getY() * point.getY()
				+ (-24 * p0.getX() * p0.getX() - 24 * p0.getY() * p0.getY() + 72 * p0.getX() * p1.getX() - 48 * p1.getX() * p1.getX() + 72 * p0.getY() * p1.getY() - 48 * p1.getY() * p1.getY() - 24 * p0.getX() * p2.getX() + 24 * p1.getX() * p2.getX() - 24 * p0.getY() * p2.getY() + 24 * p1.getY() * p2.getY()) * t
				+ (12 * p0.getX() * p0.getX() + 12 * p0.getY() * p0.getY() - 48 * p0.getX() * p1.getX() + 48 * p1.getX() * p1.getX() - 48 * p0.getY() * p1.getY() + 48 * p1.getY() * p1.getY() + 24 * p0.getX() * p2.getX() - 48 * p1.getX() * p2.getX() + 12 * p2.getX() * p2.getX() + 24 * p0.getY() * p2.getY() - 48 * p1.getY() * p2.getY() + 12 * p2.getY() * p2.getY()) * t * t;
	}
	
	@Override
	public int getRenderLayer() {
		return -1000;
	}

	public Vector getSurfaceNormalVector(Vector position) {
		return getSurfaceNormalVector(position, 5);
	}

	public Vector getSurfaceNormalVector(Vector position, int searchDistance) {

		Vector normal = Vector.ZERO;

		for (int diffX = -searchDistance; diffX <= searchDistance; diffX++) {
			for (int diffY = -searchDistance; diffY <= searchDistance; diffY++) {
				Vector diff = new Vector(diffX, diffY);
				Vector fin = position.add(diff);
				if (doesTerrainExistAt(fin)) {
					normal = normal.subtract(diff);
				}
			}
		}

		return normal;
	}

	private boolean isDivideBetween(Shape before, Shape after){
		boolean beforeInside = isInsideTerrain(before);
		boolean afterInside = isInsideTerrain(after);
		return beforeInside != afterInside;
	}

	public boolean isInsideTerrain(Shape shape){
		Area tempArea = new Area(area);
		tempArea.intersect(new Area(shape));
		return !tempArea.isEmpty();
	}

	@Override
	public void render(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2.drawImage(terrainImage, 0, 0, null);
	}

	public void reset() {
		terrainGenerator = new NoiseGenerator();
		originalHeightMap = terrainGenerator.generateNoise(Tank.random, 0.4D);
		Polygon polygon = new Polygon();
		for (int x = 0; x <= Constants.WIDTH; x++) {
			polygon.addPoint(x, getOriginalHeightValue(x));
		}
		polygon.addPoint(Constants.WIDTH, Constants.HEIGHT);
		polygon.addPoint(0, Constants.WIDTH);
		area = new Area(polygon);
		RenderHelper.setAntialias(terrainGraphics, true);
		terrainGraphics.setBackground(Color.WHITE);
		terrainGraphics.clearRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
		terrainGraphics.setColor(Color.BLACK);
		terrainGraphics.fill(area);
		//RenderHelper.setAntialias(terrainGraphics, true);
		/*for (int x = 0; x < Constants.WIDTH; x++) {
			terrainGraphics.drawLine(x, getOriginalHeightValue(x), x + 1,
					getOriginalHeightValue(x + 1));
		}*/
		
		/*for (int x = 5; x <= Constants.WIDTH - 5; x += 10){
			Vector position = new Vector(x, getOriginalHeightValue(x) - 1);
			Vector normal = getSurfaceNormalVector(position);
			if (!Vector.ZERO.equals(normal)){
				Vector fPosition = position.add(normal.setLength(10D));
				terrainGraphics.setColor(Color.RED);
				terrainGraphics.drawLine(position.getIntegerX(), position.getIntegerY(), fPosition.getIntegerX(), fPosition.getIntegerY());
			}
		}*/
		
	}

	public void setTerrainAroundRadius(Vector position, double radius,
			boolean value) {
		Ellipse2D circle = new Ellipse2D.Double(position.getX() - radius, position.getY() - radius, 2D * radius, 2D * radius);
		if (value){
			area.add(new Area(circle));
		} else {
			area.subtract(new Area(circle));
		}
		terrainGraphics.setColor(value ? Color.BLACK : Color.WHITE);
		terrainGraphics.fill(circle);
	}

}
