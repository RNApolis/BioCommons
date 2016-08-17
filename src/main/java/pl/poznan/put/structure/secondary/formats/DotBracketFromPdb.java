package pl.poznan.put.structure.secondary.formats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.structure.secondary.DotBracketSymbol;

public class DotBracketFromPdb extends DotBracket {
    private final Map<DotBracketSymbol, PdbResidueIdentifier> symbolToResidue = new HashMap<>();
    private final Map<PdbResidueIdentifier, DotBracketSymbol> residueToSymbol = new HashMap<>();

    public DotBracketFromPdb(String sequence, String structure, PdbModel model) throws

                                                                                InvalidStructureException {
        super(sequence, DotBracketFromPdb.updateMissingIndices(structure, model));

        mapSymbolsAndResidues(model);
        splitStrands(model);
    }

    public DotBracketFromPdb(DotBracket dotBracket, PdbModel model) throws
                                                                    InvalidStructureException {
        this(dotBracket.sequence, dotBracket.structure, model);
    }

    private static String updateMissingIndices(String structure, PdbModel model) {
        List<PdbResidue> residues = model.getResidues();
        char[] dotBracket = structure.toCharArray();
        assert dotBracket.length == residues.size();

        for (int i = 0; i < dotBracket.length; i++) {
            if (residues.get(i).isMissing()) {
                dotBracket[i] = '-';
            }
        }

        return String.valueOf(dotBracket);
    }

    private void mapSymbolsAndResidues(PdbModel model) {
        List<PdbResidue> residues = model.getResidues();
        assert residues.size() == symbols.size();

        for (int i = 0; i < residues.size(); i++) {
            DotBracketSymbol symbol = symbols.get(i);
            PdbResidue residue = residues.get(i);
            PdbResidueIdentifier residueIdentifier = residue.getResidueIdentifier();
            symbolToResidue.put(symbol, residueIdentifier);
            residueToSymbol.put(residueIdentifier, symbol);
        }
    }

    private void splitStrands(PdbModel model) {
        strands.clear();
        int start = 0;
        int end = 0;

        for (PdbChain chain : model.getChains()) {
            end += chain.getResidues().size();
            strands.add(new Strand(this, String.valueOf(chain.getIdentifier()), start, end));
            start = end;
        }
    }

    public PdbResidueIdentifier getResidueIdentifier(DotBracketSymbol symbol) {
        return symbolToResidue.get(symbol);
    }

    public DotBracketSymbol getSymbol(PdbResidueIdentifier residueIdentifier) {
        return residueToSymbol.get(residueIdentifier);
    }

    @Override
    protected int getCtOriginalColumn(DotBracketSymbol symbol) {
        return symbolToResidue.get(symbol).getResidueNumber();
    }
}
