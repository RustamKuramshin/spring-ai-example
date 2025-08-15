#!/usr/bin/env bash
set -euo pipefail
shopt -s nullglob

# 1) Удаляем уже существующие JAR'ы в текущем каталоге (если есть)
existing=(spring-ai-example-*.jar)
if ((${#existing[@]})); then
  echo "Удаляю существующие файлы: ${existing[*]}"
  rm -f -- "${existing[@]}"
else
  echo "В текущем каталоге нет файлов spring-ai-example-*.jar"
fi

# 2) Сборка
echo "Собираю проект (gradlew clean bootJar -Pvaadin.productionMode)..."
./gradlew clean bootJar -Pvaadin.productionMode

# 3) Копируем собранный JAR из build/libs в текущий каталог
built=(build/libs/spring-ai-example-*.jar)
if ((${#built[@]} == 0)); then
  echo "Ошибка: не найден файл build/libs/spring-ai-example-*.jar"
  exit 1
fi
if ((${#built[@]} > 1)); then
  echo "Ошибка: найдено несколько JAR-файлов: ${built[*]}"
  echo "Уточните правило именования или очистите лишние артефакты."
  exit 1
fi

echo "Копирую ${built[0]} в текущий каталог..."
cp -f -- "${built[0]}" .

jar_name="$(basename -- "${built[0]}")"

# 4) Запуск JAR в текущем каталоге
echo "Запускаю: java -jar ${jar_name}"
exec java -jar "${jar_name}"
