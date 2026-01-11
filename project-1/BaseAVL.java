public abstract class BaseAVL {

    public Node root;
    protected abstract int compareCards(Card c1, Card c2);

    // Safe height getter (null -> 0)
    public int getHeight(Node node){
        if (node == null) {
            return 0;
        } else {
            return node.height;
        }
    }

    // Balance factor = left.height - right.height
    public int getBalancedFactor(Node node) {
        return getHeight(node.left) - getHeight(node.right);
    }

    // Right rotation around x
    public Node rightRotate(Node x){
        // Rewire pointers
        Node temp = x.left.right;
        Node newRoot = x.left;
        newRoot.right = x;
        x.left = temp;

        // Update heights bottom-up
        x.height = 1 + Math.max(getHeight(x.left) , getHeight(x.right));
        newRoot.height = 1 + Math.max(getHeight(newRoot.left) , getHeight(newRoot.right));

        return newRoot;
    }

    // Left rotation around y
    public Node leftRotate(Node y){
        // Rewire pointers
        Node temp = y.right.left;
        Node newRoot = y.right;
        newRoot.left = y;
        y.right = temp;

        // Update heights bottom-up
        y.height = 1 + Math.max(getHeight(y.left) , getHeight(y.right));
        newRoot.height = 1 + Math.max(getHeight(newRoot.left) , getHeight(newRoot.right));

        return newRoot;
    }

    // Public insert/delete entry points
    public void insert(Card card) {
        root = insertNode(card,root);
    }

    public void delete(Card card) {
        root = deleteNode(root,card);
    }

    // Recursive AVL insert with rebalancing
    public Node insertNode(Card x , Node currentNode){
        if (currentNode == null) {
            return new Node(x);
        }
        if (compareCards(x,currentNode.element) == -1) {
            currentNode.left = insertNode(x,currentNode.left);
        }
        if (compareCards(x, currentNode.element) == 1) {
            currentNode.right = insertNode(x,currentNode.right);
        }

        // Update height
        currentNode.height = 1 + Math.max(getHeight(currentNode.left) , getHeight(currentNode.right));

        // Rebalance (LL / LR)
        if (getBalancedFactor(currentNode) > 1) {
            // Left-Left
            if (compareCards(x,currentNode.left.element) == -1) {
                return rightRotate(currentNode);
            }
            // Left-Right
            if (compareCards(x,currentNode.left.element) == 1){
                currentNode.left = leftRotate(currentNode.left);
                return rightRotate(currentNode);
            }
        }

        // Rebalance (RR / RL)
        if (getBalancedFactor(currentNode) < -1) {
            // Right-Right
            if (compareCards(x,currentNode.right.element) == 1){
                return leftRotate(currentNode);
            }
            // Right-Left
            if (compareCards(x,currentNode.right.element) == -1) {
                currentNode.right = rightRotate(currentNode.right);
                return leftRotate(currentNode);
            }
        }

        return currentNode;
    }

    // Smallest node in subtree (leftmost)
    public Node findMin(Node node) {
        // Guard for null subtree
        if (node == null)
            return null;
        if (node.left == null)
            return node;
        return findMin(node.left);
    }

    // Recursive AVL delete with rebalancing
    public Node deleteNode(Node currentNode, Card target){
        if (currentNode == null) {
            return null;
        }
        if (compareCards(target,currentNode.element) == -1) {
            currentNode.left = deleteNode(currentNode.left, target);
        } else if (compareCards(target, currentNode.element) == 1) {
            currentNode.right = deleteNode(currentNode.right ,target);
        } else {
            // Found target: handle 0/1/2 children cases
            if ((currentNode.right == null) && (currentNode.left == null)) {
                // No child
                return null;
            } else if ((currentNode.right == null) || (currentNode.left == null)){
                // One child
                if (currentNode.left == null)
                    return currentNode.right;
                else
                    return currentNode.left;
            } else {
                // Two children: replace with inorder successor
                Node successor = findMin(currentNode.right);
                currentNode.element = successor.element; // copy successor value
                currentNode.right = deleteNode(currentNode.right , successor.element); // delete successor
            }
        }

        // Update height
        currentNode.height = 1 + Math.max(getHeight(currentNode.left), getHeight(currentNode.right));

        // Rebalance (LL / LR)
        if (getBalancedFactor(currentNode) > 1) {
            if (getBalancedFactor(currentNode.left) >= 0) {
                return rightRotate(currentNode); // Left-Left
            } else {
                currentNode.left = leftRotate(currentNode.left); // Left-Right
                return rightRotate(currentNode);
            }
        }

        // Rebalance (RR / RL)
        if (getBalancedFactor(currentNode) < -1) {
            if (getBalancedFactor(currentNode.right) <= 0){
                return leftRotate(currentNode); // Right-Right
            } else {
                currentNode.right = rightRotate(currentNode.right); // Right-Left
                return leftRotate(currentNode);
            }
        }

        return currentNode;
    }

}