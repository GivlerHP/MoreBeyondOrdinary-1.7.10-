# Angelica в dev-среде MBO

Запуск из этой папки:

```bat
run-angelica-dev.bat
```

Для сравнения без Angelica:

```bat
run-angelica-dev.bat -PwithoutAngelica
```

Обычная Gradle-задача `runClient` тоже включает Angelica по умолчанию. Скрипт выбирает установленный JDK 8 и использует уже подготовленный офлайн-кэш ForgeGradle. Другой JDK 8 можно задать через `MBO_JAVA8_HOME`.

Среда использует `run/`, исходники MBO и Angelica 2.2.33-dev. Зависимости: GTNHLib 0.11.54-dev, UniMixins 0.3.1, CodeChickenCore 1.4.22-dev со встроенной CodeChickenLib, NEI 2.8.161-GTNH-dev. Dev-JAR скачаны из официальных GitHub Releases GTNewHorizons; UniMixins скопирован из тестовой сборки.

`prepareAngelicaDev` переводит существующие библиотеки из `libs/` в MCP-имена для старого ForgeGradle 1.2. Подготовленные копии лежат в `.angelica-dev/mapped-libs/`; исходные JAR остаются в `libs/`. UniMixins загружается первым, старые ChickenBones JAR исключаются из classpath запуска с Angelica.

Dev-запуск настроен на Java 8 / OpenGL LWJGL2. В Prism используется Java 21 / LWJGL3: это разные графические backend, поэтому совпадение поведения мерцания между ними ещё предстоит проверить. Многопоточная сборка чанков включена.

Старый OpenAL из Gradle natives падал в нативном коде при открытии устройства. В `.angelica-dev/natives/` подготовлена локальная копия natives с OpenAL Soft из официального LWJGL 3.3.3 (`lwjgl-openal-natives-windows-3.3.3.jar`). Gradle-кэш не менялся. Этот путь используется для обоих вариантов dev-запуска.

Лог проверочного запуска: `build/angelica-dev-launch7.log`. Мир автоматически не открывается.

Проверка завершена: Forge успешно загрузил 33 мода, включая MBO и Angelica; OpenAL инициализирован. Пользователь подтвердил успешный запуск. После закрытия клиента задача `runClient` завершилась с `BUILD SUCCESSFUL`.
