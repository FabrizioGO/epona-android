# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# --- Epona release rules -------------------------------------------------------

# Keep line numbers in crash reports; the mapping file deobfuscates the rest.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization ships its own R8 rules for @Serializable classes. These keep
# the DTOs' generated serializers reachable even when only referenced reflectively
# by supabase-kt's decodeList<T>() / decodeSingle<T>().
-keepattributes *Annotation*, InnerClasses, Signature
-keep,includedescriptorclasses class com.fabriziogo.epona.core.network.dto.**$$serializer { *; }
-keepclassmembers class com.fabriziogo.epona.core.network.dto.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor finds its HTTP engine through ServiceLoader when supabase-kt builds the client
# without an explicit engine.
-keep class io.ktor.client.engine.android.AndroidEngineContainer { *; }
-keep class * implements io.ktor.client.HttpClientEngineContainer { *; }

# Ktor references JVM-only classes that do not exist on Android.
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**
