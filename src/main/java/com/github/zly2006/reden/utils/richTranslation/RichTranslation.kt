package com.github.zly2006.reden.utils.richTranslation

import com.github.zly2006.reden.access.TranslationStorageAccess
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent
import net.minecraft.text.Text
import net.minecraft.util.Language

/**
 * Note:
 * + out rich translation is inspired by owo lib, and we did not do some work to skip vanilla processing, thus, this
 *   function depends on owo, although it is entirely different form the owo method.
 * + not implemented all specifications, %s and %d etc are all accepted
 * + use %s for sequence format, and %1$s for index format
 *
 */
private enum class VisitMode {
    Index,
    Sequence,
    Unknown
}

private val indexFormatRegex = Regex("""%[0-9]+\$[a-zA-Z]""")
private val sequenceFormatRegex = Regex("""%[a-zA-Z]""")

private fun visitLiteral(text: MutableText, mode: VisitMode, args: MutableList<Any>): MutableText {
    val literal = (text.content as PlainTextContent.Literal).string
    var detectedMode = mode
    if (detectedMode == VisitMode.Unknown) detectedMode =
        if (indexFormatRegex.containsMatchIn(literal)) VisitMode.Index
        else if (sequenceFormatRegex.containsMatchIn(literal)) VisitMode.Sequence
        else VisitMode.Unknown
    return when (detectedMode) {
        VisitMode.Index -> {
            require(!sequenceFormatRegex.containsMatchIn(literal)) {
                "Cannot mix index and sequence format in the same text"
            }
            val strings = indexFormatRegex.split(literal)
            val matches = indexFormatRegex.findAll(literal).map {
                it.value.substringBefore("$").substringAfter("%").toIntOrNull()
            }
            val newText = Text.literal(strings[0]).setStyle(text.style)
            matches.forEachIndexed { index, i ->
                val value = i?.let { args.getOrNull(it - 1) } ?: ""
                newText.append(value.toString())
                newText.append(strings[index + 1])
            }
            newText
        }

        VisitMode.Sequence -> {
            require(!indexFormatRegex.containsMatchIn(literal)) {
                "Cannot mix index and sequence format in the same text"
            }
            val strings = sequenceFormatRegex.split(literal)
            val newText = Text.literal(strings[0]).setStyle(text.style)
            strings.drop(1).forEach {
                newText.append(args.removeFirst().toString())
                newText.append(it)
            }
            newText
        }

        VisitMode.Unknown -> Text.literal(literal).setStyle(text.style)
    }
}

private fun visitContent(text: MutableText, args: MutableList<Any>, mode: VisitMode): MutableText {
    val mutableText =
        if (text.content is PlainTextContent.Literal) visitLiteral(text, mode, args)
        else text.copy()
    text.siblings.forEach {
        mutableText.append(visitContent(it.copy(), args, mode))
    }
    return mutableText
}

fun processTranslate(language: Language, key: String, args: Array<Any>): Text? {
    val copy = (language as? TranslationStorageAccess?)?.textMap?.get(key)?.copy()
        ?: return null
    val argList = args.toMutableList()
    return visitContent(copy, argList, VisitMode.Unknown)
}
