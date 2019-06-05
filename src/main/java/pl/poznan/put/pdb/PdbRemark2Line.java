package pl.poznan.put.pdb;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class PdbRemark2Line implements Serializable {
  public static final String PROLOGUE =
      "REMARK   2                                                      " + "                ";
  private static final long serialVersionUID = -4818750947185900609L;
  // @formatter:off
  // COLUMNS        DATA TYPE     FIELD          DEFINITION
  // --------------------------------------------------------------------------------
  //  1 -  6        Record name   "REMARK"
  // 10             LString(1)    "2"
  // 12 - 22        LString(11)   "RESOLUTION."
  // 24 - 30        Real(7.2)     resolution     Resolution.
  // 32 - 41        LString(10)   "ANGSTROMS."
  //
  // @formatter:on
  private static final String FORMAT =
      "REMARK   2 RESOLUTION. %7.2f ANGSTROMS.                         " + "              ";

  // @formatter:off
  // COLUMNS        DATA TYPE     FIELD          DEFINITION
  // --------------------------------------------------------------------------------
  //  1 -  6        Record name   "REMARK"
  // 10             LString(1)    "2"
  // 12 - 38        LString(28)   "RESOLUTION.  NOT APPLICABLE."
  // @formatter:on
  private static final String NOT_APPLICABLE =
      "REMARK   2 RESOLUTION. NOT APPLICABLE.                          " + "                ";

  private static final PdbRemark2Line EMPTY_INSTANCE = new PdbRemark2Line(Double.NaN);
  private final double resolution;

  public PdbRemark2Line(final double resolution) {
    super();
    this.resolution = resolution;
  }

  public static PdbRemark2Line emptyInstance() {
    return PdbRemark2Line.EMPTY_INSTANCE;
  }

  public static PdbRemark2Line parse(final String line) throws PdbParsingException {
    if (!line.startsWith("REMARK   2 RESOLUTION.")) {
      throw new PdbParsingException("Failed to parse REMARK   2 RESOLUTION. line: " + line);
    }

    try {
      final String resolutionString = line.substring(23).trim();
      double resolution = Double.NaN;

      if (!Objects.equals("NOT APPLICABLE.", resolutionString)) {
        resolution = Double.parseDouble(line.substring(23, 30).trim());
      }

      return new PdbRemark2Line(resolution);
    } catch (final NumberFormatException e) {
      throw new PdbParsingException("Failed to parse REMARK   2 RESOLUTION. line", e);
    }
  }

  public final double getResolution() {
    return resolution;
  }

  @Override
  public final String toString() {
    if (Double.isNaN(resolution)) {
      return PdbRemark2Line.NOT_APPLICABLE;
    }

    return String.format(Locale.US, PdbRemark2Line.FORMAT, resolution);
  }
}
