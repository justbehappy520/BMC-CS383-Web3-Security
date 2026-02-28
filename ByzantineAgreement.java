import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ByzantineAgreement {

    public static int     DEFAULT = 0;

    // ---------------------------------------------------------------
    // Validity: if the general is honest, every honest party must
    //           output exactly the general's input value v.
    // ---------------------------------------------------------------
    public static boolean validity(int v, List<Party> parties) {
        // check if general is honest
        boolean honestLeader = false;
        for (Party p : parties) {
            if (p.isLeader && p.isHonest) {
                honestLeader = true;
            }
        }
        // general is honest
        boolean valid = true;
        if (honestLeader) {
            for (Party p : parties) {
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
    public static boolean agreement(List<Party> parties) {
        if (parties.size() == 1) {
            return true;
        }
        int honestVal = -1;
        for (Party p : parties) {
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
    public static boolean protocol(List<Party> parties, Map<Integer, Keys.PublicKey> PKI) {
        // find the general
        Party general = null;
        for (Party p : parties) {
            if (p.isLeader) {
                general = p;
                break;
            }
        }
        // round 1
        int v = 1;
        Message signed = general.sign(v);
        for (Party p : parties) {
            if (p != general && general != null) {
                general.send(p, signed, PKI);
            }
        }
        // round 2
        for (Party p : parties) {
            // skip if general
            if (p.isLeader) {
                continue;
            }
            // send to everyone
            List<Message> round1 = new ArrayList<>(p.msgs);
            for (Message m : round1) {
                for (Party q : parties) {
                    if (p != q) {
                        p.send(q, m, PKI);
                    }
                }
            }
        }
        // decision round
        for (Party p : parties) {
            p.decide();
        }
        // return check
        return validity(v, parties) && agreement(parties);
    }

    public static void main(String[] args) {
        int n = 6;
        int trials = 500;
        // set dishonest peeps
        Set<Integer> dishonestIndices = new HashSet<>();
        dishonestIndices.add(0);
        // dishonestIndices.add(2);
        // track validity and agreement
        int valDeaths = 0;
        int agrDeaths = 0;
        // running in circles
        for (int i = 0; i < trials; i++) {
            List<Party> parties = new ArrayList<>();
            HashMap<Integer, Keys.PublicKey> PKI = new HashMap<>();
            // assigning things
            for (int j = 0; j < n; j++) {
                // set variables
                boolean isLeader = false;
                if (j == 1) {
                    isLeader = true;
                }
                boolean isHonest = false;
                if (!dishonestIndices.contains(j)) {
                    isHonest = true;
                }
                // add parties
                Party p = new Party(isLeader, isHonest);
                parties.add(p);
                // setup PKI
                PKI.put(j, p.getPublicKey());
            }
            // checking
            if (!protocol(parties, PKI)) {
                if (!validity(1, parties)) {
                    valDeaths++;
                }
                if (!agreement(parties)) {
                    agrDeaths++;
                }
            }
        }
        // print
        System.out.println("n=" + n);
        System.out.println("dishonest peeps: " + dishonestIndices);
        System.out.println("validity death count: " + valDeaths);
        System.out.println("agreement death count: " + agrDeaths);
    }
}
