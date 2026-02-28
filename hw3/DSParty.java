import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DSParty {
    public final int     partyId;   // 1-indexed; party 1 is the leader
    public final boolean isLeader;
    public final boolean isHonest;
    private final Wallet wallet;

    public final List<DSMessage> msgs;
    public Integer output;

    public static int DEFAULT = 0;

    public DSParty(int partyId, boolean isLeader, boolean isHonest) {
        this.partyId  = partyId;
        this.isLeader = isLeader;
        this.isHonest = isHonest;
        this.wallet   = new Wallet(512);
        this.msgs     = new ArrayList<>();
        this.output   = null;
    }

    public Keys.PublicKey getPublicKey() {
        return wallet.getPublicKey();
    }

    public DSMessage.Signature sign(DSMessage msg) {
        try {
            // use baos to build a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            // add value
            dos.writeInt(msg.value);
            // add existing sigs in order
            for (DSMessage.Signature sig : msg.getSignatures()) {
                dos.writeInt(sig.partyId); // adds partyId
                dos.writeInt(sig.bytes.length); // adds/indicates len of bytes
                dos.write(sig.bytes); // adds bytes
            }
            dos.flush();
            byte[] dataToSign = baos.toByteArray();
            // sign full chain
            byte[] s = this.wallet.sign(dataToSign);
            return new DSMessage.Signature(this.partyId, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(DSParty receiver, DSMessage msg, int roundNum, Map<Integer, Keys.PublicKey> PKI) {
        if (!isHonest) {
            Random r = new Random();
            int rand = r.nextInt(4);
            // send message unchanged
            if (rand == 3) {
                receiver.receive(msg, roundNum, PKI);
                return;
            }
            // send nothing
            else if (rand == 2) {
                return;
            }
            // modify the value
            int rogueVal = (rand == 0) ? 1 : 0;
            // new rogue message
            DSMessage rogue = new DSMessage(rogueVal);
            // sign with this.peep's key
            DSMessage.Signature sig = this.sign(rogue);
            rogue = rogue.addSig(sig);
            // send it out to the world
            receiver.receive(rogue, roundNum, PKI);            
            return;
        }
        receiver.receive(msg, roundNum, PKI);
    }

    public void receive(DSMessage msg, int roundNum, Map<Integer, Keys.PublicKey> PKI) {
        // check roundNum (correct number of signatures for this round) against DSMessage.chainLength()
        if (roundNum != msg.chainLength()) {
            return;
        }
        // verify DSMessage.getSignatures.get(1) is from the leader
        List<DSMessage.Signature> sigs = msg.getSignatures();
        if (sigs.isEmpty() || sigs.get(0).partyId != 1) {
            return;
        }
        // verify every signature in DSMessage.getSignatures()
        // do so by recreating the signature at this point and verifying that
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            // add the value first
            dos.writeInt(msg.value);
            dos.flush();
            // ensure every peep signs only once
            Set<Integer> seen = new HashSet<>();
            // loop through sig chain of msg
            for (int i = 0; i < sigs.size(); i++) {
                DSMessage.Signature sig = sigs.get(i);
                // ensure no duplicate peeps
                if (seen.contains(sig.partyId)) {
                    return;
                }
                seen.add(sig.partyId);
                // get the pk
                Keys.PublicKey pk = PKI.get(sig.partyId);
                if (pk == null) {
                    return;
                }
                // produce a byte[]
                byte[] dataSoFar = baos.toByteArray();
                // verify signature of data so far
                if (!Wallet.verify(pk, dataSoFar, sig.bytes)) {
                    return;
                }
                // append!!
                dos.writeInt(sig.partyId);
                dos.writeInt(sig.bytes.length);
                dos.write(sig.bytes);
                dos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // add only unique msg
        boolean alreadyHave = false;
        // make sure all aspects of existing and msg matches
        for (DSMessage existing : msgs) {
            // check value
            if (existing.value != msg.value) {
                continue;
            }
            // check chain length
            if (existing.chainLength() != msg.chainLength()) {
                continue;
            }
            // check sig chain
            List<DSMessage.Signature> s1 = existing.getSignatures();
            List<DSMessage.Signature> s2 = msg.getSignatures();
            boolean same = true;
            for (int i = 0; i < s1.size(); i++) {
                if (s1.get(i).partyId != s2.get(i).partyId ||
                    !Arrays.equals(s1.get(i).bytes, s2.get(i).bytes)) {
                    same = false;
                    break;
                }
            }
            if (same) {
                alreadyHave = true;
                break;
            }
        }
        // add!!
        if (!alreadyHave) {
            this.msgs.add(msg);
        }
    }

    public void relay(List<DSParty> allParties, int roundNum, Map<Integer, Keys.PublicKey> PKI) {
        if (isHonest) {
            // for each message in msgs, add signature and send to all
            for (DSMessage msg : new ArrayList<>(msgs)) {
                // only relay messages from previous round
                if (msg.chainLength() != roundNum - 1) {
                    continue;
                }
                // skip if already signed
                boolean signed = false;
                for (DSMessage.Signature sig : msg.getSignatures()) {
                    if (sig.partyId == partyId) {
                        signed = true;
                        break;
                    }
                }
                if (signed) {
                    continue;
                }
                // else sign and send
                DSMessage signedMsg = msg.addSig(sign(msg));
                for (DSParty p : allParties) {
                    // skip self
                    if (p.partyId != partyId) {
                        send(p, signedMsg, roundNum, PKI);
                    }
                }
            }
        } else {
            // implement dishonest relay behavior
            Random rand = new Random();
            for (DSParty p : allParties) {
                if (rand.nextBoolean()) {
                    if (!msgs.isEmpty()) {
                        send(p, msgs.get(0), roundNum, PKI);
                    }
                }
            }
        }
    }

    public void decide() {
        if (!isHonest) {
            Random r = new Random();
            output = r.nextInt(1000);
            return;
        }
        if (msgs.isEmpty()) {
            output = DEFAULT;
            return;
        }
        // collect unique values
        Set<Integer> unique = new HashSet<>();
        for (DSMessage m : msgs) {
            unique.add(m.value);
        }
        // determine based on whether only 1 unique value exists
        if (unique.size() == 1) {
            output = unique.iterator().next();
        } else {
            output = DEFAULT;
        }
    }

    public static void main(String[] args) {
        // setup PKI
        Map<Integer, Keys.PublicKey> PKI = new HashMap<>();
        // parties
        DSParty p1 = new DSParty(1, true, true); // honest
        DSParty p2 = new DSParty(2, false, true); // honest
        DSParty p3 = new DSParty(3, false, false); // dishonest
        List<DSParty> allParties = Arrays.asList(p1, p2, p3);
        // pk
        PKI.put(1, p1.getPublicKey());
        PKI.put(2, p2.getPublicKey());
        PKI.put(3, p3.getPublicKey());
        // send (round 1)
        int roundNum = 1;
        DSMessage msgGen = new DSMessage(1);
        // general signs and sends
        DSMessage msgGenSig = msgGen.addSig(p1.sign(msgGen));
        for (DSParty p : allParties) {
            if (p.partyId != 1) {
                p.receive(msgGenSig, roundNum, PKI);
            }
        }
        // relay (round 2)
        roundNum = 2;
        for (DSParty p : allParties) {
            p.relay(allParties, roundNum, PKI);
        }
        // decide
        for (DSParty p : allParties) {
            p.decide();
            System.out.println("Party " + p.partyId + " output = " + p.output);
        }
    }

}