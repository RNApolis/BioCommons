package pl.poznan.put.protein.torsion;

import pl.poznan.put.atom.AtomName;
import pl.poznan.put.constant.Unicode;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.torsion.AtomBasedTorsionAngleType;
import pl.poznan.put.types.Quadruplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Chi5 extends AtomBasedTorsionAngleType {
  public static final Quadruplet<AtomName> ARGININE_ATOMS =
      new Quadruplet<>(AtomName.CD, AtomName.NE, AtomName.CZ, AtomName.NH1);

  private static final Map<Quadruplet<AtomName>, Chi5> INSTANCE_CACHE = new HashMap<>();

  private Chi5(final Quadruplet<AtomName> atoms) {
    super(MoleculeType.PROTEIN, Unicode.CHI5, atoms, new Quadruplet<>(0, 0, 0, 0));
  }

  public static Chi5[] getInstances() {
    final List<Chi5> instances = new ArrayList<>();
    instances.add(Chi5.getInstance(Chi5.ARGININE_ATOMS));
    return instances.toArray(new Chi5[instances.size()]);
  }

  public static Chi5 getInstance(final Quadruplet<AtomName> atoms) {
    if (!Chi5.INSTANCE_CACHE.containsKey(atoms)) {
      Chi5.INSTANCE_CACHE.put(atoms, new Chi5(atoms));
    }
    return Chi5.INSTANCE_CACHE.get(atoms);
  }
}
