package game.control;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Ring Buffer for Sound Analysis
 * @author Samuel Walz <samuel.walz@gmail.com>
 *
 * @param <Item>
 */
public class RingBuffer<Item> implements Iterable<Item> {
    private Item[] a;            // queue elements
    private int N = 0;           // number of elements on queue
    private int first = 0;       // index of first element of queue
    private int last  = 0;       // index of next available slot

    // cast needed since no generic array creation in Java
    @SuppressWarnings("unchecked")
	public RingBuffer(int capacity) {
        a = (Item[]) new Object[capacity];
    }

    public boolean isEmpty() { return N == 0; }
    public int size()        { return N;      }

    public void enqueue(Item item) {     
        a[last] = item;
        last = (last + 1) % a.length;     // wrap-around
        if (N < a.length) {N++;}
        else {first = (first + 1) % a.length;} //wrap beginning
    }

    // remove the least recently added item - doesn't check for underflow
    public Item dequeue() {
        if (isEmpty()) { throw new RuntimeException("Ring buffer underflow"); }
        Item item = a[first];
        a[first] = null;                  // to help with garbage collection
        N--;
        first = (first + 1) % a.length;   // wrap-around
        return item;
    }

    public Iterator<Item> iterator() { return new RingBufferIterator(); }

    // an iterator, doesn't implement remove() since it's optional
    private class RingBufferIterator implements Iterator<Item> {
        private int i = 0;
        public boolean hasNext()  { return i < N;                               }
        public void remove()      { throw new UnsupportedOperationException();  }

        public Item next() {
            if (!hasNext()) throw new NoSuchElementException();
    
            Item item = a[(last - i - 1 + a.length) % a.length]; //backwards
            //Item item = a[(first + i) % a.length];
            i++;
            return item;
        }
    }



    // a test client
    public static void main(String[] args) {
        RingBuffer<String> ring = new RingBuffer<String>(20);
        ring.enqueue("Delete");
        ring.enqueue("This");
        ring.enqueue("is");
        ring.enqueue("a");
        ring.enqueue("test.");
        ring.dequeue();

        for (String s : ring) {
            System.out.println(s);
        }

        System.out.println();

        while (!ring.isEmpty())  {
            System.out.println(ring.dequeue());
        }

    }


}



