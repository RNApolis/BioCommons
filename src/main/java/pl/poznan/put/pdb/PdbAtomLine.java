package pl.poznan.put.pdb;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.AtomImpl;
import org.biojava.nbio.structure.Element;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.HetatomImpl;
import org.biojava.nbio.structure.ResidueNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.atom.AtomName;

import javax.vecmath.Point3d;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class PdbAtomLine implements Serializable, ChainNumberICode {
  private static final long serialVersionUID = -6626889209014265608L;
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
  public static final String CIF_LOOP =
      "loop_\n"
          + "_atom_site.group_PDB\n"
          + "_atom_site.id\n"
          + "_atom_site.auth_atom_id\n"
          + "_atom_site.label_alt_id\n"
          + "_atom_site.auth_comp_id\n"
          + "_atom_site.auth_asym_id\n"
          + "_atom_site.auth_seq_id\n"
          + "_atom_site.pdbx_PDB_ins_code\n"
          + "_atom_site.Cartn_x\n"
          + "_atom_site.Cartn_y\n"
          + "_atom_site.Cartn_z\n"
          + "_atom_site.occupancy\n"
          + "_atom_site.B_iso_or_equiv\n"
          + "_atom_site.type_symbol\n"
          + "_atom_site.pdbx_formal_charge";
  // @formatter:on

  private static final String RECORD_NAME = "ATOM";
  private final int serialNumber;
  private final String atomName;
  private final String alternateLocation;
  private final String residueName;
  private final String chainIdentifier;
  private final int residueNumber;
  private final String insertionCode;
  private final double x;
  private final double y;
  private final double z;
  private final double occupancy;
  private final double temperatureFactor;
  private final String elementSymbol;
  private final String charge;

  public PdbAtomLine(
      final int serialNumber,
      final String atomName,
      final String alternateLocation,
      final String residueName,
      final String chainIdentifier,
      final int residueNumber,
      final String insertionCode,
      final double x,
      final double y,
      final double z,
      final double occupancy,
      final double temperatureFactor,
      final String elementSymbol,
      final String charge) {
    super();
    this.serialNumber = serialNumber;
    this.atomName = atomName;
    this.alternateLocation = alternateLocation;
    this.residueName = residueName;
    this.chainIdentifier = chainIdentifier;
    this.residueNumber = residueNumber;
    this.insertionCode = insertionCode;
    this.x = x;
    this.y = y;
    this.z = z;
    this.occupancy = occupancy;
    this.temperatureFactor = temperatureFactor;
    this.elementSymbol = elementSymbol;
    this.charge = charge;
  }

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
    return new PdbAtomLine(
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

  public static PdbAtomLine parse(final String line) throws PdbParsingException {
    return PdbAtomLine.parse(line, true);
  }

  public static PdbAtomLine parse(final String line, final boolean strictMode)
      throws PdbParsingException {
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

      return new PdbAtomLine(
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

  public final int getSerialNumber() {
    return serialNumber;
  }

  public final String getAtomName() {
    return atomName;
  }

  public final String getAlternateLocation() {
    return alternateLocation;
  }

  public final String getResidueName() {
    return residueName;
  }

  @Override
  public final String getChainIdentifier() {
    return chainIdentifier;
  }

  @Override
  public final int getResidueNumber() {
    return residueNumber;
  }

  @Override
  public final String getInsertionCode() {
    return insertionCode;
  }

  @Override
  public final PdbResidueIdentifier getResidueIdentifier() {
    return new PdbResidueIdentifier(chainIdentifier, residueNumber, insertionCode);
  }

  public final double getX() {
    return x;
  }

  public final double getY() {
    return y;
  }

  public final double getZ() {
    return z;
  }

  public final double getOccupancy() {
    return occupancy;
  }

  public final double getTemperatureFactor() {
    return temperatureFactor;
  }

  public final String getElementSymbol() {
    return elementSymbol;
  }

  public final String getCharge() {
    return charge;
  }

  @Override
  public final String toString() {
    if (alternateLocation.length() != 1) {
      PdbAtomLine.LOGGER.error(
          "Field 'alternateLocation' is longer than 1 char. Only first letter will be taken");
    }
    if (chainIdentifier.length() != 1) {
      PdbAtomLine.LOGGER.error(
          "Field 'chainIdentifier' is longer than 1 char. Only first letter will be taken");
    }
    if (insertionCode.length() != 1) {
      PdbAtomLine.LOGGER.error(
          "Field 'insertionCode' is longer than 1 char. Only first letter will be taken");
    }

    final String format =
        (atomName.length() == 4) ? PdbAtomLine.FORMAT_ATOM_4_CHARACTER : PdbAtomLine.FORMAT;
    return String.format(
        Locale.US,
        format,
        serialNumber,
        atomName,
        alternateLocation.charAt(0),
        residueName,
        chainIdentifier.charAt(0),
        residueNumber,
        insertionCode.charAt(0),
        x,
        y,
        z,
        occupancy,
        temperatureFactor,
        elementSymbol,
        charge);
  }

  public final PdbAtomLine replaceSerialNumber(final int serialNumberNew) {
    return new PdbAtomLine(
        serialNumberNew,
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

  public final PdbAtomLine replaceChainIdentifier(final String chainIdentifierNew) {
    return new PdbAtomLine(
        serialNumber,
        atomName,
        alternateLocation,
        residueName,
        chainIdentifierNew,
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

  public final PdbAtomLine replaceResidueNumber(final int residueNumberNew) {
    return new PdbAtomLine(
        serialNumber,
        atomName,
        alternateLocation,
        residueName,
        chainIdentifier,
        residueNumberNew,
        insertionCode,
        x,
        y,
        z,
        occupancy,
        temperatureFactor,
        elementSymbol,
        charge);
  }

  public final PdbAtomLine replaceAlternateLocation(final String alternateLocationNew) {
    return new PdbAtomLine(
        serialNumber,
        atomName,
        alternateLocationNew,
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

  public final PdbAtomLine replaceCoordinates(
      final double xNew, final double yNew, final double zNew) {
    return new PdbAtomLine(
        serialNumber,
        atomName,
        alternateLocation,
        residueName,
        chainIdentifier,
        residueNumber,
        insertionCode,
        xNew,
        yNew,
        zNew,
        occupancy,
        temperatureFactor,
        elementSymbol,
        charge);
  }

  public final AtomName detectAtomName() {
    return AtomName.fromString(atomName);
  }

  public final double distanceTo(final PdbAtomLine other) {
    final Vector3D v1 = new Vector3D(x, y, z);
    final Vector3D v2 = new Vector3D(other.x, other.y, other.z);
    return v1.distance(v2);
  }

  public final Atom toBioJavaAtom() throws CifPdbIncompatibilityException {
    if (alternateLocation.length() != 1) {
      throw new CifPdbIncompatibilityException(
          "Cannot convert to PDB. Field 'alternateLocation' is " + "longer than 1 char");
    }
    if (insertionCode.length() != 1) {
      throw new CifPdbIncompatibilityException(
          "Cannot convert to PDB. Field 'insertionCode' is longer " + "than 1 char");
    }

    final Group group = new HetatomImpl();
    final Character icode = Objects.equals(" ", insertionCode) ? null : insertionCode.charAt(0);
    group.setResidueNumber(String.valueOf(chainIdentifier), residueNumber, icode);
    group.setPDBName(residueName);

    final String name = (atomName.length() == 4) ? atomName : String.format(" %-3s", atomName);

    final Atom atom = new AtomImpl();
    atom.setPDBserial(serialNumber);
    atom.setName(name);
    atom.setAltLoc(alternateLocation.charAt(0));
    atom.setX(x);
    atom.setY(y);
    atom.setZ(z);
    atom.setOccupancy((float) occupancy);
    atom.setTempFactor((float) temperatureFactor);
    atom.setElement(Element.valueOfIgnoreCase(elementSymbol));
    atom.setGroup(group);
    return atom;
  }

  public final String toCif() {
    final StringBuilder builder = new StringBuilder();
    builder.append("ATOM ");
    builder.append(serialNumber).append(' ');
    builder.append(atomName).append(' ');
    if (StringUtils.isNotBlank(alternateLocation)) {
      builder.append(alternateLocation).append(' ');
    } else {
      builder.append(". ");
    }
    builder.append(residueName).append(' ');
    builder.append(chainIdentifier).append(' ');
    builder.append(residueNumber).append(' ');
    if (StringUtils.isNotBlank(insertionCode)) {
      builder.append(insertionCode).append(' ');
    } else {
      builder.append("? ");
    }
    builder.append(x).append(' ');
    builder.append(y).append(' ');
    builder.append(z).append(' ');
    builder.append(occupancy).append(' ');
    builder.append(temperatureFactor).append(' ');
    builder.append(elementSymbol).append(' ');
    if (StringUtils.isNotBlank(charge)) {
      builder.append(charge).append(' ');
    } else {
      builder.append('?');
    }
    return builder.toString();
  }

  public final Point3d toPoint3d() {
    return new Point3d(x, y, z);
  }

  @Override
  public final boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    final PdbAtomLine other = (PdbAtomLine) o;
    return (residueNumber == other.residueNumber)
        && (Double.compare(other.x, x) == 0)
        && (Double.compare(other.y, y) == 0)
        && (Double.compare(other.z, z) == 0)
        && Objects.equals(atomName, other.atomName)
        && Objects.equals(residueName, other.residueName)
        && Objects.equals(chainIdentifier, other.chainIdentifier)
        && Objects.equals(insertionCode, other.insertionCode);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(
        atomName, residueName, chainIdentifier, residueNumber, insertionCode, x, y, z);
  }
}
