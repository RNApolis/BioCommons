package pl.poznan.put.pdb;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class PdbRemark465Line implements ChainNumberICode {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdbRemark465Line.class);
    // @formatter:off
    /*
        REMARK 465                                                                       
        REMARK 465 MISSING  RESIDUES                                                     
        REMARK 465 THE FOLLOWING  RESIDUES WERE NOT LOCATED IN THE                       
        REMARK 465 EXPERIMENT.  (M=MODEL NUMBER; RES=RESIDUE NAME; C=CHAIN               
        REMARK 465 IDENTIFIER;  SSSEQ=SEQUENCE NUMBER; I=INSERTION CODE.)                
        REMARK 465                                                                      
        REMARK 465   M RES C SSSEQI                                                     
        REMARK 465     ARG A    46                                                      
        REMARK 465     GLY A    47                                                      
        REMARK 465     ALA A    48                                                      
        REMARK 465     ARG A    49                                                      
        REMARK 465     MET A    50
        
        REMARK 465                                                                      
        REMARK 465 MISSING RESIDUES
        REMARK 465 THE FOLLOWING RESIDUES WERE NOT LOCATED IN THE
        REMARK 465 EXPERIMENT. (RES=RESIDUE NAME; C=CHAIN IDENTIFIER;
        REMARK 465 SSSEQ=SEQUENCE NUMBER; I=INSERTION CODE.)
        REMARK 465   MODELS 1-20
        REMARK 465     RES C SSSEQI
        REMARK 465     MET A     1
        REMARK 465     GLY A     2
     */

    private static final String[] COMMENT_LINES = new String[]{
            "REMARK 465",
            "REMARK 465 MISSING RESIDUES",
            "REMARK 465 THE FOLLOWING RESIDUES WERE NOT LOCATED IN THE",
            "REMARK 465 EXPERIMENT. (M=MODEL NUMBER; RES=RESIDUE NAME; C=CHAIN",
            "REMARK 465 IDENTIFIER; SSSEQ=SEQUENCE NUMBER; I=INSERTION CODE.)",
            "REMARK 465 M RES C SSSEQI",
            "REMARK 465 EXPERIMENT. (RES=RESIDUE NAME; C=CHAIN IDENTIFIER;",
            "REMARK 465 SSSEQ=SEQUENCE NUMBER; I=INSERTION CODE.)",
            "REMARK 465 RES C SSSEQI"
    };
    // @formatter:on
    private static final String REMARK_FORMAT = "  %1s %3s %c %5d%c                                                     ";
    private static final String FORMAT = "REMARK 465 " + PdbRemark465Line.REMARK_FORMAT;

    public static boolean isCommentLine(String line) {
        String lineTrimmed = StringUtils.normalizeSpace(line);

        for (String comment : PdbRemark465Line.COMMENT_LINES) {
            if (lineTrimmed.equals(StringUtils.normalizeSpace(comment))) {
                return true;
            }
        }

        return lineTrimmed.startsWith("REMARK 465   MODELS");
    }

    public static PdbRemark465Line parse(String line) throws PdbParsingException {
        if (line.length() < 79) {
            throw new PdbParsingException("PDB REMARK line is not at least 79 character long");
        }

        try {
            String recordName = line.substring(0, 6).trim();
            int remarkNumber = Integer.parseInt(line.substring(7, 10).trim());

            if (!"REMARK".equals(recordName)) {
                throw new PdbParsingException("PDB line does not start with REMARK");
            }
            if (remarkNumber != 465) {
                throw new PdbParsingException("Unsupported REMARK line occurred");
            }

            String remarkContent = StringUtils.stripEnd(line.substring(11, 79), null);
            int modelNumber = remarkContent.charAt(2) == ' ' ? 0 : Integer.parseInt(remarkContent.substring(2, 3));
            String residueName = remarkContent.substring(4, 7).trim();
            String chainIdentifier = Character.toString(remarkContent.charAt(8));
            int residueNumber = Integer.parseInt(remarkContent.substring(10, 15).trim());
            String insertionCode = remarkContent.length() == 15 ? " " : Character.toString(remarkContent.charAt(15));
            return new PdbRemark465Line(modelNumber, residueName, chainIdentifier, residueNumber, insertionCode);
        } catch (NumberFormatException e) {
            throw new PdbParsingException("Failed to parse PDB REMARK 465 line", e);
        }
    }

    private final int modelNumber;
    private final String residueName;
    private final String chainIdentifier;
    private final int residueNumber;
    private final String insertionCode;

    public PdbRemark465Line(int modelNumber, String residueName, String chainIdentifier, int residueNumber, String insertionCode) {
        this.modelNumber = modelNumber;
        this.residueName = residueName;
        this.chainIdentifier = chainIdentifier;
        this.residueNumber = residueNumber;
        this.insertionCode = insertionCode;
    }

    public int getModelNumber() {
        return modelNumber;
    }

    public String getResidueName() {
        return residueName;
    }

    @Override
    public String getChainIdentifier() {
        return chainIdentifier;
    }

    @Override
    public int getResidueNumber() {
        return residueNumber;
    }

    @Override
    public String getInsertionCode() {
        return insertionCode;
    }

    @Override
    public String toString() {
        if (chainIdentifier.length() != 1) {
            PdbRemark465Line.LOGGER.error("Field 'chainIdentifier' is longer than 1 char. Only first letter will be taken");
        }
        if (insertionCode.length() != 1) {
            PdbRemark465Line.LOGGER.error("Field 'insertionCode' is longer than 1 char. Only first letter will be taken");
        }
        return String.format(Locale.US, PdbRemark465Line.FORMAT, modelNumber == 0 ? " " : String.valueOf(modelNumber), residueName, chainIdentifier.charAt(0), residueNumber, insertionCode.charAt(0));
    }

    @Override
    public PdbResidueIdentifier getResidueIdentifier() {
        return new PdbResidueIdentifier(chainIdentifier, residueNumber, insertionCode);
    }
}
