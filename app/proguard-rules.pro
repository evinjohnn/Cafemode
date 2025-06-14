
# Add project specific ProGuard rules here.
-keep class com.example.cafemode.** { *; }
-keepclassmembers class * {
    native <methods>;
}

# Keep audio effect classes
-keep class android.media.audiofx.** { *; }