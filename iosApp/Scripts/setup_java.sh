# Find a valid Java installation
if [ -n "$JAVA_HOME" ]; then
    echo "Using JAVA_HOME from environment: $JAVA_HOME"
else
    # Your specific path (User Applications)
    if [ -d "$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
        export JAVA_HOME="$HOME/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    # Standard path (Global Applications - common for other devs)
    elif [ -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]; then
        export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
    # System Java Fallback
    elif [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME=$(/usr/libexec/java_home)
    fi
    echo "Resolved JAVA_HOME to: $JAVA_HOME"
fi

export PATH="$JAVA_HOME/bin:$PATH"
