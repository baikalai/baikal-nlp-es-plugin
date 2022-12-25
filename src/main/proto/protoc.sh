#!/bin/bash

work=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P)
cd $work


PROTO_SRCS=(
  baikal/language/language_service.proto
  baikal/language/dict_common.proto
  baikal/language/custom_dict.proto
  baikal/language/compiled/dict.proto
)


## check plugin installation
export PATH="$PATH:$(go env GOPATH)/bin"


PROTOC=protoc
PROTOC_VER=3.21.7
PROTOC_GEN_GO_VER=v1.28.0
PROTOC_GEN_GO_GRPC_VER=1.2.0

function check_binary() {
  if [ ! -x "$(command -v "protoc")" ]; then
    echo "please install protoc $PROTOC_VER"
    exit 1
  fi
}



function check_protoc_version() {
  local v=$(protoc --version)
  local v2=${v:10}
  if [ "$v2" != "$PROTOC_VER" ]; then
    echo "Use protoc version $PROTOC_VER"
    exit 1
  fi
}




##
## check binaries and version
##
check_binary
check_protoc_version


JS_LIB=../js/js_generated


## go lang protobuf generating
test -d ${JS_LIB} || mkdir -p ${JS_LIB}
$PROTOC \
    --js_out=$JS_LIB \
    --js-grpc_out=$JS_LIB \
    "${PROTO_SRCS[@]}"



PY_PROTO_SRCS_PB=(
  baikal/language/dict_common.proto
  google/api/annotations.proto
  google/api/http.proto
)

#PY_LIB=../client/src/python_generated/baikalai_apis
PY_LIB=../client/src/python_generated

test -d ${PY_LIB} || mkdir -p ${PY_LIB}
PYBIN=../venv/bin/python3

$PYBIN -m grpc.tools.protoc -I. \
    --python_out=${PY_LIB} \
    --mypy_out=${PY_LIB} \
   ${PY_PROTO_SRCS_PB[@]}


PY_PROTO_SRCS=(
  baikal/language/language_service.proto
  baikal/language/custom_dict.proto
)


$PYBIN -m grpc.tools.protoc -I. \
    --python_out=${PY_LIB} \
    --grpc_python_out=${PY_LIB} \
    --mypy_out=${PY_LIB} \
    --mypy_grpc_out=${PY_LIB} \
    ${PY_PROTO_SRCS[@]}


PY_PROTO_PATHS=(
  baikal/language
#  google/api
)

for d in "${PY_PROTO_PATHS[@]}"; do
  initf=${PY_LIB}/${d}/__init__.py
  test -f "${initf}" || touch "${initf}"
done

PY_PROTO_IN_SRCS=(
  baikal/language/dict_common.proto
  baikal/language/compiled/dict.proto
)

PY_IN_LIB=../engines/src/lang-proto

## the following python3 or grpc_tools.protoc modules installed at system.
$PYBIN -m grpc.tools.protoc -I . \
    --python_out=${PY_IN_LIB} \
    --mypy_out=${PY_IN_LIB} \
    ${PY_PROTO_IN_SRCS[@]}


PY_PROTO_IN_PATHS=(
  baikal/language
  baikal/language/compiled
)

for d in "${PY_PROTO_IN_PATHS[@]}"; do
  initf=${PY_IN_LIB}/${d}/__init__.py
  test -f "${initf}" || touch "${initf}"
done
