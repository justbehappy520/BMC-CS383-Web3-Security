import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DolevStrong {
    public static final int DEFAULT = 0;

    // ---------------------------------------------------------------
    // Validity: if the general is honest, every honest party outputs
    //           exactly the general's input value v.
    // ---------------------------------------------------------------
    public static boolean validity(int v, List<DSParty> parties) {
        // check if general is honest
        boolean honestLeader = false;
        for (DSParty p : parties) {
            if (p.isLeader && p.isHonest) {
                honestLeader = true;
            }
        }
        // general is honest
        boolean valid = true;
        if (honestLeader) {
            for (DSParty p : parties) {
                if (p.isHonest && (p.output != v)) {
                    valid = false;
                    break;
                }
            }
        }
        // general is not honest
        else {
            // trivially true
            return valid;
        }
        return valid;
    }

    // ---------------------------------------------------------------
    // Agreement: all honest parties output the same value.
    // ---------------------------------------------------------------
    public static boolean agreement(List<DSParty> parties) {
        if (parties.size() == 1) {
            return true;
        }
        int honestVal = -1;
        for (DSParty p : parties) {
            if (p.isHonest && (p.output != null)) {
                if (honestVal == -1) {
                    honestVal = p.output;
                } else if ((p.output != honestVal)) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Protocol - returns true iff validity and agreement are satisfied
    // ---------------------------------------------------------------
    public static boolean protocol(List<DSParty> parties, int t, Map<Integer, Keys.PublicKey> PKI) {
        // find the general
        DSParty general = null;
        for (DSParty p : parties) {
            if (p.isLeader) {
                general = p;
                break;
            }
        }
        // check
        if (general == null) {
            return false;
        }
        // round 1: leader sends its signed message to all other parties
        int v;
        if (general.msgs.isEmpty()) {
            // try not to hardcode it?
            Random r = new Random();
            v = r.nextInt(2);
        } else {
            v = general.msgs.get(0).value;
        }
        DSMessage m = new DSMessage(v);
        DSMessage s = m.addSig(general.sign(m));
        for (DSParty p : parties) {
            if (p.partyId != general.partyId) {
                general.send(p, s, 1, PKI);
            }
        }
        // round 2 through t+1: all non-leader parties call relay()
        for (int r = 2; r <= t + 1; r++) {
            for (DSParty p : parties) {
                if (!p.isLeader) {
                    // inc round for receiver
                    p.relay(parties, r, PKI); 
                }
            }
        }
        // decision round
        for (DSParty p : parties) {
            p.decide();
        }
        // return check
        return validity(v, parties) && agreement(parties);
    }

    public static void main(String[] args) {
        int n = 5;
        int t = 1;
        int trials = 500;
        // set dishonest peeps
        Set<Integer> dishonestIndices = new HashSet<>();
        dishonestIndices.add(1);
        // dishonestIndices.add(2);
        // track validity and agreement
        int valDeaths = 0;
        int agrDeaths = 0;
        // running in circles
        for (int i = 0; i < trials; i++) {
            List<DSParty> parties = new ArrayList<>();
            HashMap<Integer, Keys.PublicKey> PKI = new HashMap<>();
            // assigning things
            for (int j = 0; j < n; j++) {
                int pID = j + 1;
                // set variables
                boolean isLeader = false;
                if (j == 0) {
                    isLeader = true;
                }
                boolean isHonest = false;
                if (!dishonestIndices.contains(pID)) {
                    isHonest = true;
                }
                // add parties
                DSParty p = new DSParty(pID, isLeader, isHonest);
                parties.add(p);
                // setup PKI
                PKI.put(pID, p.getPublicKey());
            }
            // checking
            if (!protocol(parties, t, PKI)) {
                if (!validity(1, parties)) {
                    valDeaths++;
                }
                if (!agreement(parties)) {
                    agrDeaths++;
                }
            }
        }
        // print
        System.out.println("n=" + n + " t=" + t);
        System.out.println("dishonest peeps: " + dishonestIndices);
        System.out.println("validity death count: " + valDeaths);
        System.out.println("agreement death count: " + agrDeaths);
    }
}
