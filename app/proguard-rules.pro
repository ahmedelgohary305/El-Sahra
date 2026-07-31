# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Moshi rules
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepattributes Signature,annotation
-dontwarn com.squareup.moshi.**

# Retrofit rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleAnnotations, RuntimeInvisibleParameterAnnotations
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Hilt/Dagger classes
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }
-dontwarn dagger.hilt.processor.**

# Keep models (data classes) used by Moshi
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.example.elsahra.data.model.** { *; }

# Preserve line numbers for stack traces in release
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile