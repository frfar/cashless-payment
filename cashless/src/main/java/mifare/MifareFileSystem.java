package mifare;

import java.util.Objects;

public class MifareFileSystem {

    private int section;
    private int block;

    private static final MifareFileSystem NULL = new MifareFileSystem(-1,-1);

    public MifareFileSystem(int section, int block) {
        this.section = section;
        this.block = block;
    }

    /**
     * converts the block index of a card into its block and section.
     * @param cardIndex
     * @return
     */
    public static MifareFileSystem fromCardindex(int cardIndex) {
        if(cardIndex > 63) {
            return NULL;
        }

        int section = cardIndex / 4;
        int block = cardIndex % 4;

        return new MifareFileSystem(section, block);
    }

    /**
     * The card has Access blocks at the end of each section and it can not be overwritten.
     * The file index is the index without these access blocks.
     * The file index start from the section 1. Here is a simple mapping between a card index and a file index.
     * File index - 0, card index - 4 // because we start the file from the section 1.
     * File index - 1, card index - 5
     * File index - 2, card index - 6
     * File index - 3, card index - 8 // because block 7 is the last block of the section and is an access block, which can not be overwritten.
     * @param fileIndex
     * @return
     */
    public static MifareFileSystem fromFileIndex(int fileIndex) {
        int cardIndex = (fileIndex / 3 * 4) + (fileIndex % 3) + 4;

        return fromCardindex(cardIndex);
    }

    public int getSection() {
        return section;
    }

    public int getBlock() {
        return block;
    }

    public boolean isNull() {
        return equals(NULL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MifareFileSystem that = (MifareFileSystem) o;
        return section == that.section &&
                block == that.block;
    }

    @Override
    public int hashCode() {
        return Objects.hash(section, block);
    }

    @Override
    public String toString() {
        return "MifareFileSystem{" +
                "section=" + section +
                ", block=" + block +
                '}';
    }
}
