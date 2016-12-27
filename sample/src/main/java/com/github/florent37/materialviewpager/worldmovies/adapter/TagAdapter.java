package com.github.florent37.materialviewpager.worldmovies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.florent37.materialviewpager.worldmovies.Config;
import com.github.florent37.materialviewpager.worldmovies.R;
import com.github.florent37.materialviewpager.worldmovies.model.TagFilterHolder;
import com.github.florent37.materialviewpager.worldmovies.model.TagMetadata;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionView;
import com.github.florent37.materialviewpager.worldmovies.ui.widget.CollectionViewCallbacks;
import com.github.florent37.materialviewpager.worldmovies.util.UIUtils;

import java.util.List;

import static com.github.florent37.materialviewpager.worldmovies.util.UIUtils.drawCountryFlag;

/**
 * Created by aaron on 2016/12/2.
 */

public class TagAdapter implements CollectionViewCallbacks {

    private TagMetadata mTagMetadata;
    private View.OnClickListener mDrawerItemCheckBoxClickListener;
    private TagFilterHolder mTagFilterHolder;
    private static final int GROUP_TOPIC_TYPE_OR_THEME = 0;
    private static final int GROUP_LIVE_STREAM = 1;
    private static final int GROUP_COUNTRY = 2;
    private static final int GROUP_LIVE_STREAM_2 = 3;

    public TagAdapter(TagMetadata tagMetadata, View.OnClickListener ItemCheckBoxClickListener, TagFilterHolder tagFilterHolder) {
        this.mTagMetadata = tagMetadata;
        this.mDrawerItemCheckBoxClickListener = ItemCheckBoxClickListener;
        this.mTagFilterHolder = tagFilterHolder;
    }

    public CollectionView.Inventory getInventory() {
        List<TagMetadata.Tag> themes = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_THEME);
        CollectionView.Inventory inventory = new CollectionView.Inventory();

        // We need to add the Live streamed section after the Type category
        CollectionView.InventoryGroup liveStreamGroup1 = new CollectionView.InventoryGroup(GROUP_LIVE_STREAM)
                .setDataIndexStart(0)
                .setShowHeader(false)
                .addItemWithTag("Livestreamed");

        inventory.addGroup(liveStreamGroup1);

        CollectionView.InventoryGroup countryGroup = new CollectionView.InventoryGroup(GROUP_COUNTRY)
                .setDataIndexStart(0)
                .setShowHeader(false);

        List<TagMetadata.Tag> countries = mTagMetadata.getTagsInCategory(Config.Tags.CATEGORY_COUNTRY);

        if (countries != null && countries.size() > 0) {
            for (TagMetadata.Tag country : countries) {
                countryGroup.addItemWithTag(country);
            }
            inventory.addGroup(countryGroup);
        }

        CollectionView.InventoryGroup liveStreamGroup2 = new CollectionView.InventoryGroup(GROUP_LIVE_STREAM_2)
                .setDataIndexStart(0)
                .setShowHeader(true)
                .addItemWithTag("Livestreamed");

        inventory.addGroup(liveStreamGroup2);

        CollectionView.InventoryGroup themeGroup = new CollectionView.InventoryGroup(GROUP_TOPIC_TYPE_OR_THEME)
                .setDisplayCols(0)
                .setDataIndexStart(0)
                .setShowHeader(false);

        if (themes != null && inventory != null) {
            for (TagMetadata.Tag theme : themes) {
                themeGroup.addItemWithTag(theme);
            }
            inventory.addGroup(themeGroup);
        }

        return inventory;
    }

    @Override
    public View newCollectionHeaderView(Context context, int groupId, ViewGroup parent) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.explore_sessions_list_item_alt_header, parent, false);
        // We do not want the divider/header to be read out by TalkBack, so
        // inform the view that this is not important for accessibility.
        UIUtils.setAccessibilityIgnore(view);
        return view;
    }

    @Override
    public void bindCollectionHeaderView(Context context, View view, int groupId, String headerLabel, Object headerTag) {
    }

    @Override
    public View newCollectionItemView(Context context, int groupId, ViewGroup parent) {
        if (groupId == GROUP_LIVE_STREAM)
            return LayoutInflater.from(context).inflate(R.layout.explore_sessions_list_item_livestream1_alt_drawer, parent, false);
        else if (groupId == GROUP_TOPIC_TYPE_OR_THEME)
            return LayoutInflater.from(context).inflate(R.layout.explore_sessions_list_item_alt_drawer, parent, false);
        else if (groupId == GROUP_LIVE_STREAM_2)
            return LayoutInflater.from(context).inflate(R.layout.explore_sessions_list_item_livestream2_alt_drawer, parent, false);
        else
            return LayoutInflater.from(context).inflate(R.layout.explore_sessions_list_item_alt_drawer, parent, false);
    }

    @Override
    public void bindCollectionItemView(Context context, View view, int groupId, int indexInGroup, int dataIndex, Object tag) {
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.filter_checkbox);
        if (groupId == GROUP_LIVE_STREAM || groupId == GROUP_LIVE_STREAM_2) {
            //Do nothing
        } else {
            TagMetadata.Tag theTag = (TagMetadata.Tag) tag;
            if (theTag != null && groupId == GROUP_TOPIC_TYPE_OR_THEME) {
                ((TextView) view.findViewById(R.id.text_view)).setText(theTag.getName());
                // set the original checked state by looking up our tags.
                checkBox.setChecked(mTagFilterHolder.contains(theTag.getId()));
                checkBox.setTag(theTag);
                checkBox.setOnClickListener(mDrawerItemCheckBoxClickListener);
            } else if (theTag != null && groupId == GROUP_COUNTRY) {
                ((TextView) view.findViewById(R.id.text_view)).setText(theTag.getName());
                drawCountryFlag(view, theTag.getOrderInCategory());
                // set the original checked state by looking up our tags.
                checkBox.setChecked(mTagFilterHolder.contains(theTag.getId()));
                checkBox.setTag(theTag);
                checkBox.setOnClickListener(mDrawerItemCheckBoxClickListener);
            }
            //TODO channel like ptt
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.performClick();
            }
        });
    }
}
