public class Commitment {
    public final byte[] c; // commitment value = H(r || m)
    public final byte[] r; 

    public Commitment(byte[] c, byte[] r) {
        this.c = c;
        this.r = r;
    }

    public byte[] getCom() {
        return c;
    }

    public byte[] getSalt() {
        return r;
    }
}

