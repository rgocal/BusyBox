#!/bin/bash

for arch in arm arm64 x86 mips
do
  for arg in pie nopie static
  do
    ./compile-busybox.sh $arch $arg
  done
done
