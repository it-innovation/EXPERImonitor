package west.importer;

public class Counter
{
    int count;

    public Counter()
    { 
      count = 0;
    }

    public Counter(int i)
    {
      count = i;
    }

    public synchronized void reset()
    {
      count = 0;
    }

    public synchronized int click()
    {
      //System.out.println(count);
      count++;
      return count;
    }

    public synchronized void increase()
    {
      count++;
    }

    public synchronized int intValue()
    {
      return count;
    }

}
