package diarsid.beam.navigator.filesystem;

import java.text.DecimalFormat;

import static diarsid.beam.navigator.filesystem.Bytes.ONE_GB;
import static diarsid.beam.navigator.filesystem.Bytes.ONE_KB;
import static diarsid.beam.navigator.filesystem.Bytes.ONE_MB;
import static diarsid.beam.navigator.filesystem.Bytes.ONE_TB;

public enum SizeInBytes {

    BYTES("B", 0),
    KILOBYTES("KB", ONE_KB),
    MEGABYTES("MB", ONE_MB),
    GIGABYTES("GB", ONE_GB),
    TERABYTES("TB", ONE_TB);

    private final static DecimalFormat SIZE_FORMAT = new DecimalFormat("#.##");

    public final String shortName;
    private final long one;

    SizeInBytes(String shortName, long one) {
        this.shortName = shortName;
        this.one = one;
    }

    public static SizeInBytes of(long bytes) {
        if ( bytes >= ONE_TB ) {
            return TERABYTES;
        }
        else if ( bytes >= ONE_GB ) {
            return GIGABYTES;
        }
        else if ( bytes >= ONE_MB ) {
            return MEGABYTES;
        }
        else if ( bytes >= ONE_KB ) {
            return KILOBYTES;
        }
        else {
            return BYTES;
        }
    }

    public String format(long bytes) {
        if ( this.equals(BYTES) ) {
            return bytes + " " + this.shortName;
        }
        else {
            float xBytes = ((float) bytes) / this.one;
            return SIZE_FORMAT.format(xBytes) + " " + this.shortName;
        }
    }
}
