#!/bin/bash

# 1. Definir variables dinámicas basadas en la ubicación actual
PROJECT_DIR=$(pwd)
JAR_FILE="$PROJECT_DIR/target/strike-orm-1.0-SNAPSHOT.jar"
ICON_FILE="$PROJECT_DIR/src/main/resources/images/desktop/app-icon.png"
DESKTOP_FILE_NAME="strike-app.desktop"
INSTALL_DIR="$HOME/.local/share/applications"

# 2. Verificar si el JAR existe (¿hiciste mvn install?)
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: No se encuentra el archivo JAR."
    echo "   Ejecuta 'mvn clean install' primero."
    exit 1
fi

# 3. Verificar si el ícono existe
if [ ! -f "$ICON_FILE" ]; then
    echo "⚠️ Advertencia: No se encuentra el ícono en $ICON_FILE"
    # Podrías usar un icono por defecto si quieres
fi

# 4. Crear el contenido del archivo .desktop en memoria
DESKTOP_CONTENT="[Desktop Entry]
Version=1.0
Type=Application
Name=Strike App
Comment=Sistema de Gestión de Fútbol (Hibernate Edition)
Exec=/usr/bin/java -jar \"$JAR_FILE\"
Icon=$ICON_FILE
Terminal=false
Categories=Development;Education;
StartupNotify=true"

# 5. Escribir el archivo en la carpeta de aplicaciones del usuario
echo "$DESKTOP_CONTENT" > "$INSTALL_DIR/$DESKTOP_FILE_NAME"

# 6. Dar permisos de ejecución (crucial para Ubuntu moderno)
chmod +x "$INSTALL_DIR/$DESKTOP_FILE_NAME"

echo "✅ ¡Instalación completada!"
echo "   El lanzador se ha creado en: $INSTALL_DIR/$DESKTOP_FILE_NAME"
echo "   Ahora puedes buscar 'Strike App' en tus aplicaciones."