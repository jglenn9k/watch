package com.jglenn9k.aviator.sensors.complications

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.wear.watchface.complications.data.ComplicationText
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

internal fun shortTextData(
    context: Context,
    title: String,
    text: String,
    description: String,
    @DrawableRes iconRes: Int,
    activity: Class<out Activity>,
): ShortTextComplicationData {
    val tapIntent = PendingIntent.getActivity(
        context,
        activity.name.hashCode(),
        Intent(context, activity).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    return ShortTextComplicationData.Builder(
        text = plainText(text),
        contentDescription = plainText(description),
    )
        .setTitle(plainText(title))
        .setMonochromaticImage(MonochromaticImage.Builder(Icon.createWithResource(context, iconRes)).build())
        .setTapAction(tapIntent)
        .build()
}

private fun plainText(value: String): ComplicationText = PlainComplicationText.Builder(value).build()

fun requestUpdate(context: Context, service: Class<*>) {
    ComplicationDataSourceUpdateRequester.create(context, ComponentName(context, service)).requestUpdateAll()
}
