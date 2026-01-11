public class Game {
    // Active deck of playable cards (grouped/managed by AttackAVL)
    private AttackAVL deck = new AttackAVL();
    private int survivorPts = 0;
    private int strangerPts = 0;
    private int entryOrderCounter = 0;
    private int deckSize = 0;
    private int discardPileSize = 0;
    private int discardentryOrderCounter = -1;
    private DiscardAVL discardPile = new DiscardAVL();

    // Creates a new Card with initial stats and inserts it into the deck.
    public String draw_card(String name, int att, int hp){
        Card card = new Card(name, att, hp, entryOrderCounter++);
        deck.insert(card);
        deckSize++;
        return "Added " + name + " to the deck";
    }

    public String battle(int att,int  hp,int heal){
        Card best = null;
        int priority = 0;

        // Priority search (1 → 4). First matching priority sets `priority` and `best`.
        best = deck.findForPriority1(att, hp);
        if (best != null) {
            priority = 1;
        } else {
            best = deck.findForPriority2(att, hp);
            if (best != null) {
                priority = 2;
            } else {
                best = deck.findForPriority3(att, hp);
                if (best != null) {
                    priority = 3;
                } else {
                    best = deck.findForPriority4(att,hp);
                    if (best != null) priority = 4;
                }
            }
        }

        // No playable card → Stranger gets +2; still run healing (Type-2)
        if (best == null) {
            int revived = runHealing(heal);
            strangerPts += 2;
            return "No cards to play, " +revived+" cards revived";
        }

        // Remove chosen card from the deck before resolution
        deck.delete(best);

        // Simultaneous damage resolution
        int newHealth = best.getHcur() - att;
        int newStrangerHealth = hp - best.getAcur();

        // Scoring (both sides can score in same round)
        if (newHealth <= 0) strangerPts += 2;
        if (newStrangerHealth <= 0) survivorPts += 2;
        if (newHealth > 0 && newHealth < best.getHbase()) strangerPts += 1;
        if (newStrangerHealth > 0 && newStrangerHealth < hp) survivorPts += 1;

        // Build output and apply post-battle state transitions
        String output;
        if (newHealth > 0) {
            // Survived: update Hcur, recompute Acur from Abase/H ratios, reinsert to deck
            best.setHcur(newHealth);
            best.setEntryOrder(++entryOrderCounter);
            int newAcur = Math.max(1, (best.getAbase() * best.getHcur()) / best.getHbase());
            best.setAcur(newAcur);
            deck.insert(best);
            output =  "Found with priority " + priority + ", Survivor plays " + best.getName() + ", the played card returned to deck, %REV% cards revived";
        } else {
            // Died: move to discard pile as DeadCard with initial revivalProgress = 0
            best.setHcur(0);
            deckSize--;
            DeadCard dc = new DeadCard(best, ++discardentryOrderCounter);
            dc.setRevivalProgress(0);
            discardPile.insert(dc);
            discardPileSize++;

            output = "Found with priority " + priority + ", Survivor plays " + best.getName() + ", the played card is discarded, %REV% cards revived";
        }

        // Healing phase (Type-2; no effect for Type-1 since heal==0)
        int revived = runHealing(heal);
        output = output.replace("%REV%", String.valueOf(revived));
        return output;
    }

    // Healing algorithm for Type-2:
    private int runHealing(int heal) {
        int revived = 0;
        if (heal <= 0 || discardPile.root == null) return 0;

        // FULL: pick max Hmissing that is <= current heal (bestFull(heal)); loop until no more fits
        while (heal > 0) {
            Node n = discardPile.bestFull(heal);
            if (n == null) break;

            DeadCard dc = (DeadCard) n.element;
            int need = dc.getHmissing();

            discardPile.delete(dc);
            discardPileSize--;

            dc.applyFullRevive();

            Card ref = dc.getRef();
            ref.setEntryOrder(++entryOrderCounter);
            deck.insert(ref);
            deckSize++;

            heal -= need;
            revived++;

            if (discardPile.root == null) break;
        }

        // PARTIAL: if heal remains, apply to the smallest Hmissing card (single target)
        if (heal > 0 && discardPile.root != null) {
            Node m = discardPile.findMin(discardPile.root);   // smallest Hmissing node
            if (m != null) {
                DeadCard dc = (DeadCard) m.element;

                // Remove first since its key (Hmissing) will change
                discardPile.delete(dc);

                dc.applyPartial(heal);

                dc.setDeathOrder(++discardentryOrderCounter);
                discardPile.insert(dc);

                heal = 0; // remaining heal is fully consumed by this single partial revive
            }
        }

        return revived;
    }


    public String deckCount(){
        return "Number of cards in the deck: " + this.deckSize;
    }

    public String findWinning(){
        if (survivorPts >= strangerPts)
            return "The Survivor, Score: " + survivorPts;
        else
            return "The Stranger, Score: " + strangerPts;
    }

    public String steal_card(int att, int hp) {
        if (deck.root == null) return "No card to steal";

        Node band = deck.firstBandGreaterThan(att); // find first attack band with A > att
        while (band != null) {
            HealthAVL inner = deck.innerOf(band);
            Card pick = inner.minStrictGreaterH(hp); // pick the smallest H with H > hp
            if (pick != null) {
                deck.deleteCard(pick);               // remove from the correct inner tree/band
                deckSize--;
                return "The Stranger stole the card: " + pick.getName();
            }
            band = deck.nextBand(band);
        }
        return "No card to steal";
    }

    // Query: discard_pile_count (Type-2)
    public String discardPileCount(){
        return "Number of cards in the discard pile: "+ discardPileSize;
    }

}