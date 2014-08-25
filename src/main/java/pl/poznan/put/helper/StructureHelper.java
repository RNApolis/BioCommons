package pl.poznan.put.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;

import pl.poznan.put.atoms.AtomName;

public class StructureHelper {
    public static Atom findAtom(Group residue, AtomName atomName) {
        for (Atom atom : residue.getAtoms()) {
            if (atomName.matchesName(atom.getFullName())) {
                return atom;
            }
        }
        return null;
    }

    public static Atom[] findAllAtoms(Chain chain, AtomName atomName) {
        List<Atom> result = new ArrayList<>();

        for (Group group : chain.getAtomGroups()) {
            Atom atom = StructureHelper.findAtom(group, atomName);
            if (atom != null) {
                result.add(atom);
            }
        }

        return result.toArray(new Atom[result.size()]);
    }

    public static Atom[] findAllAtoms(Structure structure, AtomName atomName) {
        List<Atom> result = new ArrayList<>();
        for (Chain chain : structure.getChains()) {
            Atom[] atomsChain = StructureHelper.findAllAtoms(chain, atomName);
            result.addAll(Arrays.asList(atomsChain));
        }
        return result.toArray(new Atom[result.size()]);
    }

    public static void mergeAltLocs(Group group) {
        LinkedHashSet<Atom> atoms = new LinkedHashSet<>();
        atoms.addAll(group.getAtoms());

        for (Group altloc : group.getAltLocs()) {
            for (Atom atom : altloc.getAtoms()) {
                if (!atoms.contains(atom)) {
                    atoms.add(atom);
                }
            }
        }

        group.setAtoms(new ArrayList<>(atoms));
    }

    private StructureHelper() {
    }
}
