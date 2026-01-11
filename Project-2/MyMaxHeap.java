public class MyMaxHeap<T extends Comparable<T>> {

    private T[] heap;   // 1-based index array (heap[0] is unused)
    private int size;

    private MyHashTable<T, Integer> indexMap;

    MyMaxHeap(int capacity) {
        heap = (T[]) new Comparable[capacity + 1]; // index 0 is unused
        size = 0;
        // Keep indexMap capacity slightly larger than initial heap capacity
        indexMap = new MyHashTable<>(capacity * 2 + 1);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void insert(T value) {
        if (value == null) return;

        if (size + 1 >= heap.length) {
            resize();
        }
        size++;
        heap[size] = value;
        // Store index of the inserted element
        indexMap.put(value, size);
        percolateUp(size);
    }

    public T extractMax() {
        if (size == 0) return null;

        T max = heap[1];

        // Remove max from index map
        indexMap.remove(max);

        heap[1] = heap[size];
        heap[size] = null;
        size--;

        if (size > 0 && heap[1] != null) {
            indexMap.put(heap[1], 1);
            percolateDown(1);
        }

        return max;
    }


    private void percolateUp(int i) {
        while (i > 1) {
            int parent = i / 2;
            // If child is greater than parent, swap upwards
            if (heap[i].compareTo(heap[parent]) > 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    private void percolateDown(int i) {
        while (2 * i <= size) {
            int left = 2 * i;
            int right = left + 1;
            int larger = left;

            if (right <= size && heap[right].compareTo(heap[left]) > 0) {
                larger = right;
            }

            if (heap[larger].compareTo(heap[i]) > 0) {
                swap(i, larger);
                i = larger;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        if (i == j) return;
        T tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;

        // Update indices in indexMap after swapping
        if (heap[i] != null) {
            indexMap.put(heap[i], i);
        }
        if (heap[j] != null) {
            indexMap.put(heap[j], j);
        }
    }

    private void resize() {
        int newCap = heap.length * 2;
        T[] newArr = (T[]) new Comparable[newCap];
        for (int i = 1; i <= size; i++) {
            newArr[i] = heap[i];
        }
        heap = newArr;
        // Indices stay the same, so indexMap entries remain valid
    }

    // Remove element using indexMap for O(1 + log n) time
    public boolean remove(T element) {
        if (size == 0 || element == null) {
            return false;
        }

        Integer idxObj = indexMap.get(element);
        if (idxObj == null) {
            return false;
        }
        int index = idxObj;

        // Swap with last element and remove
        T last = heap[size];
        heap[index] = last;
        heap[size] = null;
        size--;

        // Remove deleted element from indexMap
        indexMap.remove(element);

        if (index <= size && heap[index] != null) {
            indexMap.put(heap[index], index);

            // Decide whether to swim up or sink down
            int parent = index / 2;
            if (parent >= 1 && heap[index].compareTo(heap[parent]) > 0) {
                percolateUp(index);
            } else {
                percolateDown(index);
            }
        }

        return true;
    }

}