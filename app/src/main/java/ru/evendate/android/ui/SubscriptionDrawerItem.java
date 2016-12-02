package ru.evendate.android.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.utils.ViewHolderFactory;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.mikepenz.materialize.util.UIUtils;

import ru.evendate.android.R;

/**
 * Created by ds_gordeev on 04.03.2016.
 */
public class SubscriptionDrawerItem extends ProfileDrawerItem {
    protected StringHolder mBadge;
    protected BadgeStyle mBadgeStyle = new BadgeStyle();
    protected Pair<Integer, ColorStateList> colorStateList;
    protected ColorHolder selectedTextColor;
    protected ColorHolder disabledTextColor;

    public SubscriptionDrawerItem withBadge(StringHolder badge) {
        this.mBadge = badge;
        return this;
    }

    public SubscriptionDrawerItem withBadge(String badge) {
        this.mBadge = new StringHolder(badge);
        return this;
    }

    public SubscriptionDrawerItem withBadge(@StringRes int badgeRes) {
        this.mBadge = new StringHolder(badgeRes);
        return this;
    }

    public SubscriptionDrawerItem withBadgeStyle(BadgeStyle badgeStyle) {
        this.mBadgeStyle = badgeStyle;
        return this;
    }

    public SubscriptionDrawerItem withSelectedTextColor(@ColorInt int selectedTextColor) {
        this.selectedTextColor = ColorHolder.fromColor(selectedTextColor);
        return this;
    }

    public SubscriptionDrawerItem withSelectedTextColorRes(@ColorRes int selectedColorRes) {
        this.selectedTextColor = ColorHolder.fromColorRes(selectedColorRes);
        return this;
    }

    public StringHolder getBadge() {
        return mBadge;
    }

    public BadgeStyle getBadgeStyle() {
        return mBadgeStyle;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.drawer_item_subscription;
    }

    @Override
    public String getType() {
        return "SUB_ITEM";
    }

    @Override
    public void bindView(RecyclerView.ViewHolder holder) {
        Context ctx = holder.itemView.getContext();
        //get our viewHolder
        ViewHolder viewHolder = (ViewHolder)holder;

        //set the identifier from the drawerItem here. It can be used to run tests
        viewHolder.itemView.setId(getIdentifier());

        //set the item selected if it is
        viewHolder.itemView.setSelected(isSelected());

        //get the correct color for the background
        int selectedColor = ColorHolder.color(getSelectedColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_selected, com.mikepenz.materialdrawer.R.color.material_drawer_selected);
        //get the correct color for the text
        int color = ColorHolder.color(getTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_primary_text, com.mikepenz.materialdrawer.R.color.material_drawer_primary_text);

        UIUtils.setBackground(viewHolder.badgeContainer, DrawerUIUtils.getSelectableBackground(ctx, selectedColor));

        if (nameShown) {
            viewHolder.name.setVisibility(View.VISIBLE);
            StringHolder.applyTo(this.getName(), viewHolder.name);
        } else {
            viewHolder.name.setVisibility(View.GONE);
        }
        //the MaterialDrawer follows the Google Apps. those only show the e-mail
        //within the profile switcher. The problem this causes some confusion for
        //some developers. And if you only set the name, the item would be empty
        //so here's a small fallback which will prevent this issue of empty items ;)
        if (!nameShown && this.getEmail() == null && this.getName() != null) {
            StringHolder.applyTo(this.getName(), viewHolder.email);
        } else {
            StringHolder.applyTo(this.getEmail(), viewHolder.email);
        }

        if (getTypeface() != null) {
            viewHolder.name.setTypeface(getTypeface());
            viewHolder.email.setTypeface(getTypeface());
        }

        if (nameShown) {
            viewHolder.name.setTextColor(color);
        }
        viewHolder.email.setTextColor(color);

        //cancel previous started image loading processes
        DrawerImageLoader.getInstance().cancelImage(viewHolder.profileIcon);
        //set the icon
        ImageHolder.applyToOrSetInvisible(getIcon(), viewHolder.profileIcon, DrawerImageLoader.Tags.PROFILE_DRAWER_ITEM.name());

        //set the text for the badge or hide
        boolean badgeVisible = StringHolder.applyToOrHide(mBadge, viewHolder.badge);
        //style the badge if it is visible
        if (badgeVisible) {
            mBadgeStyle.style(viewHolder.badge, getTextColorStateList(getColor(ctx), getSelectedTextColor(ctx)));
            viewHolder.badgeContainer.setVisibility(View.VISIBLE);
        } else {
            viewHolder.badgeContainer.setVisibility(View.GONE);
        }

        //define the typeface for our textViews
        if (getTypeface() != null) {
            viewHolder.badge.setTypeface(getTypeface());
        }

        //call the onPostBindView method to trigger post bind view actions (like the listener to modify the item if required)
        onPostBindView(this, holder.itemView);
    }

    @Override
    public ViewHolderFactory getFactory() {
        return new ItemFactory();
    }

    public static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder factory(View v) {
            return new ViewHolder(v);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private View badgeContainer;
        private ImageView profileIcon;
        private TextView name;
        private TextView email;
        private TextView badge;

        public ViewHolder(View view) {
            super(view);
            this.badgeContainer = view.findViewById(com.mikepenz.materialdrawer.R.id.material_drawer_badge_container);
            this.profileIcon = (ImageView)view.findViewById(com.mikepenz.materialdrawer.R.id.material_drawer_profileIcon);
            this.name = (TextView)view.findViewById(com.mikepenz.materialdrawer.R.id.material_drawer_name);
            this.email = (TextView)view.findViewById(com.mikepenz.materialdrawer.R.id.material_drawer_email);
            this.badge = (TextView)view.findViewById(com.mikepenz.materialdrawer.R.id.material_drawer_badge);
        }
    }

    private ColorStateList getTextColorStateList(@ColorInt int color, @ColorInt int selectedTextColor) {
        if (colorStateList == null || color + selectedTextColor != colorStateList.first) {
            colorStateList = new Pair<>(color + selectedTextColor, DrawerUIUtils.getTextColorStateList(color, selectedTextColor));
        }

        return colorStateList.second;
    }

    protected int getColor(Context ctx) {
        int color;
        if (this.isEnabled()) {
            color = ColorHolder.color(getTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_primary_text, com.mikepenz.materialdrawer.R.color.material_drawer_primary_text);
        } else {
            color = ColorHolder.color(getDisabledTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_hint_text, com.mikepenz.materialdrawer.R.color.material_drawer_hint_text);
        }
        return color;
    }

    protected int getSelectedTextColor(Context ctx) {
        return ColorHolder.color(getSelectedTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_selected_text, com.mikepenz.materialdrawer.R.color.material_drawer_selected_text);
    }

    public ColorHolder getTextColor() {
        return textColor;
    }

    public ColorHolder getSelectedTextColor() {
        return selectedTextColor;
    }

    public ColorHolder getDisabledTextColor() {
        return disabledTextColor;
    }

}
