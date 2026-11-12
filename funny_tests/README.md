# Компиляция и запуск

Команды для PowerShell из папки `funny_tests`:

```powershell
chcp.com 65001
[Console]::InputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
javac -encoding UTF-8 -d out Animals.java Dog.java Cat.java Main.java
java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' -cp out funny_tests.Main
```
