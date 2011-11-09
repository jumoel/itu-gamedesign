package util;

public class BString
{
	public static String join(String[] arr, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < arr.length - 1; i++)
		{
			sb.append(arr[i]);
			sb.append(delimiter);
		}
		
		sb.append(arr[arr.length - 1]);
		
		return sb.toString();
	}
}
