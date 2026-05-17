# Supabase / Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.madebysai.bansagar.**$$serializer { *; }
-keepclassmembers class com.madebysai.bansagar.** { *** Companion; }
-keepclasseswithmembers class com.madebysai.bansagar.** { kotlinx.serialization.KSerializer serializer(...); }
