package pl.poznan.put;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import pl.poznan.put.pdb.PdbAtomLine;
import pl.poznan.put.pdb.PdbParsingException;

public class TestPdbAtomLine {
    private final String validLine = "ATOM      1  OP3   G A   1      50.193  51.190  50.534  1.00 99.85           O  ";
    private final String tooShortLine = validLine.substring(0, validLine.length() - 10);
    private final String misalignedLine = StringUtils.normalizeSpace(validLine);
    private final String validLineWithOneLetterAtom = "ATOM      2  P     G A   1      50.626  49.730  50.573  1.00100.19           P  ";
    private final String missingTempFactor = "ATOM      1  N   GLU     1      42.189  22.849  47.437  1.00                 N  ";

    @Test
    public void testParseToString() throws PdbParsingException {
        PdbAtomLine atomLine = PdbAtomLine.parse(validLine);
        String atomLineString = atomLine.toString();
        assertEquals(validLine, atomLineString);
    }

    @Test
    public void testParseToStringOneLetterAtom() throws PdbParsingException {
        PdbAtomLine atomLine = PdbAtomLine.parse(validLineWithOneLetterAtom);
        String atomLineString = atomLine.toString();
        assertEquals(validLineWithOneLetterAtom, atomLineString);
    }

    @Test(expected = PdbParsingException.class)
    public void testShortLine() throws PdbParsingException {
        PdbAtomLine.parse(tooShortLine);
    }

    @Test(expected = PdbParsingException.class)
    public void testMisalignedLine() throws PdbParsingException {
        PdbAtomLine.parse(misalignedLine);
    }

    @Test
    public void testMissingTempFactor() throws PdbParsingException {
        PdbAtomLine.parse(missingTempFactor);
    }
}
