package org.rootio.tools.utils;

public class PoorManStatistics {

	public static double min(double[] input){
		double min = input.length > 0? input[0]: 0;
		for(double value : input)
		{
			min = Math.min(min,  value);
		}
		return min;
	}
	
	public static double max(double[] input)
	{
		double max = 0;
		for(double value : input)
		{
			max = Math.max(max,  value);
		}
		return max;
	}
	
	public static double mean(double[] input)
	{
		double mean = 0;
		for(double value : input)
		{
			mean += value;
		}
		return mean / input.length;
	}
}
