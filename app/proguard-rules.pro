# ProGuard / R8 rules for Detroit Red Wings Widget

# Keep Room entities and DAOs
-keep class com.redwings.widget.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Moshi JSON models
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep class com.redwings.widget.data.api.** { *; }

# Keep AppWidgetProvider and RemoteViews targets
-keep class com.redwings.widget.widget.RedWingsWidgetProvider { *; }
-keep class com.redwings.widget.MainActivity { *; }

# Keep Compose Preview parameters
-keepclassmembers class * implements androidx.compose.ui.tooling.preview.PreviewParameterProvider {
    <init>();
}
