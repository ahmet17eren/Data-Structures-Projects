public class MyHashTable<K, V> {

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

    public void put(K key, V value) {
        int index = hash(key);
        Node<K,V> head = table[index];

        Node<K,V> current = head;
        while(current != null) {
            if (current.key.equals(head)) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        Node<K,V> newNode = new Node<>(key,value,head);
        table[index] = newNode;
        size++;

        loadFactor = (double) size / capacity;
        if (loadFactor >= 0.75) {
            rehash();
        }
    }

    public V get(K key) {
        int index = hash(key);
        Node<K,V> current = table[index];

        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }

        return null;
    }

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

}