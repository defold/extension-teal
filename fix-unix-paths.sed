s/\"/\\\"/g;
s/\\\"\$@\\\"/"$@"/g;
s/'/"/g;
s@/[^; ]*\.luarocks/@\$SCRIPT_DIR/@g;
s@/[^; ]*\.lua/@\$SCRIPT_DIR/@g;
1a\
SCRIPT_DIR="$(dirname -- "$(readlink -f -- "$0")")/.."
