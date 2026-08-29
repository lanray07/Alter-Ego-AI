# Room, Retrofit, and Gson use annotations and generated adapters. Keep model names
# stable for the shared AI backend response contract.
-keep class com.alteregoai.app.data.** { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
