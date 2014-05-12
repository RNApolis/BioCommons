package pl.poznan.put.protein;

import java.util.ArrayList;
import java.util.List;

import pl.poznan.put.atoms.AtomName;

public class ProteinBackboneAtoms {
    private static final List<AtomName> ATOMS = new ArrayList<AtomName>();

    static {
        ProteinBackboneAtoms.ATOMS.add(AtomName.N);
        ProteinBackboneAtoms.ATOMS.add(AtomName.HN);
        ProteinBackboneAtoms.ATOMS.add(AtomName.CA);
        ProteinBackboneAtoms.ATOMS.add(AtomName.HA);
        ProteinBackboneAtoms.ATOMS.add(AtomName.C);
        ProteinBackboneAtoms.ATOMS.add(AtomName.O);
    }

    public static List<AtomName> getAtoms() {
        return ProteinBackboneAtoms.ATOMS;
    }

    private ProteinBackboneAtoms() {
    }
}
