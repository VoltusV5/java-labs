# Лабораторная работа №4 — работа с текстовыми файлами

Вариант 21: автоматическая кофемашина. Продолжение ЛР №3.

## Запуск

Команды PowerShell из папки `lab4`:

```powershell
chcp.com 65001
javac -encoding UTF-8 -d out *.java
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp out Main
```

## Структура файла

Кодировка UTF-8. Каждый объект записывается отдельным блоком:

```text
DripCoffeeMaker {
    Manufacturer: Philips;
    WaterCapacity: 1200;
    Strength: 2;
}
```

Название блока — `DripCoffeeMaker`, `LatteMachine` или `GrinderCoffeeMachine`.
