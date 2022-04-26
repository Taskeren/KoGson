# KoGson

An extension of Gson in Kotlin.

## TheMagicGson

All the objects passed to KoGson is actually serialized by the Gson located in `KoGson.kt`. If you want to register
TypeAdaptor or TypeAdaptorFactory, you can use `KoGson.updateMagicGson` function.

## Use & License

With JitPack repository:

`implementation("com.github.Taskeren:KoGson:1.0.1")`

This project is licensed under WTFPL, feel free to do anything.