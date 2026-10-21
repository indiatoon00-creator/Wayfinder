# Wayfinder Ring (Forge 1.20.1)

1. Install JDK 17.
2. Add the Gradle wrapper: copy `gradlew`, `gradlew.bat` and the `gradle/` folder from the official
   Forge 1.20.1 MDK into this folder, or run `gradle wrapper --gradle-version 8.1.1`.
3. Run `./gradlew build` (Windows: `gradlew.bat build`).
4. The jar is at `build/libs/wayfinder-1.0.0.jar` - upload that to CurseForge.

Optional: add your own icon at `src/main/resources/assets/wayfinder/textures/item/wayfinder_ring.png`
and change `layer0` in `models/item/wayfinder_ring.json` to `wayfinder:item/wayfinder_ring`.
