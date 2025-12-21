package com.saicon.games.callcard.components.util;

import com.saicon.games.callcard.entity.CallCardTemplateEntry;
import java.util.Comparator;

public class CallCardTemplateEntryComparator implements Comparator<CallCardTemplateEntry> {

    @Override
    public int compare(CallCardTemplateEntry o1, CallCardTemplateEntry o2) {
        if (o1 == null || o2 == null) {
            return 0;
        }

        Integer order1 = o1.getOrdering();
        Integer order2 = o2.getOrdering();

        if (order1 == null) {
            order1 = 0;
        }
        if (order2 == null) {
            order2 = 0;
        }

        return order1.compareTo(order2);
    }
}
