import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.ByteBuffer;

public class Utils {

    private static final SecureRandom RNG = new SecureRandom();

    /** SHA-256 using Java standard library. */
    public static byte[] sha256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input);
    }

    /** Truncated SHA-256 hash - returns only the first 'bits' bits */
    public static byte[] hashTruncated(byte[] input, int bits) throws NoSuchAlgorithmException {
        byte[] fullHash = sha256(input);
        long hash = ByteBuffer.wrap(fullHash).getLong();

        int numBytes = (bits+7)/8;
        byte[] truncated = new byte[numBytes];
        System.arraycopy(fullHash, 0, truncated, 0, numBytes); 

        return truncated; 
    }
     
    /** Concatenate byte arrays in order. */
    public static byte[] concat(byte[]... parts) {
        int total = 0;
        for (byte[] p : parts) total += p.length;
        byte[] out = new byte[total];
        int off = 0;
        for (byte[] p : parts) {
            System.arraycopy(p, 0, out, off, p.length);
            off += p.length;
        }
        return out;
    }

    /** Generate a random 32 byte salt **/
    public static byte[] genSalt() {
        byte[] r = new byte[32];
        RNG.nextBytes(r);
        return r;
    }

}
