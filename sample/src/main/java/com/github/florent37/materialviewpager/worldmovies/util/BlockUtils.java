package com.github.florent37.materialviewpager.worldmovies.util;

import com.github.florent37.materialviewpager.worldmovies.model.BlockItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/7/14.
 */
final public class BlockUtils {
    int currentOffset;

    public BlockUtils() {
    }

    public List<BlockItem> moarItems(int qty) {
        List<BlockItem> items = new ArrayList<>();

        for (int i = 0; i < qty; i++) {
            int colSpan = Math.random() < 0.2f ? 2 : 1;
            // Swap the next 2 lines to have items with variable
            // column/row span.
            int rowSpan;

            if (colSpan == 1)
                rowSpan = colSpan;
            else
                rowSpan = Math.random() < 0.2f ? 2 : 1;
//            int rowSpan = colSpan;
            BlockItem item = new BlockItem(colSpan, rowSpan, currentOffset + i);
            items.add(item);
        }

        currentOffset += qty;

        return items;
    }
}
