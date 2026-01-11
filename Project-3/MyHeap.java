public class MyHeap<T extends Comparable<T>> {

    private T[] heap;
    private int size;

    @SuppressWarnings("unchecked")
    public MyHeap(int capacity) {
        // Initialize heap array with 1-based indexing for easier parent/child calculations
        heap = (T[]) new Comparable[capacity + 1];
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void insert(T value) {
        // Resize if the heap is full
        if (size >= heap.length - 1) resize();

        // Add value to the next available position and restore heap property
        heap[++size] = value;
        percolateUp(size);
    }

    public T extractMin() {
        if (size == 0) return null;

        T min = heap[1]; // The root is always the minimum

        // Move the last element to the root and reduce size
        heap[1] = heap[size];
        heap[size--] = null; // Prevent memory leak

        // Restore heap property by sinking the new root down
        if (size > 0) percolateDown(1);

        return min;
    }

    private void percolateUp(int k) {
        // Swim up the tree as long as the current node is smaller than its parent
        while (k > 1 && heap[k].compareTo(heap[k / 2]) < 0) {
            swap(k, k / 2);
            k = k / 2;
        }
    }

    private void percolateDown(int k) {
        // Sink down the tree until the heap order is restored
        while (2 * k <= size) {
            int j = 2 * k; // Left child

            // Choose the smaller of the two children
            if (j < size && heap[j + 1].compareTo(heap[j]) < 0) j++;

            // If parent is smaller than the smallest child, we are done
            if (heap[k].compareTo(heap[j]) <= 0) break;

            swap(k, j);
            k = j;
        }
    }

    private void swap(int i, int j) {
        T t = heap[i];
        heap[i] = heap[j];
        heap[j] = t;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        // Double the capacity of the heap array
        T[] newHeap = (T[]) new Comparable[heap.length * 2];
        System.arraycopy(heap, 1, newHeap, 1, size);
        heap = newHeap;
    }
}