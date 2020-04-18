# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn android.support.**

-keep class com.simplemvp.annotations.* { *; }
-keepnames public class com.simplemvp.common.* { *; }
-keepnames public class com.simplemvp.presenter.MvpPresenterManager {
  public *;
}
# keep presenter classes to trace errors
-keepclasseswithmembernames public class * extends com.simplemvp.presenter.MvpBasePresenter { *; }
# some methods of these classes can be invoked using reflection
-keepclassmembers public class * extends com.simplemvp.presenter.MvpBasePresenter {
  public <init>(android.content.Context, ***);
  @MvpHandler public void * (...);
}
# some methods of this class are invoked using reflection
-keepclassmembers class com.simplemvp.view.MvpEventHandler {
  @MvpHandler public void * (...);
}
-keepclasseswithmembernames public class * extends com.simplemvp.view.MvpActivity {*; }
-keepclasseswithmembernames public class * extends com.simplemvp.view.MvpFragment {*; }
-keepclasseswithmembernames public class * extends com.simplemvp.view.MvpDialogFragment {*; }