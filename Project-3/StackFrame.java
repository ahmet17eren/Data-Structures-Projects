public class StackFrame {
    Host current;
    Host parent;

    public StackFrame(Host current, Host parent) {
        this.current = current;
        this.parent = parent;
    }
}
