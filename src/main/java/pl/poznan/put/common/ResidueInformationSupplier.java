package pl.poznan.put.common;

import java.util.List;

public interface ResidueInformationSupplier extends AtomContainer {
    MoleculeType getMoleculeType();

    List<ResidueComponent> getAllMoleculeComponents();

    String getDescription();

    char getOneLetterName();

    String getDefaultPdbName();

    List<String> getPdbNames();
}
