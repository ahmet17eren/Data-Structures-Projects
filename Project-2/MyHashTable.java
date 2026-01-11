public class MyHashTable<K, V> {

    // Array of buckets (each bucket is a linked list of nodes)
    private Node<K, V>[] table;
    private int capacity;
    private int size;
    private double loadFactor;

    public MyHashTable(int initialCapacity) {
        this.capacity = initialCapacity;
        this.table = (Node<K, V>[]) new Node[initialCapacity];
        this.size = 0;
    }

    // Hash function: map key to bucket index in [0, capacity)
    private int hash(K key) {
        int h = key.hashCode();
        h = h & 0x7fffffff; // clear sign bit to ensure a non-negative value
        return h % capacity;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void put(K key, V value) {
        int index = hash(key);
        Node<K, V> head = table[index];

        Node<K, V> current = head;
        while (current != null) {
            if (current.key.equals(key)) {
                // Update existing key
                current.value = value;
                return;
            }
            current = current.next;
        }

        // Insert new node at the head of the bucket's linked list
        Node<K, V> newNode = new Node<>(key, value, head);
        table[index] = newNode;
        size++;

        // Check load factor and rehash if necessary
        loadFactor = (double) size / capacity;
        if (loadFactor >= 0.75) {
            rehash();
        }
    }

    public V get(K key) {
        int index = hash(key);
        Node<K, V> current = table[index];

        while (current != null) {
            if (current.key.equals(key))
                return current.value;
            current = current.next;
        }

        return null;
    }

    public V remove(K key) {
        int index = hash(key);
        Node<K, V> current = table[index];
        Node<K, V> previous = null;

        while (current != null) {
            if (current.key.equals(key)) {
                if (previous == null) {
                    table[index] = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return current.value;
            }

            previous = current;
            current = current.next;
        }
        return null;
    }

    // Rebuild the hash table with a larger capacity
    private void rehash() {
        int oldCapacity = capacity;
        Node<K, V>[] oldTable = table;

        capacity = oldCapacity * 2 + 1;
        table = (Node<K, V>[]) new Node[capacity];
        size = 0;

        for (int i = 0; i < oldCapacity; i++) {
            Node<K, V> current = oldTable[i];
            while (current != null) {
                put(current.key, current.value);
                current = current.next;
            }
        }
    }

    public boolean isContainKey(K key) {
        int index = hash(key);
        Node<K, V> cur = table[index];
        while (cur != null) {
            if (cur.key.equals(key))
                return true;
            cur = cur.next;
        }
        return false;
    }

    public int getCapacity() {
        return capacity;
    }

    public Node<K, V>[] getTable() {
        return table;
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            table[i] = null;
        }

        size = 0;
        // loadFactor will be recalculated on next put
    }
}