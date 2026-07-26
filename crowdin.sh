#!/usr/bin/env bash

set -e

ACTION="$1"
if [ -z "${ACTION}" ]; then
    echo "Verwendung: $0 [upload|download]"
    exit 1
fi

if [ -z "${CROWDIN_PERSONAL_TOKEN}" ]; then
    echo "Fehler: Die Umgebungsvariable CROWDIN_PERSONAL_TOKEN ist nicht gesetzt."
    exit 1
fi

case "${ACTION}" in
    upload)
        crowdin upload sources
        ;;
    download)
        crowdin download
        ;;
    *)
        echo "Fehler: Unbekannte Aktion '${ACTION}'. Verwende 'upload' oder 'download'."
        exit 1
        ;;
esac