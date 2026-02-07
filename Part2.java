import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Part2 {

    // -----------------------------
    // Part 2: Commitments
    // -----------------------------
    /** TODO: return (c, r) where r is random 32 bytes and c = SHA256(r || message). */
    public static Commitment commit(byte[] message, int hashFunction) throws NoSuchAlgorithmException {
        byte[] r = Utils.genSalt();
        byte[] c;
        if (hashFunction == 1) {
            c = Utils.sha256(Utils.concat(r, message));
            return new Commitment(c, r);
        } else if (hashFunction == 2) {
            c = Utils.hashTruncated(Utils.concat(r, message), 8);
            return new Commitment(c, r);
        } else if (hashFunction == 3) {
            c = Utils.hashTruncated(Utils.concat(r, message), 16);
            return new Commitment(c, r);
        }
        return null;
    }

    /** TODO: return true iff c.c equals SHA256(c.r || message). */
    public static boolean verify(Commitment c, byte[] message, int hashFunction) throws NoSuchAlgorithmException {
        byte[] md;
        if (hashFunction == 1) {
            md =  Utils.sha256(Utils.concat(c.r, message));
            if (Arrays.equals(c.c, md)) {
                return true;
            }
        } else if (hashFunction == 2) {
            md = Utils.hashTruncated(Utils.concat(c.r, message), 8);
            if (Arrays.equals(c.c, md)) {
                return true;
            }
        } else if (hashFunction == 3) {
            md = Utils.hashTruncated(Utils.concat(c.r, message), 16);
            if (Arrays.equals(c.c, md)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        // testing sha256
        byte[] message = "hello".getBytes();
        int hashFunction = 1;
        // commit
        Commitment comm = commit(message, hashFunction);
        // verify
        System.out.println(verify(comm, message, hashFunction));

        // hiding property testing
        byte[] msg = "secret".getBytes();
        Commitment comit = commit(msg, 3); // 8-bit 2; 16-bit 3
        int attempts = 0;
        long start = System.currentTimeMillis();
        byte[] found = null;
        while (true) {
            byte[] guess = ByteBuffer.allocate(4).putInt(attempts).array();
            byte[] test = Utils.hashTruncated(Utils.concat(comit.r, guess), 16); // 8-bit 8; 16-bit 16
            if (Arrays.equals(test, comit.c)) {
                found = guess;
                break;
            }
            attempts++;
        }
        long end = System.currentTimeMillis();
        System.out.println(attempts);
        System.out.println(end - start);
        System.out.println(msg + " " + found);
        // sha256
        Commitment comm256 = commit(msg, 1);
        int attempts256 = 0;
        int maxAttempts = 2147483647;
        long start256 = System.currentTimeMillis();
        while (attempts256 < maxAttempts) {
            byte[] guess = ByteBuffer.allocate(4).putInt(attempts256).array();
            Utils.sha256(Utils.concat(comm256.r, guess));
            attempts256++;
        }
        long end256 = System.currentTimeMillis();
        System.out.println(end256 - start256);
    }
}
