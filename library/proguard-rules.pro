# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn android.support.**

-keep class com.simplemvp.annotations.** { *; }

-keep public class * extends com.simplemvp.view.MvpActivity