package model;

import java.util.Arrays;
import java.util.Random;

public class NoiseGenerator {

	private static final int OCTAVES = 7;

	private double[][] randomValues = new double[OCTAVES][];
	private double[][] smoothedValues = new double[OCTAVES][];
	private double[] unsmoothedFinalValues = new double[Constants.WIDTH + 1];

	/*private double cosineInterpolate(double a, double b, double x) {
		double f = (1 - Math.cos(Math.PI * x)) * 0.5D;
		return a * (1 - f) + b * f;
	}*/
	
	private double cubicInterpolate(double v0, double v1, double v2, double v3, double x){
		double p = (v3 - v2) - (v0 - v1);
		double q = (v0 - v1) - p;
		double r = v2 - v0;
		double s = v1;
		
		double x2 = x * x;
		
		return p * x2 * x + q * x2 + r * x + s;
	}

	public int[] generateNoise(Random random,
			double persistence) {

		int[] finalValues = new int[unsmoothedFinalValues.length];
		
		populateSmoothedValues(random);

		Arrays.fill(unsmoothedFinalValues, 0D);

		for (int i = 0; i < OCTAVES; i++) {

			double amplitude = Math.pow(persistence, OCTAVES - i - 1)
					* (Constants.HEIGHT / (4 * persistence));
			double valueAdjust = 1.0 / (1 << i);
			for (int j = 0; j < Constants.WIDTH; j++) {
				unsmoothedFinalValues[j] += (int) (interpolate(i, j
						* valueAdjust) * amplitude);
			}
		}

		finalValues[0] = (int) unsmoothedFinalValues[0];
		finalValues[Constants.WIDTH] = (int) unsmoothedFinalValues[Constants.WIDTH];

		for (int i = 1; i < Constants.WIDTH; i++) {
			finalValues[i] = (int) ((unsmoothedFinalValues[i - 1] + unsmoothedFinalValues[i + 1]) * 0.25D + unsmoothedFinalValues[i] * 0.5D);
		}

		for (int x = 0; x < Constants.WIDTH + 1; x++) {
			finalValues[x] = Constants.HEIGHT - finalValues[x];
		}

		return finalValues;
	}

	private double interpolate(int octave, double x) {
		int integerX = (int) x;
		double fractionalX = x - integerX;
		double v0 = smoothedValues[octave][integerX];
		double v1 = smoothedValues[octave][integerX + 1];
		double v2 = smoothedValues[octave][integerX + 2];
		double v3 = smoothedValues[octave][integerX + 3];
		return cubicInterpolate(v0, v1, v2, v3, fractionalX);
	}

	private void populateSmoothedValues(Random random) {
		for (int i = 0; i < OCTAVES; i++) {
			int size = (Constants.WIDTH >> i) + 3;
			randomValues[i] = new double[size];
			smoothedValues[i] = new double[size];
			for (int j = 0; j < size; j++) {
				randomValues[i][j] = random.nextDouble();
			}
			smoothedValues[i][0] = randomValues[i][0] * 0.5
					+ randomValues[i][1] * 0.5;
			smoothedValues[i][size - 1] = randomValues[i][size - 1] * 0.5
					+ randomValues[i][size - 2] * 0.5;
			for (int j = 1; j < size - 1; j++) {
				smoothedValues[i][j] = randomValues[i][j] * 0.5
						+ randomValues[i][j - 1] * 0.25
						+ randomValues[i][j + 1] * 0.25;
			}
		}
	}
}
