/*
 * Copyright (C) 2017 JRummy Apps Inc.
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
 */

package com.jrummyapps.busybox.utils;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.jrummyapps.android.app.App;
import com.jrummyapps.android.util.IoUtils;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

public class ZipSigner {

  private static final String TAG = "ZipSigner";

  private static final String MANIFEST_VERSION = "1.0";
  private static final String CREATED_BY = "Jared Rummler";

  private static final String CERT_SF_NAME = "META-INF/CERT.SF";
  private static final String CERT_RSA_NAME = "META-INF/CERT.RSA";
  private static final String PRIVATE_KEY = "keys/testkey.pk8";
  private static final String PUBLIC_KEY = "keys/testkey.x509.pem";
  private static final String TEST_KEY = "keys/testkey.sbt";

  private static final char LAST_2_BYTE = (char) Integer.parseInt("00000011", 2);
  private static final char LAST_4_BYTE = (char) Integer.parseInt("00001111", 2);
  private static final char LAST_6_BYTE = (char) Integer.parseInt("00111111", 2);
  private static final char LEAD_6_BYTE = (char) Integer.parseInt("11111100", 2);
  private static final char LEAD_4_BYTE = (char) Integer.parseInt("11110000", 2);
  private static final char LEAD_2_BYTE = (char) Integer.parseInt("11000000", 2);

  private static final char[] ENCODE_TABLE = new char[]{
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
      'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
      'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
      'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

  /*
   * Encodes byte array into String using Base64 encoding scheme.
   */
  private static String base64encode(byte[] from) {
    //noinspection StringBufferMayBeStringBuilder
    StringBuffer to = new StringBuffer((int) (from.length * 1.34) + 3);
    int num = 0;
    char currentByte = 0;
    for (int i = 0; i < from.length; i++) {
      num = num % 8;
      while (num < 8) {
        switch (num) {
          case 0:
            currentByte = (char) (from[i] & LEAD_6_BYTE);
            currentByte = (char) (currentByte >>> 2);
            break;
          case 2:
            currentByte = (char) (from[i] & LAST_6_BYTE);
            break;
          case 4:
            currentByte = (char) (from[i] & LAST_4_BYTE);
            currentByte = (char) (currentByte << 2);
            if ((i + 1) < from.length) {
              currentByte |= (from[i + 1] & LEAD_2_BYTE) >>> 6;
            }
            break;
          case 6:
            currentByte = (char) (from[i] & LAST_2_BYTE);
            currentByte = (char) (currentByte << 4);
            if ((i + 1) < from.length) {
              currentByte |= (from[i + 1] & LEAD_4_BYTE) >>> 4;
            }
            break;
        }
        to.append(ENCODE_TABLE[currentByte]);
        num += 6;
      }
    }
    if (to.length() % 4 != 0) {
      for (int i = 4 - to.length() % 4; i > 0; i--) {
        to.append('=');
      }
    }
    return to.toString();
  }

  /**
   * Tool to sign JAR files (including APKs and OTA updates) in a way compatible with the mincrypt verifier, using
   * SHA1 and RSA keys.
   *
   * @param unsignedZip
   *     The path to the APK, ZIP, JAR to sign
   * @param destination
   *     The output file
   * @return true if successfully signed the file
   */
  public static boolean signZip(File unsignedZip, File destination) {
    final AssetManager am = App.getContext().getAssets();
    JarArchiveOutputStream outputJar = null;
    JarFile inputJar = null;

    try {
      X509Certificate publicKey = readPublicKey(am.open(PUBLIC_KEY));
      PrivateKey privateKey = readPrivateKey(am.open(PRIVATE_KEY));

      // Assume the certificate is valid for at least an hour.
      long timestamp = publicKey.getNotBefore().getTime() + 3600L * 1000;

      inputJar = new JarFile(unsignedZip, false); // Don't verify.
      FileOutputStream stream = new FileOutputStream(destination);
      outputJar = new JarArchiveOutputStream(stream);
      outputJar.setLevel(9);

      // MANIFEST.MF
      Manifest manifest = addDigestsToManifest(inputJar);
      JarArchiveEntry je = new JarArchiveEntry(JarFile.MANIFEST_NAME);
      je.setTime(timestamp);
      outputJar.putArchiveEntry(je);
      manifest.write(outputJar);

      ZipSignature signature1 = new ZipSignature();
      signature1.initSign(privateKey);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      writeSignatureFile(manifest, out);

      // CERT.SF
      Signature signature = Signature.getInstance("SHA1withRSA");
      signature.initSign(privateKey);
      je = new JarArchiveEntry(CERT_SF_NAME);
      je.setTime(timestamp);
      outputJar.putArchiveEntry(je);
      byte[] sfBytes = writeSignatureFile(manifest, new SignatureOutputStream(outputJar, signature));

      signature1.update(sfBytes);
      byte[] signatureBytes = signature1.sign();

      // CERT.RSA
      je = new JarArchiveEntry(CERT_RSA_NAME);
      je.setTime(timestamp);
      outputJar.putArchiveEntry(je);

      outputJar.write(readContentAsBytes(am.open(TEST_KEY)));
      outputJar.write(signatureBytes);

      copyFiles(manifest, inputJar, outputJar, timestamp);
    } catch (Exception e) {
      Crashlytics.logException(e);
      return false;
    } finally {
      IoUtils.closeQuietly(inputJar);
      IoUtils.closeQuietly(outputJar);
    }
    return true;
  }

  /** Write to another stream and also feed it to the Signature object. */
  private static class SignatureOutputStream extends FilterOutputStream {

    private final Signature signature;

    public SignatureOutputStream(OutputStream out, Signature sig) {
      super(out);
      signature = sig;
    }

    @Override public void write(@NonNull byte[] buffer, int offset, int length) throws IOException {
      try {
        signature.update(buffer, offset, length);
      } catch (SignatureException e) {
        e.printStackTrace();
      }
      super.write(buffer, offset, length);
    }

    @Override public void write(int oneByte) throws IOException {
      try {
        signature.update((byte) oneByte);
      } catch (SignatureException e) {
        e.printStackTrace();
      }
      super.write(oneByte);
    }
  }

  /** Add the SHA1 of every file to the manifest, creating it if necessary. */
  private static Manifest addDigestsToManifest(final JarFile jar) throws IOException, GeneralSecurityException {
    final Manifest input = jar.getManifest();
    final Manifest output = new Manifest();

    final Attributes main = output.getMainAttributes();
    main.putValue("Manifest-Version", MANIFEST_VERSION);
    main.putValue("Created-By", CREATED_BY);

    // We sort the input entries by name, and add them to the output manifest in sorted order.
    // We expect that the output map will be deterministic.
    final TreeMap<String, JarEntry> byName = new TreeMap<>();
    for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
      JarEntry entry = e.nextElement();
      byName.put(entry.getName(), entry);
    }

    final MessageDigest md = MessageDigest.getInstance("SHA1");
    final byte[] buffer = new byte[4096];
    int num;

    for (JarEntry entry : byName.values()) {
      final String name = entry.getName();
      if (!entry.isDirectory() && !name.equals(JarFile.MANIFEST_NAME)
          && !name.equals(CERT_SF_NAME)
          && !name.equals(CERT_RSA_NAME)) {
        InputStream data = jar.getInputStream(entry);
        while ((num = data.read(buffer)) > 0) {
          md.update(buffer, 0, num);
        }

        Attributes attr = null;
        if (input != null) {
          attr = input.getAttributes(name);
        }
        attr = attr != null ? new Attributes(attr) : new Attributes();
        attr.putValue("SHA1-Digest", base64encode(md.digest()));
        output.getEntries().put(name, attr);
      }
    }

    return output;
  }

  /** Copy all the files in a manifest from input to output. */
  private static void copyFiles(Manifest manifest, JarFile in, JarArchiveOutputStream out, long timestamp)
      throws IOException {
    final byte[] buffer = new byte[4096];
    int num;

    final Map<String, Attributes> entries = manifest.getEntries();
    final List<String> names = new ArrayList<>(entries.keySet());
    Collections.sort(names);
    for (final String name : names) {
      final JarEntry inEntry = in.getJarEntry(name);
      if (inEntry.getMethod() == JarArchiveEntry.STORED) {
        // Preserve the STORED method of the input entry.
        out.putArchiveEntry(new JarArchiveEntry(inEntry));
      } else {
        // Create a new entry so that the compressed len is recomputed.
        final JarArchiveEntry je = new JarArchiveEntry(name);
        je.setTime(timestamp);
        out.putArchiveEntry(je);
      }

      final InputStream data = in.getInputStream(inEntry);
      while ((num = data.read(buffer)) > 0) {
        out.write(buffer, 0, num);
      }
      out.flush();
      out.closeArchiveEntry();
    }
  }

  /**
   * Decrypt an encrypted PKCS 8 format private key.
   *
   * Based on ghstark's post on Aug 6, 2006 at
   * http://forums.sun.com/thread.jspa?threadID=758133&messageID=4330949
   *
   * @param encryptedPrivateKey
   *     The raw data of the private key
   */
  private static KeySpec decryptPrivateKey(final byte[] encryptedPrivateKey) throws GeneralSecurityException {
    EncryptedPrivateKeyInfo epkInfo;
    try {
      epkInfo = new EncryptedPrivateKeyInfo(encryptedPrivateKey);
    } catch (final IOException ex) {
      // Probably not an encrypted key.
      return null;
    }

    final String pass = "android";
    final char[] password = pass.toCharArray();
    final SecretKeyFactory skFactory = SecretKeyFactory.getInstance(epkInfo.getAlgName());
    final Key key = skFactory.generateSecret(new PBEKeySpec(password));
    final Cipher cipher = Cipher.getInstance(epkInfo.getAlgName());
    cipher.init(Cipher.DECRYPT_MODE, key, epkInfo.getAlgParameters());

    try {
      return epkInfo.getKeySpec(cipher);
    } catch (final InvalidKeySpecException ex) {
      Log.e(TAG, "Password for keyFile may be bad.", ex);
      return null;
    }
  }

  private static byte[] readContentAsBytes(InputStream input) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final byte[] buffer = new byte[2048];
    int numRead = input.read(buffer);
    while (numRead != -1) {
      baos.write(buffer, 0, numRead);
      numRead = input.read(buffer);
    }
    return baos.toByteArray();
  }

  /** Read a PKCS 8 format private key. */
  private static PrivateKey readPrivateKey(InputStream file) throws IOException, GeneralSecurityException {
    final DataInputStream input = new DataInputStream(file);
    try {
      byte[] bytes = new byte[10000];
      int nBytesTotal = 0, nBytes;
      while ((nBytes = input
          .read(bytes, nBytesTotal, 10000 - nBytesTotal)) != -1) {
        nBytesTotal += nBytes;
      }

      final byte[] bytes2 = new byte[nBytesTotal];
      System.arraycopy(bytes, 0, bytes2, 0, nBytesTotal);
      bytes = bytes2;

      KeySpec spec = decryptPrivateKey(bytes);
      if (spec == null) {
        spec = new PKCS8EncodedKeySpec(bytes);
      }

      try {
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
      } catch (final InvalidKeySpecException ex) {
        return KeyFactory.getInstance("DSA").generatePrivate(spec);
      }
    } finally {
      input.close();
    }
  }

  private static X509Certificate readPublicKey(InputStream file)
      throws IOException, GeneralSecurityException {
    try {
      return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(file);
    } finally {
      file.close();
    }
  }

  /**
   * Write a .SF file with a digest the specified manifest.
   */
  private static byte[] writeSignatureFile(Manifest manifest, OutputStream out) throws IOException,
      GeneralSecurityException {
    final Manifest sf = new Manifest();
    final Attributes main = sf.getMainAttributes();
    main.putValue("Manifest-Version", MANIFEST_VERSION);
    main.putValue("Created-By", CREATED_BY);

    final MessageDigest md = MessageDigest.getInstance("SHA1");
    final PrintStream print = new PrintStream(new DigestOutputStream(new ByteArrayOutputStream(), md), true, "UTF-8");

    // Digest of the entire manifest
    manifest.write(print);
    print.flush();
    main.putValue("SHA1-Digest-Manifest", base64encode(md.digest()));

    final Map<String, Attributes> entries = manifest.getEntries();
    for (final Map.Entry<String, Attributes> entry : entries.entrySet()) {
      // Digest of the manifest stanza for this entry.
      print.print("Name: " + entry.getKey() + "\r\n");
      for (final Map.Entry<Object, Object> att : entry.getValue().entrySet()) {
        print.print(att.getKey() + ": " + att.getValue() + "\r\n");
      }
      print.print("\r\n");
      print.flush();

      final Attributes sfAttr = new Attributes();
      sfAttr.putValue("SHA1-Digest", base64encode(md.digest()));
      sf.getEntries().put(entry.getKey(), sfAttr);
    }

    final ByteArrayOutputStream sos = new ByteArrayOutputStream();
    sf.write(sos);

    String value = sos.toString();
    String done = value.replace("Manifest-Version", "Signature-Version");

    out.write(done.getBytes());

    print.close();
    sos.close();

    return done.getBytes();
  }

  public static class ZipSignature {

    private static final byte[] ALGORITHM_ID_BYTES = {0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A, 0x05, 0x00};
    private static final byte[] BEFORE_ALGORITHM_ID_BYTES = {0x30, 0x21};
    private static final byte[] AFTER_ALGORITHM_ID_BYTES = {0x04, 0x14};

    private final Cipher cipher;

    private final MessageDigest digest;

    @SuppressLint("GetInstance")
    public ZipSignature() throws IOException, GeneralSecurityException {
      digest = MessageDigest.getInstance("SHA1");
      cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }

    public void initSign(final PrivateKey privateKey)
        throws InvalidKeyException {
      cipher.init(Cipher.ENCRYPT_MODE, privateKey);
    }

    public byte[] sign() throws BadPaddingException, IllegalBlockSizeException {
      cipher.update(BEFORE_ALGORITHM_ID_BYTES);
      cipher.update(ALGORITHM_ID_BYTES);
      cipher.update(AFTER_ALGORITHM_ID_BYTES);
      cipher.update(digest.digest());
      return cipher.doFinal();
    }

    public void update(final byte[] data) {
      digest.update(data);
    }

    public void update(final byte[] data, final int offset, final int count) {
      digest.update(data, offset, count);
    }

  }

}
