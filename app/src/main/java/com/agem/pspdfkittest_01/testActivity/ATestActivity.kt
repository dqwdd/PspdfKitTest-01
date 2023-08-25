package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.agem.pspdfkittest_01.R
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.PdfActivity

class ATestActivity : PdfActivity() {


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        // Custom action bar menu creation.
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onDocumentLoaded(document: PdfDocument) {
        // Document is ready to use.
    }

    override fun onDocumentLoadFailed(exception: Throwable) {
        // Add your fancy error handling here.
    }

    override fun onGenerateMenuItemIds(menuItems: MutableList<Int>): List<Int> {
        // For example, let's say we want to add custom menu items after the outline button.
        // First, we get an index of outline buttons (all default button IDs can be retrieved
        // via `MENU_OPTION_*` variables defined in the `PdfActivity`).
        val indexOfOutlineButton = menuItems.indexOf(PdfActivity.MENU_OPTION_OUTLINE)

        val customItems = ArrayList<Int>()
        customItems.add(R.string.grantAccess2)

        // Add a custom item after the outline button.
        menuItems.addAll(indexOfOutlineButton + 1, customItems)

        // Return the new menu items order.
        return menuItems
    }

    @SuppressLint("PrivateResource")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // This will populate menu with items ordered as specified in onGenerateMenuItemIds().
        super.onCreateOptionsMenu(menu)

        // Edit first button.
        val menuItem1 = menu.findItem(R.id.custom_action1)
        menuItem1.title = "Men Itm 1"
        menuItem1.setIcon(R.drawable.ic_arrow_left)

        // Let's say we want to tint icons same as the default ones). We can read the color
        // from the theme, or specify the same color we have in theme. Reading from theme is a bit
        // more complex but a better way to do it, so here's how to:
        val a = theme.obtainStyledAttributes(
            null,
            R.styleable.pspdf__ActionBarIcons,
            R.attr.pspdf__actionBarIconsStyle,
            R.style.PSPDFKit_ActionBarIcons
        )
        val mainToolbarIconsColor = a.getColor(R.styleable.pspdf__ActionBarIcons_pspdf__iconsColor, ContextCompat.getColor(this, R.color.white))
        a.recycle()

        // Tinting all custom menu drawables (you can do it the easier way if you iterate over your ids).
        val icon1 = menuItem1.icon
        icon1?.let { DrawableCompat.setTint(it, mainToolbarIconsColor) }
        menuItem1.icon = icon1

        val icon2 = menuItem2.icon
        icon2?.let { DrawableCompat.setTint(it, mainToolbarIconsColor) }
        menuItem2.icon = icon2

        val icon3 = menuItem3.icon
        icon3?.let { DrawableCompat.setTint(it, mainToolbarIconsColor) }
        menuItem3.icon = icon3

        // All our menu items are marked as SHOW_AS_ALWAYS. If you want to just show the first 4
        // items for example and send others to the overflow, you can simply do:
        for (i in 0 until menu.size()) {
            menu.getItem(i).setShowAsAction(if (i < 4) MenuItem.SHOW_AS_ACTION_ALWAYS else MenuItem.SHOW_AS_ACTION_NEVER)
        }

        return true
    }
}