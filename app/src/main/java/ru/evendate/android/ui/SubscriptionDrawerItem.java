package ru.evendate.android.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.ColorHolder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.AbstractDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.mikepenz.materialize.util.UIUtils;

import java.util.List;

import ru.evendate.android.R;

/**
 * Custom implementation of drawer item for subscription with big icon
 */
public class SubscriptionDrawerItem extends AbstractDrawerItem<SubscriptionDrawerItem, SubscriptionDrawerItem.ViewHolder> {
    protected boolean nameShown = false;

    protected ImageHolder icon;

    protected StringHolder name;
    protected StringHolder email;

    protected ColorHolder selectedColor;
    protected ColorHolder textColor;
    protected ColorHolder selectedTextColor;
    protected ColorHolder disabledTextColor;

    protected Typeface typeface = null;
    protected StringHolder mBadge;
    protected BadgeStyle mBadgeStyle = new BadgeStyle();


    public SubscriptionDrawerItem withIcon(Drawable icon) {
        this.icon = new ImageHolder(icon);
        return this;
    }

    public SubscriptionDrawerItem withIcon(@DrawableRes int iconRes) {
        this.icon = new ImageHolder(iconRes);
        return this;
    }

    public SubscriptionDrawerItem withIcon(Bitmap iconBitmap) {
        this.icon = new ImageHolder(iconBitmap);
        return this;
    }

    public SubscriptionDrawerItem withIcon(IIcon icon) {
        this.icon = new ImageHolder(icon);
        return this;
    }

    public SubscriptionDrawerItem withIcon(String url) {
        this.icon = new ImageHolder(url);
        return this;
    }

    public SubscriptionDrawerItem withIcon(Uri uri) {
        this.icon = new ImageHolder(uri);
        return this;
    }

    public SubscriptionDrawerItem withName(String name) {
        this.name = new StringHolder(name);
        return this;
    }

    public SubscriptionDrawerItem withName(@StringRes int nameRes) {
        this.name = new StringHolder(nameRes);
        return this;
    }

    public SubscriptionDrawerItem withEmail(String email) {
        this.email = new StringHolder(email);
        return this;
    }

    public SubscriptionDrawerItem withEmail(@StringRes int emailRes) {
        this.email = new StringHolder(emailRes);
        return this;
    }

    /**
     * Whether to show the profile name in the account switcher.
     *
     * @param nameShown show name in switcher
     * @return the {@link ProfileDrawerItem}
     */
    public SubscriptionDrawerItem withNameShown(boolean nameShown) {
        this.nameShown = nameShown;
        return this;
    }

    public SubscriptionDrawerItem withSelectedColor(@ColorInt int selectedColor) {
        this.selectedColor = ColorHolder.fromColor(selectedColor);
        return this;
    }

    public SubscriptionDrawerItem withSelectedColorRes(@ColorRes int selectedColorRes) {
        this.selectedColor = ColorHolder.fromColorRes(selectedColorRes);
        return this;
    }

    public SubscriptionDrawerItem withTextColor(@ColorInt int textColor) {
        this.textColor = ColorHolder.fromColor(textColor);
        return this;
    }

    public SubscriptionDrawerItem withTextColorRes(@ColorRes int textColorRes) {
        this.textColor = ColorHolder.fromColorRes(textColorRes);
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

    public SubscriptionDrawerItem withDisabledTextColor(@ColorInt int disabledTextColor) {
        this.disabledTextColor = ColorHolder.fromColor(disabledTextColor);
        return this;
    }

    public SubscriptionDrawerItem withDisabledTextColorRes(@ColorRes int disabledTextColorRes) {
        this.disabledTextColor = ColorHolder.fromColorRes(disabledTextColorRes);
        return this;
    }

    public SubscriptionDrawerItem withTypeface(Typeface typeface) {
        this.typeface = typeface;
        return this;
    }

    public boolean isNameShown() {
        return nameShown;
    }

    public ColorHolder getSelectedColor() {
        return selectedColor;
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

    public Typeface getTypeface() {
        return typeface;
    }

    public ImageHolder getIcon() {
        return icon;
    }

    public StringHolder getName() {
        return name;
    }

    public StringHolder getEmail() {
        return email;
    }


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

    public StringHolder getBadge() {
        return mBadge;
    }

    public BadgeStyle getBadgeStyle() {
        return mBadgeStyle;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    @LayoutRes
    public int getLayoutRes() {
        return R.layout.drawer_item_subscription;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);
        Context ctx = viewHolder.itemView.getContext();

        //set the identifier from the drawerItem here. It can be used to run tests
        viewHolder.itemView.setId((int)getIdentifier());

        //set the item selected if it is
        viewHolder.itemView.setSelected(isSelected());

        //get the correct color for the background
        int selectedColor = ColorHolder.color(getSelectedColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_selected, com.mikepenz.materialdrawer.R.color.material_drawer_selected);
        //get the correct color for the text
        int color = ColorHolder.color(getTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_primary_text, com.mikepenz.materialdrawer.R.color.material_drawer_primary_text);

        UIUtils.setBackground(viewHolder.badgeContainer, UIUtils.getSelectableBackground(ctx, selectedColor, isSelectedBackgroundAnimated()));

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
        onPostBindView(this, viewHolder.itemView);
    }


    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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


    /**
     * helper method to decide for the correct color
     *
     * @param ctx
     * @return
     */
    protected int getColor(Context ctx) {
        int color;
        if (this.isEnabled()) {
            color = ColorHolder.color(getTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_primary_text, com.mikepenz.materialdrawer.R.color.material_drawer_primary_text);
        } else {
            color = ColorHolder.color(getDisabledTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_hint_text, com.mikepenz.materialdrawer.R.color.material_drawer_hint_text);
        }
        return color;
    }

    /**
     * helper method to decide for the correct color
     *
     * @param ctx
     * @return
     */
    protected int getSelectedTextColor(Context ctx) {
        return ColorHolder.color(getSelectedTextColor(), ctx, com.mikepenz.materialdrawer.R.attr.material_drawer_selected_text, com.mikepenz.materialdrawer.R.color.material_drawer_selected_text);
    }

    protected Pair<Integer, ColorStateList> colorStateList;

    /**
     * helper to get the ColorStateList for the text and remembering it so we do not have to recreate it all the time
     *
     * @param color
     * @param selectedTextColor
     * @return
     */
    protected ColorStateList getTextColorStateList(@ColorInt int color, @ColorInt int selectedTextColor) {
        if (colorStateList == null || color + selectedTextColor != colorStateList.first) {
            colorStateList = new Pair<>(color + selectedTextColor, DrawerUIUtils.getTextColorStateList(color, selectedTextColor));
        }

        return colorStateList.second;
    }
}
