# clj-dedup

This is a simple Clojure agent that will hash files to detect duplicates.

# Build and Install

```sh
# make sure you have `native-image`, `java` and `clj` on the path to build the
# native binary
./native-build.sh

# upon sucessful build, use the install script to place the artifact into the
# directory of choice or use the default $HOME/.local/bin
./install.sh # --prefix /opt/bin
```
