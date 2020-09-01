package pl.poznan.put.pdb;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.threed.Euclidean3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.HetatomImpl;
import org.biojava.nbio.structure.ResidueNumber;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.atom.AtomName;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

@Value.Immutable
public abstract class PdbAtomLine implements Serializable, ChainNumberICode {
  private static final Logger LOGGER = LoggerFactory.getLogger(PdbAtomLine.class);
  // @formatter:off
  /*
     COLUMNS        DATA  TYPE    FIELD        DEFINITION
     -------------------------------------------------------------------------------------
      1 -  6        Record name   "ATOM  "
      7 - 11        Integer       serial       Atom  serial number.
     13 - 16        Atom          name         Atom name.
     17             Character     altLoc       Alternate location indicator.
     18 - 20        Residue name  resName      Residue name.
     22             Character     chainID      Chain identifier.
     23 - 26        Integer       resSeq       Residue sequence number.
     27             AChar         iCode        Code for insertion of residues.
     31 - 38        Real(8.3)     x            Orthogonal coordinates for X in Angstroms.
     39 - 46        Real(8.3)     y            Orthogonal coordinates for Y in Angstroms.
     47 - 54        Real(8.3)     z            Orthogonal coordinates for Z in Angstroms.
     55 - 60        Real(6.2)     occupancy    Occupancy.
     61 - 66        Real(6.2)     tempFactor   Temperature  factor.
     77 - 78        LString(2)    element      Element symbol, right-justified.
     79 - 80        LString(2)    charge       Charge  on the atom.
  */
  private static final String FORMAT_ATOM_4_CHARACTER =
      "ATOM  %5d %-4s%c%3s %c%4d%c   %8.3f%8.3f%8.3f%6.2f%6.2f          %2s%2s";
  private static final String FORMAT =
      "ATOM  %5d  %-3s%c%3s %c%4d%c   %8.3f%8.3f%8.3f%6.2f%6.2f          %2s%2s";
  // @formatter:on
  private static final String RECORD_NAME = "ATOM";

  public static PdbAtomLine fromBioJavaAtom(final Atom atom) {
    final Group group = atom.getGroup();
    final String residueName = group.getPDBName();
    final ResidueNumber residueNumberObject = group.getResidueNumber();
    final String chainIdentifier = residueNumberObject.getChainName();
    final int residueNumber = residueNumberObject.getSeqNum();
    final String insertionCode =
        (residueNumberObject.getInsCode() == null)
            ? " "
            : Character.toString(residueNumberObject.getInsCode());

    final int serialNumber = atom.getPDBserial();
    final String atomName = atom.getName();
    final String alternateLocation =
        (atom.getAltLoc() == null) ? " " : Character.toString(atom.getAltLoc());
    final double x = atom.getX();
    final double y = atom.getY();
    final double z = atom.getZ();
    final double occupancy = atom.getOccupancy();
    final double temperatureFactor = atom.getTempFactor();
    final String elementSymbol = atom.getElement().name();
    final String charge = "";
    return ImmutablePdbAtomLine.of(
        serialNumber,
        atomName,
        alternateLocation,
        residueName,
        chainIdentifier,
        residueNumber,
        insertionCode,
        x,
        y,
        z,
        occupancy,
        temperatureFactor,
        elementSymbol,
        charge);
  }

  public static PdbAtomLine parse(final String line) {
    return PdbAtomLine.parse(line, true);
  }

  public static PdbAtomLine parse(final String line, final boolean strictMode) {
    // in non-strict mode, only up to X, Y, Z fields are required, rest is
    // optional
    final int minLineLenth = strictMode ? 80 : 54;
    if (line.length() < minLineLenth) {
      throw new PdbParsingException("PDB ATOM line is too short");
    }

    try {
      final String recordName = line.substring(0, 6).trim();

      if (!Objects.equals(PdbAtomLine.RECORD_NAME, recordName)
          && !Objects.equals("HETATM", recordName)) {
        throw new PdbParsingException("PDB line does not start with ATOM or HETATM");
      }

      final int serialNumber = Integer.parseInt(line.substring(6, 11).trim());
      final String atomName = line.substring(12, 16).trim();
      final String alternateLocation = Character.toString(line.charAt(16));
      final String residueName = line.substring(17, 20).trim();
      final String chainIdentifier = Character.toString(line.charAt(21));
      final int residueNumber = Integer.parseInt(line.substring(22, 26).trim());
      final String insertionCode = Character.toString(line.charAt(26));
      final double x = Double.parseDouble(line.substring(30, 38).trim());
      final double y = Double.parseDouble(line.substring(38, 46).trim());
      final double z = Double.parseDouble(line.substring(46, 54).trim());

      final double occupancy =
          ((line.length() >= 60) && StringUtils.isNotBlank(line.substring(54, 60)))
              ? Double.parseDouble(line.substring(54, 60).trim())
              : 0;
      final double temperatureFactor =
          ((line.length() >= 66) && StringUtils.isNotBlank(line.substring(60, 66)))
              ? Double.parseDouble(line.substring(60, 66).trim())
              : 0;
      final String elementSymbol = (line.length() >= 78) ? line.substring(76, 78).trim() : "";
      final String charge = (line.length() >= 80) ? line.substring(78, 80).trim() : "";

      return ImmutablePdbAtomLine.of(
          serialNumber,
          atomName,
          alternateLocation,
          residueName,
          chainIdentifier,
          residueNumber,
          insertionCode,
          x,
          y,
          z,
          occupancy,
          temperatureFactor,
          elementSymbol,
          charge);
    } catch (final NumberFormatException e) {
      throw new PdbParsingException("Failed to parse PDB ATOM line", e);
    }
  }

  public static String getRecordName() {
    return PdbAtomLine.RECORD_NAME;
  }

  @Value.Parameter
  @Value.Auxiliary
  public abstract int serialNumber();

  @Value.Parameter
  public abstract String atomName();

  @Value.Parameter
  @Value.Auxiliary
  public abstract String alternateLocation();

  @Value.Parameter
  public abstract String residueName();

  @Override
  @Value.Parameter
  public abstract String chainIdentifier();

  @Override
  @Value.Parameter
  public abstract int residueNumber();

  @Override
  @Value.Parameter
  public abstract String insertionCode();

  @Value.Parameter
  public abstract double x();

  @Value.Parameter
  public abstract double y();

  @Value.Parameter
  public abstract double z();

  @Value.Parameter
  @Value.Auxiliary
  public abstract double occupancy();

  @Value.Parameter
  @Value.Auxiliary
  public abstract double temperatureFactor();

  @Value.Parameter
  @Value.Auxiliary
  public abstract String elementSymbol();

  @Value.Parameter
  @Value.Auxiliary
  public abstract String charge();

  @Override
  public final String toString() {
    if (alternateLocation().length() != 1) {
      PdbAtomLine.LOGGER.error(
          "Field 'alternateLocation' is longer than 1 char. Only first letter will be taken");
    }
    if (chainIdentifier().length() != 1) {
      PdbAtomLine.LOGGER.error(
          "Field 'chainIdentifier' is longer than 1 char. Only first letter will be taken");
    }
    if (insertionCode().length() != 1) {
      PdbAtomLine.LOGGER.error(
          "Field 'insertionCode' is longer than 1 char. Only first letter will be taken");
    }

    final String format =
        (atomName().length() == 4) ? PdbAtomLine.FORMAT_ATOM_4_CHARACTER : PdbAtomLine.FORMAT;
    return String.format(
        Locale.US,
        format,
        serialNumber(),
        atomName(),
        alternateLocation().charAt(0),
        residueName(),
        chainIdentifier().charAt(0),
        residueNumber(),
        insertionCode().charAt(0),
        x(),
        y(),
        z(),
        occupancy(),
        temperatureFactor(),
        elementSymbol(),
        charge());
  }

  public final AtomName detectAtomName() {
    return AtomName.fromString(atomName());
  }

  public final double distanceTo(final PdbAtomLine other) {
    final Vector<Euclidean3D> v1 = new Vector3D(x(), y(), z());
    final Vector<Euclidean3D> v2 = new Vector3D(other.x(), other.y(), other.z());
    return v1.distance(v2);
  }

  public final Atom toBioJavaAtom() {
    if (alternateLocation().length() != 1) {
      throw new CifPdbIncompatibilityException(
          "Cannot convert to PDB. Field 'alternateLocation' is " + "longer than 1 char");
    }
    if (insertionCode().length() != 1) {
      throw new CifPdbIncompatibilityException(
          "Cannot convert to PDB. Field 'insertionCode' is longer " + "than 1 char");
    }

    final Group group = new HetatomImpl();
    final Character icode = Objects.equals(" ", insertionCode()) ? null : insertionCode().charAt(0);
    group.setResidueNumber(String.valueOf(chainIdentifier()), residueNumber(), icode);
    group.setPDBName(residueName());

    final String name =
        (atomName().length() == 4) ? atomName() : String.format(" %-3s", atomName());

    final Atom atom = new AtomImpl();
    atom.setPDBserial(serialNumber());
    atom.setName(name);
    atom.setAltLoc(alternateLocation().charAt(0));
    atom.setX(x());
    atom.setY(y());
    atom.setZ(z());
    atom.setOccupancy((float) occupancy());
    atom.setTempFactor((float) temperatureFactor());
    atom.setElement(Element.valueOfIgnoreCase(elementSymbol()));
    atom.setGroup(group);
    return atom;
  }

  public final String toCif() {
    final StringBuilder builder = new StringBuilder();
    builder.append("ATOM ");
    builder.append(serialNumber()).append(' ');
    if (atomName().contains("'")) {
      builder.append('"').append(atomName()).append("\" ");
    } else {
      builder.append(atomName()).append(' ');
    }
    if (StringUtils.isNotBlank(alternateLocation())) {
      builder.append(alternateLocation()).append(' ');
    } else {
      builder.append(". ");
    }
    builder.append(residueName()).append(' ');
    builder.append(chainIdentifier()).append(' ');
    builder.append(residueNumber()).append(' ');
    if (StringUtils.isNotBlank(insertionCode())) {
      builder.append(insertionCode()).append(' ');
    } else {
      builder.append("? ");
    }
    builder.append(x()).append(' ');
    builder.append(y()).append(' ');
    builder.append(z()).append(' ');
    builder.append(occupancy()).append(' ');
    builder.append(temperatureFactor()).append(' ');
    builder.append(elementSymbol()).append(' ');
    if (StringUtils.isNotBlank(charge())) {
      builder.append(charge()).append(' ');
    } else {
      builder.append('?');
    }
    return builder.toString();
  }

  public final Vector3D toVector3D() {
    return new Vector3D(x(), y(), z());
  }
}
