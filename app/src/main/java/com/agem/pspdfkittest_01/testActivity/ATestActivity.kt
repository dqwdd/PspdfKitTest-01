package com.agem.pspdfkittest_01.testActivity

import android.annotation.SuppressLint
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.agem.pspdfkittest_01.R
import com.pspdfkit.annotations.Annotation
import com.pspdfkit.annotations.AnnotationType
import com.pspdfkit.document.PdfDocument
import com.pspdfkit.ui.PdfActivity
import io.reactivex.rxjava3.disposables.Disposable
import java.util.*
import kotlin.math.min

/**
 * 이 Activity 는 그냥 PDF 나오게 한 Activity
 * */

class ATestActivity : PdfActivity() {

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        // Custom action bar menu creation.
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onDocumentLoaded(document: PdfDocument) {
        Log.e("tetest", "65555555")
        // Document is ready to use.
        annotationLoadingDisposable = document.annotationProvider
            .getAllAnnotationsOfTypeAsync(EnumSet.allOf(AnnotationType::class.java))
            .toList()
            .subscribe { annotations -> documentAnnotations.addAll(annotations) }
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
        customItems.add(R.id.custom_my_id_1)

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
        val menuItem1 = menu.findItem(R.id.custom_my_id_1)
        menuItem1.title = "Men Itm 1"
        menuItem1.setIcon(R.drawable.ic_launcher_background)

        // Tinting all custom menu drawables (you can do it the easier way if you iterate over your ids).
        val icon1 = menuItem1.icon
//        icon1?.let { DrawableCompat.setTint(it, mainToolbarIconsColor) }
        menuItem1.icon = icon1

        // All our menu items are marked as SHOW_AS_ALWAYS. If you want to just show the first 4
        // items for example and send others to the overflow, you can simply do:
        for (i in 0 until menu.size()) {
            menu.getItem(i).setShowAsAction(if (i < 4) MenuItem.SHOW_AS_ACTION_ALWAYS else MenuItem.SHOW_AS_ACTION_NEVER)
        }

        return true
    }

    /**
     * Override onOptionsItemSelected(MenuItem) to handle click events for your custom menu items.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val handled = when (item.itemId) {
            R.id.custom_my_id_1 -> {
                Toast.makeText(this, "Selected custom_my_id_101", Toast.LENGTH_SHORT).show()
                getAnnotationSize()
                true
            }

            else -> {
                false
            }
        }

        // Return true if you have handled the current event. If your code has not handled the event,
        // pass it on to the superclass. This is important or standard PSPDFKit actions won't work.
        return handled || super.onOptionsItemSelected(item)
    }



    private val documentAnnotations = mutableListOf<Annotation>()

    /** This holds reference to the currently zoomed annotation. */
    private var currentAnnotation: Annotation? = null

    private var annotationLoadingDisposable: Disposable? = null

    private fun getAnnotationSize() {
        Log.e("tetest", "getAnnotationSize")
        Log.e("tetest", "documentAnnotations.isEmpty() == ${documentAnnotations.isEmpty()}")

        if (documentAnnotations.isEmpty()) return

        var currentAnnotation = this.currentAnnotation
        val currentAnnotationIndex = if (currentAnnotation == null) {
            -1
        } else {
            documentAnnotations.indexOf(currentAnnotation)
        }
        val nextAnnotationIndex = min(currentAnnotationIndex + 1, documentAnnotations.size - 1)

        if (nextAnnotationIndex != currentAnnotationIndex) {
            currentAnnotation = documentAnnotations[nextAnnotationIndex]
            this.currentAnnotation = currentAnnotation

            val boundingBox = currentAnnotation.boundingBox
            boundingBox.inset(-ANNOTATION_BOUNDING_BOX_PADDING_PX.toFloat(), -ANNOTATION_BOUNDING_BOX_PADDING_PX.toFloat())

            Log.e("tetest", "366666")
            Log.e("tetest", "boundingBox === $boundingBox")
        }

        Log.e("tetest", "21512")
        Log.e("tetest", "currentAnnotation.boundingBox === ${currentAnnotation?.boundingBox}")
    }

    companion object {
        /** Padding around annotation bounding box used when zooming. */
        const val ANNOTATION_BOUNDING_BOX_PADDING_PX = 16
    }
}