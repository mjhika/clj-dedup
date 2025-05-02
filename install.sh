#!/bin/bash

PREFIX="$HOME/.local/bin"
PRG_NAME=$(basename $(pwd))

while [[ $# -gt 0 ]]; do
  case $1 in
    -p|--prefix)
      if [[ -d $2 ]]; then
        PREFIX="$2"
        shift
        shift
      fi
      ;;
    -h|--help)
      echo "Usage: ./install.sh [OPTION] ...
Options:
  -h, --help    print this message
  -p, --prefix  install directory; default: $HOME/.local/bin"
  exit 1
     ;;
    -*)
     echo "Unknown"
     exit 1
     ;;
   *)
     PREFIX=$1
     return 0
     ;;
 esac
done

if [[ ! -d $PREFIX ]]; then
  mkdir -p $PREFIX
fi

if [[ ! -f ./target/$PRG_NAME ]]; then
  echo "No compiled program found in target directory"
  exit 1
fi

mv ./target/$PRG_NAME $PREFIX
