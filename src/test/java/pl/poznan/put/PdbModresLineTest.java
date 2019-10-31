package pl.poznan.put;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import pl.poznan.put.pdb.PdbModresLine;
import pl.poznan.put.pdb.PdbParsingException;

import static org.hamcrest.Matchers.is;

public class PdbModresLineTest {
  // @formatter:off
  private static final String VALID_LINE =
      "MODRES 1EHZ 2MG A   10    G  2N-METHYLGUANOSINE-5'-MONOPHOSPHATE                ";
  // @formatter:on

  private static final String TOO_SHORT_LINE =
      PdbModresLineTest.VALID_LINE.substring(0, PdbModresLineTest.VALID_LINE.length() - 20);
  private static final String MISALIGNED_LINE =
      StringUtils.normalizeSpace(PdbModresLineTest.VALID_LINE);

  @Test
  public final void testParseToString() {
    final PdbModresLine parsed = PdbModresLine.parse(PdbModresLineTest.VALID_LINE);
    final String parsedToString = parsed.toString();
    Assert.assertThat(parsedToString, is(PdbModresLineTest.VALID_LINE));
  }

  @Test(expected = PdbParsingException.class)
  public final void testShortLine() {
    PdbModresLine.parse(PdbModresLineTest.TOO_SHORT_LINE);
  }

  @Test(expected = PdbParsingException.class)
  public final void testMisalignedLine() {
    PdbModresLine.parse(PdbModresLineTest.MISALIGNED_LINE);
  }
}
