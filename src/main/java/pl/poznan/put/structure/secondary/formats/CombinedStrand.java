package pl.poznan.put.structure.secondary.formats;

import lombok.EqualsAndHashCode;
import pl.poznan.put.structure.secondary.DotBracketSymbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode
public class CombinedStrand implements DotBracketInterface {
  protected final List<Strand> strands = new ArrayList<>();
  protected final List<DotBracketSymbol> symbols = new ArrayList<>();

  public CombinedStrand(final Iterable<? extends Strand> strands) {
    super();

    final Map<DotBracketSymbol, Integer> symbolToIndex = new HashMap<>();
    int i = 0;
    for (final Strand strand : strands) {
      for (final DotBracketSymbol symbol : strand.getSymbols()) {
        symbolToIndex.put(symbol, i);
        i += 1;
      }
    }

    for (final Strand strand : strands) {
      final List<DotBracketSymbol> strandSymbols = new ArrayList<>();
      for (final DotBracketSymbol symbol : strand.getSymbols()) {
        final char sequence = symbol.getSequence();
        final char structure = symbol.getStructure();
        final int index = symbolToIndex.get(symbol);
        final DotBracketSymbol renumbered = new DotBracketSymbol(sequence, structure, index);
        strandSymbols.add(renumbered);
        symbols.add(renumbered);
      }
      this.strands.add(new StrandDirect(strand.getName(), strandSymbols));
    }

    for (final Strand strand : strands) {
      for (final DotBracketSymbol symbol : strand.getSymbols()) {
        if (symbol.isPairing()) {
          final DotBracketSymbol u = symbols.get(symbolToIndex.get(symbol));
          final DotBracketSymbol v = symbols.get(symbolToIndex.get(symbol.getPair()));
          u.setPair(v);
          v.setPair(u);
        }
      }
    }
  }

  CombinedStrand() {
    super();
  }

  public final List<Strand> getStrands() {
    return Collections.unmodifiableList(strands);
  }

  public final int getLength() {
    return strands.stream().mapToInt(Strand::getLength).sum();
  }

  @Override
  public final List<DotBracketSymbol> getSymbols() {
    return Collections.unmodifiableList(symbols);
  }

  @Override
  public final DotBracketSymbol getSymbol(final int index) {
    return getSymbols().get(index);
  }

  public final Iterable<TerminalMissing> getTerminalMissing() {
    final Collection<TerminalMissing> result = new ArrayList<>();
    for (final Strand strand : strands) {
      result.add(strand.getMissingBegin());
      result.add(strand.getMissingEnd());
    }
    return result;
  }

  public final List<DotBracketSymbol> getInternalMissing() {
    // collect all missing from beginning and ends of strands
    final Set<DotBracketSymbol> missingNonInternal =
        strands.stream()
            .flatMap(
                strand ->
                    Stream.concat(
                        strand.getMissingBegin().getSymbols().stream(),
                        strand.getMissingEnd().getSymbols().stream()))
            .collect(Collectors.toSet());

    // get all missing symbols which are internal
    return strands.stream()
        .flatMap(strand -> strand.getSymbols().stream())
        .filter(dotBracketSymbol -> !missingNonInternal.contains(dotBracketSymbol))
        .filter(DotBracketSymbol::isMissing)
        .collect(Collectors.toList());
  }

  public final int getPseudoknotOrder() {
    return strands.stream()
        .max(Comparator.comparingInt(Strand::getPseudoknotOrder))
        .map(Strand::getPseudoknotOrder)
        .orElse(0);
  }

  public final boolean contains(final DotBracketSymbol symbol) {
    return strands.stream().anyMatch(strand -> strand.getSymbols().contains(symbol));
  }

  @Override
  public final String toString() {
    final String builder = strands.stream().map(Strand::getName).collect(Collectors.joining());

    return ">strand_" + builder + '\n' + getSequence(false) + '\n' + getStructure(false);
  }

  @Override
  public final String toStringWithStrands() {
    return strands.stream()
        .map(strand -> String.valueOf(strand) + '\n')
        .collect(Collectors.joining());
  }

  @Override
  public final List<? extends CombinedStrand> combineStrands() {
    return Collections.singletonList(this);
  }

  @Override
  public int getRealSymbolIndex(final DotBracketSymbol symbol) {
    return symbol.getIndex() + 1;
  }

  private String getSequence(final boolean separateStrands) {
    final StringBuilder builder = new StringBuilder();
    for (final Strand strand : strands) {
      builder.append(strand.getSequence());
      if (separateStrands) {
        builder.append('&');
      }
    }
    return builder.toString();
  }

  private String getStructure(final boolean separateStrands) {
    final StringBuilder builder = new StringBuilder();
    for (final Strand strand : strands) {
      builder.append(strand.getStructure());
      if (separateStrands) {
        builder.append('&');
      }
    }
    return builder.toString();
  }

  @Override
  public final String getSequence() {
    return getSequence(false);
  }

  @Override
  public final String getStructure() {
    return getStructure(false);
  }

  /**
   * Check if the strand is invalid i.e. if it contains ONLY dots and minuses (no base-pairs).
   *
   * @return True if the strand contains only dots or minuses.
   */
  public final boolean isInvalid() {
    for (final Strand strand : strands) {
      for (final char c : strand.getStructure().toCharArray()) {
        if ((c != '.') && (c != '-')) {
          return false;
        }
      }
    }

    return true;
  }

  public final int indexOfSymbol(final DotBracketSymbol symbol) {
    int baseIndex = 0;
    for (final Strand strand : strands) {
      if (strand.getSymbols().contains(symbol)) {
        return baseIndex + strand.getSymbols().indexOf(symbol);
      }
      baseIndex += strand.getLength();
    }
    throw new IllegalArgumentException("Failed to find symbol " + symbol + " in strands:\n" + this);
  }

  public final Strand getStrand(final DotBracketSymbol symbol) {
    for (final Strand strand : strands) {
      if (strand.getSymbols().contains(symbol)) {
        return strand;
      }
    }
    throw new IllegalArgumentException("Failed to find strand containing symbol: " + symbol);
  }
}
