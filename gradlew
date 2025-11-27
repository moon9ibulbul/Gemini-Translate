#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Attempt to set APP_HOME
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/`basename "$link"`
    fi
done
SAVED="`pwd`"
cd `dirname "$PRG"`/ > /dev/null
APP_HOME=`pwd -P`
cd "$SAVED" > /dev/null

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_JAR_BASE64="$APP_HOME/gradle/wrapper/gradle-wrapper.jar.base64"
WRAPPER_PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"

decode_wrapper_jar() {
    if [ ! -f "$WRAPPER_JAR_BASE64" ]; then
        return 1
    fi

    if command -v base64 >/dev/null 2>&1; then
        base64 --decode "$WRAPPER_JAR_BASE64" > "$WRAPPER_JAR" 2>/dev/null || rm -f "$WRAPPER_JAR"
    elif command -v python3 >/dev/null 2>&1; then
        python3 - <<PY
import base64
from pathlib import Path
source = Path("$WRAPPER_JAR_BASE64")
target = Path("$WRAPPER_JAR")
target.write_bytes(base64.b64decode(source.read_bytes()))
PY
    else
        warn "Neither base64 nor python3 is available to decode the Gradle wrapper JAR"
    fi

    if [ -f "$WRAPPER_JAR" ]; then
        return 0
    fi

    return 1
}

ensure_wrapper_jar() {
    if [ -f "$WRAPPER_JAR" ]; then
        return
    fi

    [ -d "$APP_HOME/gradle/wrapper" ] || mkdir -p "$APP_HOME/gradle/wrapper"

    if decode_wrapper_jar; then
        return
    fi

    distributionUrl=""
    if [ -f "$WRAPPER_PROPERTIES" ]; then
        distributionUrl=`grep '^distributionUrl=' "$WRAPPER_PROPERTIES" | cut -d= -f2-`
    fi

    distributionVersion=""
    if [ -n "$distributionUrl" ]; then
        distributionVersion=`echo "$distributionUrl" | sed -n 's/.*gradle-\([^-]*\)-.*/\1/p'`
    fi

    if [ -z "$distributionVersion" ]; then
        distributionVersion="8.14.3"
    fi

    wrapperUrl="https://repo.maven.apache.org/maven2/org/gradle/gradle-wrapper/${distributionVersion}/gradle-wrapper-${distributionVersion}.jar"
    if command -v curl >/dev/null 2>&1; then
        curl -sSfL "$wrapperUrl" -o "$WRAPPER_JAR" || warn "curl failed to download Gradle wrapper JAR"
    elif command -v wget >/dev/null 2>&1; then
        wget -q "$wrapperUrl" -O "$WRAPPER_JAR" || warn "wget failed to download Gradle wrapper JAR"
    else
        warn "Neither curl nor wget is available to download the Gradle wrapper JAR"
    fi

    if [ ! -f "$WRAPPER_JAR" ]; then
        warn "Could not obtain Gradle wrapper JAR from $wrapperUrl"
        exit 1
    fi
}

ensure_wrapper_jar

CLASSPATH=$WRAPPER_JAR

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME\n\nPlease set the JAVA_HOME variable in your environment to match the\nlocation of your Java installation."
    fi
else
    JAVACMD="java"
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`
fi

# Escape application args
save () {
    for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\ /"; done
    echo " "
}
APP_ARGS=$(save "$@")

# Collect all arguments for the java command, following the shell quoting and substitution rules
eval set -- $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$APP_ARGS"

exec "$JAVACMD" "$@"
