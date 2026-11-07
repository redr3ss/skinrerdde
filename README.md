# SkinAll (Fabric, Minecraft 1.21.11)

Клиентский мод: подменяет скин ВСЕМ игрокам (и по желанию вам) на выбранный вами.
Подмена происходит только у вас на экране — сервер и другие игроки ничего не знают.
Ставить на сервер НЕ нужно.

## Сборка
1. Установите JDK 21 и Gradle 9.2+.
2. В папке проекта: `gradle wrapper` (один раз), затем `./gradlew build` (Windows: `gradlew.bat build`).
3. Готовый jar: `build/libs/skinall-1.0.0+1.21.11.jar` -> в папку `mods` (нужен Fabric Loader + Fabric API).

## Использование
1. Положите скин 64x64 PNG в `.minecraft/config/skinall/skin.png`.
2. Зайдите в игру/на сервер, команды:
   - `/skinall reload` — перезагрузить скин
   - `/skinall set имя.png` — выбрать другой файл из этой папки
   - `/skinall slim` / `/skinall classic` — тонкие/обычные руки
   - `/skinall self on|off` — применять ли и к себе
   - `/skinall on|off` — включить/выключить
