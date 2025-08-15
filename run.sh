#!/usr/bin/env bash

set -Eeuo pipefail
shopt -s nullglob

# 1) Если в текущем каталоге есть spring-ai-example-*.jar — удаляем
local_jars=(spring-ai-example-*.jar)
if (( ${#local_jars[@]} )); then
  echo "Удаляю старые JAR-файлы: ${local_jars[*]}"
  rm -f -- "${local_jars[@]}"
fi

# 2) Сборка
echo "Собираю проект через Gradle..."
./gradlew clean bootJar -Pvaadin.productionMode

# 3) Копируем собранный JAR из build/libs в текущий каталог
build_jars=(build/libs/spring-ai-example-*.jar)
if (( ${#build_jars[@]} == 0 )); then
  echo "ОШИБКА: Не найден собранный JAR в build/libs/spring-ai-example-*.jar"
  exit 1
elif (( ${#build_jars[@]} > 1 )); then
  echo "ОШИБКА: Найдено несколько JAR в build/libs, неясно какой запускать:"
  printf ' - %s\n' "${build_jars[@]}"
  exit 1
fi

jar_in_build="${build_jars[0]}"
echo "Копирую ${jar_in_build} в текущий каталог..."
cp -f -- "${jar_in_build}" .

# 4) Запуск JAR из build/libs
echo "Запускаю: java -jar \"${jar_in_build}\""
exec java -jar "${jar_in_build}"
