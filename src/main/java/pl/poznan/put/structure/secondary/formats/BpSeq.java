package pl.poznan.put.structure.secondary.formats;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.pdb.PdbResidueIdentifier;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.pdb.analysis.ResidueCollection;
import pl.poznan.put.structure.secondary.BasePair;
import pl.poznan.put.structure.secondary.ClassifiedBasePair;
import pl.poznan.put.structure.secondary.DotBracketSymbol;

import java.io.Serializable;
import java.util.*;

public class BpSeq implements Serializable {
    public static class Entry implements Comparable<Entry>, Serializable {
        private final int index;
        private final int pair;
        private final char seq;
        private final String comment;

        public Entry(int index, int pair, char seq) {
            super();
            this.index = index;
            this.pair = pair;
            this.seq = seq;
            comment = "";
        }

        public Entry(int index, int pair, char seq, String comment) {
            super();
            this.index = index;
            this.pair = pair;
            this.seq = seq;
            this.comment = comment;
        }

        public int getIndex() {
            return index;
        }

        public int getPair() {
            return pair;
        }

        public char getSeq() {
            return seq;
        }

        public String getComment() {
            return comment;
        }

        @Override
        public int compareTo(Entry e) {
            if (e == null) {
                throw new NullPointerException();
            }

            if (equals(e)) {
                return 0;
            }

            if (index < e.index) {
                return -1;
            }
            if (index > e.index) {
                return 1;
            }
            return 0;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + index;
            result = prime * result + pair;
            result = prime * result + seq;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Entry other = (Entry) obj;
            return index == other.index && pair == other.pair && seq == other.seq;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (BpSeq.printComments && !StringUtils.isBlank(comment)) {
                builder.append('#');
                builder.append(comment);
                builder.append('\n');
            }
            builder.append(index);
            builder.append(' ');
            builder.append(seq);
            builder.append(' ');
            builder.append(pair);
            return builder.toString();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BpSeq.class);
    private static boolean printComments = false;

    public static BpSeq fromString(String data) throws InvalidSecondaryStructureException {
        List<BpSeq.Entry> entries = new ArrayList<>();

        for (String line : data.split("\n")) {
            line = line.trim();

            int hash = line.indexOf('#');
            if (hash != -1) {
                line = line.substring(0, hash);
            }

            if (line.length() == 0) {
                continue;
            }

            String[] split = line.split("\\s+");

            if (split.length != 3 || split[1].length() != 1) {
                throw new InvalidSecondaryStructureException("Line does not conform to BPSEQ format: " + line);
            }

            int index, pair;
            char seq;

            try {
                index = Integer.valueOf(split[0]);
                seq = split[1].charAt(0);
                pair = Integer.valueOf(split[2]);
            } catch (NumberFormatException e) {
                throw new InvalidSecondaryStructureException("Line does not conform to BPSEQ format: " + line, e);
            }

            entries.add(new Entry(index, pair, seq));
        }

        return new BpSeq(entries);
    }

    public static BpSeq fromCt(Ct ct) throws InvalidSecondaryStructureException {
        List<BpSeq.Entry> bpseqEntries = new ArrayList<>();

        for (Ct.Entry e : ct.getEntries()) {
            bpseqEntries.add(new BpSeq.Entry(e.getIndex(), e.getPair(), e.getSeq()));
        }

        return new BpSeq(bpseqEntries);
    }

    public static BpSeq fromDotBracket(DotBracket db) throws InvalidSecondaryStructureException {
        List<BpSeq.Entry> entries = new ArrayList<>();

        for (DotBracketSymbol symbol : db.getSymbols()) {
            DotBracketSymbol pair = symbol.getPair();
            int index = symbol.getIndex() + 1;
            int pairIndex = pair != null ? pair.getIndex() + 1 : 0;
            char sequence = symbol.getSequence();

            entries.add(new Entry(index, pairIndex, sequence));
        }

        return new BpSeq(entries);
    }

    public static BpSeq fromResidueCollection(ResidueCollection residueCollection, List<ClassifiedBasePair> basePairs) throws InvalidSecondaryStructureException {
        List<BasePair> allBasePairs = new ArrayList<>();
        Map<BasePair, String> basePairToComment = new HashMap<>();

        for (ClassifiedBasePair classifiedBasePair : basePairs) {
            BasePair basePair = classifiedBasePair.getBasePair();
            allBasePairs.add(basePair);

            String comment = classifiedBasePair.isCanonical() ? "" : classifiedBasePair.generateComment();
            basePairToComment.put(basePair, comment);
            basePairToComment.put(basePair.invert(), comment);
        }

        List<BpSeq.Entry> entries = new ArrayList<>();
        entries.addAll(BpSeq.generateEntriesForPaired(residueCollection, allBasePairs, basePairToComment));
        entries.addAll(BpSeq.generateEntriesForUnpaired(residueCollection, allBasePairs));
        return new BpSeq(entries);
    }

    private static List<BpSeq.Entry> generateEntriesForUnpaired(ResidueCollection residueCollection, List<BasePair> allBasePairs) {
        List<BpSeq.Entry> entries = new ArrayList<>();
        List<PdbResidue> residues = residueCollection.getResidues();
        Set<PdbResidueIdentifier> paired = new HashSet<>();

        for (BasePair basePair : allBasePairs) {
            paired.add(basePair.getLeft());
            paired.add(basePair.getRight());
        }

        for (int i = 0; i < residues.size(); i++) {
            PdbResidue residue = residues.get(i);
            if (!paired.contains(residue.getResidueIdentifier())) {
                entries.add(new BpSeq.Entry(i + 1, 0, residue.getOneLetterName()));
            }
        }

        return entries;
    }

    private static List<BpSeq.Entry> generateEntriesForPaired(ResidueCollection residueCollection, Collection<BasePair> basePairs, Map<BasePair, String> basePairToComment) {
        List<BpSeq.Entry> entries = new ArrayList<>();
        List<PdbResidue> residues = residueCollection.getResidues();

        for (BasePair basePair : basePairs) {
            PdbResidue left = residueCollection.findResidue(basePair.getLeft());
            PdbResidue right = residueCollection.findResidue(basePair.getRight());
            int indexL = 1 + residues.indexOf(left);
            int indexR = 1 + residues.indexOf(right);
            entries.add(new Entry(indexL, indexR, left.getOneLetterName(), basePairToComment.get(basePair)));
            entries.add(new Entry(indexR, indexL, right.getOneLetterName(), basePairToComment.get(basePair)));
            BpSeq.LOGGER.trace("Storing pair (" + indexL + " -> " + indexR + ") which is (" + left + " -> " + right + ")");
        }

        return entries;
    }

    public static void setPrintComments(boolean printComments) {
        BpSeq.printComments = printComments;
    }

    private final SortedSet<Entry> entries;

    public BpSeq(Collection<BpSeq.Entry> entries) throws InvalidSecondaryStructureException {
        this.entries = new TreeSet<>(entries);
        validate();
    }

    /*
     * Check if all pairs match.
     */
    private void validate() throws InvalidSecondaryStructureException {
        Map<Integer, Integer> map = new HashMap<>();

        for (Entry e : entries) {
            if (e.index == e.pair) {
                throw new InvalidSecondaryStructureException("Invalid line in BPSEQ data, a residue cannot be paired with itself! Line: " + e);
            }

            map.put(e.index, e.pair);
        }

        int previous = 0;

        for (Entry e : entries) {
            if (e.index - previous != 1) {
                throw new InvalidSecondaryStructureException("Inconsistent numbering in BPSEQ format: previous=" + previous + ", current=" + e.index);
            }
            previous = e.index;

            int pair = map.get(e.index);
            if (pair != 0) {
                if (!map.containsKey(pair)) {
                    throw new InvalidSecondaryStructureException("Inconsistency in BPSEQ format: (" + e.index + " -> " + pair + ")");
                }
                if (map.get(pair) != e.index) {
                    throw new InvalidSecondaryStructureException("Inconsistency in BPSEQ format: (" + e.index + " -> " + pair + ") and (" + pair + " -> " + map.get(pair) + ")");
                }
            }
        }
    }

    public SortedSet<Entry> getEntries() {
        return Collections.unmodifiableSortedSet(entries);
    }

    public String getSequence() {
        StringBuilder builder = new StringBuilder();
        for (Entry e : entries) {
            builder.append(e.seq);
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (entries == null ? 0 : entries.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BpSeq other = (BpSeq) obj;
        if (entries == null) {
            if (other.entries != null) {
                return false;
            }
        } else if (!CollectionUtils.isEqualCollection(entries, other.entries)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Entry e : entries) {
            builder.append(e.toString());
            builder.append('\n');
        }

        return builder.toString();
    }
}
