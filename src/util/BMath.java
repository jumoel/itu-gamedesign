package util;

public class BMath
{
	public static double addTowardsZero(double inval, double add)
	{
		double sign = Math.signum(inval);
		
		return inval - sign * add;
	}
	
	public static double clamp(double inval, double minval, double maxval)
	{
		double t;
		
		if (minval > maxval)
		{
			t = minval;
			minval = maxval;
			maxval = t;
		}
		
		if (inval < minval)
		{
			return minval;
		}
		else if (inval > maxval)
		{
			return maxval;
		}
		
		return inval;
	}
}
