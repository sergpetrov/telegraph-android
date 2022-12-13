-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-renamesourcefileattribute SourceFile

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ===== Retrofit =====

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature,InnerClasses,EnclosingMethod,Exceptions
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions


# ===== Gson =====

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.telex.base.model.source.remote.data.** { *; }
-keep class com.telex.base.model.source.local.** { *; }
# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer


# ===== Okhttp3 =====

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform


# ===== Moxy =====

-keep class **$$PresentersBinder
-keep class **$$State
-keep class **$$ParamsHolder
-keep class **$$ViewStateClassNameProvider
-keepnames class * extends moxy.*


# ===== Firebase =====

-keep class com.google.firebase.** { *; }


# ===== Toothpick =====

# Note that if we could use kapt to generate registries, possible to get rid of this
-keepattributes *Annotation*
# Do not obfuscate classes with Injected Constructors
-keepclasseswithmembernames class * { @javax.inject.Inject <init>(...); }
# Do not obfuscate classes with Injected Fields
-keepclasseswithmembernames class * { @javax.inject.Inject <fields>; }
# Do not obfuscate classes with Injected Methods
-keepclasseswithmembernames class * { @javax.inject.Inject <methods>; }
-keep @android.support.annotation.Keep class *
-dontwarn javax.inject.**
-dontwarn javax.annotation.**
-keep class **__Factory { *; }
-keep class **__MemberInjector { *; }

-adaptclassstrings
-keep class toothpick.** { *; }

-keep @javax.inject.Singleton class *

-keepnames class * { @javax.inject.Inject <init>(...); }

-keepclassmembers enum * { *; }

# ===== Aztec =====

-keep class org.ccil.cowan.tagsoup.** { *; }



