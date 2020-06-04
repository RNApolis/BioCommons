package pl.poznan.put.protein.torsion;

import pl.poznan.put.atom.AtomName;
import pl.poznan.put.constant.Unicode;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.torsion.PseudoTorsionAngleType;
import pl.poznan.put.types.ImmutableQuadruplet;
import pl.poznan.put.types.Quadruplet;

public final class Calpha extends PseudoTorsionAngleType {
  private static final Calpha INSTANCE = new Calpha();

  private Calpha() {
    super(
        MoleculeType.PROTEIN,
        Unicode.CALPHA,
        ImmutableQuadruplet.of(AtomName.CA, AtomName.CA, AtomName.CA, AtomName.CA),
        ImmutableQuadruplet.of(0, 1, 2, 3));
  }

  public static Calpha getInstance() {
    return Calpha.INSTANCE;
  }
}
