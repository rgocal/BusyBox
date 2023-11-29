/*
 * Copyright (C) 2016 Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jrummyapps.busybox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import static java.io.File.separator;

public class Main {

  private static final String[] COMPILED_VERSION = {"1.26.2", "1.25.1", "1.24.2", "1.24.1", "1.23.2"};
  private static final String LATEST_VERSION = "1.26.2";
  private static final String[] ABIS = {"arm", "mips", "x86"};
  private static final String[] FLAVORS = {"pie", "nopie"};
  private static final String DOWNLOAD_URL =
      "https://github.com/jrummyapps/BusyBox/blob/master/busybox-compiler/compiled-%s/%s/%s/bin/busybox?raw=true";

  private static final File BUSYBOX_COMPILER_DIRECTORY = new File("busybox-compiler");
  private static final File BINARIES_JSON_FILE = new File("app"
      + separator + "src" + separator + "main" + separator + "res" + separator + "raw" + separator + "binaries.json");

  public static void main(String[] args) throws IOException {
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    List<Binary> binaries = new ArrayList<>();

    for (String version : COMPILED_VERSION) {
      File directory = new File(BUSYBOX_COMPILER_DIRECTORY, "compiled-" + version);
      for (String abi : ABIS) {
        for (String flavor : FLAVORS) {
          try {
            File busybox = new File(directory, abi + separator + flavor + separator + "bin" + separator + "busybox");
            byte[] bytes = Files.readAllBytes(Paths.get(busybox.getAbsolutePath()));
            byte[] hash = MessageDigest.getInstance("MD5").digest(bytes);
            Binary binary = new Binary();
            binary.name = "BusyBox " + version;
            binary.filename = busybox.getName();
            binary.abi = abi;
            binary.md5sum = DatatypeConverter.printHexBinary(hash).toUpperCase();
            binary.size = busybox.length();
            binary.flavor = flavor;
            if (version.equals(LATEST_VERSION) && abi.equals("arm") && !flavor.equals("static")) {
              // nopie and pie are included in the app's assets directory
              binary.url = "busybox/" + abi + "/" + flavor + "/busybox";
            } else {
              binary.url = String.format(DOWNLOAD_URL, version, abi, flavor);
            }
            binaries.add(binary);
          } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
          }
        }
      }
    }

    String json = gson.toJson(binaries);
    Files.write(Paths.get(BINARIES_JSON_FILE.getAbsolutePath()), json.getBytes());
  }

  static class Binary {

    String name;
    String filename;
    String abi;
    String flavor;
    String url;
    String md5sum;
    long size;

  }

}
