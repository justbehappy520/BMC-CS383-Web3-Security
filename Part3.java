import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public class Part3 {
    // -----------------------------
    // Part 3: Puzzle Friendliness
    // -----------------------------
    
    /**
     * Find a solution x (nonce) such that H(puzzleID || x) ∈ Y,
     * where Y is the set of all hashes with at least 'difficulty' leading zero bits.
     * 
     * @param puzzleID the puzzle identifier (arbitrary data - high min-entropy value)
     * @param difficulty number of leading zero bits required (defines |Y| = 2^(256-difficulty))
     * @return a nonce x that solves the puzzle, or -1 if not found within reasonable attempts
     */
    public static long solvePuzzle(byte[] puzzleID, int difficulty) throws Exception {
        long x = 0;
        while (true) {
            byte[] nonce = ByteBuffer.allocate(Long.BYTES).putLong(x).array();
            byte[] hash = Utils.sha256(Utils.concat(puzzleID, nonce));
            int zeroBits = 0;
            for (byte b : hash) {
                if (b == 0) {
                    zeroBits += 8;
                } else {
                    int zeros = b & 0xFF;
                    zeroBits += Integer.numberOfLeadingZeros(zeros) - 24;
                    break;
                }
            }
            if (zeroBits >= difficulty) {
                return x;
            }
            x++;
            if (x == Long.MAX_VALUE) {
                return -1;
            }
        }
    }
    
    /**
     * Verify that x is a valid solution: H(puzzleID || x) ∈ Y.
     * 
     * @param puzzleID the puzzle identifier
     * @param x the proposed solution (nonce)
     * @param difficulty required number of leading zero bits (defines Y)
     * @return true if H(puzzleID || x) has at least 'difficulty' leading zeros
     */
    public static boolean verifyPuzzle(byte[] puzzleID, long x, int difficulty) 
            throws Exception {
        byte[] nonce = ByteBuffer.allocate(Long.BYTES).putLong(x).array();
        byte[] hash = Utils.sha256(Utils.concat(puzzleID, nonce));
        int zeroBits = 0;
        for (byte b : hash) {
            if (b == 0) {
                zeroBits += 8;
            } else {
                int zeros = b & 0xFF;
                zeroBits += Integer.numberOfLeadingZeros(zeros) - 24;
                break;
            }
        }
        return zeroBits >= difficulty;
    }

    public static void main(String[] args) throws Exception {
        byte[] puzzleID = "test".getBytes();
        int difficulty = 16;
        // verify
        long x = solvePuzzle(puzzleID, difficulty);
        System.out.println(x);
        System.out.println(verifyPuzzle(puzzleID, x, difficulty));

        // testing puzzle friendliness
        byte[] puzzID = "test".getBytes();
        int[] difficulties = {12, 16, 20, 24};
        for (int diff : difficulties) {
            long startTime = System.currentTimeMillis();
            long y = solvePuzzle(puzzID, diff);
            long endTime = System.currentTimeMillis();
            boolean valid = verifyPuzzle(puzzID, y, diff);
            // target set size = 2^(256 - difficulty)
            //BigInteger targetSetSize = BigInteger.valueOf(2).pow(256 - diff);
            System.out.println("time: " + (endTime - startTime));
            System.out.println("attempts: " + y);
            System.out.println(valid);
        }
    }
}
