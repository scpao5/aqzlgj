#!/system/bin/sh
#此工具由<是白白吖>制作
A=$(dirname "$0")
B="$A/1.dex"
[ ! -f "$B" ] && echo "找不到 $B" && exit 1
if [ $(getprop ro.build.version.sdk) -ge 34 ]; then
    if [ -w "$B" ]; then
        echo "Android 14+ 要求 dex 不可写，尝试移除写权限..."
        chmod 400 "$B"
    fi
    if [ -w "$B" ]; then
        echo "无法移除 $B 的写权限，请将 dex 文件复制到只读目录"
        exit 1
    fi
fi
[ -z "$RISH_APPLICATION_ID" ] && export RISH_APPLICATION_ID="com.sbby.aqzlgj"
exec /system/bin/app_process -Djava.class.path="$B" /system/bin --nice-name=rish rikka.shizuku.shell.ShizukuShellLoader "$@"