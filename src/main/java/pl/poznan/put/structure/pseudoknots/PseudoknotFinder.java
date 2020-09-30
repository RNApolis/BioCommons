package pl.poznan.put.structure.pseudoknots;

import pl.poznan.put.structure.formats.BpSeq;

import java.util.List;

/** Interface for classes which find pseudoknots from secondary structures. */
@FunctionalInterface
public interface PseudoknotFinder {
  /**
   * Find pairs in 'flat' BPSEQ information which are pseudoknots i.e. their removal will leave a
   * nested RNA structure. Potentially from a single BPSEQ, there can be many subsets of pairs
   * considered to be pseudoknots.
   *
   * @param bpSeq An input BPSEQ structure with all pairs.
   * @return A list of BPSEQ structures where each contains only pairs considered to be pseudoknots.
   *     Each BPSEQ is a full copy of original one, but contains zeroed 'pair' columns for entries
   *     which are non-pseudoknots.
   */
  List<BpSeq> findPseudoknots(BpSeq bpSeq);
}
