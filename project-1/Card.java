public class Card {
    //Card features
    private int Ainit;
    private int Abase;
    private int Acur;
    private int Hinit;
    private int Hbase;
    private int Hcur;
    private String name;
    private int entryOrder;

    Card () {

    }

    Card(String name, int Ainit, int Hinit, int entryOrder){
        this.entryOrder = entryOrder;
        this.name = name;
        this.Ainit = Ainit;
        this.Hinit = Hinit;
        this.Abase = Ainit;
        this.Hbase = Hinit;
        this.Acur = Ainit;
        this.Hcur = Hinit;
    }

    // Getter - setter methods
    public int getEntryOrder() {
        return entryOrder;
    }

    public void setEntryOrder(int entryOrder) {
        this.entryOrder = entryOrder;
    }

    public int getAbase() {
        return Abase;
    }

    public int getAinit() {
        return Ainit;
    }

    public int getAcur() {
        return Acur;
    }

    public int getHbase() {
        return Hbase;
    }

    public int getHcur() {
        return Hcur;
    }

    public int getHinit() {
        return Hinit;
    }

    public void setAbase(int abase) {
        Abase = abase;
    }

    public void setAcur(int acur) {
        Acur = acur;
    }

    public void setHcur(int hcur) {
        Hcur = hcur;
    }

    public String getName() {
        return name;
    }

}
