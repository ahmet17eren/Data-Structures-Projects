public class Node {
    public Card element;
    public Node left;
    public Node right;
    public int height;

    Node(Card element){
        this.element =element;
        left = null;
        right = null;
        height = 1;
    }

}
