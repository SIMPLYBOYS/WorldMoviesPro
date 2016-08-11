package com.github.florent37.materialviewpager.sample.util;

import com.github.florent37.materialviewpager.sample.model.DemoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 2016/7/14.
 */
final public class BlockUtils {
    int currentOffset;

    public BlockUtils() {
    }

    public List<DemoItem> moarItems(int qty) {
        List<DemoItem> items = new ArrayList<>();

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
            DemoItem item = new DemoItem(colSpan, rowSpan, currentOffset + i);
            items.add(item);
        }

        currentOffset += qty;

        return items;
    }
}
