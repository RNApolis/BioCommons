package pl.poznan.put.rna;

import pl.poznan.put.atom.AtomName;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.ResidueComponent;

import java.util.List;

public abstract class NucleicAcidResidueComponent extends ResidueComponent {
    private final RNAResidueComponentType type;

    protected NucleicAcidResidueComponent(
            final RNAResidueComponentType type, final List<AtomName> atoms,
            final List<AtomName> additionalAtoms) {
        super(type.name().toLowerCase(), MoleculeType.RNA, atoms,
              additionalAtoms);
        this.type = type;
    }

    protected NucleicAcidResidueComponent(
            final RNAResidueComponentType type, final List<AtomName> atoms) {
        super(type.name().toLowerCase(), MoleculeType.RNA, atoms);
        this.type = type;
    }

    public RNAResidueComponentType getType() {
        return type;
    }
}
