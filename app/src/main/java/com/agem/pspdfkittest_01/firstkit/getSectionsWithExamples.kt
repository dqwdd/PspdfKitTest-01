package com.agem.pspdfkittest_01.firstkit

import android.content.Context
import com.agem.pspdfkittest_01.ExternalDocumentExample
import com.agem.pspdfkittest_01.PSPDFExample
import com.agem.pspdfkittest_01.R

fun getSectionsWithExamples(context: Context) = listOf(

    PSPDFExample.Section(
        context.getString(R.string.example_section_opening_documents),
        R.drawable.ic_opening_documents,
        ExternalDocumentExample(context),
    )

)