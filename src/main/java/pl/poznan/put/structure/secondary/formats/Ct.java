package pl.poznan.put.structure.secondary.formats;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.structure.secondary.DotBracketSymbol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Ct implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ct.class);
    private static final boolean FIX_LAST_ENTRY = true;
    private static boolean printComments = true;
    private final SortedSet<Entry> entries;

    public Ct(List<Entry> entries) throws InvalidStructureException {
        this.entries = new TreeSet<>(entries);
        validate();
    }

    /*
     * Check if all pairs match.
     */
    private void validate() throws InvalidStructureException {
        if (Ct.LOGGER.isTraceEnabled()) {
            Ct.LOGGER.trace("CT to be validated:\n" + toString());
        }

        Map<Integer, Integer> map = new HashMap<>();

        for (Entry e : entries) {
            map.put(e.index, e.pair);
        }

        int previous = 0;

        for (Entry e : entries) {
            if (e.index - previous != 1) {
                throw new InvalidStructureException(
                        "Inconsistent numbering in CT format: previous="
                        + previous + ", current" + e.index);
            }

            previous = e.index;
            int pair = map.get(e.index);

            if (pair != 0) {
                if (!map.containsKey(pair)) {
                    throw new InvalidStructureException(
                            "Inconsistency in CT format: (" + e.index + " -> "
                            + pair + ")");
                }

                if (map.get(pair) != e.index) {
                    throw new InvalidStructureException(
                            "Inconsistency in CT format: (" + e.index + " -> "
                            + pair + ") and (" + pair + " -> " + map.get(pair)
                            + ")");
                }
            }
        }

        // previous == maximum index

        for (Entry e : entries) {
            if (e.before < 0 || e.before >= previous) {
                throw new InvalidStructureException(
                        "Inconsistency in CT format. Third column has invalid"
                        + " value in entry: " + e);
            }

            if (e.after == 1 || e.after < 0 || e.after > previous + 1) {
                throw new InvalidStructureException(
                        "Inconsistency in CT format. Fourth column has "
                        + "invalid value in entry: " + e);
            }
        }

        /*
         * Check if strands' ends are correct
         */
        boolean expectNewStrand = true;
        Entry prevEntry = null;

        for (Entry e : entries) {
            if (e.getBefore() != 0 && expectNewStrand
                || e.getBefore() == 0 && !expectNewStrand) {
                throw new InvalidStructureException(
                        "Inconsistency in CT format. The field 'before' is "
                        + "non-zero for the first entry in a strand: " + e);
            }

            if (prevEntry != null && (
                    prevEntry.getAfter() != 0 && expectNewStrand
                    || prevEntry.getAfter() == 0 && !expectNewStrand)) {
                throw new InvalidStructureException(
                        "Inconsistency in CT format. The field 'after' is "
                        + "non-zero for the last entry in a strand: "
                        + prevEntry);
            }

            expectNewStrand = e.getAfter() == 0;
            prevEntry = e;
        }

        Entry lastEntry = entries.last();

        if (lastEntry.after != 0) {
            if (Ct.FIX_LAST_ENTRY) {
                entries.remove(lastEntry);
                entries.add(new Entry(lastEntry.index, lastEntry.pair,
                                      lastEntry.before, 0, lastEntry.original,
                                      lastEntry.seq));
            } else {
                throw new InvalidStructureException(
                        "The field 'after' in the last entry is non-zero: "
                        + lastEntry);
            }
        }
    }

    public static Ct fromString(String data) throws InvalidStructureException {
        List<Entry> entries = new ArrayList<>();
        boolean firstLine = true;

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

            if (firstLine) {
                try {
                    int lineCount = Integer.parseInt(split[0]);
                    if (lineCount < 0) {
                        throw new InvalidStructureException(
                                "Invalid CT format. Line count < 0 detected: "
                                + line);
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidStructureException(
                            "Invalid CT format. Failed to parse line count: "
                            + line, e);
                }
                firstLine = false;
                continue;
            }

            if (split.length != 6) {
                throw new InvalidStructureException(
                        "Invalid CT format. Six columns not found in line: "
                        + line);
            }

            int index, pair, before, after, original;
            char seq;

            try {
                index = Integer.valueOf(split[0]);
                seq = split[1].charAt(0);
                before = Integer.valueOf(split[2]);
                after = Integer.valueOf(split[3]);
                pair = Integer.valueOf(split[4]);
                original = Integer.valueOf(split[5]);
            } catch (NumberFormatException e) {
                throw new InvalidStructureException(
                        "Invalid CT format. Failed to parse column values: "
                        + line, e);
            }

            entries.add(new Entry(index, pair, before, after, original, seq));
        }

        return new Ct(entries);
    }

    public static Ct fromBpSeq(BpSeq bpSeq) throws InvalidStructureException {
        List<Ct.Entry> ctEntries = new ArrayList<>();
        SortedSet<BpSeq.Entry> entries = bpSeq.getEntries();
        int size = entries.size();

        for (BpSeq.Entry entry : entries) {
            int index = entry.getIndex();
            int pair = entry.getPair();
            char seq = entry.getSeq();
            String comment = entry.getComment();
            ctEntries.add(new Ct.Entry(index, pair, index - 1,
                                       (index + 1) % (size + 1), index, seq,
                                       comment));
        }

        return new Ct(ctEntries);
    }

    public static Ct fromBpSeqAndPdbModel(BpSeq bpSeq, PdbModel model)
            throws InvalidStructureException {
        PdbModel rna;
        try {
            rna = model.filteredNewInstance(MoleculeType.RNA);
        } catch (PdbParsingException e) {
            throw new InvalidStructureException("Failed to filter RNA chains",
                                                e);
        }

        List<Ct.Entry> ctEntries = new ArrayList<>();
        List<PdbResidue> residues = rna.getResidues();
        SortedSet<BpSeq.Entry> entries = bpSeq.getEntries();
        int i = 0;

        for (BpSeq.Entry entry : entries) {
            PdbResidue residue = residues.get(i);
            PdbChain chain = rna.findChainContainingResidue(
                    residue.getResidueIdentifier());
            List<PdbResidue> chainResidues = chain.getResidues();

            int index = entry.getIndex();
            int pair = entry.getPair();
            int before = chainResidues.indexOf(residue);
            int after = (before + 2) % (chainResidues.size() + 1);
            int original = residue.getResidueNumber();
            char seq = entry.getSeq();
            String comment = entry.getComment();
            ctEntries
                    .add(new Ct.Entry(index, pair, before, after, original, seq,
                                      comment));

            i += 1;
        }

        return new Ct(ctEntries);
    }

    public static Ct fromDotBracket(DotBracket dotBracket)
            throws InvalidStructureException {
        List<Ct.Entry> entries = new ArrayList<>();

        for (Strand s : dotBracket.getStrands()) {
            for (int i = 0, j = s.getFrom(); j < s.getTo(); i++, j++) {
                DotBracketSymbol symbol = dotBracket.getSymbol(j);
                DotBracketSymbol pair = symbol.getPair();

                int index = symbol.getIndex() + 1;
                int pairIndex = pair != null ? pair.getIndex() + 1 : 0;
                int after = j == s.getTo() - 1 ? 0 : i + 2;
                int original = dotBracket.getCtOriginalColumn(symbol);
                char seq = symbol.getSequence();

                entries.add(new Ct.Entry(index, pairIndex, i, after, original,
                                         seq));
            }
        }

        return new Ct(entries);
    }

    public static void setPrintComments(boolean printComments) {
        Ct.printComments = printComments;
    }

    public int getStrandCount() {
        int count = 0;
        for (Ct.Entry entry : entries) {
            if (entry.getAfter() == 0) {
                count += 1;
            }
        }
        return count;
    }

    public SortedSet<Entry> getEntries() {
        return Collections.unmodifiableSortedSet(entries);
    }

    public static class Entry implements Serializable, Comparable<Ct.Entry> {
        private final int index;
        private final int pair;
        private final int before;
        private final int after;
        private final int original;
        private final char seq;
        private final String comment;

        public Entry(int index, int pair, int before, int after, int original,
                     char seq) {
            super();
            this.index = index;
            this.pair = pair;
            this.before = before;
            this.after = after;
            this.seq = seq;
            this.original = original;
            comment = "";
        }

        public Entry(int index, int pair, int before, int after, int original,
                     char seq, String comment) {
            super();
            this.index = index;
            this.pair = pair;
            this.before = before;
            this.after = after;
            this.seq = seq;
            this.original = original;
            this.comment = comment;
        }

        public int getIndex() {
            return index;
        }

        public int getPair() {
            return pair;
        }

        public int getBefore() {
            return before;
        }

        public int getAfter() {
            return after;
        }

        public int getOriginal() {
            return original;
        }

        public char getSeq() {
            return seq;
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
            result = prime * result + after;
            result = prime * result + before;
            result =
                    prime * result + (comment == null ? 0 : comment.hashCode());
            result = prime * result + index;
            result = prime * result + original;
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
            if (after != other.after) {
                return false;
            }
            if (before != other.before) {
                return false;
            }
            if (comment == null) {
                if (other.comment != null) {
                    return false;
                }
            } else if (!comment.equals(other.comment)) {
                return false;
            }
            return index == other.index && original == other.original
                   && pair == other.pair && seq == other.seq;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(index);
            builder.append(' ');
            builder.append(seq);
            builder.append(' ');
            builder.append(before);
            builder.append(' ');
            builder.append(after);
            builder.append(' ');
            builder.append(pair);
            builder.append(' ');
            builder.append(original);
            if (Ct.printComments && !StringUtils.isBlank(comment)) {
                builder.append(" # ");
                builder.append(comment);
            }
            return builder.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(entries.size());
        builder.append('\n');

        for (Entry e : entries) {
            builder.append(e.toString());
            builder.append('\n');
        }

        return builder.toString();
    }
}
