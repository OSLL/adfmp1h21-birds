# adfmp1h21-birds

[![Test](https://github.com/OSLL/adfmp1h21-birds/actions/workflows/gradle-test.yml/badge.svg)](https://github.com/OSLL/adfmp1h21-birds/actions/workflows/gradle-test.yml)

## Демонстрация работы Android Studio

Демонстрацию можно посмотреть по ссылке (Google Drive): 
https://drive.google.com/file/d/1dttXKmo4zar56NfXonvk9h7tOOBJOc20/view?usp=sharing

## Макет пользовательского интерфейса

Макет пользовательского интерфейса доступен по ссылке (Moqups):
https://app.moqups.com/y6DSrRqWxZ/view/page/ae8fe8eb0

> В бесплатной версии приложения ограничение в 200 объектов на проект, поэтому активность профиля 
> пришлось умещать на одном экране. Логически он разделён на три части:
> * Не вошёл в профиль
> * Вошёл в профиль, но не добавил ни одной птицы
> * Вошёл в профиль и добавил несколько птиц

## Google Maps API ключ

Для работы приложения (даже для того, чтобы его собрать) необходимо наличие API-ключа для Google 
карт. Ключ должен находиться в файле `local.properties` в корне проекта. Этот файл представляет из 
себя набор пар ключ-значение 
([описание формата и как им пользоваться](https://docs.gradle.org/current/userguide/build_environment.html)).
В этом файле должен присутствовать ключ `maps_api_key` со значением – API ключ Google карт.

Инструкцию по тому, как получить API ключ для Google карт, можно найти 
[в официальной документации](https://developers.google.com/maps/documentation/android-sdk/get-api-key?hl=ru).
