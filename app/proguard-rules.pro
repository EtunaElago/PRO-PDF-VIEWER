# Add your project specific ProGuard rules here.

# Hilt
-keep class com.PRO.propdf.Hilt_* { *; }

# Room
-keep class * extends androidx.room.RoomDatabase {
    public static <methods>;
}

# Keep data classes used with Room
-keepclassmembers class com.PRO.propdf.data.entities.** {
    *;
}

# Navigation
-keep class androidx.navigation.** { *; }

# Material Components
-keep class com.google.android.material.** { *; }
-keep class androidx.recyclerview.widget.** { *; }

# Google Ads
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# PDF Viewer
-keep class com.github.barteksc.pdfviewer.** { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keep class kotlin.reflect.jvm.internal.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Lifecycle
-keep class androidx.lifecycle.ViewModelProvider$Factory { *; }

# Data classes (if you use reflection)
-keepclassmembers class * {
    public <init>();
}

# Serializable classes
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Retrofit (if added later)
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp (if added later)
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# GSON (if added later)
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Keep application class
-keep public class com.PRO.propdf.MainApplication

# Keep entry points
-keep class androidx.startup.InitializationProvider
-keep class androidx.startup.AppInitializer { *; }