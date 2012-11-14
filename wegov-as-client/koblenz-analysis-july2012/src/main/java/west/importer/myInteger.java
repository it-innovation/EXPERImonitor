package west.importer;

public class myInteger implements Comparable
{
  int value = 0;
  
  public myInteger(int val)
  {
    value = val;
  }
  
  public void increase()
  {value++;}

  public void increase(int x)
  {value = value + x;}  
  
  public void decrease()
  {value--;}

  public void decrease(int x)
  {value = value - x;}    
  
  public int intValue()
  {return value;}
  
  public float floatValue()
  {return new Integer(value).floatValue();}

  public double doubleValue()
  {return new Integer(value).doubleValue();}
  
  public int compareTo(Object who)
  {
    if (((myInteger)who).intValue() > value)
      return -1;
    else if (((myInteger)who).intValue() < value)
      return +1;
    else
      return 0;
  }
  
}
