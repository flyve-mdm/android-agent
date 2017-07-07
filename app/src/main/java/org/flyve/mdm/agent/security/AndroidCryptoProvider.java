/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android Agent.
 *
 * Flyve MDM Android Agent is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android-agent
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.security;

import android.content.Context;
import android.util.Base64;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.pkcs.PKCS10CertificationRequest;
import org.spongycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.spongycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import javax.security.auth.x500.X500Principal;

public class AndroidCryptoProvider {

    private final File csrFile;
    private final File certFile;
    private final File keyFile;
    private X509Certificate cert;
    private PKCS10CertificationRequest csr;
    private RSAPrivateKey key;
    private byte[] pemCsrBytes;
    private static final Object globalCryptoLock = new Object();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public AndroidCryptoProvider(Context c) {
        String dataPath = c.getFilesDir().getAbsolutePath();

        csrFile = new File(dataPath + File.separator + "client.csr");
        keyFile = new File(dataPath + File.separator + "client.key");
        certFile = new File(dataPath + File.separator + "client.crt");
    }

    private byte[] loadFileToBytes(File f) {
        if (!f.exists()) {
            return new byte[0];
        }
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(f);
            byte[] fileData = new byte[(int) f.length()];
            if (fin.read(fileData) != f.length()) {
                // Failed to read
                fileData = null;
            }
            return fileData;
        } catch (IOException e) {
            FlyveLog.e("loadFileToBytes IOException",e);
            return new byte[0];
        } finally {
            try {
                if(fin!=null) {
                    fin.close();
                }
            } catch (Exception e) {
                FlyveLog.e("close FileInputStream, IO exception", e);
            }
        }
    }

    public String getlCsr() {
        byte[] csrBytes = loadFileToBytes(csrFile);
        String strCsr = new String(csrBytes);
        // If either file was missing, we definitely can't succeed
        if (csrBytes == new byte[0]) {
            FlyveLog.i("loadCsr: Missing csr need to generate a new one");
            return "";
        }
        return strCsr;
    }

    public boolean loadCsr() {
        byte[] csrBytes = loadFileToBytes(csrFile);
        // If either file was missing, we definitely can't succeed
        if (csrBytes == new byte[0]) {
            FlyveLog.i("loadCsr: Missing csr need to generate a new one");
            return false;
        }
        return true;
    }


    public void generateRequest(generateCallback callback) {
        byte[] snBytes = new byte[8];
        new SecureRandom().nextBytes(snBytes);

        KeyPair keyPair = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
            keyPairGenerator.initialize(4096);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            FlyveLog.wtf("generateRequest", ex);
            callback.onGenerate(false);
        }

        X500Principal subjectName = new X500Principal("CN=mydevice.stork-mdm.com");

        ContentSigner signGen = null;
        try {
            signGen = new JcaContentSignerBuilder("SHA1withRSA").build(keyPair.getPrivate());
            if(signGen == null) {
                callback.onGenerate(false);
            }
        } catch (Exception ex) {
            FlyveLog.e("generateRequest",ex);
            callback.onGenerate(false);
        }

        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(subjectName, keyPair.getPublic());
        csr = builder.build(signGen);

        try {
            key = (RSAPrivateKey) keyPair.getPrivate();
        } catch (Exception ex) {
            FlyveLog.wtf("generateRequest",ex);
            callback.onGenerate(false);
        }

        // Save the resulting pair
        saveCsrKey();

        // true or false
        boolean bvar = loadCsr();
        callback.onGenerate(bvar);
    }

    private void saveCsrKey() {
        FileOutputStream csrOut = null;
        FileOutputStream keyOut = null;
        try {
            csrOut = new FileOutputStream(csrFile);
            keyOut = new FileOutputStream(keyFile);

            // Write the certificate in OpenSSL PEM format (important for the server)
            StringWriter strWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(strWriter);
            pemWriter.writeObject(csr);
            pemWriter.close();

            // Line endings MUST be UNIX for the PC to accept the cert properly
            OutputStreamWriter csrWriter = new OutputStreamWriter(csrOut);
            String pemStr = strWriter.getBuffer().toString();
            for (int i = 0; i < pemStr.length(); i++) {
                char c = pemStr.charAt(i);
                if (c != '\r')
                    csrWriter.append(c);
            }
            csrWriter.close();

            // Write the private out in PKCS8 format
            keyOut.write(key.getEncoded());
        } catch (IOException e) {
            FlyveLog.e("saveCsrKey",e);
        } finally {
            try {
                if(keyOut!=null) {
                    keyOut.close();
                }
                if(csrOut!=null) {
                    csrOut.close();
                }
            } catch (IOException e){
                FlyveLog.e("saveCsrKey, IOException", e);
            }
        }
    }

    public void saveCertKey(String certString) {
        FileOutputStream certOut = null;

        try {
            certOut = new FileOutputStream(certFile);

            byte[] certBytes = certString.getBytes();

            CertificateFactory certFactory = null;
            certFactory = CertificateFactory.getInstance("X.509", "BC");
            cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

            // Write the certificate in OpenSSL PEM format (important for the server)
            StringWriter strWriter = new StringWriter();
            JcaPEMWriter pemWriter = new JcaPEMWriter(strWriter);
            pemWriter.writeObject(cert);
            pemWriter.close();

            // Line endings MUST be UNIX for the PC to accept the cert properly
            OutputStreamWriter certWriter = new OutputStreamWriter(certOut);
            String pemStr = strWriter.getBuffer().toString();
            for (int i = 0; i < pemStr.length(); i++) {
                char c = pemStr.charAt(i);
                if (c != '\r')
                    certWriter.append(c);
            }
            certWriter.close();

        } catch (Exception e) {
            FlyveLog.e("saveCertKey",e);
        } finally {
            try {
                if(certOut!=null) {
                    certOut.close();
                }
            } catch (Exception e){
                FlyveLog.e("saveCertKey, IOException", e);
            }
        }
    }

    public String encodeBase64String(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    public interface generateCallback {
        void onGenerate(boolean work);
    }
}

