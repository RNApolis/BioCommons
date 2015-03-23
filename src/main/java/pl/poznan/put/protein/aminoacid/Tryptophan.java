package pl.poznan.put.protein.aminoacid;

import java.util.Arrays;

import pl.poznan.put.atom.AtomName;
import pl.poznan.put.protein.ProteinChiType;
import pl.poznan.put.protein.ProteinSidechain;
import pl.poznan.put.types.Quadruplet;

public class Tryptophan extends ProteinSidechain {
    private static final Tryptophan INSTANCE = new Tryptophan();

    public static Tryptophan getInstance() {
        return Tryptophan.INSTANCE;
    }

    private Tryptophan() {
        super(Arrays.asList(new AtomName[] { AtomName.CB, AtomName.HB1, AtomName.HB2, AtomName.CG, AtomName.CD1, AtomName.HD1, AtomName.NE1, AtomName.HE1, AtomName.CE2, AtomName.CD2, AtomName.CE3, AtomName.HE3, AtomName.CZ3, AtomName.HZ3, AtomName.CZ2, AtomName.HZ2, AtomName.CH2, AtomName.HH2 }), "Tryptophan", 'W', "TRP");
    }

    @Override
    protected void fillChiAtomsMap() {
        chiAtoms.put(ProteinChiType.CHI1, new Quadruplet<AtomName>(AtomName.N, AtomName.CA, AtomName.CB, AtomName.CG));
        chiAtoms.put(ProteinChiType.CHI2, new Quadruplet<AtomName>(AtomName.CA, AtomName.CB, AtomName.CG, AtomName.CD1));
    }
}
