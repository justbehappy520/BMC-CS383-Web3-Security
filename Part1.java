import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

public class Part1 {
    // -----------------------------
    // Part 1: Message Digests
    // -----------------------------
    
    /**
     * Compute the message digest of a message using specified hash function.
     * 
     * @param message the message to hash
     * @param hashFunction which hash to use:
     *                     1 = full SHA-256 (256 bits)
     *                     2 = SHA-256 truncated to 8 bits
     *                     3 = SHA-256 truncated to 16 bits
     *                     other = print error and return null
     * @return the message digest as a byte array
     */
    public static byte[] computeDigest(byte[] message, int hashFunction) throws NoSuchAlgorithmException {
        byte[] md;
        if (hashFunction == 1) {
            md = Utils.sha256(message);
            return md;
        } else if (hashFunction == 2) {
            md = Utils.hashTruncated(message, 8);
            return md;
        } else if (hashFunction == 3) {
            md = Utils.hashTruncated(message, 16);
            return md;
        } else {
            System.out.println("invalid hashfunction option, select 1, 2, or 3");
            return null;
        }
    }
    
    /**
     * Verify that a message matches an expected digest.
     * 
     * @param message the message to verify
     * @param expectedDigest the expected digest
     * @param hashFunction which hash function to use (1, 2, or 3)
     * @return true if message's digest matches expectedDigest
     */
    public static boolean verifyIntegrity(byte[] message, byte[] expectedDigest, 
                                          int hashFunction) throws NoSuchAlgorithmException {
        byte[] md = computeDigest(message, hashFunction);
        if (Arrays.equals(md, expectedDigest)) {
            return true;
        }
        return false;
    }
    
    public static void main(String[] args) throws NoSuchAlgorithmException {
        // compute and verify
        byte[] msg1 = "string part 1".getBytes();
        byte[] digest1;
        // sha256
        digest1 = computeDigest(msg1, 1);
        for (byte b : digest1) {
            System.out.printf("%02x", b);
        }
        System.out.println("");
        System.out.println(verifyIntegrity(msg1, digest1, 1));
        // truncate 8
        digest1 = computeDigest(msg1, 2);
        for (byte b : digest1) {
            System.out.printf("%02x", b);
        }
        System.out.println("");
        System.out.println(verifyIntegrity(msg1, digest1, 2));
        // truncate 16
        digest1 = computeDigest(msg1, 3);
        for (byte b : digest1) {
            System.out.printf("%02x", b);
        }
        System.out.println("");
        System.out.println(verifyIntegrity(msg1, digest1, 3));

        // collision-resistance testing
        boolean[] seen = new boolean[65536]; // 8-bit [256]; 16-bit [65536]
        int attempts = 0;
        long start = System.currentTimeMillis();
        while (true) {
            byte[] msg = ByteBuffer.allocate(4).putInt(attempts).array();
            byte[] md = computeDigest(msg, 3); // 8-bit 2; 16-bit 3
            int value = ((md[0] & 0xFF) << 8) | (md[1] & 0xFF); // 8-bit md[0] & 0xFF; 16-bit ((md[0] & 0xFF) << 8) | (md[1] & 0xFF);
            if (seen[value]) {
                long end = System.currentTimeMillis();
                System.out.println("attempts: " + attempts);
                System.out.println("time: " + (end - start));
                break;
            }
            seen[value] = true;
            attempts++;
        }
        // sha256
        int attempts256 = 0;
        int maxAttempts = 2147483647; // reasonable bound
        long startTime = System.currentTimeMillis();
        while (attempts256 < maxAttempts) {
            byte[] msg = ByteBuffer.allocate(4).putInt(attempts256).array();
            computeDigest(msg, 1);
            attempts256++;
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time (ms): " + (endTime - startTime));
    }
}
