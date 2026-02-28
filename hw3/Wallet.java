import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Wallet {

    private final Keys.PrivateKey privateKey;
    private final Keys.PublicKey  publicKey;

    public Wallet(int keySize) {
        // call generateKeys
        Keys.Key[] pair = generateKeys(keySize);
        // update values individually
        this.privateKey = (Keys.PrivateKey) pair[0];
        this.publicKey = (Keys.PublicKey) pair[1];
    }

    public Keys.PublicKey getPublicKey() {
        return publicKey;
    }

    public Keys.Key[] generateKeys(int keySize) {
        // set value of e
        BigInteger e = BigInteger.valueOf(65537);
        // calculate two large primes
        SecureRandom random = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(keySize / 2, random);
        BigInteger q = BigInteger.probablePrime(keySize / 2, random);
        // calculate n = p x q
        BigInteger n = p.multiply(q);
        // calculate phi(n) = (p-1) x (q-1)
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        // calculate d = e^(-1) (mod phi(n))
        BigInteger d = e.modInverse(phi);
        // return key pair
        return new Keys.Key[] {
            new Keys.PrivateKey(d, n),
            new Keys.PublicKey(e, n)
        };
    }

    public byte[] sign(byte[] message) {
        try {
            // hash message
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(message);
            // convert to BigInteger
            BigInteger m = new BigInteger(1, h); // '1' ensures positive number
            // compute signature
            BigInteger sig = m.modPow(privateKey.d, privateKey.n);
            // convert to byte[]
            byte[] sigMsg = sig.toByteArray();
            // return signed message
            return sigMsg;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean verify(Keys.PublicKey pk, byte[] message, byte[] signature) {
        try {
            // convert to BigInteger
            BigInteger s = new BigInteger(1, signature); // '1' ensures positive number
            // decrypt signature
            BigInteger decrypted = s.modPow(pk.e, pk.n);
            // hash original message
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(message);
            BigInteger m = new BigInteger(1, h);
            // return comparison
            return m.equals(decrypted);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        // make a wallet
        Wallet wall_E = new Wallet(2048);
        // message
        String message = "potatoes are cool";
        byte[] msg = message.getBytes();
        // sign the message
        byte[] sig = wall_E.sign(msg);
        // verify the signature
        boolean verify = verify(wall_E.publicKey, msg, sig);
        // check output
        System.out.println(verify);
        // tampered message
        String tmpMsg = "potatoes are not cool";
        boolean tmpVer = verify(wall_E.publicKey, tmpMsg.getBytes(), sig);
        System.out.println(tmpVer);
    }
}
