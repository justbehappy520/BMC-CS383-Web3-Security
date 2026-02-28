import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Party {

    public final boolean  isLeader;
    public final boolean  isHonest;

    public static int     DEFAULT = 0;

    private final Wallet  wallet;      
    public  final List<Message> msgs;  // V_i
    public  Integer output;            // set during decision phase (decide)

    public Party(boolean isLeader, boolean isHonest) {
        this.isLeader = isLeader;
        this.isHonest = isHonest;
        this.wallet   = new Wallet(512); 
        this.msgs     = new ArrayList<>();
        this.output   = null;
    }

    public Keys.PublicKey getPublicKey() {
        return wallet.getPublicKey();
    }

    public Message sign(int v) {
        // convert value to byte[]
        byte[] msg = ByteBuffer.allocate(4).putInt(v).array();
        // sign byte[]
        byte[] sig = this.wallet.sign(msg);
        // create new Message object
        Message sig_msg = new Message(v, sig);
        return sig_msg;
    }

    public void send(Party receiver, Message msg, Map<Integer, Keys.PublicKey> PKI) {
        // 不乖的孩子不令人放心
        Random r = new Random();
        int rand = r.nextInt(3);
        if (!isHonest) {
            if (isLeader) {
                if (rand == 0) {
                    // send 1
                    receiver.receive(sign(1), PKI); 
                } else if (rand == 1) {
                    // send 0
                    receiver.receive(sign(0), PKI); 
                } else if (rand == 2) {
                    // send nothing
                }
            } else {
                if (rand == 0 && msg != null) {
                    receiver.receive(msg, PKI);
                } else if (rand == 1) {
                    // send something random
                    receiver.receive(new Message(r.nextInt(2), new byte[0]), PKI);
                }
            }
            return;
        }
        // isHonest -- so simple, 乖孩子真的令人放心
        receiver.receive(msg, PKI);
    }

    
    public void receive(Message msg, Map<Integer, Keys.PublicKey> PKI) {
        // convert msg.value to byte[]
        int v = msg.value;
        byte[] message = ByteBuffer.allocate(4).putInt(v).array();
        // 
        if (message == null || msg.sig == null) {
            return;
        }
        // confirmed PKI.get(1) is general
        Keys.PublicKey pk = PKI.get(1);
        // verify message
        if (pk != null) {
            if (Wallet.verify(pk, message, msg.sig)) {
                // only add message if unique
                boolean hasVal = false;
                for (Message m : msgs) {
                    if (m.value == v) {
                        hasVal = true;
                        break;
                    }
                }
                // does not have value already
                if (!hasVal) {
                    this.msgs.add(msg);
                }
            }
        }
    }

    public void decide() {
        if (isHonest) {
            if (msgs.isEmpty()) {
                output = DEFAULT;
                return;
            }
            int honestVal = msgs.get(0).value;
            for (Message m : msgs) {
                if (m.value != honestVal) {
                    //consistent = false;
                    output = DEFAULT;
                    break;
                }
            }
            output = honestVal;
        } else {
            // generate random int between 0-999
            Random r = new Random();
            int rand = r.nextInt(1000);
            // set output
            if (rand % 17 == 0) {
                output = -1;
            } else {
                output = rand;
            }
        }
    }

    public static void main(String[] args) {
        Party general = new Party(true, true);
        Party party1 = new Party(false, true); // honest
        Party party2 = new Party(false, false); // dishonest
        // setup pki
        Map<Integer, Keys.PublicKey> pki = new java.util.HashMap<>();
        pki.put(0, general.getPublicKey());
        // general sends 1
        Message signed = general.sign(1);
        general.send(party1, signed, pki);
        general.send(party2, signed, pki);
        // decision
        party1.decide();
        party2.decide();
        System.out.println(party1.output + ", " + party2.output);
    }
}
