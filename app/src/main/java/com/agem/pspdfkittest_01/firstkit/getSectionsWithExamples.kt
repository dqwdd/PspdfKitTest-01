package com.agem.pspdfkittest_01.firstkit

import android.content.Context
import com.agem.pspdfkittest_01.R
import com.agem.pspdfkittest_01.basesupplies.PSPDFExample
import com.agem.pspdfkittest_01.main.ExternalDocumentExample

fun getSectionsWithExamples(context: Context) = listOf(
    PSPDFExample.Section(
        context.getString(R.string.example_section_opening_documents),
        R.drawable.ic_opening_documents,
        ExternalDocumentExample(context)
    )
)