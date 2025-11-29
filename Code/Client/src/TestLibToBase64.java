import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLibToBase64 {
    @Test
    public void testEncodeSample() {
        String input = "ALBNM, PROD001, 12, 2023-01-01";
        String expected = "QUxCTk0sIFBST0QwMDEsIDEyLCAyMDIzLTAxLTAx";
        String actual = lib.encode_to_base_64(input);
        assertEquals(expected, actual);
    }
}
