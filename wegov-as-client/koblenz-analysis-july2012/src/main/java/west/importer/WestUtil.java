package west.importer;

import java.util.*;

public class WestUtil 
{
    public static int[] topK(double[] input, int k)
    {
    	TreeMap ordering = new TreeMap();
    	for (int i=0; i<input.length; i++)
    		ordering.put(new Double(input[i]), new Integer(i));
    	
    	int size = (int)Math.min(k, input.length);
    	
    	int[] res = new int[size];
    	for (int i=0; i<k && ordering.size()>0; i++)
    	{
    		Map.Entry entry = ordering.lastEntry();
    		Double key = (Double) entry.getKey();
    		Integer value = (Integer) entry.getValue();
    		res[i] = value.intValue();
    		ordering.remove(key);
    	}	
    	return res;
    }	
    
    public static double[] topK_prob(double[] input, int k)
    {
    	TreeMap ordering = new TreeMap();
    	for (int i=0; i<input.length; i++)
    		ordering.put(new Double(input[i]), new Integer(i));
    	
    	int size = (int)Math.min(k, input.length);
    	
    	double[] res = new double[size];
    	for (int i=0; i<k && ordering.size()>0; i++)
    	{
    		Map.Entry entry = ordering.lastEntry();
    		Double key = (Double) entry.getKey();
    		Integer value = (Integer) entry.getValue();
    		res[i] = key.doubleValue();
    		ordering.remove(key);
    	}	
    	return res;
    }	
    
    public static int[] topThreshold(double[] input, double threshold)
    {
    	if (input == null || input.length == 0)
    		return new int[0];
    	
    	TreeMap ordering = new TreeMap();
    	for (int i=0; i<input.length; i++)
    		ordering.put(new Double(-input[i]), new Integer(i));
    	
    	
    	int size = 0;
    	Iterator it = ordering.entrySet().iterator();
    	while (it.hasNext())
    	{
    		Map.Entry entry = (Map.Entry)it.next();
    		Double a = (Double)entry.getKey();
    		double val = a.doubleValue();
    		if (val < -threshold)
    			size++;
    		else
    			break;
    	}    	
    			
    	int[] res = new int[size];
    	
    	int pos = 0;
        it = ordering.entrySet().iterator();
    	while (it.hasNext())
    	{
    		Map.Entry entry = (Map.Entry)it.next();
    		Double a = (Double)entry.getKey();
    		Integer b = (Integer)entry.getValue();
    		double val = a.doubleValue();
    		if (val < -threshold)
    		{
    	      res[pos] = b.intValue();	
    		  pos++;	
    		}
    		else
    		  break;
    	}    
    	return res;
    }
    
    public static void main (String[] x)
    {
    	TreeMap test = new TreeMap();
    	test.put(new Double(12.5), "x");
    	test.put(new Double(-16.7), "y");
    	test.put(new Double(19.3), "z");
    	Iterator it = test.entrySet().iterator();
    	while (it.hasNext())
    	{
    		Map.Entry entry = (Map.Entry)it.next();
    		Double a = (Double)entry.getKey();
    		String b = (String)entry.getValue();
    		System.out.println(a.toString() + " - " + b);
    	}
    }

}
